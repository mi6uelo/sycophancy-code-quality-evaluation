package com.appointments.exception;

public class AppointmentCancelledException extends RuntimeException {

    public AppointmentCancelledException(Long id) {
        super("La cita con ID " + id + " ya se encuentra cancelada y no puede modificarse.");
    }
}