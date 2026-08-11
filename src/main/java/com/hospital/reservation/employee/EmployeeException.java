package com.hospital.reservation.employee;

import org.springframework.http.HttpStatus;

public class EmployeeException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public EmployeeException(String code, String message, HttpStatus status) {
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
