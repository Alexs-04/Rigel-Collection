package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.SystemMovement
import java.io.Serializable
import java.time.Instant

data class SystemMovementDto(
    val id: Long,
    val occurredAt: Instant,
    val username: String,
    val role: String,
    val method: String,
    val path: String,
    val status: Int,
    val durationMs: Long,
    val correlationId: String,
) : Serializable {
    companion object {
        fun fromEntity(entity: SystemMovement): SystemMovementDto {
            return SystemMovementDto(
                id = entity.id ?: 0,
                occurredAt = entity.occurredAt,
                username = entity.username,
                role = entity.role,
                method = entity.method,
                path = entity.path,
                status = entity.status,
                durationMs = entity.durationMs,
                correlationId = entity.correlationId,
            )
        }
    }
}

