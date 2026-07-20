package com.korebit.rigel.dto.request

import java.time.LocalDate

data class PurchaseCreateRequest(
    val code: String? = null,
    val productName: String,
    val supplierName: String,
    val quantity: Int,
    val unitPrice: Double,
    val purchaseDate: LocalDate = LocalDate.now(),
    val notes: String = "",
    val batchId: Long? = null,
    val batchCode: String? = null,
    val receptionDate: LocalDate? = null,
    val expirationDate: LocalDate? = null,
    val available: Boolean = true,
    val batchPrice: Double? = null,
)

