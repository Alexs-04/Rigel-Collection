package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.extra.ProductSupplier
import java.io.Serializable

data class SupplierRequest(
    val name: String,
    val contactEmail: String,
    val phoneNumber: String,
    val products: MutableList<ProductSupplier>
) : Serializable {
    companion object {
        fun toRequest(supplier: Supplier): SupplierRequest {
            return SupplierRequest(
                name = supplier.name,
                contactEmail = supplier.email,
                phoneNumber = supplier.numberPhone,
                products = supplier.products
            )
        }
    }
}
