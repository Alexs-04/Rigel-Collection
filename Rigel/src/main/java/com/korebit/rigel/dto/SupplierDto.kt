package com.korebit.rigel.dto

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.korebit.rigel.model.beans.Supplier
import java.io.Serializable

data class SupplierDto(
    val name: String,
    val contactEmail: String,
    val phoneNumber: String,
    val address: String = "",
    @param:JsonSetter(nulls = Nulls.AS_EMPTY)
    val products: List<ProductDto> = emptyList()
) : Serializable {
    companion object {
        fun toRequest(supplier: Supplier): SupplierDto {
            return SupplierDto(
                name = supplier.name,
                contactEmail = supplier.email,
                phoneNumber = supplier.numberPhone,
                address = supplier.address,
                products = emptyList()
            )
        }
    }
}
