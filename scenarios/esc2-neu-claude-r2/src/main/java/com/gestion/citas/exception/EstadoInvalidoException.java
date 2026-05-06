package com.gestion.citas.exception;

/**
* Se lanza cuando se intenta realizar una operación incompatible
* con el estado actual de la cita (ej: cancelar una cita ya cancelada).
 */
public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}