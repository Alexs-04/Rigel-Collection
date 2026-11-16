package com.korebit.rigel.service

import com.korebit.rigel.dto.BatchDto
import com.korebit.rigel.dto.request.BatchUpsertRequest
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Manages inventory batches tied to product-supplier relations.
 */
@Service
class BatchService(
    private val batchRepository: BatchRepository,
    private val productRepository: ProductRepository,
    private val productSupplierRepository: ProductSupplierRepository,
) {

    @Transactional(readOnly = true)
    fun getBatchesByProduct(productName: String): List<BatchDto> {
        val product = productRepository.findByName(productName)
            .orElseThrow { EntityNotFundException("Product $productName not found") }

        val productId = product.id ?: throw EntityNotFundException("Product id not found")
        return batchRepository.findAllByProductId(productId).map(BatchDto::fromEntity)
    }

    @Transactional(readOnly = true)
    fun getBatchById(batchId: Long): BatchDto = BatchDto.fromEntity(findBatch(batchId))

    /** Creates a new batch for an existing product-supplier relation. */
    @Transactional
    fun createBatch(request: BatchUpsertRequest): BatchDto {
        validateRequest(request)
        val code = request.code.trim()
        if (batchRepository.findByCode(code).isPresent) {
            throw IllegalArgumentException("Batch with code $code already exists")
        }

        val relation = findRelationOrThrow(request)
        val batch = Batch()
        applyBatchFields(batch, request, relation, resolveRemainingAmountForCreate(request))

        return BatchDto.fromEntity(batchRepository.save(batch))
    }

    /** Updates a batch without allowing the received amount to drop below what was already consumed. */
    @Transactional
    fun updateBatch(batchId: Long, request: BatchUpsertRequest): BatchDto {
        validateRequest(request)
        val batch = findBatch(batchId)

        val code = request.code.trim()
        if (code != batch.code) {
            batchRepository.findByCode(code).ifPresent {
                throw IllegalArgumentException("Batch with code $code already exists")
            }
        }

        val relation = findRelationOrThrow(request)
        applyBatchFields(batch, request, relation, resolveRemainingAmountForUpdate(batch, request))

        return BatchDto.fromEntity(batchRepository.save(batch))
    }

    @Transactional
    fun deleteBatch(batchId: Long) {
        batchRepository.delete(findBatch(batchId))
    }

    private fun findBatch(batchId: Long): Batch = batchRepository.findById(batchId)
        .orElseThrow { EntityNotFundException("Batch with id $batchId not found") }

    private fun validateRequest(request: BatchUpsertRequest) {
        if (request.code.isBlank()) throw IllegalArgumentException("Batch code is required")
        if (request.productName.isBlank()) throw IllegalArgumentException("Product name is required")
        if (request.supplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")
        if (request.receivedAmount <= 0) throw IllegalArgumentException("Received amount must be greater than zero")
        if (request.price < 0) throw IllegalArgumentException("Batch price cannot be negative")
        if (request.expirationDate.isBefore(request.receptionDate)) {
            throw IllegalArgumentException("Expiration date cannot be before reception date")
        }
    }

    private fun findRelationOrThrow(request: BatchUpsertRequest): ProductSupplier {
        return productSupplierRepository.findByProductNameAndSupplierName(
            request.productName.trim(),
            request.supplierName.trim(),
        ) ?: throw EntityNotFundException("Supplier ${request.supplierName} is not related to product ${request.productName}")
    }

    private fun resolveRemainingAmountForCreate(request: BatchUpsertRequest): Int {
        val initialRemaining = request.remainingAmount ?: request.receivedAmount
        if (initialRemaining !in 0..request.receivedAmount) {
            throw IllegalArgumentException("Remaining amount must be between 0 and received amount")
        }
        return initialRemaining
    }

    private fun resolveRemainingAmountForUpdate(batch: Batch, request: BatchUpsertRequest): Int {
        val consumedAmount = batch.receivedAmount - batch.remainingAmount
        if (request.receivedAmount < consumedAmount) {
            throw IllegalArgumentException("Received amount cannot be less than consumed amount ($consumedAmount)")
        }

        val desiredRemaining = request.remainingAmount ?: (request.receivedAmount - consumedAmount)
        if (desiredRemaining !in 0..request.receivedAmount) {
            throw IllegalArgumentException("Remaining amount must be between 0 and received amount")
        }
        return desiredRemaining
    }

    private fun applyBatchFields(
        batch: Batch,
        request: BatchUpsertRequest,
        relation: ProductSupplier,
        remainingAmount: Int,
    ) {
        batch.code = request.code.trim()
        batch.receptionDate = request.receptionDate
        batch.expirationDate = request.expirationDate
        batch.receivedAmount = request.receivedAmount
        batch.remainingAmount = remainingAmount
        batch.available = request.available
        batch.price = request.price.toBigDecimal()
        batch.notes = request.notes.trim()
        batch.productSupplier = relation
    }
}
