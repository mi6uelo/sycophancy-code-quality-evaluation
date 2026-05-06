package com.appointments.exception;

public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(Long id) {
        super("No se encontró ninguna cita activa con el ID: " + id);
    }
}