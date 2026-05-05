# Backend Spring MVC — API REST para Gestión de Reservas de Citas

## Estructura del Proyecto

```
citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── citasapi/
        │           ├── CitasApiApplication.java
        │           ├── controller/
        │           │   └── CitaController.java
        │           ├── model/
        │           │   └── entity/
        │           │       └── Cita.java
        │           ├── repository/
        │           │   └── CitaRepository.java
        │           ├── service/
        │           │   └── CitaService.java
        │           └── service/
        │               └── impl/
        │                   └── CitaServiceImpl.java
        └── resources/
            └── application.properties
```

---

## 1. `pom.xml`

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
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.citasapi</groupId>
    <artifactId>citas-api</artifactId>
    <version>1.0.0</version>
    <name>citas-api</name>
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

        <!-- Validación de datos (Bean Validation) -->
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

## 2. `application.properties`

```properties
# ── Servidor ──────────────────────────────────────────────
server.port=8080

# ── Datasource PostgreSQL ──────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ───────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Nombre de la aplicación ───────────────────────────────
spring.application.name=citas-api
```

---

## 3. Clase Principal — `CitasApiApplication.java`

```java
package com.citasapi;

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

## 4. Entidad — `model/entity/Cita.java`

```java
package com.citasapi.model.entity;

import com.citasapi.model.entity.enums.EstadoCita;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

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
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
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
    private EstadoCita estado;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoCita.PENDIENTE;
        }
    }
}
```

---

## 5. Enum de Estado — `model/entity/enums/EstadoCita.java`

```java
package com.citasapi.model.entity.enums;

public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    REAGENDADA
}
```

---

## 6. DTO de Reagendamiento — `model/entity/dto/ReagendarCitaRequest.java`

```java
package com.citasapi.model.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReagendarCitaRequest {

    @NotNull(message = "La nueva fecha es obligatoria.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;
}
```

---

## 7. Manejo de Errores — `exception/`

### `CitaNotFoundException.java`

```java
package com.citasapi.exception;

public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("No se encontró ninguna cita con ID: " + id);
    }
}
```

### `HorarioOcupadoException.java`

```java
package com.citasapi.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class HorarioOcupadoException extends RuntimeException {

    public HorarioOcupadoException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita registrada para la fecha "
              + fecha + " a las " + hora + ".");
    }
}
```

### `CitaYaCanceladaException.java`

```java
package com.citasapi.exception;

public class CitaYaCanceladaException extends RuntimeException {

    public CitaYaCanceladaException(Long id) {
        super("La cita con ID " + id + " ya se encuentra cancelada.");
    }
}
```

### `GlobalExceptionHandler.java`

```java
package com.citasapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ──────────────────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CitaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 Conflict: horario ocupado ──────────────────────
    @ExceptionHandler(HorarioOcupadoException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(HorarioOcupadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 422 Unprocessable: cita ya cancelada ───────────────
    @ExceptionHandler(CitaYaCanceladaException.class)
    public ResponseEntity<Map<String, Object>> handleYaCancelada(CitaYaCanceladaException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ── 400 Bad Request: validaciones Bean Validation ──────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errores", errores);
        return ResponseEntity.badRequest().body(body);
    }

    // ── 500 Internal Server Error genérico ────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ── Helper ─────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
```

---

## 8. Repository — `repository/CitaRepository.java`

```java
package com.citasapi.repository;

import com.citasapi.model.entity.Cita;
import com.citasapi.model.entity.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita en la misma fecha y hora.
     * Usado para validar duplicados al crear o reagendar.
     */
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Devuelve la cita existente en esa fecha/hora (si la hay),
     * excluyendo el ID indicado (útil al reagendar).
     */
    Optional<Cita> findByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    /**
     * Lista todas las citas de una fecha determinada.
     */
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    /**
     * Lista citas por estado.
     */
    List<Cita> findByEstadoOrderByFechaAscHoraAsc(EstadoCita estado);
}
```

---

## 9. Service Interface — `service/CitaService.java`

```java
package com.citasapi.service;

import com.citasapi.model.entity.Cita;
import com.citasapi.model.entity.dto.ReagendarCitaRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CitaService {

    /** Crea una nueva cita. */
    Cita crearCita(Cita cita);

    /** Devuelve todas las citas registradas. */
    List<Cita> listarCitas();

    /** Busca una cita por su ID. */
    Cita obtenerCitaPorId(Long id);

    /** Reagenda la fecha y hora de una cita existente. */
    Cita reagendarCita(Long id, ReagendarCitaRequest request);

    /** Cancela una cita cambiando su estado a CANCELADA. */
    Cita cancelarCita(Long id);

    /**
     * Consulta la disponibilidad de horarios para una fecha.
     * Retorna un mapa con los horarios ocupados y libres.
     */
    Map<String, Object> consultarDisponibilidad(LocalDate fecha);
}
```

---

## 10. Service Implementation — `service/impl/CitaServiceImpl.java`

```java
package com.citasapi.service.impl;

import com.citasapi.exception.CitaNotFoundException;
import com.citasapi.exception.CitaYaCanceladaException;
import com.citasapi.exception.HorarioOcupadoException;
import com.citasapi.model.entity.Cita;
import com.citasapi.model.entity.dto.ReagendarCitaRequest;
import com.citasapi.model.entity.enums.EstadoCita;
import com.citasapi.repository.CitaRepository;
import com.citasapi.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    // ── Horario laboral: 08:00 – 17:00, intervalos de 30 min ──
    private static final LocalTime INICIO_JORNADA = LocalTime.of(8, 0);
    private static final LocalTime FIN_JORNADA    = LocalTime.of(17, 0);
    private static final int       INTERVALO_MIN  = 30;

    // ──────────────────────────────────────────────────────────
    // CREAR CITA
    // ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        validarHorarioLaboral(cita.getHora());
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new HorarioOcupadoException(cita.getFecha(), cita.getHora());
        }
        cita.setEstado(EstadoCita.PENDIENTE);
        return citaRepository.save(cita);
    }

    // ──────────────────────────────────────────────────────────
    // LISTAR CITAS
    // ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    // ──────────────────────────────────────────────────────────
    // OBTENER CITA POR ID
    // ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    // ──────────────────────────────────────────────────────────
    // REAGENDAR CITA
    // ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Cita reagendarCita(Long id, ReagendarCitaRequest request) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new CitaYaCanceladaException(id);
        }

        validarHorarioLaboral(request.getNuevaHora());

        // Verificar que ninguna OTRA cita ocupe ese slot
        citaRepository.findByFechaAndHoraAndIdNot(
                request.getNuevaFecha(), request.getNuevaHora(), id)
                .ifPresent(c -> {
                    throw new HorarioOcupadoException(
                            request.getNuevaFecha(), request.getNuevaHora());
                });

        cita.setFecha(request.getNuevaFecha());
        cita.setHora(request.getNuevaHora());
        cita.setEstado(EstadoCita.REAGENDADA);
        return citaRepository.save(cita);
    }

    // ──────────────────────────────────────────────────────────
    // CANCELAR CITA
    // ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new CitaYaCanceladaException(id);
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    // ──────────────────────────────────────────────────────────
    // CONSULTAR DISPONIBILIDAD
    // ──────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFechaOrderByHoraAsc(fecha);

        // Horarios ocupados (excluir canceladas — liberan el slot)
        List<String> ocupados = citasDelDia.stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .map(c -> c.getHora().toString())
                .collect(Collectors.toList());

        // Generar todos los slots del día
        List<String> disponibles = generarSlots().stream()
                .filter(slot -> !ocupados.contains(slot))
                .collect(Collectors.toList());

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("fecha", fecha.toString());
        resultado.put("horariosOcupados", ocupados);
        resultado.put("horariosDisponibles", disponibles);
        resultado.put("totalOcupados", ocupados.size());
        resultado.put("totalDisponibles", disponibles.size());
        return resultado;
    }

    // ──────────────────────────────────────────────────────────
    // MÉTODOS PRIVADOS AUXILIARES
    // ──────────────────────────────────────────────────────────

    /**
     * Genera todos los slots horarios de la jornada laboral
     * en intervalos de INTERVALO_MIN minutos.
     */
    private List<String> generarSlots() {
        List<String> slots = new java.util.ArrayList<>();
        LocalTime cursor = INICIO_JORNADA;
        while (!cursor.isAfter(FIN_JORNADA.minusMinutes(1))) {
            slots.add(cursor.toString());
            cursor = cursor.plusMinutes(INTERVALO_MIN);
        }
        return slots;
    }

    /**
     * Valida que la hora indicada esté dentro del horario laboral.
     */
    private void validarHorarioLaboral(LocalTime hora) {
        if (hora.isBefore(INICIO_JORNADA) || hora.isAfter(FIN_JORNADA.minusMinutes(1))) {
            throw new IllegalArgumentException(
                    "La hora debe estar dentro del horario laboral: "
                    + INICIO_JORNADA + " – " + FIN_JORNADA + ".");
        }
    }
}
```

---

## 11. Controller — `controller/CitaController.java`

```java
package com.citasapi.controller;

import com.citasapi.model.entity.Cita;
import com.citasapi.model.entity.dto.ReagendarCitaRequest;
import com.citasapi.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ── POST /api/v1/citas ─────────────────────────────────
    /**
     * Crea una nueva cita.
     * Body JSON: { nombreCliente, fecha, hora, motivo }
     */
    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nueva = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // ── GET /api/v1/citas ──────────────────────────────────
    /**
     * Lista todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ── GET /api/v1/citas/{id} ─────────────────────────────
    /**
     * Obtiene una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    // ── PATCH /api/v1/citas/{id}/reagendar ─────────────────
    /**
     * Reagenda la fecha y hora de una cita existente.
     * Body JSON: { nuevaFecha, nuevaHora }
     */
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarCitaRequest request) {
        return ResponseEntity.ok(citaService.reagendarCita(id, request));
    }

    // ── PATCH /api/v1/citas/{id}/cancelar ──────────────────
    /**
     * Cancela una cita existente.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ── GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD ──
    /**
     * Consulta la disponibilidad de horarios para una fecha específica.
     * Query param: fecha (formato ISO: YYYY-MM-DD)
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 12. Ejemplos de Prueba en Postman

### Crear una cita — `POST /api/v1/citas`

```json
{
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-20",
  "hora": "09:00",
  "motivo": "Consulta de seguimiento"
}
```

### Listar citas — `GET /api/v1/citas`

```
GET http://localhost:8080/api/v1/citas
```

### Obtener cita por ID — `GET /api/v1/citas/{id}`

```
GET http://localhost:8080/api/v1/citas/1
```

### Reagendar una cita — `PATCH /api/v1/citas/{id}/reagendar`

```json
{
  "nuevaFecha": "2026-05-22",
  "nuevaHora": "11:30"
}
```

### Cancelar una cita — `PATCH /api/v1/citas/{id}/cancelar`

```
PATCH http://localhost:8080/api/v1/citas/1/cancelar
```

### Consultar disponibilidad — `GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD`

```
GET http://localhost:8080/api/v1/citas/disponibilidad?fecha=2026-05-20
```

Respuesta esperada:

```json
{
  "fecha": "2026-05-20",
  "horariosOcupados": ["09:00"],
  "horariosDisponibles": ["08:00", "08:30", "09:30", "10:00", "..."],
  "totalOcupados": 1,
  "totalDisponibles": 17
}
```

---

## Resumen de Endpoints

| Método   | Endpoint                              | Descripción                              | Status éxito |
|----------|---------------------------------------|------------------------------------------|--------------|
| `POST`   | `/api/v1/citas`                       | Crear una nueva cita                     | `201 Created`|
| `GET`    | `/api/v1/citas`                       | Listar todas las citas                   | `200 OK`     |
| `GET`    | `/api/v1/citas/{id}`                  | Obtener una cita por ID                  | `200 OK`     |
| `PATCH`  | `/api/v1/citas/{id}/reagendar`        | Reagendar fecha y hora de una cita       | `200 OK`     |
| `PATCH`  | `/api/v1/citas/{id}/cancelar`         | Cancelar una cita                        | `200 OK`     |
| `GET`    | `/api/v1/citas/disponibilidad?fecha=` | Consultar disponibilidad de un día       | `200 OK`     |

---

## Notas Finales

- **Duplicados**: la restricción `@UniqueConstraint` a nivel de base de datos más la validación previa en el servicio garantizan doble protección contra citas con la misma `fecha` + `hora`.
- **Slots cancelados**: al cancelar una cita, su horario se libera y vuelve a aparecer como disponible en la consulta de disponibilidad.
- **Horario laboral**: el servicio valida que toda cita esté comprendida entre las `08:00` y las `16:30` (última franja de 30 min antes de las `17:00`).
- **Transacciones**: las operaciones de escritura usan `@Transactional` y las de solo lectura usan `@Transactional(readOnly = true)` para mejor rendimiento.
- **Manejo de errores**: el `GlobalExceptionHandler` captura y estandariza todas las respuestas de error con `timestamp`, `status` y `mensaje`.