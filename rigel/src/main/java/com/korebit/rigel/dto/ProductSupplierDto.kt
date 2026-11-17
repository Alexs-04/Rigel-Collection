package com.korebit.rigel.dto

import com.korebit.rigel.model.extra.ProductSupplier
import java.io.Serializable

/** A supplier as seen from one product: its supply price and the batches it delivered. */
data class ProductSupplierDto(
    val name: String,
    val supplyPrice: Double,
    val batches: List<BatchDto> = emptyList(),
) : Serializable {
    companion object {
        /** Returns `null` for a relation whose supplier is missing. */
        fun fromEntity(relation: ProductSupplier): ProductSupplierDto? {
            val supplier = relation.supplier ?: return null
            return ProductSupplierDto(
                name = supplier.name,
                supplyPrice = relation.supplyPrice.toDouble(),
                batches = relation.batches
                    .sortedByDescending { it.receptionDate }
                    .map(BatchDto::fromEntity),
            )
        }
    }
}
