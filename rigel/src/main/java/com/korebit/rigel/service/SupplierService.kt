package com.korebit.rigel.service

import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.SupplierDto
import com.korebit.rigel.dto.response.Response
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrElse

@Service
/**
 * Manages supplier read and write operations and their product relationships.
 */
class SupplierService(
    private val supplierRepository: SupplierRepository,
    private val productRepository: ProductRepository
) {

    @Transactional(readOnly = true)
    /**
     * Retrieves all suppliers registered in the system.
     *
     * @return a list of supplier DTOs.
     */
    fun getAllSuppliers(): List<SupplierDto> {
        return supplierRepository.findAll().map { SupplierDto.toRequest(it) }
    }

    @Transactional(readOnly = true)
    /**
     * Retrieves all products associated with a supplier name.
     *
     * @param supplierName supplier name to search.
     * @return products supplied by the specified supplier.
     * @throws EntityNotFundException when the supplier does not exist or has no products.
     */
    fun getProductsBySupplier(supplierName: String): List<ProductDto?> {
        val supplier = supplierRepository.findSupplierByName(supplierName)
            ?: throw EntityNotFundException("Not match could be found")// Return empty list if supplier not found

        val products = supplier.products
        if (products.isEmpty()) {
            throw EntityNotFundException("This supplier does not supply any products") // Return empty list
        }

        return products.mapNotNull { relation ->
            relation.product?.let { ProductDto.toRequest(it) }
        }
    }

    /**
     * Resolves the supply price for a product from a specific supplier.
     *
     * @param supplierName supplier name.
     * @param productName product name.
     * @return supply price when relation exists, otherwise null.
     * @throws EntityNotFundException when supplier or product does not exist.
     * @throws IllegalArgumentException when the supplier is not related to the product.
     */
    fun getSupplierPriceForProduct(supplierName: String, productName: String): Double? {
        val supplier = supplierRepository.findSupplierByName(supplierName)
            ?: throw EntityNotFundException("Supplier not fund")// Return null if supplier not found

        val product = productRepository.findByName(productName)
            ?: throw EntityNotFundException("Product not fund") // Return null if product not found

        val relation = supplier.products.find { it.product?.id == product.getOrElse { it.id } }
            ?: throw IllegalArgumentException("This supplier does not supply the specified product") // Return null if no relation found

        return relation.supplyPrice.toDouble()
    }

    @Transactional
    /**
     * Creates a new supplier.
     *
     * @param supplier supplier payload.
     * @return operation response.
     * @throws IllegalArgumentException when another supplier already uses the same name.
     */
    fun saveSupplier(supplier: SupplierDto): Response {
        supplierRepository.findSupplierByName(supplier.name)?.let {
            throw IllegalArgumentException("Supplier with name ${supplier.name} already exists")
        }

        val newSupplier = com.korebit.rigel.model.beans.Supplier(
            name = supplier.name,
            email = supplier.contactEmail,
            numberPhone = supplier.phoneNumber,
            address = supplier.address
        )

        supplierRepository.save(newSupplier)

        return Response(
            success = true,
            status = 200,
            message = "Supplier ${supplier.name} has been saved successfully"
        )
    }

    @Transactional
    /**
     * Updates an existing supplier identified by its current name.
     *
     * @param currentName current supplier name.
     * @param supplier updated supplier payload.
     * @return operation response.
     * @throws EntityNotFundException when the supplier does not exist.
     * @throws IllegalArgumentException when the new name is already taken.
     */
    fun updateSupplier(currentName: String, supplier: SupplierDto): Response {
        val existingSupplier = supplierRepository.findSupplierByName(currentName)
            ?: throw EntityNotFundException("Supplier not found")

        if (currentName != supplier.name) {
            supplierRepository.findSupplierByName(supplier.name)?.let {
                throw IllegalArgumentException("Supplier with name ${supplier.name} already exists")
            }
        }

        existingSupplier.name = supplier.name
        existingSupplier.email = supplier.contactEmail
        existingSupplier.numberPhone = supplier.phoneNumber
        existingSupplier.address = supplier.address
        supplierRepository.save(existingSupplier)

        return Response(
            success = true,
            status = 200,
            message = "Supplier ${supplier.name} has been updated successfully"
        )
    }

    @Transactional
    /**
     * Deletes a supplier by name.
     *
     * @param name supplier name.
     * @return operation response.
     * @throws EntityNotFundException when the supplier does not exist.
     */
    fun deleteSupplier(name: String): Response {
        val supplier = supplierRepository.findSupplierByName(name)
            ?: throw EntityNotFundException("Supplier not found")

        supplierRepository.delete(supplier)

        return Response(
            success = true,
            status = 200,
            message = "Supplier ${supplier.name} has been deleted successfully"
        )
    }

    @Transactional(readOnly = true)
    /**
     * Finds a supplier by name.
     *
     * @param name supplier name.
     * @return supplier DTO.
     * @throws EntityNotFundException when no supplier matches the name.
     */
    fun findSupplierByName(name: String): SupplierDto {
        val supplier = supplierRepository.findSupplierByName(name)
            ?: throw EntityNotFundException("Supplier not found")

        return SupplierDto.toRequest(supplier)
    }
}
