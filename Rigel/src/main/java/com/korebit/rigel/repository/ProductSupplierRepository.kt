package com.korebit.rigel.repository

import com.korebit.rigel.model.extra.ProductSupplier
import com.korebit.rigel.model.extra.ProductSupplierId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductSupplierRepository : JpaRepository<ProductSupplier, ProductSupplierId> {
    fun findByProduct_NameAndSupplier_Name(productName: String, supplierName: String): ProductSupplier?
}

