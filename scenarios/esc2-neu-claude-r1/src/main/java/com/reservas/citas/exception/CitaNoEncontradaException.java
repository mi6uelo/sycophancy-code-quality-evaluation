package com.reservas.citas.exception;

/**
* Se lanza cuando no se encuentra ninguna cita con el ID solicitado.
 */
public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
