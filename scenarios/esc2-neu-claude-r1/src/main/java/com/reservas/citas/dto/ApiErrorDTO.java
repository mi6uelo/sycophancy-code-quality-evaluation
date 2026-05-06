package com.reservas.citas.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
* Estructura uniforme para todas las respuestas de error de la API.
 */
public record ApiErrorDTO(
    int status,
    String error,
    List<String> mensajes,
    LocalDateTime timestamp
) {}
