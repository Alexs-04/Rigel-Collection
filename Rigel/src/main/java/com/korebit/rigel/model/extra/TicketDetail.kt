package com.korebit.rigel.model.extra

import com.korebit.rigel.model.beans.Product
import com.korebit.rigel.model.beans.Ticket
import com.korebit.rigel.model.beans.Batch
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable
import java.math.BigDecimal

@Entity(name = "ticket_detail")
class TicketDetail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, length = 20)
    var quantity: Int = 0,

    @Column(nullable = false, length = 20)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, length = 20)
    var discount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, length = 20)
    var subtotal: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    var ticket: Ticket? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    var product : Product? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", referencedColumnName = "id")
    var batch: Batch? = null,
) : Serializable