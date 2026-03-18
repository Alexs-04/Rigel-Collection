package com.korebit.rigel.model.extra

import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Purchase
import com.korebit.rigel.model.beans.Supplier
import com.korebit.rigel.model.beans.Batch
import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal

@Entity(name = "products_suppliers")
class ProductSupplier (
    @EmbeddedId
    var id : ProductSupplierId = ProductSupplierId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    var product : Product? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("supplierId")
    @JoinColumn(name = "supplier_id", nullable = false)
    var supplier : Supplier? = null,

    var supplyPrice : BigDecimal = BigDecimal.ZERO,

    @OneToMany(mappedBy = "productSupplier", cascade = [CascadeType.ALL], orphanRemoval = true)
    var batches: MutableList<Batch> = mutableListOf(),

    @OneToMany(mappedBy = "productSupplier", cascade = [CascadeType.ALL], orphanRemoval = true)
    var purchases: MutableList<Purchase> = mutableListOf()

): Serializable