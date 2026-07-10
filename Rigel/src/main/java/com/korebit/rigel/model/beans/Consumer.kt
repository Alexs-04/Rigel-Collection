package com.korebit.rigel.model.beans

import com.korebit.rigel.enums.Role
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.io.Serializable

@Entity(name = "consumers")
class Consumer (
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null,

    @Column(nullable = false, length = 64)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    @Column(nullable = false, length = 32, unique = true)
    var username: String = "",

    @Column(nullable = true, length = 128)
    var password: String? = null,

    @Column(unique = true, length = 128, nullable = false)
    var email: String = "",

    @Column(nullable = true, length = 15)
    var phoneNumber: String = "",

    @Column(nullable = false)
    var active: Boolean = true,

    @OneToMany(mappedBy = "consumer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var tokens: MutableList<Token> = mutableListOf(),

    @OneToMany(mappedBy = "consumer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var tickets: MutableList<Ticket> = mutableListOf(),

    @OneToMany(mappedBy = "consumer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var containers : MutableList<Amount> = mutableListOf(),
) : Serializable