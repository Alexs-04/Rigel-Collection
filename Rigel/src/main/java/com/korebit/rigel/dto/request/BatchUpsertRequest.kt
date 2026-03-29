package com.korebit.rigel.dto.request

import java.io.Serializable
import java.time.LocalDate

data class BatchUpsertRequest(
    val code: String,
    val productName: String,
    val supplierName: String,
    val receptionDate: LocalDate,
    val expirationDate: LocalDate,
    val receivedAmount: Int,
    val remainingAmount: Int? = null,
    val available: Boolean = true,
    val price: Double,
    val notes: String = "",
) : Serializable

