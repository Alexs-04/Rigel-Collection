package com.korebit.rigel.model.extra

import com.korebit.rigel.model.Product
import com.korebit.rigel.model.Supplier
import jakarta.persistence.*
import java.io.Serializable
import java.math.BigDecimal

@Entity(name = "products_suppliers")
class ProductSupplier (
    @EmbeddedId
    var id : ProductSupplierId? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    var product : Product? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("supplierId") // Mapea el ID del proveedor desde la clave compuesta
    @JoinColumn(name = "supplier_id", nullable = false)
    var supplier : Supplier? = null,

    var supplyPrice : BigDecimal = BigDecimal.ZERO

): Serializable