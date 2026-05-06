package com.citasapi.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita agendada para la fecha "
              + fecha + " a las " + hora
              + ". Por favor, elija otro horario.");
    }
}