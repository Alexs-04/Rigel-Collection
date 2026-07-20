package com.korebit.rigel.dto.request

import com.korebit.rigel.enums.Role

data class InviteUserRequest(
    val name: String,
    val username: String,
    val email: String,
    val role: Role? = Role.USER,
    val phoneNumber: String? = null,
)