package com.korebit.rigel.dto

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.math.BigDecimal

data class TicketDetailDto(
	val barcode: String,
	val quantity: Int,
	val price: BigDecimal,
	val discount: BigDecimal = BigDecimal.ZERO,
	val batchCode: String? = null,
	@param:JsonSetter(nulls = Nulls.AS_EMPTY)
	val productName: String = "",
)
