# markdown name=citas-api.md

# Backend Spring MVC — API REST de Gestión de Reservas de Citas

```bash

# ---

```

## Tabla de Contenido

## 1. [Estructura del Proyecto](#1-estructura-del-proyecto)

## 2. [pom.xml](#2-pomxml)

## 3. [application.properties](#3-applicationproperties)

## 4. [EstadoCita (Enum)](#4-estadocita-enum)

## 5. [Entidad Cita](#5-entidad-cita)

## 6. [DTOs](#6-dtos)

## 7. [Repository](#7-repository)

## 8. [Service — Interfaz](#8-service--interfaz)

## 9. [Service — Implementación](#9-service--implementación)

## 10. [Excepciones de Dominio](#10-excepciones-de-dominio)

## 11. [Manejo Global de Errores](#11-manejo-global-de-errores)

## 12. [Controller](#12-controller)

## 13. [Clase Principal](#13-clase-principal)

## 14. [Pruebas con Postman](#14-pruebas-con-postman)

## 15. [Resumen de Endpoints](#15-resumen-de-endpoints)

---

## 1. Estructura del Proyecto

```text
citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── reservas/
        │           └── citas/
        │               ├── CitasApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
        │               ├── dto/
        │               │   ├── ApiErrorDTO.java
        │               │   ├── CitaRequestDTO.java
        │               │   ├── CitaResponseDTO.java
        │               │   └── DisponibilidadResponseDTO.java
        │               ├── exception/
        │               │   ├── CitaDuplicadaException.java
        │               │   ├── CitaNoEncontradaException.java
        │               │   ├── EstadoInvalidoException.java
        │               │   └── GlobalExceptionHandler.java
        │               ├── model/
        │               │   └── entity/
        │               │       ├── Cita.java
        │               │       └── EstadoCita.java
        │               ├── repository/
        │               │   └── CitaRepository.java
        │               └── service/
        │                   ├── CitaService.java
        │                   └── impl/
        │                       └── CitaServiceImpl.java
        └── resources/
            └── application.properties
```

---

## 2. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.reservas</groupId>
    <artifactId>citas-api</artifactId>
    <version>1.0.0</version>
    <name>citas-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring Web (MVC + REST) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Validation (Bean Validation / Hibernate Validator) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Driver PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Tests -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

---

## 3. application.properties

# ── Servidor ──────────────────────────────────────────────────────────────────

```properties
server.port=8080

# ── Base de datos PostgreSQL ───────────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ────────────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Serialización de fechas (ISO-8601) ─────────────────────────────────────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 4. EstadoCita (Enum)

```java
package com.reservas.citas.model.entity;

```

/**
* Estados posibles de una cita.

```text
 */
public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    REAGENDADA
}
```

---

## 5. Entidad Cita

```java
package com.reservas.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* Entidad JPA que representa una cita agendada.
* La combinación (fecha + hora) debe ser única para evitar duplicados.

```text
 */
@Entity
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cita_fecha_hora",
            columnNames = {"fecha", "hora"}
        )
    }
```

## )

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres.")
    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy.")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCita estado = EstadoCita.PENDIENTE;
}
```

---

## 6. DTOs

### CitaRequestDTO

```java
package com.reservas.citas.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* DTO de entrada para crear o reagendar una cita.

```text
 */
public record CitaRequestDTO(

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 120)
    String nombreCliente,

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    LocalDate fecha,

    @NotNull(message = "La hora es obligatoria.")
    LocalTime hora,

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 255)
    String motivo
```

## ) {}

### CitaResponseDTO

```java
package com.reservas.citas.dto;

import com.reservas.citas.model.entity.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* DTO de salida que expone los datos de una cita al cliente HTTP.

```text
 */
public record CitaResponseDTO(
    Long id,
    String nombreCliente,
    LocalDate fecha,
    LocalTime hora,
    String motivo,
    EstadoCita estado
```

## ) {}

### DisponibilidadResponseDTO

```java
package com.reservas.citas.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

```

/**
* DTO que informa los horarios ocupados y libres para una fecha dada.

```text
 */
public record DisponibilidadResponseDTO(
    LocalDate fecha,
    List<LocalTime> horasOcupadas,
    List<LocalTime> horasDisponibles
```

## ) {}

### ApiErrorDTO

```java
package com.reservas.citas.dto;

import java.time.LocalDateTime;
import java.util.List;

```

/**
* Estructura uniforme para todas las respuestas de error de la API.

```text
 */
public record ApiErrorDTO(
    int status,
    String error,
    List<String> mensajes,
    LocalDateTime timestamp
```

## ) {}

---

## 7. Repository

```java
package com.reservas.citas.repository;

import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

```

/**
* Repositorio JPA para la entidad Cita.

```text
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
        LocalDate fecha,
        LocalTime hora,
        EstadoCita estado
    );

    /**
     * Devuelve todas las citas de una fecha específica que no estén canceladas.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);

    /**
     * Busca una cita por fecha y hora (para validar duplicados al reagendar).
     */
    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Lista todas las citas cuyo estado sea el indicado.
     */
    List<Cita> findByEstado(EstadoCita estado);
}
```

---

## 8. Service — Interfaz

```java
package com.reservas.citas.service;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.dto.DisponibilidadResponseDTO;

import java.time.LocalDate;
import java.util.List;

```

/**
* Contrato de negocio para la gestión de citas.

```text
 */
public interface CitaService {

    /**
     * Registra una nueva cita validando que el horario esté libre.
     */
    CitaResponseDTO crearCita(CitaRequestDTO dto);

    /**
     * Devuelve todas las citas registradas en el sistema.
     */
    List<CitaResponseDTO> listarCitas();

    /**
     * Obtiene el detalle de una cita por su identificador.
     */
    CitaResponseDTO obtenerCitaPorId(Long id);

    /**
     * Modifica la fecha y/o la hora de una cita existente.
     */
    CitaResponseDTO reagendarCita(Long id, CitaRequestDTO dto);

    /**
     * Marca una cita como CANCELADA.
     */
    CitaResponseDTO cancelarCita(Long id);

    /**
     * Calcula los horarios disponibles y ocupados para una fecha concreta.
     * Horario de atención: 08:00 a 17:00 en bloques de 30 minutos.
     */
    DisponibilidadResponseDTO consultarDisponibilidad(LocalDate fecha);
}
```

---

## 9. Service — Implementación

```java
package com.reservas.citas.service.impl;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.dto.DisponibilidadResponseDTO;
import com.reservas.citas.exception.CitaDuplicadaException;
import com.reservas.citas.exception.CitaNoEncontradaException;
import com.reservas.citas.exception.EstadoInvalidoException;
import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.entity.EstadoCita;
import com.reservas.citas.repository.CitaRepository;
import com.reservas.citas.service.CitaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

```

/**
* Implementación de CitaService con lógica de negocio y validaciones.

```text
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CitaServiceImpl implements CitaService {

    /* ── Horario de atención ──────────────────────────────────────────────── */
    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN    = LocalTime.of(17, 0);
    private static final int       BLOQUE_MINS = 30;

    private final CitaRepository citaRepository;

    /* ── Crear ────────────���───────────────────────────────────────────────── */

    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO dto) {
        log.info("Creando cita para '{}' el {} a las {}", dto.nombreCliente(), dto.fecha(), dto.hora());

        validarHorarioLaboral(dto.hora());
        validarDisponibilidad(dto.fecha(), dto.hora(), null);

        Cita cita = Cita.builder()
            .nombreCliente(dto.nombreCliente())
            .fecha(dto.fecha())
            .hora(dto.hora())
            .motivo(dto.motivo())
            .estado(EstadoCita.PENDIENTE)
            .build();

        return toDTO(citaRepository.save(cita));
    }

    /* ── Listar ───────────────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        log.info("Listando todas las citas");
        return citaRepository.findAll()
            .stream()
            .map(this::toDTO)
            .toList();
    }

    /* ── Obtener por ID ───────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        log.info("Consultando cita con id={}", id);
        return toDTO(buscarOLanzarError(id));
    }

    /* ── Reagendar ────────────────────────────────────────────────────────── */

    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, CitaRequestDTO dto) {
        log.info("Reagendando cita id={} a {} {}", id, dto.fecha(), dto.hora());

        Cita cita = buscarOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                "No es posible reagendar una cita que ya fue cancelada. ID: " + id
            );
        }

        validarHorarioLaboral(dto.hora());

        boolean mismoHorario = cita.getFecha().equals(dto.fecha())
                            && cita.getHora().equals(dto.hora());
        if (!mismoHorario) {
            validarDisponibilidad(dto.fecha(), dto.hora(), id);
        }

        cita.setNombreCliente(dto.nombreCliente());
        cita.setFecha(dto.fecha());
        cita.setHora(dto.hora());
        cita.setMotivo(dto.motivo());
        cita.setEstado(EstadoCita.REAGENDADA);

        return toDTO(citaRepository.save(cita));
    }

    /* ── Cancelar ─────────────────────────────────────────────────────────── */

    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {
        log.info("Cancelando cita id={}", id);

        Cita cita = buscarOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                "La cita con ID " + id + " ya se encuentra cancelada."
            );
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    /* ── Disponibilidad ───────────────────────────────────────────────────── */

    @Override
    @Transactional(readOnly = true)
    public DisponibilidadResponseDTO consultarDisponibilidad(LocalDate fecha) {
        log.info("Consultando disponibilidad para {}", fecha);

        List<LocalTime> ocupadas = citaRepository
            .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
            .stream()
            .map(Cita::getHora)
            .toList();

        List<LocalTime> disponibles = new ArrayList<>();
        LocalTime cursor = HORA_INICIO;

        while (cursor.isBefore(HORA_FIN)) {
            if (!ocupadas.contains(cursor)) {
                disponibles.add(cursor);
            }
            cursor = cursor.plusMinutes(BLOQUE_MINS);
        }

        return new DisponibilidadResponseDTO(fecha, ocupadas, disponibles);
    }

    /* ── Helpers privados ─────────────────────────────────────────────────── */

    /**
     * Busca una cita por ID o lanza CitaNoEncontradaException.
     */
    private Cita buscarOLanzarError(Long id) {
        return citaRepository.findById(id)
            .orElseThrow(() ->
                new CitaNoEncontradaException("No existe ninguna cita con ID: " + id)
            );
    }

    /**
     * Verifica que la hora solicitada esté dentro del horario de atención
     * y que coincida con un bloque válido de BLOQUE_MINS minutos.
     */
    private void validarHorarioLaboral(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || !hora.isBefore(HORA_FIN)) {
            throw new EstadoInvalidoException(
                "La hora debe estar dentro del horario de atención: "
                + HORA_INICIO + " – " + HORA_FIN + "."
            );
        }
        if (hora.getMinute() % BLOQUE_MINS != 0) {
            throw new EstadoInvalidoException(
                "Las citas deben agendarse en bloques de " + BLOQUE_MINS
                + " minutos (ej. 08:00, 08:30, 09:00)."
            );
        }
    }

    /**
     * Valida que no exista otra cita activa en la misma fecha/hora.
     * El parámetro idExcluir permite ignorar la propia cita al reagendar.
     */
    private void validarDisponibilidad(LocalDate fecha, LocalTime hora, Long idExcluir) {
        citaRepository.findByFechaAndHora(fecha, hora).ifPresent(existente -> {
            boolean esMisma   = existente.getId().equals(idExcluir);
            boolean cancelada = existente.getEstado() == EstadoCita.CANCELADA;

            if (!esMisma && !cancelada) {
                throw new CitaDuplicadaException(
                    "Ya existe una cita activa el " + fecha + " a las " + hora + "."
                );
            }
        });
    }

    /**
     * Convierte una entidad Cita en su DTO de respuesta.
     */
    private CitaResponseDTO toDTO(Cita cita) {
        return new CitaResponseDTO(
            cita.getId(),
            cita.getNombreCliente(),
            cita.getFecha(),
            cita.getHora(),
            cita.getMotivo(),
            cita.getEstado()
        );
    }
}
```

---

## 10. Excepciones de Dominio

```java
package com.reservas.citas.exception;

```

/**
* Se lanza cuando no se encuentra ninguna cita con el ID solicitado.

```text
 */
public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
```

```java
package com.reservas.citas.exception;

```

/**
* Se lanza cuando se intenta registrar una cita en un horario ya ocupado.

```text
 */
public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
```

```java
package com.reservas.citas.exception;

```

/**
* Se lanza ante operaciones inválidas sobre el estado de una cita
* o ante violaciones de reglas de horario de atención.

```text
 */
public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 11. Manejo Global de Errores

```java
package com.reservas.citas.exception;

import com.reservas.citas.dto.ApiErrorDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

```

/**
* Captura todas las excepciones de la API y devuelve respuestas HTTP uniformes.

```text
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* ── 404 Not Found ────────────────────────────────────────────────────── */

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(CitaNoEncontradaException ex) {
        log.warn("Cita no encontrada: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(ex.getMessage()));
    }

    /* ── 409 Conflict ─────────────────────────────────────────────────────── */

    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<ApiErrorDTO> handleDuplicate(CitaDuplicadaException ex) {
        log.warn("Cita duplicada: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflicto de horario", List.of(ex.getMessage()));
    }

    /* ── 422 Unprocessable Entity ─────────────────────────────────────────── */

    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<ApiErrorDTO> handleEstado(EstadoInvalidoException ex) {
        log.warn("Operación inválida: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Operación no permitida", List.of(ex.getMessage()));
    }

    /* ── 400 Bean Validation (@RequestBody) ───────────────────────────────── */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
        log.warn("Validación fallida: {}", errores);
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", errores);
    }

    /* ── 400 Constraint Violation (path/query params) ─────────────────────── */

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleConstraint(ConstraintViolationException ex) {
        List<String> errores = ex.getConstraintViolations()
            .stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .toList();
        return build(HttpStatus.BAD_REQUEST, "Parámetro inválido", errores);
    }

    /* ── 409 Integridad de BD (unique constraint a nivel SQL) ─────────────── */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleIntegrity(DataIntegrityViolationException ex) {
        log.error("Violación de integridad en BD: {}", ex.getMostSpecificCause().getMessage());
        return build(
            HttpStatus.CONFLICT,
            "Violación de integridad de datos",
            List.of("Ya existe un registro con los mismos datos únicos (fecha/hora).")
        );
    }

    /* ── 500 Fallback ─────────────────────────────────────────────────────── */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGeneric(Exception ex) {
        log.error("Error inesperado: ", ex);
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor",
            List.of("Ocurrió un error inesperado. Contacta al administrador.")
        );
    }

    /* ── Builder ──────────────────────────────────────────────────────────── */

    private ResponseEntity<ApiErrorDTO> build(HttpStatus status, String error, List<String> mensajes) {
        return ResponseEntity
            .status(status)
            .body(new ApiErrorDTO(status.value(), error, mensajes, LocalDateTime.now()));
    }
}
```

---

## 12. Controller

```java
package com.reservas.citas.controller;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.dto.DisponibilidadResponseDTO;
import com.reservas.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

```

/**
* Controlador REST para la gestión de citas.
* Ruta base: /api/v1/citas

```text
 */
@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    /**
     * POST /api/v1/citas
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO dto) {
        CitaResponseDTO respuesta = citaService.crearCita(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * GET /api/v1/citas
     * Lista todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/v1/citas/{id}
     * Consulta una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/v1/citas/{id}/reagendar
     * Reagenda una cita existente con nuevos datos de fecha/hora.
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendar(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequestDTO dto) {
        return ResponseEntity.ok(citaService.reagendarCita(id, dto));
    }

    /**
     * PATCH /api/v1/citas/{id}/cancelar
     * Cancela una cita marcándola como CANCELADA.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD
     * Consulta los horarios disponibles y ocupados para una fecha.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadResponseDTO> disponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 13. Clase Principal

```java
package com.reservas.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

```

/**
* Punto de entrada de la aplicación Spring Boot — API de Gestión de Citas.

```text
 */
@SpringBootApplication
public class CitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}
```

---

## 14. Pruebas con Postman

> **Base URL:** `http://localhost:8080/api/v1/citas`

### POST / — Crear una cita

## // Body → raw → JSON

```json
{
    "nombreCliente": "María López",
    "fecha": "2026-06-15",
    "hora": "09:00",
    "motivo": "Consulta general"
}
```

## Respuesta esperada 201 Created:

```json
{
    "id": 1,
    "nombreCliente": "María López",
    "fecha": "2026-06-15",
    "hora": "09:00:00",
    "motivo": "Consulta general",
    "estado": "PENDIENTE"
}
```

### GET / — Listar todas las citas

GET http://localhost:8080/api/v1/citas

## Respuesta esperada 200 OK:

```json
[
    {
        "id": 1,
        "nombreCliente": "María López",
        "fecha": "2026-06-15",
        "hora": "09:00:00",
        "motivo": "Consulta general",
        "estado": "PENDIENTE"
    }
]
```

### GET /{id} — Consultar por ID

GET http://localhost:8080/api/v1/citas/1

## Respuesta esperada 200 OK:

```json
{
    "id": 1,
    "nombreCliente": "María López",
    "fecha": "2026-06-15",
    "hora": "09:00:00",
    "motivo": "Consulta general",
    "estado": "PENDIENTE"
}
```

### PUT /{id}/reagendar — Reagendar una cita

// PUT http://localhost:8080/api/v1/citas/1/reagendar

## // Body → raw → JSON

```json
{
    "nombreCliente": "María López",
    "fecha": "2026-06-20",
    "hora": "10:30",
    "motivo": "Consulta general — reagendada"
}
```

## Respuesta esperada 200 OK:

```json
{
    "id": 1,
    "nombreCliente": "María López",
    "fecha": "2026-06-20",
    "hora": "10:30:00",
    "motivo": "Consulta general — reagendada",
    "estado": "REAGENDADA"
}
```

### PATCH /{id}/cancelar — Cancelar una cita

PATCH http://localhost:8080/api/v1/citas/1/cancelar

## Respuesta esperada 200 OK:

```json
{
    "id": 1,
    "nombreCliente": "María López",
    "fecha": "2026-06-20",
    "hora": "10:30:00",
    "motivo": "Consulta general — reagendada",
    "estado": "CANCELADA"
}
```

### GET /disponibilidad?fecha= — Consultar disponibilidad

GET http://localhost:8080/api/v1/citas/disponibilidad?fecha=2026-06-15

## Respuesta esperada 200 OK:

```json
{
    "fecha": "2026-06-15",
    "horasOcupadas": ["09:00:00"],
    "horasDisponibles": [
        "08:00:00", "08:30:00", "09:30:00", "10:00:00",
        "10:30:00", "11:00:00", "11:30:00", "12:00:00",
        "12:30:00", "13:00:00", "13:30:00", "14:00:00",
        "14:30:00", "15:00:00", "15:30:00", "16:00:00",
        "16:30:00"
    ]
}
```

### Error — Horario duplicado 409 Conflict

```json
{
    "status": 409,
    "error": "Conflicto de horario",
    "mensajes": ["Ya existe una cita activa el 2026-06-15 a las 09:00."],
    "timestamp": "2026-05-03T14:22:10.123"
}
```

### Error — Cita no encontrada 404 Not Found

```json
{
    "status": 404,
    "error": "Recurso no encontrado",
    "mensajes": ["No existe ninguna cita con ID: 99"],
    "timestamp": "2026-05-03T14:25:00.456"
}
```

### Error — Validación de campos 400 Bad Request

```json
{
    "status": 400,
    "error": "Datos de entrada inválidos",
    "mensajes": [
        "nombreCliente: El nombre del cliente es obligatorio.",
        "fecha: La fecha no puede ser en el pasado."
    ],
    "timestamp": "2026-05-03T14:30:00.789"
}
```

---

## 15. Resumen de Endpoints

| Método  | Ruta                              | Descripción                          | HTTP éxito |
|---------|-----------------------------------|--------------------------------------|:----------:|
| POST    | /api/v1/citas                     | Crear una nueva cita                 | 201        |
| GET     | /api/v1/citas                     | Listar todas las citas               | 200        |
| GET     | /api/v1/citas/{id}                | Consultar cita por ID                | 200        |
| PUT     | /api/v1/citas/{id}/reagendar      | Reagendar cita existente             | 200        |
| PATCH   | /api/v1/citas/{id}/cancelar       | Cancelar cita                        | 200        |
| GET     | /api/v1/citas/disponibilidad      | Consultar disponibilidad por fecha   | 200        |

---

## Notas de Arquitectura

- **model/entity** — Entidad JPA con restricción `UNIQUE` a nivel de base de datos como segunda línea de defensa contra duplicados, complementando la validación en la capa de servicio.
- **repository** — Solo consultas derivadas de Spring Data JPA; sin SQL nativo para mayor portabilidad entre motores.
- **service** — Interfaz pura de contrato desacoplada completamente de la implementación concreta.
- **service/impl** — Toda la lógica de negocio centralizada: validación de horario laboral, bloques de 30 minutos, detección de duplicados antes de persistir y transiciones de estado.
- **controller** — Exclusivamente orquesta HTTP → Service → HTTP; sin lógica de negocio.
- **exception** — Jerarquía de excepciones propias de dominio más un `@RestControllerAdvice` global que garantiza respuestas de error uniformes en formato `ApiErrorDTO` para todos los endpoints.

````
```
