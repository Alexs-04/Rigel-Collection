package com.korebit.rigel.dto.request

import java.io.Serializable

data class LoginRequest(
    val username: String,
    val password: String
) : Serializable
