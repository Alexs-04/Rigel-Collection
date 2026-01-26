package com.korebit.rigel.model

import com.korebit.rigel.model.enums.Role
import jakarta.persistence.*
import java.io.Serializable

@Entity(name = "consumers")
class Consumer (
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(nullable = false, length = 64)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    @Column(nullable = false, length = 32, unique = true)
    var username: String = "",

    @Column(nullable = false, length = 128)
    var password: String? = "",

    @Column(unique = true, length = 128, nullable = false)
    var email: String = "",

    @Column(nullable = true, length = 15)
    var phoneNumber: String = "",

    @OneToMany(mappedBy = "consumer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var tokens: MutableList<Token> = mutableListOf(),
) : Serializable