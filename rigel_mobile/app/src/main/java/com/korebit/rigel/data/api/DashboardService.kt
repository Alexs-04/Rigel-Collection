package com.korebit.rigel.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for dashboard endpoints.
 * Mirrors backend endpoints under /dashboard
 */
interface DashboardService {
    @GET("dashboard/snapshot")
    suspend fun getSnapshot(
        @Query("date") date: String?,
        @Query("limit") limit: Int? = 5
    ): Response<DashboardSnapshotDto>
}

// DTOs used by the mobile client. We keep referenceDate as String to avoid
// LocalDate parsing issues with Gson default configuration.
data class DashboardStatsDto(
    val totalProducts: Long = 0,
    val totalSuppliers: Long = 0,
    val totalBatches: Long = 0,
    val totalTickets: Long = 0,
)

data class DashboardTopItemDto(
    val name: String = "",
    val total: Long = 0,
)

data class DashboardSalesSummaryDto(
    val day: Double = 0.0,
    val month: Double = 0.0,
    val year: Double = 0.0,
)

data class DashboardSnapshotDto(
    val referenceDate: String = "",
    val stats: DashboardStatsDto? = null,
    val sales: DashboardSalesSummaryDto? = null,
    val topProducts: List<DashboardTopItemDto> = emptyList(),
    val topSuppliers: List<DashboardTopItemDto> = emptyList(),
)

