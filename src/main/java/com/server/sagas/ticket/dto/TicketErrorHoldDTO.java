package com.server.sagas.ticket.dto;

import com.server.sagas.order.OrderModel;

public class TicketErrorHoldDTO {
    private String message;
    private OrderModel order;

    public TicketErrorHoldDTO(String message, OrderModel order){
        this.message = message;
        this.order = order;
    }

    public TicketErrorHoldDTO(){}

    public String getMessage() {
        return message;
    }

    public OrderModel getOrder() {
        return order;
    }
}
