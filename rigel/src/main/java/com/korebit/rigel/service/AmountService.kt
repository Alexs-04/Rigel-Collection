package com.korebit.rigel.service

import com.korebit.rigel.dto.AmountDto
import com.korebit.rigel.dto.ContainerTypeOptionDto
import com.korebit.rigel.dto.request.AmountBuyoutRequest
import com.korebit.rigel.dto.request.AmountUpsertRequest
import com.korebit.rigel.enums.ContainerType
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Amount
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.AmountRepository
import com.korebit.rigel.repository.ConsumerRepository
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Handles amount voucher lifecycle operations used for returnable container charges.
 */
@Service
class AmountService(
    private val amountRepository: AmountRepository,
    private val consumerRepository: ConsumerRepository,
) {

    /** Lists all vouchers, newest first. */
    @Transactional(readOnly = true)
    fun getAllAmounts(): List<AmountDto> {
        return amountRepository.findAll(NEWEST_FIRST).map(AmountDto::fromEntity)
    }

    @Transactional(readOnly = true)
    fun getAmountByFolio(folio: Long): AmountDto = AmountDto.fromEntity(findAmountByFolio(folio))

    fun getContainerTypes(): List<ContainerTypeOptionDto> =
        ContainerType.entries.map(ContainerTypeOptionDto::fromType)

    /**
     * Creates a new voucher owned by the authenticated user.
     *
     * @param actorEmail email (security principal name) of the user performing the action.
     */
    @Transactional
    fun createAmount(request: AmountUpsertRequest, actorEmail: String): AmountDto {
        validateRequest(request)

        val saleUnitPrice = request.saleUnitPrice.toBigDecimal()
        val amount = Amount(
            description = request.description.trim(),
            type = request.type,
            customerName = request.customerName.trim(),
            quantity = request.quantity,
            saleUnitPrice = saleUnitPrice,
            buyoutUnitPrice = request.buyoutUnitPrice?.toBigDecimal(),
            total = saleUnitPrice.multiply(BigDecimal.valueOf(request.quantity.toLong())),
            created = LocalDateTime.now(),
            expirationDate = request.expirationDate,
            returned = false,
            notes = request.notes.trim(),
            consumer = resolveConsumer(actorEmail),
        )

        return AmountDto.fromEntity(amountRepository.save(amount))
    }

    /** Updates a voucher while it is still open (not returned nor bought out). */
    @Transactional
    fun updateAmount(folio: Long, request: AmountUpsertRequest): AmountDto {
        validateRequest(request)

        val amount = findAmountByFolio(folio)
        if (amount.returned || amount.boughtOutAt != null) {
            throw IllegalArgumentException("No se puede editar un importe cerrado")
        }

        val saleUnitPrice = request.saleUnitPrice.toBigDecimal()
        amount.description = request.description.trim()
        amount.type = request.type
        amount.customerName = request.customerName.trim()
        amount.quantity = request.quantity
        amount.saleUnitPrice = saleUnitPrice
        amount.buyoutUnitPrice = request.buyoutUnitPrice?.toBigDecimal()
        amount.total = saleUnitPrice.multiply(BigDecimal.valueOf(request.quantity.toLong()))
        amount.expirationDate = request.expirationDate
        amount.notes = request.notes.trim()

        return AmountDto.fromEntity(amountRepository.save(amount))
    }

    @Transactional
    fun markAsReturned(folio: Long): AmountDto {
        val amount = findAmountByFolio(folio)
        requireOpen(amount)

        amount.returned = true
        amount.returnedAt = LocalDateTime.now()
        return AmountDto.fromEntity(amountRepository.save(amount))
    }

    /** Registers the buyout of an expired, still open voucher. */
    @Transactional
    fun markAsBoughtOut(folio: Long, request: AmountBuyoutRequest): AmountDto {
        val amount = findAmountByFolio(folio)
        requireOpen(amount)
        if (request.buyoutUnitPrice < 0) {
            throw IllegalArgumentException("El precio de compra no puede ser negativo")
        }
        if (amount.expirationDate.isAfter(LocalDateTime.now())) {
            throw IllegalArgumentException("Solo se puede comprar un importe vencido")
        }

        val buyoutUnitPrice = request.buyoutUnitPrice.toBigDecimal()
        amount.buyoutUnitPrice = buyoutUnitPrice
        amount.buyoutTotal = buyoutUnitPrice.multiply(BigDecimal.valueOf(amount.quantity.toLong()))
        amount.boughtOutAt = LocalDateTime.now()
        val extraNotes = request.notes.trim()
        if (extraNotes.isNotBlank()) {
            amount.notes = listOf(amount.notes, extraNotes).filter { it.isNotBlank() }.joinToString(" | ")
        }
        return AmountDto.fromEntity(amountRepository.save(amount))
    }

    @Transactional
    fun deleteAmount(folio: Long) {
        amountRepository.delete(findAmountByFolio(folio))
    }

    private fun requireOpen(amount: Amount) {
        if (amount.returned) {
            throw IllegalArgumentException("El importe ${amount.folio} ya fue devuelto")
        }
        if (amount.boughtOutAt != null) {
            throw IllegalArgumentException("El importe ${amount.folio} ya fue comprado")
        }
    }

    private fun findAmountByFolio(folio: Long): Amount {
        return amountRepository.findById(folio)
            .orElseThrow { EntityNotFundException("Importe con folio $folio no encontrado") }
    }

    private fun resolveConsumer(email: String): Consumer {
        return consumerRepository.findByEmail(email)
            ?: throw EntityNotFundException("Usuario $email no encontrado")
    }

    private fun validateRequest(request: AmountUpsertRequest) {
        if (request.customerName.isBlank()) throw IllegalArgumentException("El nombre del cliente es obligatorio")
        if (request.quantity <= 0) throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        if (request.saleUnitPrice < 0) throw IllegalArgumentException("El precio de venta no puede ser negativo")
        if (request.buyoutUnitPrice != null && request.buyoutUnitPrice < 0) {
            throw IllegalArgumentException("El precio de compra no puede ser negativo")
        }
        if (!request.expirationDate.isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw IllegalArgumentException("La fecha limite debe ser posterior a la fecha actual")
        }
    }

    companion object {
        private val NEWEST_FIRST: Sort = Sort.by(Sort.Order.desc("created"), Sort.Order.desc("folio"))
    }
}
