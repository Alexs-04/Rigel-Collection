package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Product
import java.io.Serializable

data class ProductRequest(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String,
) : Serializable {
    object Companion {
        fun toRequest(product: Product): ProductRequest {
            return ProductRequest(
                name = product.name,
                description = product.description,
                price = product.price.toDouble(),
                stock = product.stockQuantity,
                imageUrl = product.imageUrl,
            )
        }
    }
}
