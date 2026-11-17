package com.korebit.rigel.model.beans

import com.korebit.rigel.model.extra.ProductSupplier
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Entity(name = "batches")
@Table(
    name = "batches",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_batches_code", columnNames = ["code"])
    ]
)
class Batch(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(length = 100, nullable = false)
    var code: String = "",

    @Column(nullable = false)
    var receptionDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    var expirationDate: LocalDate = LocalDate.now(),

    @Column(nullable = false, length = 20)
    var receivedAmount: Int = 0,

    @Column(nullable = false, length = 20)
    var remainingAmount: Int = 0,

    @Column(nullable = false)
    var available: Boolean = true,

    @Column(nullable = false, precision = 19, scale = 4)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var notes: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns(
        value = [
            JoinColumn(name = "product_id", referencedColumnName = "product_id", nullable = false),
            JoinColumn(name = "supplier_id", referencedColumnName = "supplier_id", nullable = false)
        ]
    )
    var productSupplier: ProductSupplier? = null,

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], orphanRemoval = true)
    var purchases: MutableList<Purchase> = mutableListOf(),
) : Serializable
