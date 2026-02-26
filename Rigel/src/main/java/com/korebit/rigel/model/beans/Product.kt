package com.korebit.rigel.model.beans

import com.korebit.rigel.model.enums.Category
import com.korebit.rigel.model.extra.ProductSupplier
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.io.Serializable
import java.math.BigDecimal

@Entity(name = "products")
class Product (
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id : Long? = null,

    @Column(nullable = false, length = 64)
    var name : String = "",

    @Column(nullable = false, length = 64)
    var description : String = "",

    @Column(nullable = false, length = 32, unique = true)
    var barcode : String = "",

    @Column(nullable = false, precision = 19, scale = 4)
    var price : BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, length = 32)
    var stockQuantity : Int = 0,

    @Enumerated(EnumType.STRING)
    var category : Category = Category.OTHERS,

    @Column(nullable = true, length = 256)
    var imageUrl : String = "",

    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    var suppliers : MutableList<ProductSupplier> = mutableListOf()
) : Serializable