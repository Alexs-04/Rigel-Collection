package com.korebit.rigel.service

import com.korebit.rigel.dto.PurchaseDto
import com.korebit.rigel.dto.request.PurchaseCreateRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Purchase
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import com.korebit.rigel.repository.PurchaseRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val productSupplierRepository: ProductSupplierRepository,
    private val batchRepository: BatchRepository,
    private val productRepository: ProductRepository,
) {

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
        validateRequest(request)

        val relation = resolveRelation(request.productName, request.supplierName)
        val product = relation.product ?: throw EntityNotFundException("Product not found in relation")

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

        product.stockQuantity += quantity

        val unitPrice = request.unitPrice.toBigDecimal()
        val totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))

        val purchase = Purchase(
            code = resolvePurchaseCode(request.code),
            purchaseDate = request.purchaseDate,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            notes = request.notes.trim(),
            productSupplier = relation,
            batch = batch,
        )

        relation.purchases.add(purchase)
        batch.purchases.add(purchase)

        batchRepository.save(batch)
        productRepository.save(product)
        purchaseRepository.save(purchase)

        return Response(
            message = "Purchase ${purchase.code} created successfully",
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

    private fun validateRequest(request: PurchaseCreateRequest) {
        when {
            request.productName.trim().isEmpty() -> throw IllegalArgumentException("Product name is required")
            request.supplierName.trim().isEmpty() -> throw IllegalArgumentException("Supplier name is required")
            request.quantity <= 0 -> throw IllegalArgumentException("Quantity must be greater than zero")
            request.unitPrice < 0 -> throw IllegalArgumentException("Unit price cannot be negative")
        }

        request.code?.trim()?.takeIf { it.isNotEmpty() }?.let { code ->
            if (purchaseRepository.findByCode(code).isPresent) {
                throw IllegalArgumentException("Purchase with code $code already exists")
            }
        }
    }

    private fun resolvePurchaseCode(rawCode: String?): String {
        val normalized = rawCode?.trim().orEmpty()
        if (normalized.isNotEmpty()) {
            return normalized
        }

        var generated: String
        do {
            generated = "PUR-${UUID.randomUUID().toString().replace("-", "").take(10).uppercase()}"
        } while (purchaseRepository.findByCode(generated).isPresent)

        return generated
    }
}

