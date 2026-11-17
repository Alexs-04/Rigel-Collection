package com.korebit.rigel.service

import com.korebit.rigel.dto.BatchDto
import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.request.AddRelationRequest
import com.korebit.rigel.dto.request.ProductAddRequest
import com.korebit.rigel.dto.request.ProductBatchRequest
import com.korebit.rigel.enums.Category
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * Manages products, product-supplier relations, and product-level batch operations.
 */
@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository,
    private val batchRepository: BatchRepository,
    private val productSupplierRepository: ProductSupplierRepository,
) {

    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductDto> = productRepository.findAll().map(ProductDto::fromEntity)

    @Transactional(readOnly = true)
    fun findProductByName(name: String): ProductDto = ProductDto.fromEntity(findProduct(name))

    /** Batches of every supplier of the product, newest reception first. */
    @Transactional(readOnly = true)
    fun getBatchesByProduct(name: String): List<BatchDto> {
        return findProduct(name).suppliers
            .flatMap { it.batches }
            .sortedByDescending { it.receptionDate }
            .map(BatchDto::fromEntity)
    }

    /** Creates a product with its initial supplier relation (and optionally a first batch). */
    @Transactional
    fun saveProduct(productRequest: ProductAddRequest): ProductDto {
        val fields = validateCommonFields(productRequest)

        productRepository.findByName(fields.name).ifPresent {
            throw IllegalArgumentException("Product with name ${fields.name} already exists")
        }
        productRepository.findByBarcode(fields.barcode).ifPresent {
            throw IllegalArgumentException("Product with barcode ${fields.barcode} already exists")
        }

        val supplier = resolveSupplier(fields.supplierName)
        val newProduct = Product(
            name = fields.name,
            description = productRequest.description.safeTrim(),
            barcode = fields.barcode,
            price = (productRequest.price ?: 0.0).toBigDecimal(),
            category = parseCategory(productRequest.category.safeTrim()),
            imageUrl = productRequest.imageUrl.safeTrim(),
            cloudinaryPublicId = productRequest.cloudinaryPublicId.safeTrim(),
            minStock = validateMinStock(productRequest.minStock),
        )

        val relation = ProductSupplier(product = newProduct, supplier = supplier, supplyPrice = fields.supplierPrice)
        productRequest.batch?.let { relation.batches.add(createBatchEntity(it, relation)) }
        newProduct.suppliers.add(relation)

        return ProductDto.fromEntity(productRepository.save(newProduct))
    }

    /** Updates core data and the price of the given supplier relation (created if missing). */
    @Transactional
    fun updateProduct(currentName: String, productRequest: ProductAddRequest): ProductDto {
        val existingProduct = findProduct(currentName)
        val fields = validateCommonFields(productRequest)

        if (existingProduct.name != fields.name) {
            productRepository.findByName(fields.name).ifPresent {
                throw IllegalArgumentException("Product with name ${fields.name} already exists")
            }
        }
        if (existingProduct.barcode != fields.barcode) {
            productRepository.findByBarcode(fields.barcode).ifPresent {
                throw IllegalArgumentException("Product with barcode ${fields.barcode} already exists")
            }
        }

        val supplier = resolveSupplier(fields.supplierName)

        existingProduct.name = fields.name
        existingProduct.description = productRequest.description.safeTrim()
        existingProduct.barcode = fields.barcode
        existingProduct.price = (productRequest.price ?: 0.0).toBigDecimal()
        existingProduct.category = parseCategory(productRequest.category.safeTrim())
        existingProduct.imageUrl = productRequest.imageUrl.safeTrim()
        existingProduct.cloudinaryPublicId = productRequest.cloudinaryPublicId.safeTrim()
        productRequest.minStock?.let { existingProduct.minStock = validateMinStock(it) }

        val activeRelation = upsertSupplierRelation(existingProduct, supplier, fields.supplierPrice)
        productRequest.batch?.let { activeRelation.batches.add(createBatchEntity(it, activeRelation)) }

        return ProductDto.fromEntity(productRepository.save(existingProduct))
    }

    @Transactional
    fun deleteProduct(name: String) {
        productRepository.delete(findProduct(name))
    }

    @Transactional
    fun addRelationToProduct(relation: AddRelationRequest): ProductDto {
        val productName = relation.productName.safeTrim()
        val supplierName = relation.supplierName.safeTrim()

        if (productName.isBlank()) throw IllegalArgumentException("Product name is required")
        if (supplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")
        if (relation.price < 0) throw IllegalArgumentException("Supplier price cannot be negative")

        val product = findProduct(productName)
        val supplier = resolveSupplier(supplierName)

        if (product.suppliers.any { it.supplier?.id == supplier.id }) {
            throw IllegalArgumentException("Supplier $supplierName is already associated with product $productName")
        }

        product.suppliers.add(ProductSupplier(product = product, supplier = supplier, supplyPrice = relation.price.toBigDecimal()))
        return ProductDto.fromEntity(productRepository.save(product))
    }

    /** Detaches a supplier unless it is the last one or it already has batches/purchases. */
    @Transactional
    fun removeRelationFromProduct(productName: String, supplierName: String): ProductDto {
        val normalizedProductName = productName.safeTrim()
        val normalizedSupplierName = supplierName.safeTrim()

        if (normalizedProductName.isBlank()) throw IllegalArgumentException("Product name is required")
        if (normalizedSupplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")

        val product = findProduct(normalizedProductName)
        val relation = productSupplierRepository.findByProductNameAndSupplierName(
            normalizedProductName,
            normalizedSupplierName
        ) ?: throw EntityNotFundException(
            "Supplier $normalizedSupplierName is not associated with product $normalizedProductName"
        )

        if (product.suppliers.size <= 1) {
            throw IllegalArgumentException("Product must keep at least one supplier associated")
        }
        if (relation.batches.isNotEmpty() || relation.purchases.isNotEmpty()) {
            throw IllegalArgumentException(
                "Cannot remove supplier relation because it has batches or purchases associated"
            )
        }

        product.suppliers.removeIf { it.id == relation.id }
        return ProductDto.fromEntity(productRepository.save(product))
    }

    /** Adds a batch to the first supplier relation of the product. */
    @Transactional
    fun addBatchToProduct(name: String, batchRequest: ProductBatchRequest): ProductDto {
        val product = findProduct(name)
        val activeRelation = product.suppliers.firstOrNull()
            ?: throw IllegalArgumentException("Product $name does not have a supplier relation")

        activeRelation.batches.add(createBatchEntity(batchRequest, activeRelation))
        return ProductDto.fromEntity(productRepository.save(product))
    }

    /** Sets (or clears, with `null`) the low-stock alert threshold. */
    @Transactional
    fun updateMinStock(barcode: String, minStock: Int?): ProductDto {
        val product = productRepository.findByBarcode(barcode)
            .orElseThrow { EntityNotFundException("Product with barcode $barcode not found") }
        product.minStock = validateMinStock(minStock)
        return ProductDto.fromEntity(productRepository.save(product))
    }

    private fun findProduct(name: String): Product = productRepository.findByName(name.trim())
        .orElseThrow { EntityNotFundException("Product $name not found") }

    private fun upsertSupplierRelation(product: Product, supplier: Supplier, supplierPrice: BigDecimal): ProductSupplier {
        product.suppliers.firstOrNull { it.supplier?.id == supplier.id }?.let { existing ->
            existing.supplyPrice = supplierPrice
            return existing
        }

        val newRelation = ProductSupplier(product = product, supplier = supplier, supplyPrice = supplierPrice)
        product.suppliers.add(newRelation)
        return newRelation
    }

    private fun createBatchEntity(batchRequest: ProductBatchRequest, relation: ProductSupplier): Batch {
        val batch = validateBatchFields(batchRequest)
        if (batchRepository.findByCode(batch.code).isPresent) {
            throw IllegalArgumentException("Batch with code ${batch.code} already exists")
        }
        batch.productSupplier = relation
        return batch
    }

    private fun resolveSupplier(name: String): Supplier {
        return supplierRepository.findSupplierByName(name.trim())
            ?: throw EntityNotFundException("Supplier $name not found")
    }

    private fun parseCategory(rawCategory: String): Category {
        return runCatching { Category.valueOf(rawCategory.trim().uppercase()) }
            .getOrElse { throw IllegalArgumentException("Invalid category: $rawCategory") }
    }

    private fun validateMinStock(minStock: Int?): Int? {
        if (minStock != null && minStock < 0) throw IllegalArgumentException("Minimum stock cannot be negative")
        return minStock
    }

    private data class CommonFields(
        val name: String,
        val barcode: String,
        val supplierName: String,
        val supplierPrice: BigDecimal,
    )

    private fun validateCommonFields(request: ProductAddRequest): CommonFields {
        val name = request.name.safeTrim()
        val barcode = request.barcode.safeTrim()
        val supplierName = request.supplierName.safeTrim()
        val supplierPrice = request.supplierPrice

        if (name.isBlank()) throw IllegalArgumentException("Product name is required")
        if (barcode.isBlank()) throw IllegalArgumentException("Product barcode is required")
        if (supplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")
        if (supplierPrice == null) throw IllegalArgumentException("Supplier price is required")
        if (supplierPrice < 0) throw IllegalArgumentException("Supplier price cannot be negative")
        if ((request.price ?: 0.0) < 0) throw IllegalArgumentException("Product price cannot be negative")

        return CommonFields(name, barcode, supplierName, supplierPrice.toBigDecimal())
    }

    /** Validates the batch payload and returns an unattached [Batch] entity. */
    private fun validateBatchFields(batchRequest: ProductBatchRequest): Batch {
        val code = batchRequest.code.safeTrim()
        val receptionDate = batchRequest.receptionDate
        val expirationDate = batchRequest.expirationDate
        val receivedAmount = batchRequest.receivedAmount
        val price = batchRequest.price

        if (code.isBlank()) throw IllegalArgumentException("Batch code is required")
        if (receptionDate == null) throw IllegalArgumentException("Batch reception date is required")
        if (expirationDate == null) throw IllegalArgumentException("Batch expiration date is required")
        if (receivedAmount == null || receivedAmount <= 0) {
            throw IllegalArgumentException("Batch received amount must be greater than zero")
        }
        val remaining = batchRequest.remainingAmount ?: receivedAmount
        if (remaining !in 0..receivedAmount) {
            throw IllegalArgumentException("Batch remaining amount must be between 0 and received amount")
        }
        if (price == null) throw IllegalArgumentException("Batch price is required")
        if (price < 0) throw IllegalArgumentException("Batch price cannot be negative")
        if (expirationDate.isBefore(receptionDate)) {
            throw IllegalArgumentException("Batch expiration date cannot be before reception date")
        }

        return Batch().apply {
            this.code = code
            this.receptionDate = receptionDate
            this.expirationDate = expirationDate
            this.receivedAmount = receivedAmount
            this.remainingAmount = remaining
            this.available = batchRequest.available ?: true
            this.price = price.toBigDecimal()
            this.notes = batchRequest.notes.safeTrim()
        }
    }

    private fun String?.safeTrim(): String = this?.trim() ?: ""
}
