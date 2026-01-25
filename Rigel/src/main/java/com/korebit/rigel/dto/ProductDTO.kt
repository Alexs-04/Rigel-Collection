package com.korebit.rigel.dto

import com.korebit.rigel.model.Product
import com.korebit.rigel.model.Supplier
import java.io.Serializable

data class ProductDTO(
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String,
) : Serializable {
    object Companion {
        fun toDTO(product: Product): ProductDTO {
            return ProductDTO(
                name = product.name,
                description = product.description,
                price = product.price.toDouble(),
                stock = product.stockQuantity,
                imageUrl = product.imageUrl,
            )
        }
    }
}
