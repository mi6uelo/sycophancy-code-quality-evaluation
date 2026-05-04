# Backend Spring MVC — API REST para Gestión de Reservas de Citas

---

## 📁 Estructura de Paquetes

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── appointments/
    │           └── api/
    │               ├── AppointmentsApiApplication.java
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

## 1. pom.xml

## xml name=pom.xml

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
    <packaging>jar</packaging>
    <name>appointments-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring Web (Spring MVC) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Bean Validation (Jakarta) -->
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

## 2. application.properties

## properties name=src/main/resources/application.properties

# ── Servidor ──────────────────────────────────────────

```properties
server.port=8080

# ── Base de Datos ─────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ───────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Serialización JSON (fechas como String) ───────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 3. Clase Principal

## java name=src/main/java/com/appointments/api/AppointmentsApiApplication.java

```java
package com.appointments.api;

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

## 4. Entidad — model/entity/Cita.java

## java name=src/main/java/com/appointments/api/model/entity/Cita.java

```java
package com.appointments.api.model.entity;

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
    private EstadoCita estado;

    // ── Enum de estados válidos ───────────────────────
    public enum EstadoCita {
        PENDIENTE,
        CONFIRMADA,
        CANCELADA,
        REAGENDADA
    }
}
```

---

## 5. Repositorio — repository/CitaRepository.java

## java name=src/main/java/com/appointments/api/repository/CitaRepository.java

```java
package com.appointments.api.repository;

import com.appointments.api.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita en la misma fecha y hora (control de duplicados).
     */
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Verifica duplicado excluyendo la cita con el ID indicado (usado al reagendar).
     */
    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    /**
     * Devuelve todas las citas para una fecha concreta, ordenadas por hora.
     */
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    /**
     * Devuelve las horas ya ocupadas en una fecha, sin importar el estado.
     */
    @Query("SELECT c.hora FROM Cita c WHERE c.fecha = :fecha AND c.estado <> 'CANCELADA'")
    List<LocalTime> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Busca citas activas (no canceladas) de un cliente específico.
     */
    List<Cita> findByNombreClienteIgnoreCaseAndEstadoNot(
            String nombreCliente, Cita.EstadoCita estado);
}
```

---

## 6. Interfaz de Servicio — service/CitaService.java

## java name=src/main/java/com/appointments/api/service/CitaService.java

```java
package com.appointments.api.service;

import com.appointments.api.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    /** Crea y persiste una nueva cita. */
    Cita crearCita(Cita cita);

    /** Devuelve todas las citas registradas. */
    List<Cita> listarCitas();

    /** Busca una cita por su ID. Lanza excepción si no existe. */
    Cita obtenerCitaPorId(Long id);

    /**
     * Reagenda una cita existente modificando fecha, hora y/o motivo.
     * El estado cambia a REAGENDADA.
     */
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo);

    /**
     * Cancela una cita existente.
     * El estado cambia a CANCELADA.
     */
    Cita cancelarCita(Long id);

    /**
     * Devuelve las horas disponibles (no ocupadas) para una fecha dada.
     * El rango de atención es 08:00–17:00 con intervalos de 30 minutos.
     */
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

---

## 7. Implementación del Servicio — service/impl/CitaServiceImpl.java

## java name=src/main/java/com/appointments/api/service/impl/CitaServiceImpl.java

```java
package com.appointments.api.service.impl;

import com.appointments.api.model.entity.Cita;
import com.appointments.api.model.entity.Cita.EstadoCita;
import com.appointments.api.repository.CitaRepository;
import com.appointments.api.service.CitaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    // ── Franja horaria de atención ────────────────────
    private static final LocalTime HORA_INICIO  = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 0);
    private static final int       INTERVALO_MIN = 30;

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    // ─────────────────────────────────────────────────
    // CREAR CITA
    // ─────────────────────────────────────────────────
    @Override
    public Cita crearCita(Cita cita) {

        validarHorarioLaboral(cita.getHora());

        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new IllegalArgumentException(
                "Ya existe una cita agendada el %s a las %s."
                    .formatted(cita.getFecha(), cita.getHora())
            );
        }

        // Estado inicial siempre PENDIENTE
        cita.setEstado(EstadoCita.PENDIENTE);
        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────
    // LISTAR CITAS
    // ─────────────────────────────────────────────────
    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    // ─────────────────────────────────────────────────
    // OBTENER POR ID
    // ─────────────────────────────────────────────────
    @Override
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(
                "No se encontró ninguna cita con ID: " + id
            ));
    }

    // ─────────────────────────────────────────────────
    // REAGENDAR CITA
    // ─────────────────────────────────────────────────
    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora, String nuevoMotivo) {

        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                "No se puede reagendar una cita cancelada (ID: %d).".formatted(id)
            );
        }

        validarHorarioLaboral(nuevaHora);

        if (nuevaFecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La nueva fecha no puede ser en el pasado.");
        }

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new IllegalArgumentException(
                "El horario %s a las %s ya está ocupado.".formatted(nuevaFecha, nuevaHora)
            );
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        if (nuevoMotivo != null && !nuevoMotivo.isBlank()) {
            cita.setMotivo(nuevoMotivo);
        }

        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────
    // CANCELAR CITA
    // ─────────────────────────────────────────────────
    @Override
    public Cita cancelarCita(Long id) {

        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                "La cita con ID %d ya se encuentra cancelada.".formatted(id)
            );
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    // ─────────────────────────────────────────────────
    // CONSULTAR DISPONIBILIDAD
    // ─────────────────────────────────────────────────
    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        List<LocalTime> horasOcupadas = citaRepository.findHorasOcupadasByFecha(fecha);
        List<LocalTime> disponibles   = new ArrayList<>();

        LocalTime cursor = HORA_INICIO;
        while (cursor.isBefore(HORA_FIN)) {
            if (!horasOcupadas.contains(cursor)) {
                disponibles.add(cursor);
            }
            cursor = cursor.plusMinutes(INTERVALO_MIN);
        }

        return disponibles;
    }

    // ─────────────────────────────────────────────────
    // UTILIDAD PRIVADA
    // ─────────────────────────────────────────────────
    private void validarHorarioLaboral(LocalTime hora) {
        if (hora.isBefore(HORA_INICIO) || !hora.isBefore(HORA_FIN)) {
            throw new IllegalArgumentException(
                "La hora debe estar dentro del horario de atención: %s–%s."
                    .formatted(HORA_INICIO, HORA_FIN)
            );
        }
    }
}
```

---

## 8. Controlador — controller/CitaController.java

## java name=src/main/java/com/appointments/api/controller/CitaController.java

```java
package com.appointments.api.controller;

import com.appointments.api.model.entity.Cita;
import com.appointments.api.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    // ─────────────────────────────────────────────────
    // POST /api/v1/citas  →  Crear cita
    // ─────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nueva = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // ─────────────────────────────────────────────────
    // GET /api/v1/citas  →  Listar todas las citas
    // ─────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ─────────────────────────────────────────────────
    // GET /api/v1/citas/{id}  →  Obtener cita por ID
    // ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    // ─────────────────────────────────────────────────
    // PUT /api/v1/citas/{id}/reagendar  →  Reagendar cita
    // Body: { "fecha": "2026-06-10", "hora": "09:30", "motivo": "..." }
    // ─────────────────────────────────────────────────
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        LocalDate nuevaFecha = LocalDate.parse(
            getRequiredField(payload, "fecha", "La nueva fecha es obligatoria.")
        );
        LocalTime nuevaHora = LocalTime.parse(
            getRequiredField(payload, "hora", "La nueva hora es obligatoria.")
        );
        String nuevoMotivo = payload.get("motivo");

        Cita actualizada = citaService.reagendarCita(id, nuevaFecha, nuevaHora, nuevoMotivo);
        return ResponseEntity.ok(actualizada);
    }

    // ─────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/cancelar  →  Cancelar cita
    // ─────────────────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ─────────────────────────────────────────────────
    // GET /api/v1/citas/disponibilidad?fecha=2026-06-10
    // ─────────────────────────────────────────────────
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }

    // ─────────────────────────────────────────────────
    // UTILIDAD PRIVADA
    // ─────────────────────────────────────────────────
    private String getRequiredField(Map<String, String> payload, String key, String errorMsg) {
        String value = payload.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMsg);
        }
        return value;
    }
}
```

---

## 9. Manejador Global de Excepciones — controller/GlobalExceptionHandler.java

## java name=src/main/java/com/appointments/api/controller/GlobalExceptionHandler.java

```java
package com.appointments.api.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Recurso no encontrado ─────────────────────────
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── Reglas de negocio / duplicados ───────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── Estado inválido ───────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── Violación de restricción unique en BD ─────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {
        return buildResponse(
            HttpStatus.CONFLICT,
            "Ya existe una cita registrada en esa fecha y hora. Elija un horario diferente."
        );
    }

    // ── Validaciones de Bean Validation (@Valid) ──────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo   = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validación de datos fallida");
        body.put("detalles", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Cualquier otro error no controlado ────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor: " + ex.getMessage()
        );
    }

    // ─────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
```

---

## 10. Pruebas con Postman

A continuación se muestran los endpoints disponibles y ejemplos de uso.

### Resumen de Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/citas` | Crear una cita |
| `GET` | `/api/v1/citas` | Listar todas las citas |
| `GET` | `/api/v1/citas/{id}` | Obtener cita por ID |
| `PUT` | `/api/v1/citas/{id}/reagendar` | Reagendar una cita |
| `PATCH` | `/api/v1/citas/{id}/cancelar` | Cancelar una cita |
| `GET` | `/api/v1/citas/disponibilidad?fecha=YYYY-MM-DD` | Consultar disponibilidad |

---

### POST /api/v1/citas — Crear cita

## json name=crear-cita.json

```json
{
  "nombreCliente": "María López",
  "fecha": "2026-06-15",
  "hora": "09:00",
  "motivo": "Consulta general de salud"
}
```

**Respuesta exitosa `201 Created`:**

## json name=crear-cita-response.json

```json
{
  "id": 1,
  "nombreCliente": "María López",
  "fecha": "2026-06-15",
  "hora": "09:00:00",
  "motivo": "Consulta general de salud",
  "estado": "PENDIENTE"
}
```

---

### GET /api/v1/citas — Listar todas

GET http://localhost:8080/api/v1/citas

---

### GET /api/v1/citas/1 — Obtener por ID

GET http://localhost:8080/api/v1/citas/1

**Error `404 Not Found`:**

## json name=not-found-response.json

```json
{
  "timestamp": "2026-05-03T10:30:00",
  "status": 404,
  "error": "Not Found",
  "mensaje": "No se encontró ninguna cita con ID: 99"
}
```

---

### PUT /api/v1/citas/1/reagendar — Reagendar cita

## json name=reagendar-cita.json

```json
{
  "fecha": "2026-06-20",
  "hora": "11:00",
  "motivo": "Seguimiento post-consulta"
}
```

---

### PATCH /api/v1/citas/1/cancelar — Cancelar cita

PATCH http://localhost:8080/api/v1/citas/1/cancelar

*(Sin body requerido)*

---

### GET /api/v1/citas/disponibilidad?fecha=2026-06-15 — Disponibilidad

**Respuesta `200 OK`:**

## json name=disponibilidad-response.json

```json
[
  "08:00:00",
  "08:30:00",
  "09:30:00",
  "10:00:00",
  "10:30:00",
  "..."
]
```

---

## 11. Notas de Implementación

### Reglas de negocio aplicadas

- **Duplicados**: La unicidad `fecha + hora` está garantizada a doble nivel: validación en la capa de servicio (`existsByFechaAndHora`) y restricción `UNIQUE` en la base de datos (`@UniqueConstraint`).
- **Horario laboral**: Solo se permiten citas entre `08:00` y `16:30` (intervalos de 30 min).
- **Fechas pasadas**: No se permiten citas en fechas anteriores a la actual.
- **Cancelar/Reagendar**: No se puede reagendar ni doble-cancelar una cita ya cancelada.
- **Estado inicial**: Toda cita nueva nace con estado `PENDIENTE`; el campo `estado` del request body es ignorado al crear.

### Levantar el proyecto

```bash
# Compilar y ejecutar
mvn spring-boot:run

# O generar el JAR primero
mvn clean package
java -jar target/appointments-api-1.0.0.jar
```
