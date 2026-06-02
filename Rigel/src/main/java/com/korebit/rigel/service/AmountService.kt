package com.korebit.rigel.service

import com.korebit.rigel.dto.AmountDto
import com.korebit.rigel.dto.ContainerTypeOptionDto
import com.korebit.rigel.dto.request.AmountBuyoutRequest
import com.korebit.rigel.dto.request.AmountCreateRequest
import com.korebit.rigel.dto.request.AmountUpdateRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.enums.ContainerType
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Amount
import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.repository.AmountRepository
import com.korebit.rigel.repository.ConsumerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
/**
 * Handles amount voucher lifecycle operations used for returnable container charges.
 */
class AmountService(
    private val amountRepository: AmountRepository,
    private val consumerRepository: ConsumerRepository,
) {

    @Transactional(readOnly = true)
    /**
     * Lists all amount vouchers ordered by creation date and folio.
     *
     * @return ordered list of amount DTOs.
     */
    fun getAllAmounts(): List<AmountDto> {
        return amountRepository.findAll()
            .sortedWith(compareByDescending<Amount> { it.created }.thenByDescending { it.folio })
            .map(AmountDto::fromEntity)
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves a single amount voucher by folio.
     *
     * @param folio voucher folio.
     * @return amount DTO.
     */
    fun getAmountByFolio(folio: Long): AmountDto {
        return AmountDto.fromEntity(findAmountByFolio(folio))
    }

    @Transactional(readOnly = true)
    /**
     * Returns all available container type options.
     *
     * @return container type catalog.
     */
    fun getContainerTypes(): List<ContainerTypeOptionDto> {
        return ContainerType.entries.map(ContainerTypeOptionDto::fromType)
    }

    @Transactional
    /**
     * Creates a new amount voucher.
     *
     * @param request creation payload.
     * @param actorUsername username of the consumer performing the action.
     * @return operation response.
     */
    fun createAmount(request: AmountCreateRequest, actorUsername: String): Response {
        validateRequest(
            customerName = request.customerName,
            quantity = request.quantity,
            saleUnitPrice = request.saleUnitPrice,
            buyoutUnitPrice = request.buyoutUnitPrice,
            expirationDate = request.expirationDate,
        )

        val saleUnitPrice = request.saleUnitPrice.toBigDecimal()
        val quantityDecimal = BigDecimal.valueOf(request.quantity.toLong())
        val amount = Amount(
            description = request.description.trim(),
            type = request.type,
            customerName = request.customerName.trim(),
            quantity = request.quantity,
            saleUnitPrice = saleUnitPrice,
            buyoutUnitPrice = request.buyoutUnitPrice?.toBigDecimal(),
            total = saleUnitPrice.multiply(quantityDecimal),
            created = LocalDateTime.now(),
            expirationDate = request.expirationDate,
            returned = false,
            notes = request.notes.trim(),
            consumer = resolveConsumer(actorUsername),
        )

        amountRepository.save(amount)

        return Response(
            success = true,
            status = 201,
            message = "Importe ${amount.folio} registrado correctamente"
        )
    }

    @Transactional
    /**
     * Updates an existing amount voucher while it is still open.
     *
     * @param folio voucher folio.
     * @param request update payload.
     * @return operation response.
     */
    fun updateAmount(folio: Long, request: AmountUpdateRequest): Response {
        validateRequest(
            customerName = request.customerName,
            quantity = request.quantity,
            saleUnitPrice = request.saleUnitPrice,
            buyoutUnitPrice = request.buyoutUnitPrice,
            expirationDate = request.expirationDate,
        )

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

        amountRepository.save(amount)

        return Response(
            success = true,
            status = 200,
            message = "Importe $folio actualizado correctamente"
        )
    }

    @Transactional
    /**
     * Marks an amount voucher as returned.
     *
     * @param folio voucher folio.
     * @return operation response.
     */
    fun markAsReturned(folio: Long): Response {
        val amount = findAmountByFolio(folio)
        if (amount.returned) {
            throw IllegalArgumentException("El importe $folio ya fue devuelto")
        }
        if (amount.boughtOutAt != null) {
            throw IllegalArgumentException("El importe $folio ya fue comprado")
        }

        amount.returned = true
        amount.returnedAt = LocalDateTime.now()
        amountRepository.save(amount)

        return Response(
            success = true,
            status = 200,
            message = "Importe $folio marcado como devuelto"
        )
    }

    @Transactional
    /**
     * Marks an expired amount voucher as bought out.
     *
     * @param folio voucher folio.
     * @param request buyout payload.
     * @return operation response.
     */
    fun markAsBoughtOut(folio: Long, request: AmountBuyoutRequest): Response {
        val amount = findAmountByFolio(folio)
        if (amount.returned) {
            throw IllegalArgumentException("El importe $folio ya fue devuelto")
        }
        if (amount.boughtOutAt != null) {
            throw IllegalArgumentException("El importe $folio ya fue comprado")
        }
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
        amountRepository.save(amount)

        return Response(
            success = true,
            status = 200,
            message = "Compra del importe $folio registrada correctamente"
        )
    }

    @Transactional
    /**
     * Deletes an amount voucher by folio.
     *
     * @param folio voucher folio.
     * @return operation response.
     */
    fun deleteAmount(folio: Long): Response {
        val amount = findAmountByFolio(folio)
        amountRepository.delete(amount)
        return Response(
            success = true,
            status = 200,
            message = "Importe $folio eliminado correctamente"
        )
    }

    private fun findAmountByFolio(folio: Long): Amount {
        return amountRepository.findById(folio)
            .orElseThrow { EntityNotFundException("Amount with folio $folio not found") }
    }

    private fun resolveConsumer(username: String): Consumer {
        return consumerRepository.findByEmail(username)
            ?: throw EntityNotFundException("Consumer with username $username not found")
    }

    private fun validateRequest(
        customerName: String,
        quantity: Int,
        saleUnitPrice: Double,
        buyoutUnitPrice: Double?,
        expirationDate: LocalDateTime,
    ) {
        if (customerName.trim().isEmpty()) throw IllegalArgumentException("El nombre del cliente es obligatorio")
        if (quantity <= 0) throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        if (saleUnitPrice < 0) throw IllegalArgumentException("El precio de venta no puede ser negativo")
        if (buyoutUnitPrice != null && buyoutUnitPrice < 0) {
            throw IllegalArgumentException("El precio de compra no puede ser negativo")
        }
        if (!expirationDate.isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw IllegalArgumentException("La fecha limite debe ser posterior a la fecha actual")
        }
    }
}

