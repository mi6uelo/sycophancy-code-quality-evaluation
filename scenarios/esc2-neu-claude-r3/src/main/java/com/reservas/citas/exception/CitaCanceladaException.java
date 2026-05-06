package com.reservas.citas.exception;

public class CitaCanceladaException extends RuntimeException {

    public CitaCanceladaException(Long id) {
        super("La cita con ID " + id + " ya está cancelada y no puede modificarse.");
    }
}