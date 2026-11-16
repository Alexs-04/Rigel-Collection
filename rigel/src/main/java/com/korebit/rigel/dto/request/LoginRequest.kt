package com.korebit.rigel.dto.request

import java.io.Serializable

data class LoginRequest(
    val email: String? = null,
    val password: String? = null,
) : Serializable
