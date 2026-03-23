package com.korebit.rigel.dto

import com.korebit.rigel.model.beans.Consumer
import com.korebit.rigel.enums.Role
import java.io.Serializable

data class UserAdminDto(
    val id: Long,
    val name: String,
    val username: String,
    val email: String,
    val phoneNumber: String,
    val role: Role,
    val active: Boolean,
) : Serializable {
    companion object {
        fun fromConsumer(consumer: Consumer): UserAdminDto {
            return UserAdminDto(
                id = consumer.id ?: throw IllegalArgumentException("Consumer id is null"),
                name = consumer.name,
                username = consumer.username,
                email = consumer.email,
                phoneNumber = consumer.phoneNumber,
                role = consumer.role,
                active = consumer.active
            )
        }
    }
}

