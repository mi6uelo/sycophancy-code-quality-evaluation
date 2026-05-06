package com.reservas.citas.exception;

public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}