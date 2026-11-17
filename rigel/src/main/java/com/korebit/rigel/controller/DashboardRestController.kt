package com.korebit.rigel.controller

import com.korebit.rigel.dto.DashboardSnapshot
import com.korebit.rigel.dto.DashboardStats
import com.korebit.rigel.dto.DashboardTopItem
import com.korebit.rigel.service.DashboardService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
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
    fun getDashboardStats(): ResponseEntity<DashboardStats> {
        return ResponseEntity.ok(dashboardService.getDashboardStats())
    }

    @GetMapping("/snapshot")
    fun getDashboardSnapshot(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false, defaultValue = "5")
        limit: Int,
    ): ResponseEntity<DashboardSnapshot> {
        return ResponseEntity.ok(dashboardService.getDashboardSnapshot(date, limit))
    }

    @GetMapping("/sales/day")
    fun getSalesForDay(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): ResponseEntity<BigDecimal> {
        return ResponseEntity.ok(dashboardService.getSalesForDay(date))
    }

    @GetMapping("/sales/month")
    fun getSalesForMonth(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): ResponseEntity<BigDecimal> {
        return ResponseEntity.ok(dashboardService.getSalesForMonth(date))
    }

    @GetMapping("/sales/year")
    fun getSalesForYear(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
    ): ResponseEntity<BigDecimal> {
        return ResponseEntity.ok(dashboardService.getSalesForYear(date))
    }

    @GetMapping("/sales/top-products")
    fun getMayorProductSale(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false, defaultValue = "5")
        limit: Int,
    ): ResponseEntity<List<DashboardTopItem>> {
        return ResponseEntity.ok(dashboardService.getMayorProductSale(date, limit))
    }

    @GetMapping("/sales/top-suppliers")
    fun getTopSellingSupplier(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?,
        @RequestParam(required = false, defaultValue = "5")
        limit: Int,
    ): ResponseEntity<List<DashboardTopItem>> {
        return ResponseEntity.ok(dashboardService.getTopSellingSupplier(date, limit))
    }
}