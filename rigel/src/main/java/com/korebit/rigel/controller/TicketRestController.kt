package com.korebit.rigel.controller;

import com.korebit.rigel.dto.TicketDto;
import com.korebit.rigel.dto.request.TicketAddRequest;
import com.korebit.rigel.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketRestController {

    private final TicketService ticketService;

    public TicketRestController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<TicketDto>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    /**
     * Registers a sale. The seller is the authenticated user.
     */
    @PostMapping
    public ResponseEntity<TicketDto> createTicket(@RequestBody TicketAddRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addTicket(request, authentication.getName()));
    }

    @GetMapping("/{barcode}")
    public ResponseEntity<TicketDto> getTicketByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(ticketService.getTicketByBarcode(barcode));
    }

    @DeleteMapping("/{barcode}")
    public ResponseEntity<Void> deleteTicket(@PathVariable String barcode) {
        ticketService.deleteTicket(barcode);
        return ResponseEntity.noContent().build();
    }
}
