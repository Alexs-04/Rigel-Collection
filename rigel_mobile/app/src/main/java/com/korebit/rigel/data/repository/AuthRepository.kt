package com.korebit.rigel.data.repository

import com.korebit.rigel.data.api.LoginRequest
import com.korebit.rigel.data.api.LoginResponse
import com.korebit.rigel.data.api.RetrofitClient

class AuthRepository {
    private val authService = RetrofitClient.authService

    suspend fun login(email: String, password: String): Result<LoginResponse> = try {
        val request = LoginRequest(email = email, password = password)
        val response = authService.login(request)

        if (response.isSuccessful) {
            Result.success(response.body() ?: LoginResponse(false, "Respuesta vacía"))
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            Result.failure(Exception(errorBody))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

