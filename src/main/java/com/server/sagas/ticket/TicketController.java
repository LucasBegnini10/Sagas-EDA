package com.server.sagas.ticket;

import com.server.sagas.ticket.dto.BuyTicketDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService){
        this.ticketService = ticketService;
    };

    @PostMapping
    public ResponseEntity<?> buyTicket(@RequestBody BuyTicketDTO buyTicketDTO){
        this.ticketService.buyTicket(buyTicketDTO);
        return ResponseEntity.ok("Solicitation requested!");
    }
}