package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDate

interface TicketRepository : JpaRepository<Ticket, Long> {
    interface DashboardTopProjection {
        val name: String
        val total: Long
    }

    fun findTicketByBarcode(barcode: String): Ticket?

    @Query(
        """
        select coalesce(sum(t.totalAmount), 0)
        from tickets t
        where t.dateAndTime between :startDate and :endDate
        """
    )
    fun sumTotalAmountBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): BigDecimal?

    @Query(
        """
        select td.product.name as name, coalesce(sum(td.quantity), 0) as total
        from ticket_detail td
        join td.ticket t
        where td.product is not null
          and t.dateAndTime between :startDate and :endDate
        group by td.product.name
        order by total desc
        """
    )
    fun findTopSellingProducts(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<DashboardTopProjection>

    @Query(
        """
        select s.name as name, coalesce(sum(td.quantity), 0) as total
        from ticket_detail td
        join td.ticket t
        join td.batch b
        join b.productSupplier ps
        join ps.supplier s
        where t.dateAndTime between :startDate and :endDate
        group by s.name
        order by total desc
        """
    )
    fun findTopSellingSuppliers(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<DashboardTopProjection>
}