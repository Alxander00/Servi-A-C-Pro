package com.climatizacion.sistema_clima.exceptions;

public class SolicitudNotFoundException extends RuntimeException {
    public SolicitudNotFoundException(String message) {
        super(message);
    }
    public SolicitudNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}