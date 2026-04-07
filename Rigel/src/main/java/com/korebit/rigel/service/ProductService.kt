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
import com.korebit.rigel.enums.Category
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.ProductSupplierRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository,
    private val batchRepository: BatchRepository,
    private val productSupplierRepository: ProductSupplierRepository,
) {

    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductDto> {
        val products = productRepository.findAll()
        return products.map { product -> ProductDto.toRequest(product) }
    }

    @Transactional
    fun saveProduct(productRequest: ProductAddRequest): Response {
        val name = productRequest.name.safeTrim()
        val barcode = productRequest.barcode.safeTrim()
        val supplierName = productRequest.supplierName.safeTrim()
        val supplierPrice = productRequest.supplierPrice

        validateCommonFields(name, barcode, supplierName, supplierPrice)

        productRepository.findByName(name).ifPresent {
            throw IllegalArgumentException("Product with name $name already exists")
        }

        productRepository.findByBarcode(barcode).ifPresent {
            throw IllegalArgumentException("Product with barcode $barcode already exists")
        }

        val supplier = resolveSupplier(supplierName)

        val newProduct = Product(
            name = name,
            description = productRequest.description.safeTrim(),
            barcode = barcode,
            price = (productRequest.price ?: 0.0).toBigDecimal(),
            category = parseCategory(productRequest.category.safeTrim()),
            imageUrl = productRequest.imageUrl.safeTrim()
        )

        val relation = ProductSupplier(
            product = newProduct,
            supplier = supplier,
            supplyPrice = supplierPrice!!.toBigDecimal()
        )

        productRequest.batch?.let { initialBatch ->
            validateBatchFields(initialBatch)
            relation.batches.add(createBatchEntity(initialBatch, relation))
        }

        newProduct.suppliers.add(relation)

        productRepository.save(newProduct)

        return Response(
            success = true,
            message = "Product saved successfully.",
            status = 200
        )
    }

    @Transactional
    fun addRelationToProduct(relation: AddRelationRequest): Response {
        val productName = relation.productName.safeTrim()
        val supplierName = relation.supplierName.safeTrim()

        if (productName.isBlank()) throw IllegalArgumentException("Product name is required")
        if (supplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")
        if (relation.price < 0) throw IllegalArgumentException("Supplier price cannot be negative")

        val product = productRepository.findByName(productName)
            .orElseThrow { EntityNotFundException("Product with name $productName not found") }

        val supplier = supplierRepository.findSupplierByName(supplierName)
            ?: throw EntityNotFundException("Supplier with name $supplierName not found")

        if (product.suppliers.any { it.supplier?.name.equals(supplierName, ignoreCase = true) }) {
            throw IllegalArgumentException("Supplier $supplierName is already associated with product $productName")
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
            message = "Supplier $supplierName was associated with product $productName",
            status = 200
        )
    }

    @Transactional
    fun removeRelationFromProduct(productName: String, supplierName: String): Response {
        val normalizedProductName = productName.safeTrim()
        val normalizedSupplierName = supplierName.safeTrim()

        if (normalizedProductName.isBlank()) throw IllegalArgumentException("Product name is required")
        if (normalizedSupplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")

        val product = productRepository.findByName(normalizedProductName)
            .orElseThrow { EntityNotFundException("Product with name $normalizedProductName not found") }

        val relation = productSupplierRepository.findByProduct_NameAndSupplier_Name(
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

        product.suppliers.removeIf { relationItem ->
            relationItem.supplier?.name.equals(normalizedSupplierName, ignoreCase = true)
        }
        productRepository.save(product)

        return Response(
            success = true,
            message = "Supplier $normalizedSupplierName was removed from product $normalizedProductName",
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

        val newName = productRequest.name.safeTrim()
        val newBarcode = productRequest.barcode.safeTrim()
        val supplierName = productRequest.supplierName.safeTrim()
        val supplierPrice = productRequest.supplierPrice

        validateCommonFields(newName, newBarcode, supplierName, supplierPrice)

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

        val supplier = resolveSupplier(supplierName)

        existingProduct.name = newName
        existingProduct.description = productRequest.description.safeTrim()
        existingProduct.barcode = newBarcode
        existingProduct.price = (productRequest.price ?: 0.0).toBigDecimal()
        existingProduct.category = parseCategory(productRequest.category.safeTrim())
        existingProduct.imageUrl = productRequest.imageUrl.safeTrim()

        val activeRelation = updateSupplierRelation(
            product = existingProduct,
            supplier = supplier,
            supplierPrice = supplierPrice!!
        )

        productRequest.batch?.let { newBatch ->
            validateBatchFields(newBatch)
            activeRelation.batches.add(createBatchEntity(newBatch, activeRelation))
        }

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
        productRepository.save(product)

        return Response(
            success = true,
            status = 200,
            message = "Batch ${batchRequest.code.safeTrim()} added to product ${product.name} successfully"
        )
    }

    private fun updateSupplierRelation(product: Product, supplier: Supplier, supplierPrice: Double): ProductSupplier {
        val normalizedPrice = supplierPrice.toBigDecimal()
        val currentRelationForSupplier = product.suppliers.firstOrNull { relation ->
            relation.supplier?.id == supplier.id
        }

        if (currentRelationForSupplier != null) {
            currentRelationForSupplier.supplyPrice = normalizedPrice
            return currentRelationForSupplier
        }

        val newRelation = ProductSupplier(
            product = product,
            supplier = supplier,
            supplyPrice = normalizedPrice
        )
        product.suppliers.add(newRelation)
        return newRelation
    }

    private fun createBatchEntity(batchRequest: ProductBatchRequest, relation: ProductSupplier): Batch {
        val code = batchRequest.code.safeTrim()
        val receivedAmount = batchRequest.receivedAmount ?: 0
        if (batchRepository.findByCode(code).isPresent) {
            throw IllegalArgumentException("Batch with code $code already exists")
        }

        val remaining = batchRequest.remainingAmount ?: receivedAmount
        return Batch().apply {
            this.code = code
            this.receptionDate = batchRequest.receptionDate!!
            this.expirationDate = batchRequest.expirationDate!!
            this.receivedAmount = receivedAmount
            this.remainingAmount = remaining
            this.available = batchRequest.available ?: true
            this.price = (batchRequest.price ?: 0.0).toBigDecimal()
            this.notes = batchRequest.notes.safeTrim()
            this.productSupplier = relation
        }
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

    private fun validateCommonFields(name: String, barcode: String, supplierName: String, supplierPrice: Double?) {
        if (name.isBlank()) throw IllegalArgumentException("Product name is required")
        if (barcode.isBlank()) throw IllegalArgumentException("Product barcode is required")
        if (supplierName.isBlank()) throw IllegalArgumentException("Supplier name is required")
        if (supplierPrice == null) throw IllegalArgumentException("Supplier price is required")
        if (supplierPrice < 0) throw IllegalArgumentException("Supplier price cannot be negative")
    }

    private fun validateBatchFields(batchRequest: ProductBatchRequest) {
        val code = batchRequest.code.safeTrim()
        val receptionDate = batchRequest.receptionDate
        val expirationDate = batchRequest.expirationDate
        val receivedAmount = batchRequest.receivedAmount
        val price = batchRequest.price
        val remaining = batchRequest.remainingAmount ?: receivedAmount ?: 0

        if (code.isBlank()) throw IllegalArgumentException("Batch code is required")
        if (receptionDate == null) throw IllegalArgumentException("Batch reception date is required")
        if (expirationDate == null) throw IllegalArgumentException("Batch expiration date is required")
        if (receivedAmount == null || receivedAmount <= 0) {
            throw IllegalArgumentException("Batch received amount must be greater than zero")
        }
        if (remaining !in 0..receivedAmount) {
            throw IllegalArgumentException("Batch remaining amount must be between 0 and received amount")
        }
        if (price == null) throw IllegalArgumentException("Batch price is required")
        if (price < 0) throw IllegalArgumentException("Batch price cannot be negative")
        if (expirationDate.isBefore(receptionDate)) {
            throw IllegalArgumentException("Batch expiration date cannot be before reception date")
        }
    }

    private fun String?.safeTrim(): String = this?.trim() ?: ""
}