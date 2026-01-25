package com.korebit.rigel.service

import com.korebit.rigel.dto.ProductDTO
import com.korebit.rigel.dto.request.Response
import com.korebit.rigel.model.Product
import com.korebit.rigel.model.Supplier
import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository
) {

    fun getAllProducts(): List<ProductDTO?>? {
        val products = productRepository.findAll()

        return products?.map { product -> ProductDTO.Companion.toDTO(product) }
    }

    fun saveProduct(product: ProductDTO, supplierName: String, supplierPrice: Double): Response {
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