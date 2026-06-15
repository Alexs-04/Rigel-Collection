package com.korebit.rigel.data.repository

import com.korebit.rigel.data.api.DashboardSnapshotDto
import com.korebit.rigel.data.api.DashboardService
import com.korebit.rigel.data.api.RetrofitClient

/**
 * Small repository that wraps the DashboardService calls and returns
 * the DTO or throws an exception in case of errors. Callers may use
 * Kotlin coroutines to call these suspend functions.
 */
class DashboardRepository {
    private val service: DashboardService = RetrofitClient.createService(DashboardService::class.java)

    suspend fun fetchSnapshot(date: String?, limit: Int = 5): Result<DashboardSnapshotDto> = try {
        val response = service.getSnapshot(date = date, limit = limit)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) Result.success(body) else Result.failure(Exception("Empty response body"))
        } else {
            val msg = response.errorBody()?.string() ?: "Unknown error"
            Result.failure(Exception(msg))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

