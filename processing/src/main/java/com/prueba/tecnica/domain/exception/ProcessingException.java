package com.prueba.tecnica.domain.exception;

/**
 * Base exception for all domain-level errors in the processing service.
 */
public class ProcessingException extends RuntimeException {

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
