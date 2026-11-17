package com.korebit.rigel.dto

import com.korebit.rigel.enums.MethodPayment
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
    val payment: MethodPayment = MethodPayment.CASH
) : Serializable {
    companion object {
        fun fromEntity(ticket: Ticket): TicketDto {
            return TicketDto(
                ticket.consumer?.email ?: "Unknown",
                ticket.barcode,
                ticket.description,
                ticket.dateAndTime,
                ticket.totalAmount.toDouble(),
                ticket.ticketDetails.map(TicketDetailDto::fromEntity),
                ticket.payment
            )
        }
    }
}
