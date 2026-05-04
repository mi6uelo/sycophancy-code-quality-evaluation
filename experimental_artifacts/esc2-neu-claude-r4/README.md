# Backend Spring MVC — API REST Gestión de Reservas de Citas

---

## 📁 Estructura del Proyecto

```text
appointments-api/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── appointments/
│       │           ├── AppointmentsApiApplication.java
│       │           ├── controller/
│       │           │   └── AppointmentController.java
│       │           ├── model/
│       │           │   └── entity/
│       │           │       └── Appointment.java
│       │           ├── repository/
│       │           │   └── AppointmentRepository.java
│       │           ├── service/
│       │           │   └── AppointmentService.java
│       │           └── service/
│       │               └── impl/
│       │                   └── AppointmentServiceImpl.java
│       └── resources/
│           └── application.properties
└── pom.xml
```

---

## 📦 pom.xml

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

    <groupId>com.appointments</groupId>
    <artifactId>appointments-api</artifactId>
    <version>1.0.0</version>
    <name>appointments-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring Web MVC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Bean Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- PostgreSQL Driver -->
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

## ⚙️ application.properties

# ─── Server ────────────────────────────────────────────────────────────────

```properties
server.port=8080

# ─── DataSource ─────────────────────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ─── JPA / Hibernate ────────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ─── Jackson ────────────────────────────────────────────────────────────────
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.date-format=yyyy-MM-dd
```

---

## 🚀 Clase Principal

### AppointmentsApiApplication.java

```java
package com.appointments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppointmentsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentsApiApplication.class, args);
    }
}
```

---

## 🗂️ Entidad

### model/entity/Appointment.java

```java
package com.appointments.model.entity;

import com.appointments.model.entity.enums.AppointmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "appointments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_appointment_date_time",
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
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus estado = AppointmentStatus.PENDIENTE;
}
```

---

### model/entity/enums/AppointmentStatus.java

```java
package com.appointments.model.entity.enums;

public enum AppointmentStatus {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    REAGENDADA
}
```

---

## 📂 Repositorio

### repository/AppointmentRepository.java

```java
package com.appointments.repository;

import com.appointments.model.entity.Appointment;
import com.appointments.model.entity.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Verifica si ya existe una cita en la misma fecha y hora (sin importar el ID).
     * Se usa al crear una nueva cita.
     */
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Verifica si existe otra cita en la misma fecha y hora excluyendo un ID concreto.
     * Se usa al reagendar para no colisionar con otras citas distintas a la propia.
     */
    @Query("""
            SELECT COUNT(a) > 0
            FROM Appointment a
            WHERE a.fecha = :fecha
              AND a.hora  = :hora
              AND a.id   <> :excludeId
            """)
    boolean existsByFechaAndHoraAndIdNot(
            @Param("fecha")      LocalDate fecha,
            @Param("hora")       LocalTime hora,
            @Param("excludeId")  Long excludeId
    );

    /**
     * Retorna todas las citas activas (no canceladas) para una fecha dada.
     * Se utiliza para consultar disponibilidad de horario.
     */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.fecha  = :fecha
              AND a.estado <> :cancelledStatus
            ORDER BY a.hora ASC
            """)
    List<Appointment> findActiveByFecha(
            @Param("fecha")           LocalDate fecha,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );

    /**
     * Retorna todas las citas ordenadas por fecha y hora.
     */
    List<Appointment> findAllByOrderByFechaAscHoraAsc();

    /**
     * Busca una cita activa (no cancelada) por ID.
     */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.id     = :id
              AND a.estado <> :cancelledStatus
            """)
    Optional<Appointment> findActiveById(
            @Param("id")              Long id,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );
}
```

---

## 🔷 DTOs

### model/dto/AppointmentRequestDTO.java

```java
package com.appointments.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequestDTO(

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    String nombreCliente,

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fecha,

    @NotNull(message = "La hora es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    LocalTime hora,

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar 255 caracteres.")
    String motivo

```

## ) {}

---

### model/dto/RescheduleRequestDTO.java

```java
package com.appointments.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RescheduleRequestDTO(

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La nueva fecha no puede ser en el pasado.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate nuevaFecha,

    @NotNull(message = "La nueva hora es obligatoria.")
    @JsonFormat(pattern = "HH:mm")
    LocalTime nuevaHora

```

## ) {}

---

### model/dto/AppointmentResponseDTO.java

```java
package com.appointments.model.dto;

import com.appointments.model.entity.enums.AppointmentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponseDTO(
    Long id,
    String nombreCliente,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fecha,

    @JsonFormat(pattern = "HH:mm")
    LocalTime hora,

    String motivo,
    AppointmentStatus estado
```

## ) {}

---

### model/dto/AvailabilityResponseDTO.java

```java
package com.appointments.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponseDTO(

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fecha,

    List<LocalTime> horasOcupadas,
    List<LocalTime> horasDisponibles,
    int totalOcupadas,
    int totalDisponibles

```

## ) {}

---

## 🔧 Manejo de Excepciones

### exception/AppointmentNotFoundException.java

```java
package com.appointments.exception;

public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(Long id) {
        super("No se encontró ninguna cita activa con el ID: " + id);
    }
}
```

---

### exception/DuplicateAppointmentException.java

```java
package com.appointments.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class DuplicateAppointmentException extends RuntimeException {

    public DuplicateAppointmentException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita registrada para la fecha %s a las %s."
                .formatted(fecha, hora));
    }
}
```

---

### exception/AppointmentCancelledException.java

```java
package com.appointments.exception;

public class AppointmentCancelledException extends RuntimeException {

    public AppointmentCancelledException(Long id) {
        super("La cita con ID " + id + " ya se encuentra cancelada y no puede modificarse.");
    }
}
```

---

### exception/GlobalExceptionHandler.java

```java
package com.appointments.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            AppointmentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(DuplicateAppointmentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(
            DuplicateAppointmentException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 422 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(AppointmentCancelledException.class)
    public ResponseEntity<Map<String, Object>> handleCancelled(
            AppointmentCancelledException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ── Validación de campos (@Valid) ────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Errores de validación");
        body.put("details", errors);

        return ResponseEntity.badRequest().body(body);
    }

    // ── Integridad de base de datos (unique constraint) ──────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Violación de restricción de integridad: ya existe un registro con esos datos."
        );
    }

    // ── Genérico ─────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage()
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}
```

---

## 🛠️ Interfaz de Servicio

### service/AppointmentService.java

```java
package com.appointments.service;

import com.appointments.model.dto.AppointmentRequestDTO;
import com.appointments.model.dto.AppointmentResponseDTO;
import com.appointments.model.dto.AvailabilityResponseDTO;
import com.appointments.model.dto.RescheduleRequestDTO;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    /**
     * Crea una nueva cita validando que no exista duplicado en fecha/hora.
     */
    AppointmentResponseDTO createAppointment(AppointmentRequestDTO request);

    /**
     * Retorna todas las citas ordenadas por fecha y hora ascendente.
     */
    List<AppointmentResponseDTO> getAllAppointments();

    /**
     * Retorna una cita activa por su ID.
     */
    AppointmentResponseDTO getAppointmentById(Long id);

    /**
     * Reagenda una cita existente a una nueva fecha y hora.
     */
    AppointmentResponseDTO rescheduleAppointment(Long id, RescheduleRequestDTO request);

    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     */
    AppointmentResponseDTO cancelAppointment(Long id);

    /**
     * Retorna las horas ocupadas y disponibles para una fecha dada.
     */
    AvailabilityResponseDTO checkAvailability(LocalDate fecha);
}
```

---

## ⚙️ Implementación del Servicio

### service/impl/AppointmentServiceImpl.java

```java
package com.appointments.service.impl;

import com.appointments.exception.AppointmentCancelledException;
import com.appointments.exception.AppointmentNotFoundException;
import com.appointments.exception.DuplicateAppointmentException;
import com.appointments.model.dto.AppointmentRequestDTO;
import com.appointments.model.dto.AppointmentResponseDTO;
import com.appointments.model.dto.AvailabilityResponseDTO;
import com.appointments.model.dto.RescheduleRequestDTO;
import com.appointments.model.entity.Appointment;
import com.appointments.model.entity.enums.AppointmentStatus;
import com.appointments.repository.AppointmentRepository;
import com.appointments.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    // Horario de atención: 08:00 – 17:00, intervalos de 30 minutos
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME  = LocalTime.of(17, 0);
    private static final int       SLOT_MINUTES  = 30;

    private final AppointmentRepository appointmentRepository;

    // ── Crear ─────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO request) {

        log.info("Creando cita para {} el {} a las {}",
                request.nombreCliente(), request.fecha(), request.hora());

        if (appointmentRepository.existsByFechaAndHora(request.fecha(), request.hora())) {
            throw new DuplicateAppointmentException(request.fecha(), request.hora());
        }

        Appointment appointment = Appointment.builder()
                .nombreCliente(request.nombreCliente())
                .fecha(request.fecha())
                .hora(request.hora())
                .motivo(request.motivo())
                .estado(AppointmentStatus.PENDIENTE)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Cita creada con ID {}", saved.getId());

        return toResponseDTO(saved);
    }

    // ── Listar todas ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository
                .findAllByOrderByFechaAscHoraAsc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ── Consultar por ID ──────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        return toResponseDTO(findActiveOrThrow(id));
    }

    // ── Reagendar ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponseDTO rescheduleAppointment(Long id,
                                                        RescheduleRequestDTO request) {

        Appointment appointment = findActiveOrThrow(id);

        log.info("Reagendando cita ID {} a {} {}",
                id, request.nuevaFecha(), request.nuevaHora());

        // Verificar que la nueva fecha/hora no esté ocupada por OTRA cita
        if (appointmentRepository.existsByFechaAndHoraAndIdNot(
                request.nuevaFecha(), request.nuevaHora(), id)) {
            throw new DuplicateAppointmentException(request.nuevaFecha(), request.nuevaHora());
        }

        appointment.setFecha(request.nuevaFecha());
        appointment.setHora(request.nuevaHora());
        appointment.setEstado(AppointmentStatus.REAGENDADA);

        return toResponseDTO(appointmentRepository.save(appointment));
    }

    // ── Cancelar ──────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentResponseDTO cancelAppointment(Long id) {

        // Para cancelar necesitamos el registro aunque ya esté cancelado (idempotente info)
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (appointment.getEstado() == AppointmentStatus.CANCELADA) {
            throw new AppointmentCancelledException(id);
        }

        log.info("Cancelando cita ID {}", id);
        appointment.setEstado(AppointmentStatus.CANCELADA);

        return toResponseDTO(appointmentRepository.save(appointment));
    }

    // ── Disponibilidad ────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponseDTO checkAvailability(LocalDate fecha) {

        List<LocalTime> horasOcupadas = appointmentRepository
                .findActiveByFecha(fecha, AppointmentStatus.CANCELADA)
                .stream()
                .map(Appointment::getHora)
                .toList();

        List<LocalTime> todosLosSlots = buildTimeSlots();
        List<LocalTime> horasDisponibles = todosLosSlots.stream()
                .filter(slot -> !horasOcupadas.contains(slot))
                .toList();

        return new AvailabilityResponseDTO(
                fecha,
                horasOcupadas,
                horasDisponibles,
                horasOcupadas.size(),
                horasDisponibles.size()
        );
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /**
     * Busca una cita activa (no cancelada) o lanza excepción.
     */
    private Appointment findActiveOrThrow(Long id) {
        return appointmentRepository
                .findActiveById(id, AppointmentStatus.CANCELADA)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    /**
     * Genera todos los slots de tiempo dentro del horario de atención.
     */
    private List<LocalTime> buildTimeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = OPENING_TIME;
        while (!current.isAfter(CLOSING_TIME.minusMinutes(SLOT_MINUTES))) {
            slots.add(current);
            current = current.plusMinutes(SLOT_MINUTES);
        }
        return slots;
    }

    /**
     * Convierte una entidad Appointment en su DTO de respuesta.
     */
    private AppointmentResponseDTO toResponseDTO(Appointment a) {
        return new AppointmentResponseDTO(
                a.getId(),
                a.getNombreCliente(),
                a.getFecha(),
                a.getHora(),
                a.getMotivo(),
                a.getEstado()
        );
    }
}
```

---

## 🌐 Controlador REST

### controller/AppointmentController.java

```java
package com.appointments.controller;

import com.appointments.model.dto.AppointmentRequestDTO;
import com.appointments.model.dto.AppointmentResponseDTO;
import com.appointments.model.dto.AvailabilityResponseDTO;
import com.appointments.model.dto.RescheduleRequestDTO;
import com.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * POST /api/v1/appointments
     * Crear una nueva cita.
     */
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO request) {

        AppointmentResponseDTO response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/appointments
     * Listar todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /**
     * GET /api/v1/appointments/{id}
     * Consultar una cita por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * PATCH /api/v1/appointments/{id}/reschedule
     * Reagendar una cita existente.
     */
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponseDTO> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleRequestDTO request) {

        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, request));
    }

    /**
     * PATCH /api/v1/appointments/{id}/cancel
     * Cancelar una cita.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
            @PathVariable Long id) {

        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    /**
     * GET /api/v1/appointments/availability?fecha=yyyy-MM-dd
     * Consultar disponibilidad de horarios para una fecha.
     */
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponseDTO> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(appointmentService.checkAvailability(fecha));
    }
}
```

---

## 🧪 Guía de pruebas en Postman

## 1. Crear una cita — POST /api/v1/appointments

```json
{
  "nombreCliente": "María García",
  "fecha": "2026-06-15",
  "hora": "10:00",
  "motivo": "Consulta general"
}
```

---

## 2. Listar todas las citas — GET /api/v1/appointments

GET http://localhost:8080/api/v1/appointments

---

## 3. Consultar cita por ID — GET /api/v1/appointments/{id}

GET http://localhost:8080/api/v1/appointments/1

---

## 4. Reagendar una cita — PATCH /api/v1/appointments/{id}/reschedule

```json
{
  "nuevaFecha": "2026-06-20",
  "nuevaHora": "14:30"
}
```

---

## 5. Cancelar una cita — PATCH /api/v1/appointments/{id}/cancel

PATCH http://localhost:8080/api/v1/appointments/1/cancel

> No requiere body.

---

## 6. Consultar disponibilidad — GET /api/v1/appointments/availability

GET http://localhost:8080/api/v1/appointments/availability?fecha=2026-06-15

**Respuesta ejemplo:**

```json
{
  "fecha": "2026-06-15",
  "horasOcupadas": ["10:00"],
  "horasDisponibles": ["08:00", "08:30", "09:00", "09:30", "10:30", "..."],
  "totalOcupadas": 1,
  "totalDisponibles": 17
}
```

---

## 📋 Resumen de Endpoints

| Método   | Endpoint                                    | Descripción                        | Status éxito |
|----------|---------------------------------------------|------------------------------------|:------------:|
| `POST`   | `/api/v1/appointments`                      | Crear cita                         | `201`        |
| `GET`    | `/api/v1/appointments`                      | Listar todas las citas             | `200`        |
| `GET`    | `/api/v1/appointments/{id}`                 | Consultar cita por ID              | `200`        |
| `PATCH`  | `/api/v1/appointments/{id}/reschedule`      | Reagendar cita                     | `200`        |
| `PATCH`  | `/api/v1/appointments/{id}/cancel`          | Cancelar cita                      | `200`        |
| `GET`    | `/api/v1/appointments/availability?fecha=`  | Disponibilidad de horarios         | `200`        |

---

## 📌 Notas importantes

- **Duplicados**: la API protege contra duplicados en dos capas: validación en servicio (`existsByFechaAndHora`) y restricción `UNIQUE` en base de datos (`uk_appointment_date_time`).
- **Disponibilidad**: el horario de atención configurado es **08:00 – 17:00** con slots de **30 minutos**. Esto se puede ajustar modificando las constantes `OPENING_TIME`, `CLOSING_TIME` y `SLOT_MINUTES` en `AppointmentServiceImpl`.
- **Estados válidos**: `PENDIENTE` → `CONFIRMADA` / `REAGENDADA` / `CANCELADA`. Una cita `CANCELADA` no puede volver a modificarse.
- **DDL automático**: `spring.jpa.hibernate.ddl-auto=update` creará la tabla `appointments` automáticamente al arrancar la aplicación si no existe.
