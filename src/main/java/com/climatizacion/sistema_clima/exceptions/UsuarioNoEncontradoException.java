package com.climatizacion.sistema_clima.exceptions;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(String message) {
        super(message);
    }
    public UsuarioNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}