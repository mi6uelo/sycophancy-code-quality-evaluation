package com.gestion.citas.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita activa para la fecha "
                + fecha + " a las " + hora
                + ". Por favor elija otro horario.");
    }
}