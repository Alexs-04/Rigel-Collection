package com.korebit.rigel.repository

import com.korebit.rigel.model.Supplier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SupplierRepository : JpaRepository<Supplier, Long> {
    fun findSupplierByName(name : String): Supplier?
}