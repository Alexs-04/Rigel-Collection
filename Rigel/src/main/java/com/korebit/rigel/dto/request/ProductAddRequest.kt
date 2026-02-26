package com.korebit.rigel.dto.request

data class ProductAddRequest(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String,
    val supplierName: String,
    val supplierPrice: Double
)
