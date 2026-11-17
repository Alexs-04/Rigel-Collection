package com.korebit.rigel.dto.request

import com.korebit.rigel.dto.TicketDetailDto
import com.korebit.rigel.enums.MethodPayment
import java.io.Serializable
import java.time.LocalDate

/**
 * Payload to register a sale.
 *
 * The seller is always the authenticated user; it is never taken from the request body.
 */
data class TicketAddRequest(
    val description: String? = null,
    val dateAndTime: LocalDate? = null,
    val products: List<TicketDetailDto>? = null,
    val methodPayment: MethodPayment? = null,
) : Serializable
