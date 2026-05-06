package com.citasapi.exception;

public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}