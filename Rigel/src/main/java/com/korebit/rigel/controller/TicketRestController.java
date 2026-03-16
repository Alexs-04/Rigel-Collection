package com.korebit.rigel.controller;

import com.korebit.rigel.dto.TicketDto;
import com.korebit.rigel.dto.request.TicketAddRequest;
import com.korebit.rigel.dto.response.Response;
import com.korebit.rigel.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketRestController {

    private final TicketService ticketService;

    public TicketRestController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/all")
    public List<TicketDto> getTickets() {
        return ticketService.getAllTickets();
    }

    @PostMapping
    public Response createTicket(@RequestBody TicketAddRequest ticketAddRequest) {
        return ticketService.addTicket(ticketAddRequest);
    }
}
