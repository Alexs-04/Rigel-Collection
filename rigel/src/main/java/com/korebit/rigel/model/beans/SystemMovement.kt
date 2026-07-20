package com.korebit.rigel.model.beans

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant

@Entity(name = "system_movements")
@Table(
    name = "system_movements",
    indexes = [
        Index(name = "idx_system_movements_occurred_at", columnList = "occurred_at"),
        Index(name = "idx_system_movements_username", columnList = "username"),
        Index(name = "idx_system_movements_path", columnList = "path"),
    ],
)
class SystemMovement(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = Instant.now(),

    @Column(nullable = false, length = 120)
    var username: String = "anonymous",

    @Column(nullable = false, length = 32)
    var role: String = "ANONYMOUS",

    @Column(nullable = false, length = 12)
    var method: String = "GET",

    @Column(nullable = false, length = 255)
    var path: String = "",

    @Column(nullable = false)
    var status: Int = 200,

    @Column(nullable = false)
    var durationMs: Long = 0,

    @Column(nullable = false, length = 64)
    var correlationId: String = "N/A",
) : Serializable


