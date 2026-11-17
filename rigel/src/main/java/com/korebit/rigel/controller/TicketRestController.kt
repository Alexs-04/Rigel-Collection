package com.korebit.rigel.controller

import com.korebit.rigel.dto.TicketDto
import com.korebit.rigel.dto.request.TicketAddRequest
import com.korebit.rigel.service.TicketService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tickets")
class TicketRestController(
    private val ticketService: TicketService,
) {

    @GetMapping
    fun getAllTickets(): ResponseEntity<List<TicketDto>> = ResponseEntity.ok(ticketService.getAllTickets())

    /**
     * Registers a sale. The seller is the authenticated user.
     */
    @PostMapping
    fun createTicket(@RequestBody request: TicketAddRequest, authentication: Authentication): ResponseEntity<TicketDto> =
        ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addTicket(request, authentication.name))

    @GetMapping("/{barcode}")
    fun getTicketByBarcode(@PathVariable barcode: String): ResponseEntity<TicketDto> =
        ResponseEntity.ok(ticketService.getTicketByBarcode(barcode))

    @DeleteMapping("/{barcode}")
    fun deleteTicket(@PathVariable barcode: String): ResponseEntity<Void> {
        ticketService.deleteTicket(barcode)
        return ResponseEntity.noContent().build()
    }
}
