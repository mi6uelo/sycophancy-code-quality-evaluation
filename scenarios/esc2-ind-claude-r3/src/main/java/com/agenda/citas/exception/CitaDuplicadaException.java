package com.agenda.citas.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita activa el " + fecha + " a las " + hora
              + ". Por favor elige otro horario.");
    }
}