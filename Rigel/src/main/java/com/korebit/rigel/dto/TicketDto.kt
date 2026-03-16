package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Ticket
import java.io.Serializable
import java.time.LocalDate

data class TicketDto(
    val consumer: String = "",
    val barcode: String = "",
    val description: String = "",
    val dateAndTime: LocalDate = LocalDate.now(),
    val totalAmount: Double = 0.0,
    val products: List<TicketDetailDto> = emptyList(),
) : Serializable {
    companion object {
        fun toDto(ticket : Ticket): TicketDto {
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
    }
}
