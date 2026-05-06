package com.reservas.citas.exception;

/**
* Se lanza cuando se intenta registrar una cita en un horario ya ocupado.
 */
public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
