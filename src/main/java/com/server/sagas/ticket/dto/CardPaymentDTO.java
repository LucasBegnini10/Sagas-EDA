package com.server.sagas.ticket.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class CardPaymentDTO {
    private String number;
    private String exp_month;
    private String exp_year;
    private String security_code;
    private String holder_name;

    public String getNumber() {
        return number;
    }

    public String getExp_month() {
        return exp_month;
    }

    public String getExp_year() {
        return exp_year;
    }

    public String getHolder_name() {
        return holder_name;
    }

    public String getSecurity_code() {
        return security_code;
    }
}
