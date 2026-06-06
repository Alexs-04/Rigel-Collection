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
) : Serializable {
    companion object {
        fun toRequest(product: Product): ProductDto {
            val suppliers = product.suppliers.mapNotNull { relation ->
                val supplier = relation.supplier ?: return@mapNotNull null
                val batches = relation.batches
                    .sortedByDescending { it.receptionDate }
                    .map(ProductBatchDto::fromEntity)

                ProductSupplierDto(
                    name = supplier.name,
                    supplyPrice = relation.supplyPrice.toDouble(),
                    batches = batches,
                )
            }

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
            )
        }
    }
}