package com.korebit.rigel.dto

import com.korebit.rigel.enums.AmountStatus
import com.korebit.rigel.enums.ContainerType
import com.korebit.rigel.model.beans.Amount
import java.time.LocalDateTime

data class AmountDto(
    val folio: Long,
    val description: String,
    val type: ContainerType,
    val typeLabel: String,
    val customerName: String,
    val quantity: Int,
    val saleUnitPrice: Double,
    val buyoutUnitPrice: Double?,
    val total: Double,
    val buyoutTotal: Double?,
    val created: LocalDateTime,
    val expirationDate: LocalDateTime,
    val returned: Boolean,
    val returnedAt: LocalDateTime?,
    val boughtOutAt: LocalDateTime?,
    val ownerId: Long?,
    val ownerUsername: String?,
    val notes: String,
    val status: AmountStatus,
) {
    companion object {
        fun fromEntity(amount: Amount, now: LocalDateTime = LocalDateTime.now()): AmountDto {
            val status = when {
                amount.returned -> AmountStatus.RETURNED
                amount.boughtOutAt != null -> AmountStatus.BOUGHT_OUT
                amount.expirationDate.isBefore(now) -> AmountStatus.EXPIRED
                else -> AmountStatus.ACTIVE
            }

            return AmountDto(
                folio = amount.folio,
                description = amount.description,
                type = amount.type,
                typeLabel = amount.type.label,
                customerName = amount.customerName,
                quantity = amount.quantity,
                saleUnitPrice = amount.saleUnitPrice.toDouble(),
                buyoutUnitPrice = amount.buyoutUnitPrice?.toDouble(),
                total = amount.total.toDouble(),
                buyoutTotal = amount.buyoutTotal?.toDouble(),
                created = amount.created,
                expirationDate = amount.expirationDate,
                returned = amount.returned,
                returnedAt = amount.returnedAt,
                boughtOutAt = amount.boughtOutAt,
                ownerId = amount.consumer?.id,
                ownerUsername = amount.consumer?.username,
                notes = amount.notes,
                status = status,
            )
        }
    }
}

