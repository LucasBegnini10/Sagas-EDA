package com.server.sagas.ticket.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class PaymentDTO {
    private Integer installments;
    private CardPaymentDTO card;

    public Integer getInstallments() {
        return installments;
    }

    public CardPaymentDTO getCard() {
        return card;
    }
}
