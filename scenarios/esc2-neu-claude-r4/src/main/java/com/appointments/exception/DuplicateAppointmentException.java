package com.appointments.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class DuplicateAppointmentException extends RuntimeException {

    public DuplicateAppointmentException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita registrada para la fecha %s a las %s."
                .formatted(fecha, hora));
    }
}