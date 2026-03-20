package com.korebit.rigel.model.beans

import com.korebit.rigel.model.enums.ContainerType
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

@Entity(name = "amounts")
class Amount(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var folio: Long = 0,

    @Column(length = 100)
    var description: String = "",

    @Enumerated(EnumType.STRING)
    var type: ContainerType = ContainerType.BEER_CONTAINER,

    @Column(nullable = false)
    var quantity: Int = 0,

    @Column(nullable = false)
    var total: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var created: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var expirationDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var returned : Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    var consumer: Consumer? = null,
) : Serializable