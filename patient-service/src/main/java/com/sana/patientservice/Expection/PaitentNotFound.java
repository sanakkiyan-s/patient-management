package com.sana.patientservice.Expection;

public class PaitentNotFound extends RuntimeException {
    public PaitentNotFound(String message) {
        super(message);
    }
}
