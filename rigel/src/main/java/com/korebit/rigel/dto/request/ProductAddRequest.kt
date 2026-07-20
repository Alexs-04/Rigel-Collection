package com.korebit.rigel.dto.request

import java.time.LocalDate

data class ProductBatchRequest(
    val code: String? = null,
    val receptionDate: LocalDate? = null,
    val expirationDate: LocalDate? = null,
    val receivedAmount: Int? = null,
    val remainingAmount: Int? = null,
    val available: Boolean? = true,
    val price: Double? = null,
    val notes: String? = "",
)

data class ProductAddRequest(
    val name: String? = null,
    val description: String? = null,
    val barcode: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val imageUrl: String? = null,
    val cloudinaryPublicId: String? = null,
    val supplierName: String? = null,
    val supplierPrice: Double? = null,
    val batch: ProductBatchRequest? = null,
)
