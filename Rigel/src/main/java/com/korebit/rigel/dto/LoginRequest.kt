package com.korebit.rigel.dto

import java.io.Serializable

data class LoginRequest(
    val password: String? = null,
    val email: String? = null
) : Serializable