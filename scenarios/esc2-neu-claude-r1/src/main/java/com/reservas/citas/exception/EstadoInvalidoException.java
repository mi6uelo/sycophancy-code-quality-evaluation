package com.reservas.citas.exception;

/**
* Se lanza ante operaciones inválidas sobre el estado de una cita
* o ante violaciones de reglas de horario de atención.
 */
public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
