package com.korebit.rigel.service

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
/**
 * Handles point-of-sale ticket creation, stock allocation, and ticket retrieval.
 */
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
    /**
     * Deletes a ticket and restores consumed batch stock.
     *
     * @param barcode ticket barcode.
     * @return operation response.
     */
    fun deleteTicket(barcode: String): Response {
        val ticket = ticketRepository.findTicketByBarcode(barcode)
            ?: throw EntityNotFundException("Ticket with barcode $barcode not found")

        val restoredByBatch = mutableMapOf<Batch, Int>()

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

        ticketRepository.delete(ticket)

        return Response(
            success = true,
            message = "Ticket deleted successfully",
            status = 200
        )
    }

    @Transactional
    /**
     * Creates a new ticket and allocates quantities from sellable batches.
     *
     * @param ticket ticket creation payload.
     * @return operation response.
     */
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
            payment = ticket.methodPayment
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

    /**
     * Retrieves a ticket by barcode.
     *
     * @param barcode ticket barcode.
     * @return ticket DTO.
     */
    fun getTicketByBarcode(barcode: String): TicketDto {
        val ticket = ticketRepository.findTicketByBarcode(barcode)
            ?: throw EntityNotFundException("Ticket with barcode $barcode not found")

        return TicketDto.toDto(ticket)
    }


    private fun generateTicketBarcode(): String {
        var barcode: String
        do {
            barcode = "TCK-${UUID.randomUUID().toString().replace("-", "").take(12).uppercase()}"
        } while (ticketRepository.findTicketByBarcode(barcode) != null)

        return barcode
    }

    /**
     * Retrieves all tickets.
     *
     * @return list of ticket DTOs.
     */
     fun getAllTickets() : List<TicketDto> {
        return ticketRepository.findAll().map { ticket ->
            TicketDto.toDto(ticket)
        }
    }
}