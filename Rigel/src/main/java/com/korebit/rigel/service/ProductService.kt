package com.korebit.rigel.service

import com.korebit.rigel.dto.BatchDto
import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.request.AddRelationRequest
import com.korebit.rigel.dto.request.ProductAddRequest
import com.korebit.rigel.dto.request.ProductBatchRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Batch
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.enums.Category
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository,
    private val batchRepository: BatchRepository,
) {

    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductDto> {
        val products = productRepository.findAll()
        return products.map { product -> ProductDto.toRequest(product) }
    }

    @Transactional
    fun saveProduct(productRequest: ProductAddRequest): Response {
        val name = productRequest.name.trim()
        val barcode = productRequest.barcode.trim()
        val initialBatch = productRequest.batch
            ?: throw IllegalArgumentException("Batch data is required when creating a product")

        validateCommonFields(name, barcode, productRequest.supplierName, productRequest.supplierPrice)
        validateBatchFields(initialBatch)

        productRepository.findByName(name).ifPresent {
            throw IllegalArgumentException("Product with name $name already exists")
        }

        productRepository.findByBarcode(barcode).ifPresent {
            throw IllegalArgumentException("Product with barcode $barcode already exists")
        }

        val supplier = resolveSupplier(productRequest.supplierName)

        val newProduct = Product(
            name = name,
            description = productRequest.description.trim(),
            barcode = barcode,
            price = productRequest.price.toBigDecimal(),
            stockQuantity = 0,
            category = parseCategory(productRequest.category),
            imageUrl = productRequest.imageUrl.trim()
        )

        val relation = ProductSupplier(
            product = newProduct,
            supplier = supplier,
            supplyPrice = productRequest.supplierPrice.toBigDecimal()
        )

        relation.batches.add(createBatchEntity(initialBatch, relation))

        newProduct.suppliers.add(relation)
        recalculateStock(newProduct)

        productRepository.save(newProduct)

        return Response(
            success = true,
            message = "Product saved successfully.",
            status = 200
        )
    }

    fun addRelationToProduct(relation: AddRelationRequest): Response {
        val product = productRepository.findByName(relation.productName)
            .orElseThrow { EntityNotFundException("Product with name ${relation.productName} not found") }

        val supplier = supplierRepository.findSupplierByName(relation.supplierName)
            ?: throw EntityNotFundException("Supplier with name ${relation.supplierName} not found")

        if (product.suppliers.isNotEmpty()) {
            throw IllegalArgumentException("Product ${product.name} already has a supplier relation. Use product update instead.")
        }

        val newRelation = ProductSupplier(
            product = product,
            supplier = supplier,
            supplyPrice = relation.price.toBigDecimal()
        )

        product.suppliers.add(newRelation)
        productRepository.save(product)

        return Response(
            success = true,
            message = "Relation = $relation",
            status = 200
        )
    }

    @Transactional(readOnly = true)
    fun findProductByName(name: String): ProductDto {
        val product = productRepository
            .findByName(name)
            .orElseThrow { EntityNotFundException("Product not found") }

        return ProductDto.toRequest(product)
    }

    @Transactional
    fun updateProduct(currentName: String, productRequest: ProductAddRequest): Response {
        val existingProduct = productRepository.findByName(currentName)
            .orElseThrow { EntityNotFundException("Product not found") }

        val newName = productRequest.name.trim()
        val newBarcode = productRequest.barcode.trim()

        validateCommonFields(newName, newBarcode, productRequest.supplierName, productRequest.supplierPrice)

        if (!currentName.equals(newName, ignoreCase = false)) {
            productRepository.findByName(newName).ifPresent {
                throw IllegalArgumentException("Product with name $newName already exists")
            }
        }

        if (existingProduct.barcode != newBarcode) {
            productRepository.findByBarcode(newBarcode).ifPresent {
                throw IllegalArgumentException("Product with barcode $newBarcode already exists")
            }
        }

        val supplier = resolveSupplier(productRequest.supplierName)

        existingProduct.name = newName
        existingProduct.description = productRequest.description.trim()
        existingProduct.barcode = newBarcode
        existingProduct.price = productRequest.price.toBigDecimal()
        existingProduct.category = parseCategory(productRequest.category)
        existingProduct.imageUrl = productRequest.imageUrl.trim()

        val activeRelation = updateSupplierRelation(
            product = existingProduct,
            supplier = supplier,
            supplierPrice = productRequest.supplierPrice
        )

        productRequest.batch?.let { newBatch ->
            validateBatchFields(newBatch)
            activeRelation.batches.add(createBatchEntity(newBatch, activeRelation))
        }

        recalculateStock(existingProduct)

        productRepository.save(existingProduct)

        return Response(
            success = true,
            status = 200,
            message = "Product ${existingProduct.name} has been updated successfully"
        )
    }

    @Transactional
    fun deleteProduct(name: String): Response {
        val product = productRepository.findByName(name)
            .orElseThrow { EntityNotFundException("Product not found") }

        productRepository.delete(product)

        return Response(
            success = true,
            status = 200,
            message = "Product ${product.name} has been deleted successfully"
        )
    }

    @Transactional(readOnly = true)
    fun getBatchesByProduct(name: String): List<BatchDto> {
        val product = productRepository.findByName(name)
            .orElseThrow { EntityNotFundException("Product not found") }

        return product.suppliers
            .flatMap { it.batches }
            .sortedByDescending { it.receptionDate }
            .map(BatchDto::fromEntity)
    }

    @Transactional
    fun addBatchToProduct(name: String, batchRequest: ProductBatchRequest): Response {
        val product = productRepository.findByName(name)
            .orElseThrow { EntityNotFundException("Product not found") }

        val activeRelation = product.suppliers.firstOrNull()
            ?: throw IllegalArgumentException("Product $name does not have a supplier relation")

        validateBatchFields(batchRequest)
        activeRelation.batches.add(createBatchEntity(batchRequest, activeRelation))
        recalculateStock(product)
        productRepository.save(product)

        return Response(
            success = true,
            status = 200,
            message = "Batch ${batchRequest.code.trim()} added to product ${product.name} successfully"
        )
    }

    private fun updateSupplierRelation(product: Product, supplier: Supplier, supplierPrice: Double): ProductSupplier {
        val normalizedPrice = supplierPrice.toBigDecimal()
        val currentRelationForSupplier = product.suppliers.firstOrNull { relation ->
            relation.supplier?.id == supplier.id
        }

        if (currentRelationForSupplier != null) {
            currentRelationForSupplier.supplyPrice = normalizedPrice
            val removableRelations = product.suppliers.filter { relation ->
                relation !== currentRelationForSupplier && relation.batches.isEmpty()
            }
            product.suppliers.removeAll(removableRelations)
            return currentRelationForSupplier
        }

        val blockedRelation = product.suppliers.firstOrNull { it.batches.isNotEmpty() }
        if (blockedRelation != null) {
            throw IllegalArgumentException("Cannot change supplier because there are batches linked to the current supplier")
        }

        product.suppliers.clear()
        val newRelation = ProductSupplier(
            product = product,
            supplier = supplier,
            supplyPrice = normalizedPrice
        )
        product.suppliers.add(newRelation)
        return newRelation
    }

    private fun createBatchEntity(batchRequest: ProductBatchRequest, relation: ProductSupplier): Batch {
        val code = batchRequest.code.trim()
        if (batchRepository.findByCode(code).isPresent) {
            throw IllegalArgumentException("Batch with code $code already exists")
        }

        val remaining = batchRequest.remainingAmount ?: batchRequest.receivedAmount
        return Batch().apply {
            this.code = code
            this.receptionDate = batchRequest.receptionDate
            this.expirationDate = batchRequest.expirationDate
            this.receivedAmount = batchRequest.receivedAmount
            this.remainingAmount = remaining
            this.available = batchRequest.available
            this.price = batchRequest.price.toBigDecimal()
            this.notes = batchRequest.notes.trim()
            this.productSupplier = relation
        }
    }

    private fun recalculateStock(product: Product) {
        product.stockQuantity = product.suppliers
            .flatMap { it.batches }
            .sumOf { it.remainingAmount }
    }

    private fun resolveSupplier(name: String): Supplier {
        val normalizedName = name.trim()
        return supplierRepository.findSupplierByName(normalizedName)
            ?: throw EntityNotFundException("Supplier not found")
    }

    private fun parseCategory(rawCategory: String): Category {
        val normalized = rawCategory.trim().uppercase()
        return runCatching { Category.valueOf(normalized) }
            .getOrElse {
                throw IllegalArgumentException("Invalid category: $rawCategory")
            }
    }

    private fun validateCommonFields(name: String, barcode: String, supplierName: String, supplierPrice: Double) {
        if (name.isBlank()) throw IllegalArgumentException("Product name is required")
        if (barcode.isBlank()) throw IllegalArgumentException("Product barcode is required")
        if (supplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")
        if (supplierPrice < 0) throw IllegalArgumentException("Supplier price cannot be negative")
    }

    private fun validateBatchFields(batchRequest: ProductBatchRequest) {
        val code = batchRequest.code.trim()
        val remaining = batchRequest.remainingAmount ?: batchRequest.receivedAmount

        if (code.isBlank()) throw IllegalArgumentException("Batch code is required")
        if (batchRequest.receivedAmount <= 0) throw IllegalArgumentException("Batch received amount must be greater than zero")
        if (remaining < 0 || remaining > batchRequest.receivedAmount) {
            throw IllegalArgumentException("Batch remaining amount must be between 0 and received amount")
        }
        if (batchRequest.price < 0) throw IllegalArgumentException("Batch price cannot be negative")
        if (batchRequest.expirationDate.isBefore(batchRequest.receptionDate)) {
            throw IllegalArgumentException("Batch expiration date cannot be before reception date")
        }
    }
}