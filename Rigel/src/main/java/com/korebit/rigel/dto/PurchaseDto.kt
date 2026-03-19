package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Purchase
import java.io.Serializable
import java.time.LocalDate

data class PurchaseDto(
    val id: Long,
    val code: String,
    val purchaseDate: LocalDate,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val notes: String,
    val productName: String,
    val supplierName: String,
    val batchId: Long,
    val batchCode: String,
) : Serializable {
    companion object {
        fun fromEntity(purchase: Purchase): PurchaseDto {
            val relation = purchase.productSupplier
                ?: throw IllegalStateException("Purchase without product-supplier relation")
            val product = relation.product ?: throw IllegalStateException("Purchase without product")
            val supplier = relation.supplier ?: throw IllegalStateException("Purchase without supplier")
            val batch = purchase.batch ?: throw IllegalStateException("Purchase without batch")
            val id = purchase.id ?: throw IllegalStateException("Purchase without id")
            val batchId = batch.id ?: throw IllegalStateException("Purchase batch without id")

            return PurchaseDto(
                id = id,
                code = purchase.code,
                purchaseDate = purchase.purchaseDate,
                quantity = purchase.quantity,
                unitPrice = purchase.unitPrice.toDouble(),
                totalPrice = purchase.totalPrice.toDouble(),
                notes = purchase.notes,
                productName = product.name,
                supplierName = supplier.name,
                batchId = batchId,
                batchCode = batch.code,
            )
        }
    }
}


