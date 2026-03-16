package com.korebit.rigel.dto

import java.math.BigDecimal

data class TicketDetailDto(
	val barcode: String,
	val quantity: Int,
	val price: BigDecimal,
	val discount: BigDecimal = BigDecimal.ZERO,
	val productName: String = "",
)
