package com.server.sagas.ticket.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record BuyTicketDTO(
        String eventId,
        String userId,
        PaymentDTO payment
){}
