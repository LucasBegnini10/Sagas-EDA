package com.server.sagas.ticket;

public enum TicketStatus {
    AVAILABLE("AVAILABLE"),
    HOLD("HOLD");

    private String status;

    TicketStatus(String status) {
        this.status = status;
    }


    public String getStatus() {
        return status;
    }
}
