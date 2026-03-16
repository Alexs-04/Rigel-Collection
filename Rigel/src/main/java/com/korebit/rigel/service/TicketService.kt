package com.korebit.rigel.service

import com.korebit.rigel.dto.TicketDetailDto
import com.korebit.rigel.dto.TicketDto
import com.korebit.rigel.dto.request.TicketAddRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Ticket
import com.korebit.rigel.model.extra.TicketDetail
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.TicketRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val consumerRepository: ConsumerRepository,
    private val productRepository: ProductRepository,
    private val batchRepository: BatchRepository,
) {

    private data class ResolvedItem(
        val barcode: String,
        val quantity: Int,
        val price: BigDecimal,
        val discount: BigDecimal,
        val product: Product,
    )

    @Transactional
    fun deleteTicket(barcode: String): Response {
        val ticket = ticketRepository.findTicketByBarcode(barcode)
            ?: throw EntityNotFundException("Ticket with barcode $barcode not found")

        val restoredByBatch = mutableMapOf<Batch, Int>()
        val stockToRestoreByBarcode = ticket.ticketDetails
            .groupBy { it.product?.barcode ?: "" }
            .mapValues { (_, details) -> details.sumOf { it.quantity } }

        val productByBarcode = ticket.ticketDetails
            .mapNotNull { it.product }
            .associateBy { it.barcode }

        stockToRestoreByBarcode.forEach { (barcode, quantityToRestore) ->
            val product = productByBarcode[barcode]
                ?: throw EntityNotFundException("Product with barcode $barcode not found")

            product.stockQuantity += quantityToRestore
        }

        ticket.ticketDetails.forEach { detail ->
            val batch = detail.batch ?: return@forEach
            restoredByBatch[batch] = (restoredByBatch[batch] ?: 0) + detail.quantity
        }

        restoredByBatch.forEach { (batch, restoredQty) ->
            batch.remainingAmount += restoredQty
            if (batch.remainingAmount > 0 && !batch.expirationDate.isBefore(LocalDate.now())) {
                batch.available = true
            }
        }

        if (restoredByBatch.isNotEmpty()) {
            batchRepository.saveAll(restoredByBatch.keys)
        }

        productRepository.saveAll(productByBarcode.values)

        ticketRepository.delete(ticket)

        return Response(
            success = true,
            message = "Ticket deleted successfully",
            status = 200
        )
    }

    @Transactional
    fun addTicket(ticket: TicketAddRequest): Response {
        val consumerEmail = ticket.currentConsumerEmail().trim()
        if (consumerEmail.isBlank()) {
            throw IllegalArgumentException("Current consumer email is required")
        }

        val items = ticket.products()
        if (items.isNullOrEmpty()) {
            throw IllegalArgumentException("Ticket must include at least one product")
        }

        val currentConsumer = consumerRepository.findByEmail(consumerEmail)
            ?: throw EntityNotFundException("Consumer with email $consumerEmail not found")

        val resolvedItems = items.map { item ->
            val barcode = item.barcode.trim()

            when {
                barcode.isBlank() -> throw IllegalArgumentException("Product barcode is required")
                item.quantity <= 0 -> throw IllegalArgumentException("Quantity for product $barcode must be greater than zero")
                item.price < BigDecimal.ZERO -> throw IllegalArgumentException("Price for product $barcode cannot be negative")
                item.discount < BigDecimal.ZERO -> throw IllegalArgumentException("Discount for product $barcode cannot be negative")
            }

            val product = productRepository.findByBarcode(barcode).orElseThrow {
                EntityNotFundException("Product with barcode $barcode not found")
            }

            ResolvedItem(
                barcode = barcode,
                quantity = item.quantity,
                price = item.price,
                discount = item.discount,
                product = product,
            )
        }

        val requiredStockByBarcode = resolvedItems
            .groupBy { it.barcode }
            .mapValues { (_, lines) -> lines.sumOf { it.quantity } }

        val productByBarcode = resolvedItems
            .associate { it.barcode to it.product }

        val sellableBatchesByBarcode = mutableMapOf<String, List<Batch>>()

        requiredStockByBarcode.forEach { (barcode, requiredQuantity) ->
            val product = productByBarcode[barcode]
                ?: throw EntityNotFundException("Product with barcode $barcode not found")

            if (product.stockQuantity < requiredQuantity) {
                throw IllegalArgumentException(
                    "Insufficient stock for product $barcode. Available: ${product.stockQuantity}, requested: $requiredQuantity"
                )
            }

            val productId = product.id ?: throw EntityNotFundException("Product id for barcode $barcode not found")
            val sellableBatches = batchRepository.findSellableByProductId(productId, LocalDate.now())
            val availableOnFloor = sellableBatches.sumOf { it.remainingAmount }

            if (availableOnFloor < requiredQuantity) {
                throw IllegalArgumentException(
                    "Insufficient sellable batch stock for product $barcode. Available on floor: $availableOnFloor, requested: $requiredQuantity"
                )
            }

            sellableBatchesByBarcode[barcode] = sellableBatches
        }

        val newTicket = Ticket(
            description = ticket.description()?.trim().orEmpty(),
            dateAndTime = ticket.dateAndTime() ?: LocalDate.now(),
            barcode = generateTicketBarcode(),
            totalAmount = BigDecimal.ZERO,
            consumer = currentConsumer,
        )

        val consumedByBatch = mutableMapOf<Batch, Int>()
        val details = mutableListOf<TicketDetail>()

        resolvedItems.groupBy { it.barcode }.forEach { (barcode, items) ->
            val batches = sellableBatchesByBarcode[barcode].orEmpty()
            var batchIndex = 0

            items.forEach { item ->
                var remainingToAllocate = item.quantity
                var firstChunk = true

                while (remainingToAllocate > 0) {
                    if (batchIndex >= batches.size) {
                        throw IllegalArgumentException("No sellable batches left for product $barcode")
                    }

                    val batch = batches[batchIndex]
                    val alreadyConsumed = consumedByBatch[batch] ?: 0
                    val freeInBatch = batch.remainingAmount - alreadyConsumed

                    if (freeInBatch <= 0) {
                        batchIndex++
                        continue
                    }

                    val consumedNow = minOf(remainingToAllocate, freeInBatch)
                    val appliedDiscount = if (firstChunk) item.discount else BigDecimal.ZERO

                    val subtotal = item.price
                        .multiply(BigDecimal.valueOf(consumedNow.toLong()))
                        .subtract(appliedDiscount)

                    if (subtotal < BigDecimal.ZERO) {
                        throw IllegalArgumentException("Subtotal for product ${item.barcode} cannot be negative")
                    }

                    details.add(
                        TicketDetail(
                            quantity = consumedNow,
                            price = item.price,
                            discount = appliedDiscount,
                            subtotal = subtotal,
                            ticket = newTicket,
                            product = item.product,
                            batch = batch,
                        )
                    )

                    consumedByBatch[batch] = alreadyConsumed + consumedNow
                    remainingToAllocate -= consumedNow
                    firstChunk = false
                }
            }
        }

        newTicket.ticketDetails.addAll(details)
        newTicket.totalAmount = details.fold(BigDecimal.ZERO) { acc, detail ->
            acc.add(detail.subtotal)
        }
        
        decrementStock(requiredStockByBarcode, productByBarcode)

        consumedByBatch.forEach { (batch, consumedQty) ->
            batch.remainingAmount -= consumedQty
            if (batch.remainingAmount <= 0) {
                batch.remainingAmount = 0
                batch.available = false
            }
        }
        if (consumedByBatch.isNotEmpty()) {
            batchRepository.saveAll(consumedByBatch.keys)
        }
        
        ticketRepository.save(newTicket)

        return Response(
            success = true,
            message = "Ticket added successfully",
            status = 200
        )
    }

    fun getTicketByBarcode(barcode: String): TicketDto {
        val ticket = ticketRepository.findTicketByBarcode(barcode)
            ?: throw EntityNotFundException("Ticket with barcode $barcode not found")

        return TicketDto(
            ticket.consumer?.email ?: "Unknown",
            ticket.barcode,
            ticket.description,
            ticket.dateAndTime,
            ticket.totalAmount.toDouble(),
            ticket.ticketDetails.map { x ->
                TicketDetailDto(x.product?.barcode ?: "", x.quantity, x.price, x.discount, x.batch?.code, x.product?.name ?: "")
            }
        )
    }

    private fun decrementStock(requiredStockByBarcode: Map<String, Int>, productByBarcode: Map<String, Product>) {
        productByBarcode.forEach { (barcode, product) ->
            val quantityToDecrement = requiredStockByBarcode[barcode] ?: 0
            if (quantityToDecrement > 0) {
                product.stockQuantity -= quantityToDecrement
            }
        }
        productRepository.saveAll(productByBarcode.values)
    }

    private fun generateTicketBarcode(): String {
        var barcode: String
        do {
            barcode = "TCK-${UUID.randomUUID().toString().replace("-", "").take(12).uppercase()}"
        } while (ticketRepository.findTicketByBarcode(barcode) != null)

        return barcode
    }

     fun getAllTickets() : List<TicketDto> {
        return ticketRepository.findAll().map { ticket ->
            TicketDto(
                ticket.consumer?.email ?: "Unknown",
                ticket.barcode,
                ticket.description,
                ticket.dateAndTime,
                ticket.totalAmount.toDouble(),
                ticket.ticketDetails.map { x ->
                    TicketDetailDto(x.product?.barcode ?: "", x.quantity, x.price, x.discount, x.batch?.code, x.product?.name ?: "")
                }
            )
        }
    }
}