package com.korebit.rigel.service

import com.korebit.rigel.dto.PurchaseDto
import com.korebit.rigel.dto.request.PurchaseCreateRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Purchase
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import com.korebit.rigel.repository.PurchaseRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val productSupplierRepository: ProductSupplierRepository,
    private val batchRepository: BatchRepository,
) {

    companion object {
        private const val PURCHASE_CODE_PREFIX = "PUR"
        private const val MAX_PURCHASE_CODE_RETRIES = 6
        private val PURCHASE_CODE_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    }

    @Transactional(readOnly = true)
    fun getAllPurchases(): List<PurchaseDto> {
        return purchaseRepository.findAll()
            .sortedWith(compareByDescending<Purchase> { it.purchaseDate }.thenByDescending { it.id ?: 0L })
            .map(PurchaseDto::fromEntity)
    }

    @Transactional(readOnly = true)
    fun getPurchaseById(id: Long): PurchaseDto {
        val purchase = purchaseRepository.findById(id)
            .orElseThrow { EntityNotFundException("Purchase with id $id not found") }
        return PurchaseDto.fromEntity(purchase)
    }

    @Transactional(readOnly = true)
    fun getPurchasesByProduct(productName: String): List<PurchaseDto> {
        return purchaseRepository.findByProductName(productName.trim())
            .map(PurchaseDto::fromEntity)
    }

    @Transactional(readOnly = true)
    fun getPurchasesBySupplier(supplierName: String): List<PurchaseDto> {
        return purchaseRepository.findBySupplierName(supplierName.trim())
            .map(PurchaseDto::fromEntity)
    }

    @Transactional
    fun createPurchase(request: PurchaseCreateRequest): Response {
        val normalizedRequestedCode = validateRequest(request)

        val relation = resolveRelation(request.productName, request.supplierName)
        val batch = resolveOrCreateBatch(request, relation)

        val quantity = request.quantity
        batch.receivedAmount += quantity
        batch.remainingAmount += quantity
        if (batch.remainingAmount > 0) {
            batch.available = request.available
        }
        if (batch.expirationDate.isBefore(LocalDate.now())) {
            batch.available = false
        }

        val unitPrice = request.unitPrice.toBigDecimal()
        val totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))

        batchRepository.save(batch)

        val persisted = persistPurchaseWithUniqueCode(
            requestedCode = normalizedRequestedCode,
            purchaseDate = request.purchaseDate,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            notes = request.notes.trim(),
            relation = relation,
            batch = batch,
        )

        return Response(
            message = "Purchase ${persisted.code} created successfully",
            status = 201,
            success = true,
        )
    }

    private fun resolveRelation(productName: String, supplierName: String): ProductSupplier {
        return productSupplierRepository.findByProduct_NameAndSupplier_Name(
            productName.trim(),
            supplierName.trim(),
        ) ?: throw EntityNotFundException("Supplier $supplierName is not related to product $productName")
    }

    private fun resolveOrCreateBatch(request: PurchaseCreateRequest, relation: ProductSupplier): Batch {
        request.batchId?.let { batchId ->
            val existing = batchRepository.findById(batchId)
                .orElseThrow { EntityNotFundException("Batch with id $batchId not found") }

            if (existing.productSupplier?.id != relation.id) {
                throw IllegalArgumentException("Batch $batchId does not belong to relation ${request.productName}-${request.supplierName}")
            }

            return existing
        }

        val normalizedCode = request.batchCode?.trim().orEmpty()
        if (normalizedCode.isEmpty()) {
            throw IllegalArgumentException("batchCode is required when batchId is not provided")
        }

        val persistedByCode = batchRepository.findByCode(normalizedCode).orElse(null)
        if (persistedByCode != null) {
            if (persistedByCode.productSupplier?.id != relation.id) {
                throw IllegalArgumentException("Batch code $normalizedCode belongs to a different product-supplier relation")
            }
            return persistedByCode
        }

        val receptionDate = request.receptionDate ?: request.purchaseDate
        val expirationDate = request.expirationDate
            ?: throw IllegalArgumentException("expirationDate is required when creating a new batch")

        if (expirationDate.isBefore(receptionDate)) {
            throw IllegalArgumentException("Batch expiration date cannot be before reception date")
        }

        val batchPrice = request.batchPrice?.toBigDecimal() ?: request.unitPrice.toBigDecimal()
        if (batchPrice < BigDecimal.ZERO) {
            throw IllegalArgumentException("Batch price cannot be negative")
        }

        val batch = Batch()
        batch.code = normalizedCode
        batch.receptionDate = receptionDate
        batch.expirationDate = expirationDate
        batch.receivedAmount = 0
        batch.remainingAmount = 0
        batch.available = request.available
        batch.price = batchPrice
        batch.notes = request.notes.trim()
        batch.productSupplier = relation

        relation.batches.add(batch)
        return batch
    }

    private fun validateRequest(request: PurchaseCreateRequest): String? {
        when {
            request.productName.trim().isEmpty() -> throw IllegalArgumentException("Product name is required")
            request.supplierName.trim().isEmpty() -> throw IllegalArgumentException("Supplier name is required")
            request.quantity <= 0 -> throw IllegalArgumentException("Quantity must be greater than zero")
            request.unitPrice < 0 -> throw IllegalArgumentException("Unit price cannot be negative")
        }

        val normalizedCode = request.code?.trim()?.takeIf { it.isNotEmpty() }
        normalizedCode?.let { code ->
            if (purchaseRepository.existsByCodeIgnoreCase(code)) {
                throw IllegalArgumentException("Purchase with code $code already exists")
            }
        }

        return normalizedCode
    }

    private fun persistPurchaseWithUniqueCode(
        requestedCode: String?,
        purchaseDate: LocalDate,
        quantity: Int,
        unitPrice: BigDecimal,
        totalPrice: BigDecimal,
        notes: String,
        relation: ProductSupplier,
        batch: Batch,
    ): Purchase {
        var attempt = 0
        var currentCode = requestedCode ?: generatePurchaseCode()

        while (attempt < MAX_PURCHASE_CODE_RETRIES) {
            attempt++

            val purchase = Purchase(
                code = currentCode,
                purchaseDate = purchaseDate,
                quantity = quantity,
                unitPrice = unitPrice,
                totalPrice = totalPrice,
                notes = notes,
                productSupplier = relation,
                batch = batch,
            )

            try {
                return purchaseRepository.saveAndFlush(purchase)
            } catch (ex: DataIntegrityViolationException) {
                if (!isPurchaseCodeConstraintViolation(ex)) {
                    throw ex
                }

                if (requestedCode != null) {
                    throw IllegalArgumentException("Purchase with code $requestedCode already exists")
                }

                currentCode = generatePurchaseCode()
            }
        }

        throw IllegalStateException("Could not generate a unique purchase code after $MAX_PURCHASE_CODE_RETRIES attempts")
    }

    private fun generatePurchaseCode(): String {
        val datePart = LocalDate.now().format(PURCHASE_CODE_DATE_FORMAT)
        val randomPart = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
        return "$PURCHASE_CODE_PREFIX-$datePart-$randomPart"
    }

    private fun isPurchaseCodeConstraintViolation(ex: DataIntegrityViolationException): Boolean {
        val messages = mutableListOf<String>()
        var current: Throwable? = ex
        while (current != null) {
            current.message?.let(messages::add)
            current = current.cause
        }
        val message = messages.joinToString(" ").lowercase()

        return message.contains("uk_purchases_code") ||
            message.contains("purchases_code_key") ||
            (message.contains("duplicate key") && message.contains("code"))
    }
}

