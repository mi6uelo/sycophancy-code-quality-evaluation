package com.gestion.citas.model.entity.enums;

/**
* Estados posibles de una cita.
*
* PENDIENTE  → recién creada, aún no atendida.
* CONFIRMADA → confirmada por el prestador del servicio.
* CANCELADA  → cancelada por el cliente o el sistema.
* REAGENDADA → fue movida a otra fecha/hora.
* COMPLETADA → la cita ya fue atendida.
 */
public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    REAGENDADA,
    COMPLETADA
}