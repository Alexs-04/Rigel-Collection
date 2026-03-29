package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.Purchase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PurchaseRepository : JpaRepository<Purchase, Long> {
    fun findByCode(code: String): Optional<Purchase>
    fun existsByCodeIgnoreCase(code: String): Boolean

    @Query(
        """
        select p from purchases p
        join p.productSupplier ps
        where ps.product.name = :productName
        order by p.purchaseDate desc, p.id desc
        """
    )
    fun findByProductName(@Param("productName") productName: String): List<Purchase>

    @Query(
        """
        select p from purchases p
        join p.productSupplier ps
        where ps.supplier.name = :supplierName
        order by p.purchaseDate desc, p.id desc
        """
    )
    fun findBySupplierName(@Param("supplierName") supplierName: String): List<Purchase>
}

