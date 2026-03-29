package com.korebit.rigel.dto

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

data class DashboardStats(
    val totalProducts: Long,
    val totalSuppliers: Long,
    val totalBatches: Long,
    val totalTickets: Long,
) : Serializable

data class DashboardTopItem(
    val name: String,
    val total: Long,
) : Serializable

data class DashboardSalesSummary(
    val day: BigDecimal,
    val month: BigDecimal,
    val year: BigDecimal,
) : Serializable

data class DashboardSnapshot(
    val referenceDate: LocalDate,
    val stats: DashboardStats,
    val sales: DashboardSalesSummary,
    val topProducts: List<DashboardTopItem>,
    val topSuppliers: List<DashboardTopItem>,
) : Serializable
