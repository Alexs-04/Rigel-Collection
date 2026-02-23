package com.korebit.rigel.model.beans

import com.korebit.rigel.model.extra.ProductSupplier
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.io.Serializable

@Entity(name = "suppliers")
class Supplier (
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false, length = 64, unique = true)
    var name : String = "",

    @Column(nullable = false, length = 64, name = "number_phone")
    var numberPhone : String = "",

    @Column(nullable = false, length = 64)
    var address : String = "",

    @Column(nullable = false, length = 128)
    var email : String = "",

    @Column(nullable = false, name = "is_active")
    var isActive: Boolean = true,

    @OneToMany(mappedBy = "supplier", cascade = [CascadeType.ALL], orphanRemoval = true)
    var products : MutableList<ProductSupplier> = mutableListOf()
) : Serializable