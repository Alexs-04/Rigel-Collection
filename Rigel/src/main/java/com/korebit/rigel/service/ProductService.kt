package com.korebit.rigel.service

import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.request.ProductAddRequest
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.enums.Category
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository
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

        validateCommonFields(name, barcode, productRequest.supplierName, productRequest.supplierPrice)

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
            stockQuantity = productRequest.stock,
            category = parseCategory(productRequest.category),
            imageUrl = productRequest.imageUrl.trim()
        )

        val relation = ProductSupplier(
            product = newProduct,
            supplier = supplier,
            supplyPrice = productRequest.supplierPrice.toBigDecimal()
        )

        newProduct.suppliers.add(relation)
        supplier.products.add(relation)

        productRepository.save(newProduct)

        return Response(
            success = true,
            message = "Product saved successfully.",
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
        existingProduct.stockQuantity = productRequest.stock
        existingProduct.category = parseCategory(productRequest.category)
        existingProduct.imageUrl = productRequest.imageUrl.trim()

        existingProduct.suppliers.clear()

        val relation = ProductSupplier(
            product = existingProduct,
            supplier = supplier,
            supplyPrice = productRequest.supplierPrice.toBigDecimal()
        )

        existingProduct.suppliers.add(relation)
        supplier.products.add(relation)

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
}