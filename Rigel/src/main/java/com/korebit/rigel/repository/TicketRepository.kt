package com.korebit.rigel.repository

import com.korebit.rigel.model.beans.Ticket
import org.springframework.data.jpa.repository.JpaRepository

interface TicketRepository : JpaRepository<Ticket, Long>{
   fun findTicketByBarcode(barcode: String): Ticket?
}