package com.korebit.rigel.dto.request

import java.io.Serializable

data class AddRelationRequest(
    val productName: String,
    val supplierName: String,
    val price: Double
) : Serializable
