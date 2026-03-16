package com.korebit.rigel.dto

import java.io.Serializable

data class ProductSupplierDto(
    val name: String,
    val supplyPrice: Double,
    val batches: List<ProductBatchDto> = emptyList(),
) : Serializable
