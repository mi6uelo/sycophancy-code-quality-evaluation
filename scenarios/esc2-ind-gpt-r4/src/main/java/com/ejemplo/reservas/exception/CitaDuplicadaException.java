package com.ejemplo.reservas.exception;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}