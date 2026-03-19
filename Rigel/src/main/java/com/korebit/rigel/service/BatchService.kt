package com.korebit.rigel.service

import com.korebit.rigel.dto.BatchDto
import com.korebit.rigel.dto.request.BatchUpsertRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
    fun getBatchById(batchId: Long): BatchDto {
        val batch = batchRepository.findById(batchId)
            .orElseThrow { EntityNotFundException("Batch with id $batchId not found") }
        return BatchDto.fromEntity(batch)
    }

    @Transactional
    fun createBatch(request: BatchUpsertRequest): Response {
        validateRequest(request)

        val relation = productSupplierRepository.findByProduct_NameAndSupplier_Name(
            request.productName.trim(),
            request.supplierName.trim(),
        ) ?: throw EntityNotFundException("Supplier ${request.supplierName} is not related to product ${request.productName}")

        val initialRemaining = request.remainingAmount ?: request.receivedAmount
        if (initialRemaining < 0 || initialRemaining > request.receivedAmount) {
            throw IllegalArgumentException("Remaining amount must be between 0 and received amount")
        }

        val batch = Batch()
        batch.code = request.code.trim()
        batch.receptionDate = request.receptionDate
        batch.expirationDate = request.expirationDate
        batch.receivedAmount = request.receivedAmount
        batch.remainingAmount = initialRemaining
        batch.available = request.available
        batch.price = request.price.toBigDecimal()
        batch.notes = request.notes.trim()
        batch.productSupplier = relation

        batchRepository.save(batch)

        return Response(
            success = true,
            status = 200,
            message = "Batch ${batch.code} created successfully",
        )
    }

    @Transactional
    fun updateBatch(batchId: Long, request: BatchUpsertRequest): Response {
        validateRequest(request)

        val batch = batchRepository.findById(batchId)
            .orElseThrow { EntityNotFundException("Batch with id $batchId not found") }

        val relation = productSupplierRepository.findByProduct_NameAndSupplier_Name(
            request.productName.trim(),
            request.supplierName.trim(),
        ) ?: throw EntityNotFundException("Supplier ${request.supplierName} is not related to product ${request.productName}")

        val consumedAmount = batch.receivedAmount - batch.remainingAmount
        if (request.receivedAmount < consumedAmount) {
            throw IllegalArgumentException("Received amount cannot be less than consumed amount ($consumedAmount)")
        }

        val desiredRemaining = request.remainingAmount ?: (request.receivedAmount - consumedAmount)
        if (desiredRemaining < 0 || desiredRemaining > request.receivedAmount) {
            throw IllegalArgumentException("Remaining amount must be between 0 and received amount")
        }

        batch.code = request.code.trim()
        batch.receptionDate = request.receptionDate
        batch.expirationDate = request.expirationDate
        batch.receivedAmount = request.receivedAmount
        batch.remainingAmount = desiredRemaining
        batch.available = request.available
        batch.price = request.price.toBigDecimal()
        batch.notes = request.notes.trim()
        batch.productSupplier = relation

        batchRepository.save(batch)

        return Response(
            success = true,
            status = 200,
            message = "Batch ${batch.code} updated successfully",
        )
    }

    @Transactional
    fun deleteBatch(batchId: Long): Response {
        val batch = batchRepository.findById(batchId)
            .orElseThrow { EntityNotFundException("Batch with id $batchId not found") }


        batchRepository.delete(batch)

        return Response(
            success = true,
            status = 200,
            message = "Batch ${batch.code} deleted successfully",
        )
    }

    private fun validateRequest(request: BatchUpsertRequest) {
        if (request.code.trim().isEmpty()) throw IllegalArgumentException("Batch code is required")
        if (request.productName.trim().isEmpty()) throw IllegalArgumentException("Product name is required")
        if (request.supplierName.trim().isEmpty()) throw IllegalArgumentException("Supplier name is required")
        if (request.receivedAmount <= 0) throw IllegalArgumentException("Received amount must be greater than zero")
        if (request.price < 0) throw IllegalArgumentException("Batch price cannot be negative")
        if (request.expirationDate.isBefore(request.receptionDate)) {
            throw IllegalArgumentException("Expiration date cannot be before reception date")
        }
    }
}

