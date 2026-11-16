package com.korebit.rigel.dto

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.korebit.rigel.model.extra.TicketDetail
import java.math.BigDecimal

data class TicketDetailDto(
	val barcode: String,
	val quantity: Int,
	val price: BigDecimal,
	val discount: BigDecimal = BigDecimal.ZERO,
	val batchCode: String? = null,
	@param:JsonSetter(nulls = Nulls.AS_EMPTY)
	val productName: String = "",
) {
	companion object {
		fun fromEntity(detail: TicketDetail): TicketDetailDto = TicketDetailDto(
			barcode = detail.product?.barcode ?: "",
			quantity = detail.quantity,
			price = detail.price,
			discount = detail.discount,
			batchCode = detail.batch?.code,
			productName = detail.product?.name ?: "",
		)
	}
}
