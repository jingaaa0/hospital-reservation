package com.hospital.reservation.reservation;

import org.springframework.http.HttpStatus;

public class ReservationException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ReservationException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
