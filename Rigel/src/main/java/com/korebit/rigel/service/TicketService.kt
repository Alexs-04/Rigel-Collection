package com.korebit.rigel.service

import com.korebit.rigel.dto.TicketDetailDto
import com.korebit.rigel.dto.TicketDto
import com.korebit.rigel.dto.request.TicketAddRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Ticket
import com.korebit.rigel.model.extra.TicketDetail
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
) {

    private data class ResolvedItem(
        val barcode: String,
        val quantity: Int,
        val price: BigDecimal,
        val discount: BigDecimal,
        val product: Product,
    )

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

        requiredStockByBarcode.forEach { (barcode, requiredQuantity) ->
            val product = productByBarcode[barcode]
                ?: throw EntityNotFundException("Product with barcode $barcode not found")

            if (product.stockQuantity < requiredQuantity) {
                throw IllegalArgumentException(
                    "Insufficient stock for product $barcode. Available: ${product.stockQuantity}, requested: $requiredQuantity"
                )
            }
        }

        val newTicket = Ticket(
            description = ticket.description()?.trim().orEmpty(),
            dateAndTime = ticket.dateAndTime() ?: LocalDate.now(),
            barcode = generateTicketBarcode(),
            totalAmount = BigDecimal.ZERO,
            consumer = currentConsumer,
        )

        val details = resolvedItems.map { item ->
            val subtotal = item.price
                .multiply(BigDecimal.valueOf(item.quantity.toLong()))
                .subtract(item.discount)

            if (subtotal < BigDecimal.ZERO) {
                throw IllegalArgumentException("Subtotal for product ${item.barcode} cannot be negative")
            }

            TicketDetail(
                quantity = item.quantity,
                price = item.price,
                discount = item.discount,
                subtotal = subtotal,
                ticket = newTicket,
                product = item.product,
            )
        }

        newTicket.ticketDetails.addAll(details)
        newTicket.totalAmount = details.fold(BigDecimal.ZERO) { acc, detail ->
            acc.add(detail.subtotal)
        }
        
        decrementStock(requiredStockByBarcode, productByBarcode)
        
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
                TicketDetailDto(x.product?.barcode ?: "", x.quantity, x.price, x.discount, x.product?.name ?: "")
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
                    TicketDetailDto(x.product?.barcode ?: "", x.quantity, x.price, x.discount, x.product?.name ?: "")
                }
            )
        }
    }
}