package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Batch
import java.io.Serializable
import java.time.LocalDate

data class BatchDto(
    val id: Long,
    val code: String,
    val productName: String,
    val supplierName: String,
    val receptionDate: LocalDate,
    val expirationDate: LocalDate,
    val receivedAmount: Int,
    val remainingAmount: Int,
    val available: Boolean,
    val price: Double,
    val notes: String,
) : Serializable {
    companion object {
        fun fromEntity(batch: Batch): BatchDto {
            val relation = batch.productSupplier
                ?: throw IllegalStateException("Batch without product-supplier relation")
            val product = relation.product ?: throw IllegalStateException("Batch without product")
            val supplier = relation.supplier ?: throw IllegalStateException("Batch without supplier")

            return BatchDto(
                id = batch.id,
                code = batch.code,
                productName = product.name,
                supplierName = supplier.name,
                receptionDate = batch.receptionDate,
                expirationDate = batch.expirationDate,
                receivedAmount = batch.receivedAmount,
                remainingAmount = batch.remainingAmount,
                available = batch.available,
                price = batch.price.toDouble(),
                notes = batch.notes,
            )
        }
    }
}

