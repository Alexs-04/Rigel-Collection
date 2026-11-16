package com.korebit.rigel.service

import com.korebit.rigel.dto.ProductDto
import com.korebit.rigel.dto.SupplierDto
import com.korebit.rigel.exception.EntityNotFundException
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Manages suppliers and exposes the products they supply.
 */
@Service
class SupplierService(
    private val supplierRepository: SupplierRepository,
) {

    @Transactional(readOnly = true)
    fun getAllSuppliers(): List<SupplierDto> = supplierRepository.findAll().map(SupplierDto::fromEntity)

    @Transactional(readOnly = true)
    fun findSupplierByName(name: String): SupplierDto = SupplierDto.fromEntity(findSupplier(name))

    /**
     * Products supplied by the given supplier. A supplier without products yields an empty list.
     *
     * @throws EntityNotFundException when the supplier does not exist.
     */
    @Transactional(readOnly = true)
    fun getProductsBySupplier(supplierName: String): List<ProductDto> {
        return findSupplier(supplierName).products
            .mapNotNull { relation -> relation.product?.let(ProductDto::fromEntity) }
    }

    /** @throws IllegalArgumentException when the name is blank or already taken. */
    @Transactional
    fun saveSupplier(supplier: SupplierDto): SupplierDto {
        val name = requireName(supplier.name)
        supplierRepository.findSupplierByName(name)?.let {
            throw IllegalArgumentException("Supplier with name $name already exists")
        }

        val newSupplier = Supplier(
            name = name,
            email = supplier.contactEmail.trim(),
            numberPhone = supplier.phoneNumber.trim(),
            address = supplier.address.trim(),
        )
        return SupplierDto.fromEntity(supplierRepository.save(newSupplier))
    }

    /** @throws IllegalArgumentException when the new name is blank or already taken. */
    @Transactional
    fun updateSupplier(currentName: String, supplier: SupplierDto): SupplierDto {
        val existingSupplier = findSupplier(currentName)
        val newName = requireName(supplier.name)

        if (existingSupplier.name != newName) {
            supplierRepository.findSupplierByName(newName)?.let {
                throw IllegalArgumentException("Supplier with name $newName already exists")
            }
        }

        existingSupplier.name = newName
        existingSupplier.email = supplier.contactEmail.trim()
        existingSupplier.numberPhone = supplier.phoneNumber.trim()
        existingSupplier.address = supplier.address.trim()
        return SupplierDto.fromEntity(supplierRepository.save(existingSupplier))
    }

    @Transactional
    fun deleteSupplier(name: String) {
        supplierRepository.delete(findSupplier(name))
    }

    private fun findSupplier(name: String): Supplier = supplierRepository.findSupplierByName(name.trim())
        ?: throw EntityNotFundException("Supplier $name not found")

    private fun requireName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) throw IllegalArgumentException("Supplier name is required")
        return trimmed
    }
}
