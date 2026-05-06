package com.empresa.citasapi.controller;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReagendarDTO(LocalDate fecha, LocalTime hora) {}