package com.korebit.rigel.dto.request

import com.korebit.rigel.enums.ContainerType
import java.time.LocalDateTime

data class AmountUpdateRequest(
    val description: String = "",
    val type: ContainerType,
    val customerName: String,
    val quantity: Int,
    val saleUnitPrice: Double,
    val buyoutUnitPrice: Double? = null,
    val expirationDate: LocalDateTime,
    val notes: String = "",
)

