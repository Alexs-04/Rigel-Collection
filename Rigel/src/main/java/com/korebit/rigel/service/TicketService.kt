package com.korebit.rigel.service

import com.korebit.rigel.dto.TicketDetailDto
import com.korebit.rigel.dto.TicketDto
import com.korebit.rigel.dto.request.TicketAddRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Ticket
import com.korebit.rigel.model.extra.TicketDetail
import com.korebit.rigel.repository.ConsumerRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.TicketRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val consumerRepository: ConsumerRepository,
    private val productRepository: ProductRepository,
) {

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

        val newTicket = Ticket(
            description = ticket.description()?.trim().orEmpty(),
            dateAndTime = ticket.dateAndTime(),
            totalAmount = BigDecimal.ZERO,
            consumer = currentConsumer,
        )

        val details = items.map { item ->
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

            val subtotal = item.price
                .multiply(BigDecimal.valueOf(item.quantity.toLong()))
                .subtract(item.discount)

            if (subtotal < BigDecimal.ZERO) {
                throw IllegalArgumentException("Subtotal for product $barcode cannot be negative")
            }

            TicketDetail(
                quantity = item.quantity,
                price = item.price,
                discount = item.discount,
                subtotal = subtotal,
                ticket = newTicket,
                product = product,
            )
        }

        newTicket.ticketDetails.addAll(details)
        newTicket.totalAmount = details.fold(BigDecimal.ZERO) { acc, detail ->
            acc.add(detail.subtotal)
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
                TicketDetailDto(x.product?.barcode ?: "", x.quantity, x.price, x.discount, x.product?.name ?: "")
            }
        )

    }
}