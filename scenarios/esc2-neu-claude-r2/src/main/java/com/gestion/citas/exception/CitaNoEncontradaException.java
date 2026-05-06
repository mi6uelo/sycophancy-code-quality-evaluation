package com.gestion.citas.exception;

/**
* Se lanza cuando se intenta acceder a una cita que no existe en la base de datos.
 */
public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}