package com.korebit.rigel.model.extra

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
class ProductSupplierId(
    var productId: Long,
    var supplierId: Long
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProductSupplierId) return false

        if (productId != other.productId) return false
        if (supplierId != other.supplierId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productId.hashCode()
        result = 31 * result + supplierId.hashCode()
        return result
    }
}