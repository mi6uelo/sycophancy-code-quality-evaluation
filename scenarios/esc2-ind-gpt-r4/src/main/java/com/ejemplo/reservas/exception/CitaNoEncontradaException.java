package com.ejemplo.reservas.exception;

public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}