package com.korebit.rigel.model

import com.korebit.rigel.model.enums.TokenType
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

@Entity(name = "tokenS")
class Token(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var token: String = "",

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var tokenType: TokenType = TokenType.BEARER,

    var revoked: Boolean = false,
    var expired: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var consumer: Consumer? = null,
) : Serializable