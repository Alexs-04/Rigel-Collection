package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Batch
import java.io.Serializable
import java.time.LocalDate

data class ProductBatchDto(
    val id: Long,
    val code: String,
    val receptionDate: LocalDate,
    val expirationDate: LocalDate,
    val receivedAmount: Int,
    val remainingAmount: Int,
    val available: Boolean,
    val price: Double,
    val notes: String,
) : Serializable {
    companion object {
        fun fromEntity(batch: Batch): ProductBatchDto {
            return ProductBatchDto(
                id = batch.id,
                code = batch.code,
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

