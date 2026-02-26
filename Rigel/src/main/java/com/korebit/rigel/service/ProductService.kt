package com.korebit.rigel.service

import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository
) {

    fun getAllProducts(): List<ProductDto?> {
        val products = productRepository.findAll()

        return products.map { product -> ProductDto.Companion.toRequest(product) }
    }

    fun saveProduct(product: ProductDto, supplierName: String, supplierPrice: Double): Response {
        val supplier: Supplier = supplierRepository.findSupplierByName(supplierName)
            ?: throw EntityNotFundException("Supplier not found")

        val newProduct = Product(
            name = product.name,
            description = product.description,
            price = supplierPrice.toBigDecimal(),
            stockQuantity = product.stock,
            imageUrl = product.imageUrl
        )

        val relation = ProductSupplier(
            product = newProduct,
            supplier = supplier,
            supplyPrice = supplierPrice.toBigDecimal()
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

    fun findProductByName(name: String): ProductDto {
        val product = productRepository
            .findByName(name)
            .orElseThrow { EntityNotFundException("Product not found") }

        return ProductDto.Companion.toRequest(product)
    }
}