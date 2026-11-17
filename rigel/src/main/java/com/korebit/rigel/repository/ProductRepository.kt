package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByBarcode(code: String): Optional<Product>

    fun findByName(name: String): Optional<Product>
}
