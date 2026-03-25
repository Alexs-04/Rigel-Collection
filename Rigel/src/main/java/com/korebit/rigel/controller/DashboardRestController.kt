package com.korebit.rigel.controller

import com.korebit.rigel.dto.DashboardSnapshot
import com.korebit.rigel.dto.DashboardStats
import com.korebit.rigel.dto.DashboardTopItem
import com.korebit.rigel.service.DashboardService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/dashboard")
class DashboardRestController(private val dashboardService: DashboardService) {

    @GetMapping("/stats")
    fun getDashboardStats(): DashboardStats {
        return dashboardService.getDashboardStats()
    }

    @GetMapping("/snapshot")
    fun getDashboardSnapshot(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false, defaultValue = "5")
        limit: Int,
    ): DashboardSnapshot {
        return dashboardService.getDashboardSnapshot(date, limit)
    }

    @GetMapping("/sales/day")
    fun getSalesForDay(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): BigDecimal {
        return dashboardService.getSalesForDay(date)
    }

    @GetMapping("/sales/month")
    fun getSalesForMonth(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): BigDecimal {
        return dashboardService.getSalesForMonth(date)
    }

    @GetMapping("/sales/year")
    fun getSalesForYear(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): BigDecimal {
        return dashboardService.getSalesForYear(date)
    }

    @GetMapping("/sales/top-products")
    fun getMayorProductSale(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false, defaultValue = "5")
        limit: Int,
    ): List<DashboardTopItem> {
        return dashboardService.getMayorProductSale(date, limit)
    }

    @GetMapping("/sales/top-suppliers")
    fun getTopSellingSupplier(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false, defaultValue = "5")
        limit: Int,
    ): List<DashboardTopItem> {
        return dashboardService.getTopSellingSupplier(date, limit)
    }
}