package com.korebit.rigel.model.beans

import com.korebit.rigel.model.extra.TicketDetail
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Entity(name = "tickets")
class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var folio: Long = 0,

    @Column(nullable = false, length = 100)
    var description: String = "",

    @Column(nullable = false)
    var dateAndTime: LocalDate = LocalDate.now(),

    @Column(nullable = false, length = 50, unique = true)
    var barcode: String = "",

    @Column(nullable = false, length = 20)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @OneToMany(mappedBy = "ticket", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ticketDetails: MutableList<TicketDetail> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id")
    var consumer: Consumer? = null,
) : Serializable