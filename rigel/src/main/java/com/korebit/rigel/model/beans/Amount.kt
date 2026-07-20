package com.korebit.rigel.model.beans

import com.korebit.rigel.enums.ContainerType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import jakarta.persistence.Table

@Entity(name = "amounts")
@Table(name = "amounts")
class Amount(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var folio: Long = 0,

    @Column(length = 100)
    var description: String = "",

    @Enumerated(EnumType.STRING)
    var type: ContainerType = ContainerType.BEER_CONTAINER,

    @Column(nullable = false, length = 128)
    var customerName: String = "",

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false, precision = 19, scale = 4)
    var saleUnitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = true, precision = 19, scale = 4)
    var buyoutUnitPrice: BigDecimal? = null,

    @Column(nullable = false)
    var total: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var created: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var expirationDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var returned : Boolean = false,

    @Column(nullable = true)
    var returnedAt: LocalDateTime? = null,

    @Column(nullable = true)
    var boughtOutAt: LocalDateTime? = null,

    @Column(nullable = true, precision = 19, scale = 4)
    var buyoutTotal: BigDecimal? = null,

    @Column(nullable = false, length = 256)
    var notes: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var consumer: Consumer? = null,
) : Serializable