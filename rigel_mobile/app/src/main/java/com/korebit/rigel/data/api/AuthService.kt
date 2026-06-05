package com.korebit.rigel.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData? = null
)

data class LoginData(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

