package com.korebit.rigel.service

import com.korebit.rigel.dto.DashboardSalesSummary
import com.korebit.rigel.dto.DashboardSnapshot
import com.korebit.rigel.dto.DashboardStats
import com.korebit.rigel.dto.DashboardTopItem
import com.korebit.rigel.repository.BatchRepository
import com.korebit.rigel.repository.ProductRepository
import com.korebit.rigel.repository.SupplierRepository
import com.korebit.rigel.repository.TicketRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate

@Service
class DashboardService(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepository,
    private val batchRepository: BatchRepository,
    private val ticketRepository: TicketRepository
) {
    @Transactional(readOnly = true)
    fun getDashboardStats(): DashboardStats {
        return DashboardStats(
            totalProducts = productRepository.count(),
            totalSuppliers = supplierRepository.count(),
            totalBatches = batchRepository.count(),
            totalTickets = ticketRepository.count(),
        )
    }

    @Transactional(readOnly = true)
    fun getSalesForMonth(date: LocalDate? = null): BigDecimal {
        val (startDate, endDate) = monthRange(date)
        return sumSalesBetween(startDate, endDate)
    }

    @Transactional(readOnly = true)
    fun getSalesForDay(date: LocalDate? = null): BigDecimal {
        val safeDate = requireDate(date)
        return sumSalesBetween(safeDate, safeDate)
    }

    @Transactional(readOnly = true)
    fun getSalesForYear(date: LocalDate? = null): BigDecimal {
        val (startDate, endDate) = yearRange(date)
        return sumSalesBetween(startDate, endDate)
    }

    @Transactional(readOnly = true)
    fun getMayorProductSale(date: LocalDate? = null, limit: Int = DEFAULT_TOP_LIMIT): List<DashboardTopItem> {
        val sanitizedLimit = sanitizeLimit(limit)
        val (startDate, endDate) = monthRange(date)
        return ticketRepository.findTopSellingProducts(startDate, endDate)
            .take(sanitizedLimit)
            .map { DashboardTopItem(name = it.name, total = it.total) }
    }

    @Transactional(readOnly = true)
    fun getTopSellingSupplier(date: LocalDate? = null, limit: Int = DEFAULT_TOP_LIMIT): List<DashboardTopItem> {
        val sanitizedLimit = sanitizeLimit(limit)
        val (startDate, endDate) = monthRange(date)
        return ticketRepository.findTopSellingSuppliers(startDate, endDate)
            .take(sanitizedLimit)
            .map { DashboardTopItem(name = it.name, total = it.total) }
    }

    @Transactional(readOnly = true)
    fun getDashboardSnapshot(date: LocalDate? = null, limit: Int = DEFAULT_TOP_LIMIT): DashboardSnapshot {
        val safeDate = requireDate(date)
        val sales = DashboardSalesSummary(
            day = getSalesForDay(safeDate),
            month = getSalesForMonth(safeDate),
            year = getSalesForYear(safeDate),
        )

        return DashboardSnapshot(
            referenceDate = safeDate,
            stats = getDashboardStats(),
            sales = sales,
            topProducts = getMayorProductSale(safeDate, limit),
            topSuppliers = getTopSellingSupplier(safeDate, limit),
        )
    }

    private fun sumSalesBetween(startDate: LocalDate, endDate: LocalDate): BigDecimal {
        validateRange(startDate, endDate)
        return ticketRepository.sumTotalAmountBetween(startDate, endDate) ?: BigDecimal.ZERO
    }

    private fun monthRange(date: LocalDate?): Pair<LocalDate, LocalDate> {
        val safeDate = requireDate(date)
        return safeDate.withDayOfMonth(1) to safeDate.withDayOfMonth(safeDate.lengthOfMonth())
    }

    private fun yearRange(date: LocalDate?): Pair<LocalDate, LocalDate> {
        val safeDate = requireDate(date)
        return safeDate.withDayOfYear(1) to safeDate.withDayOfYear(safeDate.lengthOfYear())
    }

    private fun validateRange(startDate: LocalDate, endDate: LocalDate) {
        require(!endDate.isBefore(startDate)) { "Invalid range: endDate must be greater than or equal to startDate" }
    }

    private fun sanitizeLimit(limit: Int): Int {
        require(limit > 0) { "Limit must be greater than 0" }
        require(limit <= MAX_TOP_LIMIT) { "Limit must be less than or equal to $MAX_TOP_LIMIT" }
        return limit
    }

    private fun requireDate(date: LocalDate?): LocalDate {
        return date ?: LocalDate.now()
    }

    companion object {
        private const val DEFAULT_TOP_LIMIT = 5
        private const val MAX_TOP_LIMIT = 20
    }
}
