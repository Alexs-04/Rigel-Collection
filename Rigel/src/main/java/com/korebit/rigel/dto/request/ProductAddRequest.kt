package com.korebit.rigel.dto.request

import java.time.LocalDate

data class ProductBatchRequest(
    val code: String,
    val receptionDate: LocalDate,
    val expirationDate: LocalDate,
    val receivedAmount: Int,
    val remainingAmount: Int? = null,
    val available: Boolean = true,
    val price: Double,
    val notes: String = "",
)

data class ProductAddRequest(
    val name: String,
    val description: String,
    val barcode: String,
    val category: String,
    val price: Double,
    val stock: Int = 0,
    val imageUrl: String,
    val supplierName: String,
    val supplierPrice: Double,
    val batch: ProductBatchRequest? = null,
)
