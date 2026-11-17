package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Product
import java.io.Serializable

data class ProductDto(
    val name: String,
    val description: String,
    val barcode: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String?,
    val cloudinaryPublicId: String?,
    val suppliers: List<ProductSupplierDto> = emptyList(),
    val minStock : Int? = null,
) : Serializable {
    companion object {
        fun fromEntity(product: Product): ProductDto {
            val suppliers = product.suppliers.mapNotNull(ProductSupplierDto::fromEntity)
            return ProductDto(
                name = product.name,
                description = product.description,
                barcode = product.barcode,
                category = product.category.name,
                price = product.price.toDouble(),
                stock = product.suppliers
                    .flatMap { it.batches }
                    .sumOf { it.remainingAmount },
                imageUrl = product.imageUrl,
                cloudinaryPublicId = product.cloudinaryPublicId,
                suppliers = suppliers,
                minStock = product.minStock,
            )
        }
    }
}