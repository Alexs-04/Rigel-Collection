package com.korebit.rigel.service

import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.SupplierDto
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class SupplierService(
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository
) {

    fun getAllSuppliers(): List<SupplierDto> {
        return supplierRepository.findAll().map { SupplierDto.toRequest(it) }
    }

    fun getProductsBySupplier(supplierName: String): List<ProductDto?> {
        val supplier = supplierRepository.findSupplierByName(supplierName)
            ?: throw EntityNotFundException("Not match could be found")// Return empty list if supplier not found

        val products = supplier.products
        if (products.isEmpty()) {
            throw EntityNotFundException("This supplier does not supply any products") // Return empty list
        }

        return products.mapNotNull { relation ->
            relation.product?.let { ProductDto.Companion.toRequest(it) }
        }
    }

    fun getSupplierPriceForProduct(supplierName: String, productName: String): Double? {
        val supplier = supplierRepository.findSupplierByName(supplierName)
            ?: throw EntityNotFundException("Supplier not fund")// Return null if supplier not found

        val product = productRepository.findByName(productName)
            ?: throw EntityNotFundException("Product not fund") // Return null if product not found

        val relation = supplier.products.find { it.product?.id == product.getOrElse { it.id } }
            ?: throw IllegalArgumentException("This supplier does not supply the specified product") // Return null if no relation found

        return relation.supplyPrice.toDouble()
    }

    fun saveSupplier(supplier: SupplierDto): Response {
        supplierRepository.findSupplierByName(supplier.name)?.let {
            throw IllegalArgumentException("Supplier with name ${supplier.name} already exists")
        }

        val newSupplier = com.korebit.rigel.model.beans.Supplier(
            name = supplier.name,
            email = supplier.contactEmail,
            numberPhone = supplier.phoneNumber
        )

        supplierRepository.save(newSupplier)

        return Response(
            success = true,
            status = 200,
            message = "Supplier ${supplier.name} has been saved successfully"
        )
    }

    fun findSupplierByName(name: String): SupplierDto {
        val supplier = supplierRepository.findSupplierByName(name)
            ?: throw EntityNotFundException("Supplier not found")

        return SupplierDto.toRequest(supplier)
    }
}
