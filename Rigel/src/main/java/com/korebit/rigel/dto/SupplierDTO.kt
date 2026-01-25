package com.korebit.rigel.dto

import com.korebit.rigel.model.Product
import com.korebit.rigel.model.Supplier
import java.io.Serializable

data class SupplierDTO(
    val name: String,
    val contactEmail: String,
    val phoneNumber: String,
    val products: MutableList<Product>
) : Serializable{
    fun toDTO(supplier : Supplier): SupplierDTO {
        return SupplierDTO(
            name = supplier.name,
            contactEmail = supplier.email,
            phoneNumber = supplier.numberPhone,
            products = supplier.products
        )
    }
}
