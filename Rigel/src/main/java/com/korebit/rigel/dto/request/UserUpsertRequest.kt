package com.korebit.rigel.dto.request

import com.korebit.rigel.model.enums.Role
import java.io.Serializable

data class UserUpsertRequest(
    val name: String,
    val username: String,
    val password: String? = null,
    val email: String,
    val phoneNumber: String = "",
    val role: Role = Role.USER,
) : Serializable

