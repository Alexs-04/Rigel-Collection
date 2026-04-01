package com.korebit.rigel.dto.request

data class AmountBuyoutRequest(
    val buyoutUnitPrice: Double,
    val notes: String = "",
)

