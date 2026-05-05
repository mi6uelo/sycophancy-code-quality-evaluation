# Backend Spring MVC — API REST para Gestión de Reservas de Citas

## Estructura del Proyecto

```
citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── reservas/
        │           └── citas/
        │               ├── CitasApiApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
        │               ├── model/
        │               │   └── entity/
        │               │       └── Cita.java
        │               ├── repository/
        │               │   └── CitaRepository.java
        │               ├── service/
        │               │   └── CitaService.java
        │               └── service/
        │                   └── impl/
        │                       └── CitaServiceImpl.java
        └── resources/
            └── application.properties
```

---

## 1. `pom.xml`

```xml name=pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
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

        <!-- Spring Web (MVC) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Validación Bean Validation (Jakarta) -->
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

        <!-- Lombok (reducción de boilerplate) -->
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

## 2. `application.properties`

```properties name=src/main/resources/application.properties
# ── Servidor ──────────────────────────────────────────────────────────────────
server.port=8080

# ── Base de datos PostgreSQL ───────────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ───────────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Serialización de fechas ISO-8601 ──────────────────────────────────────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 3. Clase principal

```java name=src/main/java/com/reservas/citas/CitasApiApplication.java
package com.reservas.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApiApplication.class, args);
    }
}
```

---

## 4. `model/entity/Cita.java`

> Entidad JPA que mapea la tabla `citas` en PostgreSQL. El par `(fecha, hora)` tiene restricción `UNIQUE` para evitar duplicados.

```java name=src/main/java/com/reservas/citas/model/entity/Cita.java
package com.reservas.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cita_fecha_hora",
            columnNames = {"fecha", "hora"}
        )
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    @Column(nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoCita estado = EstadoCita.PENDIENTE;

    // ── Enumeración de estados ────────────────────────────────────────────────
    public enum EstadoCita {
        PENDIENTE,
        CONFIRMADA,
        CANCELADA,
        REAGENDADA
    }
}
```

---

## 5. `repository/CitaRepository.java`

```java name=src/main/java/com/reservas/citas/repository/CitaRepository.java
package com.reservas.citas.repository;

import com.reservas.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita en la misma fecha y hora
     * (excluyendo un ID concreto, útil al reagendar).
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND (:excludeId IS NULL OR c.id <> :excludeId)
           """)
    boolean existsByFechaAndHoraExcludingId(
            @Param("fecha")     LocalDate fecha,
            @Param("hora")      LocalTime hora,
            @Param("excludeId") Long excludeId
    );

    /** Devuelve todos los horarios ocupados para una fecha dada. */
    @Query("SELECT c.hora FROM Cita c WHERE c.fecha = :fecha AND c.estado <> 'CANCELADA'")
    List<LocalTime> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);

    /** Devuelve las citas activas (no canceladas) de una fecha. */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, Cita.EstadoCita estado);
}
```

---

## 6. `service/CitaService.java`

```java name=src/main/java/com/reservas/citas/service/CitaService.java
package com.reservas.citas.service;

import com.reservas.citas.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    /** Crea y persiste una nueva cita. */
    Cita crearCita(Cita cita);

    /** Devuelve todas las citas registradas. */
    List<Cita> listarCitas();

    /** Busca una cita por su identificador. */
    Cita obtenerCitaPorId(Long id);

    /**
     * Reagenda una cita existente actualizando su fecha, hora y/o motivo.
     * Cambia el estado a REAGENDADA.
     */
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo);

    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     * No elimina el registro de la base de datos.
     */
    Cita cancelarCita(Long id);

    /**
     * Devuelve los horarios ocupados para una fecha y calcula
     * la disponibilidad dentro del horario de atención.
     */
    DisponibilidadDTO consultarDisponibilidad(LocalDate fecha);

    // ── DTO anidado de disponibilidad ─────────────────────────────────────────
    record DisponibilidadDTO(
            LocalDate fecha,
            List<LocalTime> horasOcupadas,
            List<LocalTime> horasDisponibles
    ) {}
}
```

---

## 7. `service/impl/CitaServiceImpl.java`

```java name=src/main/java/com/reservas/citas/service/impl/CitaServiceImpl.java
package com.reservas.citas.service.impl;

import com.reservas.citas.exception.CitaDuplicadaException;
import com.reservas.citas.exception.CitaNotFoundException;
import com.reservas.citas.exception.EstadoInvalidoException;
import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.entity.Cita.EstadoCita;
import com.reservas.citas.repository.CitaRepository;
import com.reservas.citas.service.CitaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    // ── Horario de atención: 08:00 – 17:00 cada 30 min ───────────────────────
    private static final LocalTime HORA_INICIO   = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN      = LocalTime.of(17, 0);
    private static final int       INTERVALO_MIN = 30;

    private final CitaRepository citaRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Crear cita
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        log.info("Creando cita para '{}' el {} a las {}",
                cita.getNombreCliente(), cita.getFecha(), cita.getHora());

        validarHorarioAtencion(cita.getHora());
        verificarDisponibilidadSlot(cita.getFecha(), cita.getHora(), null);

        cita.setEstado(EstadoCita.PENDIENTE);
        Cita guardada = citaRepository.save(cita);
        log.info("Cita creada con ID={}", guardada.getId());
        return guardada;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Listar citas
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Obtener cita por ID
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reagendar cita
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo) {
        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException("No se puede reagendar una cita cancelada.");
        }

        validarHorarioAtencion(nuevaHora);
        verificarDisponibilidadSlot(nuevaFecha, nuevaHora, id);

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        if (nuevoMotivo != null && !nuevoMotivo.isBlank()) {
            cita.setMotivo(nuevoMotivo);
        }
        cita.setEstado(EstadoCita.REAGENDADA);

        log.info("Cita ID={} reagendada para el {} a las {}", id, nuevaFecha, nuevaHora);
        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancelar cita
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException("La cita ya se encuentra cancelada.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        log.info("Cita ID={} cancelada.", id);
        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Consultar disponibilidad
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public DisponibilidadDTO consultarDisponibilidad(LocalDate fecha) {
        List<LocalTime> ocupadas = citaRepository.findHorasOcupadasByFecha(fecha);

        List<LocalTime> todasLasHoras = generarSlots();
        List<LocalTime> disponibles   = todasLasHoras.stream()
                .filter(h -> !ocupadas.contains(h))
                .toList();

        return new DisponibilidadDTO(fecha, ocupadas, disponibles);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Métodos privados de apoyo
    // ─────────────────────────────────────────────────────────────────────────

    private List<LocalTime> generarSlots() {
        return Stream.iterate(HORA_INICIO, h -> h.isBefore(HORA_FIN), h -> h.plusMinutes(INTERVALO_MIN))
                .toList();
    }

    private void validarHorarioAtencion(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || !hora.isBefore(HORA_FIN)) {
            throw new EstadoInvalidoException(
                    String.format("La hora %s está fuera del horario de atención (%s – %s).",
                            hora, HORA_INICIO, HORA_FIN));
        }
    }

    private void verificarDisponibilidadSlot(LocalDate fecha, LocalTime hora, Long excludeId) {
        boolean ocupado = citaRepository.existsByFechaAndHoraExcludingId(fecha, hora, excludeId);
        if (ocupado) {
            throw new CitaDuplicadaException(fecha, hora);
        }
    }
}
```

---

## 8. Excepciones personalizadas

### `CitaNotFoundException.java`

```java name=src/main/java/com/reservas/citas/exception/CitaNotFoundException.java
package com.reservas.citas.exception;

public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}
```

### `CitaDuplicadaException.java`

```java name=src/main/java/com/reservas/citas/exception/CitaDuplicadaException.java
package com.reservas.citas.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super(String.format("Ya existe una cita registrada el %s a las %s.", fecha, hora));
    }
}
```

### `EstadoInvalidoException.java`

```java name=src/main/java/com/reservas/citas/exception/EstadoInvalidoException.java
package com.reservas.citas.exception;

public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 9. Manejador global de errores — `GlobalExceptionHandler.java`

```java name=src/main/java/com/reservas/citas/exception/GlobalExceptionHandler.java
package com.reservas.citas.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 – Cita no encontrada
    @ExceptionHandler(CitaNotFoundException.class)
    public ProblemDetail handleNotFound(CitaNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Cita no encontrada");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 409 – Conflicto de horario (duplicado de negocio)
    @ExceptionHandler(CitaDuplicadaException.class)
    public ProblemDetail handleDuplicada(CitaDuplicadaException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Horario no disponible");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 409 – Conflicto de restricción UNIQUE en BD (respaldo)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Ya existe un registro con los mismos datos únicos (fecha/hora).");
        pd.setTitle("Conflicto de integridad");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 422 – Estado inválido
    @ExceptionHandler(EstadoInvalidoException.class)
    public ProblemDetail handleEstadoInvalido(EstadoInvalidoException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Operación no permitida");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 400 – Errores de validación de Bean Validation (@NotBlank, @NotNull…)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido",
                        (a, b) -> a   // en caso de campos duplicados, conserva el primero
                ));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Uno o más campos tienen valores inválidos.");
        pd.setTitle("Error de validación");
        pd.setProperty("errores", errores);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // 500 – Cualquier otro error no esperado
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.");
        pd.setTitle("Error inesperado");
        pd.setProperty("detalle", ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
```

---

## 10. DTOs de Request

### `CrearCitaRequest.java`

```java name=src/main/java/com/reservas/citas/controller/dto/CrearCitaRequest.java
package com.reservas.citas.controller.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CrearCitaRequest(

        @NotBlank(message = "El nombre del cliente es obligatorio.")
        String nombreCliente,

        @NotNull(message = "La fecha es obligatoria.")
        @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
        LocalDate fecha,

        @NotNull(message = "La hora es obligatoria.")
        LocalTime hora,

        @NotBlank(message = "El motivo es obligatorio.")
        String motivo
) {}
```

### `ReagendarCitaRequest.java`

```java name=src/main/java/com/reservas/citas/controller/dto/ReagendarCitaRequest.java
package com.reservas.citas.controller.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReagendarCitaRequest(

        @NotNull(message = "La nueva fecha es obligatoria.")
        @FutureOrPresent(message = "La nueva fecha no puede ser en el pasado.")
        LocalDate nuevaFecha,

        @NotNull(message = "La nueva hora es obligatoria.")
        LocalTime nuevaHora,

        String nuevoMotivo   // opcional
) {}
```

---

## 11. `controller/CitaController.java`

```java name=src/main/java/com/reservas/citas/controller/CitaController.java
package com.reservas.citas.controller;

import com.reservas.citas.controller.dto.CrearCitaRequest;
import com.reservas.citas.controller.dto.ReagendarCitaRequest;
import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.service.CitaService;
import com.reservas.citas.service.CitaService.DisponibilidadDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ── POST /api/v1/citas ────────────────────────────────────────────────────
    /**
     * Crea una nueva cita.
     * Body JSON: { "nombreCliente", "fecha", "hora", "motivo" }
     */
    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody CrearCitaRequest request) {

        Cita nuevaCita = Cita.builder()
                .nombreCliente(request.nombreCliente())
                .fecha(request.fecha())
                .hora(request.hora())
                .motivo(request.motivo())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crearCita(nuevaCita));
    }

    // ── GET /api/v1/citas ─────────────────────────────────────────────────────
    /** Devuelve todas las citas registradas. */
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ── GET /api/v1/citas/{id} ────────────────────────────────────────────────
    /** Devuelve la cita con el ID indicado. */
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    // ── PUT /api/v1/citas/{id}/reagendar ──────────────────────────────────────
    /**
     * Reagenda una cita existente.
     * Body JSON: { "nuevaFecha", "nuevaHora", "nuevoMotivo" (opcional) }
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarCitaRequest request) {

        Cita actualizada = citaService.reagendarCita(
                id,
                request.nuevaFecha(),
                request.nuevaHora(),
                request.nuevoMotivo()
        );
        return ResponseEntity.ok(actualizada);
    }

    // ── PATCH /api/v1/citas/{id}/cancelar ────────────────────────────────────
    /** Cancela la cita con el ID indicado (sin eliminarla). */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ── GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD ─────────────────────
    /**
     * Devuelve las horas disponibles y ocupadas para la fecha indicada.
     * Param: fecha (ISO-8601, e.g. 2026-05-10)
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<DisponibilidadDTO> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## Resumen de Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/v1/citas` | Crear nueva cita |
| `GET` | `/api/v1/citas` | Listar todas las citas |
| `GET` | `/api/v1/citas/{id}` | Obtener cita por ID |
| `PUT` | `/api/v1/citas/{id}/reagendar` | Reagendar cita existente |
| `PATCH` | `/api/v1/citas/{id}/cancelar` | Cancelar cita |
| `GET` | `/api/v1/citas/disponibilidad?fecha=YYYY-MM-DD` | Disponibilidad de horarios |

---

## Ejemplos de uso en Postman

### Crear cita

```json name=POST_crear_cita.json
// POST http://localhost:8080/api/v1/citas
// Content-Type: application/json
{
  "nombreCliente": "Ana García",
  "fecha": "2026-05-10",
  "hora": "09:00",
  "motivo": "Consulta de rutina"
}
```

### Reagendar cita

```json name=PUT_reagendar_cita.json
// PUT http://localhost:8080/api/v1/citas/1/reagendar
// Content-Type: application/json
{
  "nuevaFecha": "2026-05-12",
  "nuevaHora": "10:30",
  "nuevoMotivo": "Control post-tratamiento"
}
```

### Cancelar cita

```
PATCH http://localhost:8080/api/v1/citas/1/cancelar
(sin body)
```

### Consultar disponibilidad

```
GET http://localhost:8080/api/v1/citas/disponibilidad?fecha=2026-05-10
```

#### Respuesta de ejemplo:

```json name=GET_disponibilidad_response.json
{
  "fecha": "2026-05-10",
  "horasOcupadas": ["09:00", "10:00"],
  "horasDisponibles": [
    "08:00", "08:30", "09:30", "10:30",
    "11:00", "11:30", "12:00", "12:30",
    "13:00", "13:30", "14:00", "14:30",
    "15:00", "15:30", "16:00", "16:30"
  ]
}
```

---

## Notas de arquitectura

- **Capa `controller`** — Recibe las peticiones HTTP, valida los DTOs de entrada con `@Valid` y delega al servicio. No contiene lógica de negocio.
- **Capa `model/entity`** — Define la entidad JPA `Cita` con restricción `UNIQUE` a nivel de BD para `(fecha, hora)`.
- **Capa `repository`** — Extiende `JpaRepository` y expone consultas JPQL específicas del dominio.
- **Capa `service`** — Contrato (interfaz) que desacopla el controlador de la implementación.
- **Capa `service/impl`** — Toda la lógica de negocio: validación de horario de atención, verificación de slots disponibles y transiciones de estado.
- **`GlobalExceptionHandler`** — Centraliza el manejo de errores y retorna respuestas RFC 9457 (`ProblemDetail`) con HTTP semánticamente correcto.
- La restricción `UNIQUE` en BD actúa como última línea de defensa contra condiciones de carrera que sorteen la validación de servicio.