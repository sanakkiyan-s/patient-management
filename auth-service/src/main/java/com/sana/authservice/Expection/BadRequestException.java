package com.sana.authservice.Expection;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}