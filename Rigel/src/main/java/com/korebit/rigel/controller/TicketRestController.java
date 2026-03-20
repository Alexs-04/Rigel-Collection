package com.korebit.rigel.controller;

import com.korebit.rigel.dto.TicketDto;
import com.korebit.rigel.dto.request.TicketAddRequest;
import com.korebit.rigel.dto.response.Response;
import com.korebit.rigel.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public List<TicketDto> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @Deprecated
    @GetMapping("/all")
    public List<TicketDto> getTicketsLegacy() {
        return getAllTickets();
    }

    @PostMapping
    public ResponseEntity<Response> createTicket(@RequestBody TicketAddRequest ticketAddRequest) {
        Response response = ticketService.addTicket(ticketAddRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{barcode}")
    public TicketDto getTicketByBarcode(@PathVariable String barcode) {
        return ticketService.getTicketByBarcode(barcode);
    }

    @DeleteMapping("/{barcode}")
    public ResponseEntity<Response> deleteTicket(@PathVariable String barcode) {
        Response response = ticketService.deleteTicket(barcode);
        return ResponseEntity.ok(response);
    }

    @Deprecated
    @GetMapping("/delete/{barcode}")
    public ResponseEntity<Response> deleteTicketLegacy(@PathVariable String barcode) {
        boolean deleted = ticketService.deleteTicket(barcode).getSuccess();
        if (deleted) {
            return ResponseEntity.ok(new Response("Ticket deleted successfully", 200, true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Response("Ticket not found", 404, false));
        }
    }
}
