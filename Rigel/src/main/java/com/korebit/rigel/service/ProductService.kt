package com.korebit.rigel.service

import com.korebit.rigel.dto.ProductRequest
import com.korebit.rigel.dto.response.Response
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

    fun getAllProducts(): List<ProductRequest?> {
        val products = productRepository.findAll()

        return products.map { product -> ProductRequest.Companion.toRequest(product) }
    }

    fun saveProduct(product: ProductRequest, supplierName: String, supplierPrice: Double): Response {
        val supplier: Supplier = supplierRepository.findSupplierByName(supplierName) ?: return Response(
            success = false,
            message = "Supplier with name $supplierName not found.",
            status = 400
        )

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
}