package com.korebit.rigel.data.repository

import com.korebit.rigel.data.api.LoginRequest
import com.korebit.rigel.data.api.TokenResponse
import com.korebit.rigel.data.api.RetrofitClient

class AuthRepository {
    private val authService = RetrofitClient.authService

    suspend fun login(email: String, password: String): Result<TokenResponse> = try {
        val request = LoginRequest(email = email, password = password)
        val response = authService.login(request)

        if (response.isSuccessful) {
            val tokenResponse = response.body()

            if (tokenResponse != null && tokenResponse.accessToken.isNotEmpty()) {
                RetrofitClient.setAuthToken(tokenResponse.accessToken)
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("Respuesta del servidor sin token válido"))
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            Result.failure(Exception(errorBody))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}