package com.server.sagas.order;

import com.server.sagas.ticket.dto.PaymentDTO;

import java.util.UUID;

public class OrderModel {
    private String orderId;
    private String userId;
    private String eventId;
    private PaymentDTO paymentDTO;

    public OrderModel(String userId, String eventId, PaymentDTO payment){
        this(UUID.randomUUID().toString(), userId, eventId, payment);
    }

    public OrderModel(String orderId, String userId, String eventId, PaymentDTO payment){
        this.orderId = orderId;
        this.userId = userId;
        this.eventId = eventId;
        this.paymentDTO = payment;
    }

    public OrderModel(){}

    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEventId() {
        return eventId;
    }

    public PaymentDTO getPaymentDTO() {
        return paymentDTO;
    }
}
