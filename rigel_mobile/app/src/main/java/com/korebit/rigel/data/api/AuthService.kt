package com.korebit.rigel.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String = "",
    @SerializedName("refresh_token")
    val refreshToken: String? = null
)

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
}