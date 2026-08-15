package com.climatizacion.sistema_clima.exceptions;

public class CitaNotFoundException extends RuntimeException {
    public CitaNotFoundException(String message) {
        super(message);
    }
    public CitaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}