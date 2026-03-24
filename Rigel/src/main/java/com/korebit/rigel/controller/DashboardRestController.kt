package com.korebit.rigel.controller

import com.korebit.rigel.dto.DashboardStats
import com.korebit.rigel.service.DashboardService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import java.math.BigDecimal
import java.time.LocalDate

@Controller
@RequestMapping("/dashboard")
class DashboardRestController(private val dashboardService: DashboardService) {

    @GetMapping
    fun getDashboardStats() : DashboardStats{
        return dashboardService.getDashboardStats()
    }

    @GetMapping("/sales/day")
    fun getSalesForDay(date: LocalDate) : BigDecimal {
        return dashboardService.getSalesForDay(date)
    }

    @GetMapping("/sales/month")
    fun getSalesForMonth(date: LocalDate) : BigDecimal {
        return dashboardService.getSalesForMonth(date)
    }

    @GetMapping("/sales/year")
    fun getSalesForYear(date: LocalDate) : BigDecimal {
        return dashboardService.getSalesForYear(date)
    }

     @GetMapping("/sales/top-products")
    fun getMayorProductSale(date: LocalDate, limit: Int = 10) = dashboardService.getMayorProductSale(date, limit)
}