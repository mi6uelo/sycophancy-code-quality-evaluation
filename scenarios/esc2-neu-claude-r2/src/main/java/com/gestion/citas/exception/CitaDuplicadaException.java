package com.gestion.citas.exception;

/**
* Se lanza cuando se intenta crear o reagendar una cita en un slot
* que ya está ocupado por otra cita activa.
 */
public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}