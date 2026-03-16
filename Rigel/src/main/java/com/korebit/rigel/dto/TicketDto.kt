package com.korebit.rigel.dto

import java.io.Serializable
import java.time.LocalDate

data class TicketDto(
    val consumer: String = "",
    val barcode: String = "",
    val description: String = "",
    val dateAndTime: LocalDate = LocalDate.now(),
    val totalAmount: Double = 0.0,
    val products: List<TicketDetailDto> = emptyList(),
) : Serializable
