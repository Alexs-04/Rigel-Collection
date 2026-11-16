package com.korebit.rigel.service

import com.korebit.rigel.dto.PurchaseDto
import com.korebit.rigel.dto.request.PurchaseCreateRequest
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Purchase
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import com.korebit.rigel.repository.PurchaseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Handles purchase registration and stock ingestion into batches.
 */
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

    /**
     * Retrieves all purchases sorted by date descending.
     *
     * @return list of purchase DTOs.
     */
    @Transactional(readOnly = true)
    fun getAllPurchases(): List<PurchaseDto> {
        return purchaseRepository.findAll()
            .sortedWith(compareByDescending<Purchase> { it.purchaseDate }.thenByDescending { it.id ?: 0L })
            .map(PurchaseDto::fromEntity)
    }

    /**
     * Retrieves a purchase by id.
     *
     * @param id purchase id.
     * @return purchase DTO.
     */
    @Transactional(readOnly = true)
    fun getPurchaseById(id: Long): PurchaseDto {
        val purchase = purchaseRepository.findById(id)
            .orElseThrow { EntityNotFundException("Purchase with id $id not found") }
        return PurchaseDto.fromEntity(purchase)
    }

    /**
     * Retrieves purchases filtered by product name.
     *
     * @param productName product name.
     * @return matching purchase DTOs.
     */
    @Transactional(readOnly = true)
    fun getPurchasesByProduct(productName: String): List<PurchaseDto> {
        return purchaseRepository.findByProductName(productName.trim())
            .map(PurchaseDto::fromEntity)
    }

    /**
     * Retrieves purchases filtered by supplier name.
     *
     * @param supplierName supplier name.
     * @return matching purchase DTOs.
     */
    @Transactional(readOnly = true)
    fun getPurchasesBySupplier(supplierName: String): List<PurchaseDto> {
        return purchaseRepository.findBySupplierName(supplierName.trim())
            .map(PurchaseDto::fromEntity)
    }

    /**
     * Creates a purchase and updates the target batch stock.
     *
     * @param request purchase payload.
     * @return the persisted purchase.
     */
    @Transactional
    fun createPurchase(request: PurchaseCreateRequest): PurchaseDto {
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

        val persisted = purchaseRepository.save(
            Purchase(
                code = resolvePurchaseCode(normalizedRequestedCode),
                purchaseDate = request.purchaseDate,
                quantity = quantity,
                unitPrice = unitPrice,
                totalPrice = totalPrice,
                notes = request.notes.trim(),
                productSupplier = relation,
                batch = batch,
            )
        )

        return PurchaseDto.fromEntity(persisted)
    }

    private fun resolveRelation(productName: String, supplierName: String): ProductSupplier {
        return productSupplierRepository.findByProductNameAndSupplierName(
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

    /**
     * Returns the requested code (already validated as unique) or generates a new unique one.
     * A concurrent insert with the same code still fails on the unique constraint and is
     * reported as 409 by the global exception handler.
     */
    private fun resolvePurchaseCode(requestedCode: String?): String {
        if (requestedCode != null) return requestedCode
        repeat(MAX_PURCHASE_CODE_RETRIES) {
            val candidate = generatePurchaseCode()
            if (!purchaseRepository.existsByCodeIgnoreCase(candidate)) return candidate
        }
        throw IllegalStateException("Could not generate a unique purchase code after $MAX_PURCHASE_CODE_RETRIES attempts")
    }

    private fun generatePurchaseCode(): String {
        val datePart = LocalDate.now().format(PURCHASE_CODE_DATE_FORMAT)
        val randomPart = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
        return "$PURCHASE_CODE_PREFIX-$datePart-$randomPart"
    }
}
