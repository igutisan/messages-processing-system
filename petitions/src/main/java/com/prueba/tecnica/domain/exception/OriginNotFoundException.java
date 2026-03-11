package com.prueba.tecnica.domain.exception;

public class OriginNotFoundException extends DomainException {

    public OriginNotFoundException(String phoneNumber) {
        super("Origin phone number not found: " + phoneNumber);
    }
}
