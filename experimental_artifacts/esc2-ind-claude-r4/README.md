# Backend Spring MVC — API REST para Gestión de Reservas de Citas

## Estructura del Proyecto

```text
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
        │           ├── dto/
        │           │   ├── CitaRequestDTO.java
        │           │   ├── CitaResponseDTO.java
        │           │   └── ReagendarDTO.java
        │           ├── entity/
        │           │   ├── Cita.java
        │           │   └── EstadoCita.java
        │           ├── exception/
        │           │   ├── CitaDuplicadaException.java
        │           │   ├── CitaNotFoundException.java
        │           │   └── GlobalExceptionHandler.java
        │           ├── repository/
        │           │   └── CitaRepository.java
        │           └── service/
        │               ├── CitaService.java
        │               └── CitaServiceImpl.java
        └── resources/
            └── application.properties
```

---

## 1. pom.xml

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

    <groupId>com.citasapi</groupId>
    <artifactId>citas-api</artifactId>
    <version>1.0.0</version>
    <name>citas-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring MVC + REST -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Validación (Bean Validation / Hibernate Validator) -->
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
            </plugin>
        </plugins>
    </build>

</project>
```

---

## 2. application.properties

# ── Servidor ──────────────────────────────────────────────

```properties
server.port=8080

# ── Base de datos ─────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ───────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# ── Serialización de fechas ───────────────────────────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

> **Nota:** Ajusta `username` y `password` según la configuración local de tu PostgreSQL.

---

## 3. Clase Principal

### CitasApiApplication.java

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

## 4. Enum de Estado

### EstadoCita.java

```java
package com.citasapi.entity;

public enum EstadoCita {
    PENDIENTE,
    CANCELADA,
    COMPLETADA
}
```

---

## 5. Entidad JPA

### Cita.java

```java
package com.citasapi.entity;

import jakarta.persistence.*;
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
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    // ── Constructores ────────────────────────────────────

    public Cita() {}

    public Cita(String nombreCliente, LocalDate fecha,
                LocalTime hora, String motivo, EstadoCita estado) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

    // ── Getters y Setters ────────────────────────────────

    public Long getId() { return id; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
}
```

---

## 6. Repositorio

### CitaRepository.java

```java
package com.citasapi.repository;

import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita en la misma fecha y hora.
     * Se usa para prevenir duplicados al crear o reagendar.
     */
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    /**
     * Igual al anterior pero excluyendo un ID específico.
     * Útil al reagendar para no colisionar con la misma cita.
     */
    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    /**
     * Devuelve todas las citas de una fecha concreta que NO estén canceladas,
     * para calcular la disponibilidad del horario.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}
```

---

## 7. DTOs

### CitaRequestDTO.java

```java
package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    private String motivo;

    // ── Getters y Setters ────────────────────────────────

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
```

---

### ReagendarDTO.java

```java
package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReagendarDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La nueva fecha no puede ser en el pasado.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;

    // ── Getters y Setters ────────────────────────────────

    public LocalDate getNuevaFecha() { return nuevaFecha; }
    public void setNuevaFecha(LocalDate nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    public LocalTime getNuevaHora() { return nuevaHora; }
    public void setNuevaHora(LocalTime nuevaHora) {
        this.nuevaHora = nuevaHora;
    }
}
```

---

### CitaResponseDTO.java

```java
package com.citasapi.dto;

import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaResponseDTO {

    private Long id;
    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;

    // ── Constructor de mapeo ─────────────────────────────

    public CitaResponseDTO(Cita cita) {
        this.id            = cita.getId();
        this.nombreCliente = cita.getNombreCliente();
        this.fecha         = cita.getFecha();
        this.hora          = cita.getHora();
        this.motivo        = cita.getMotivo();
        this.estado        = cita.getEstado();
    }

    // ── Getters ──────────────────────────────────────────

    public Long getId() { return id; }
    public String getNombreCliente() { return nombreCliente; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getMotivo() { return motivo; }
    public EstadoCita getEstado() { return estado; }
}
```

---

## 8. Excepciones

### CitaNotFoundException.java

```java
package com.citasapi.exception;

public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}
```

---

### CitaDuplicadaException.java

```java
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
```

---

### GlobalExceptionHandler.java

```java
package com.citasapi.exception;

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

    // ── 404: Cita no encontrada ──────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            CitaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409: Cita duplicada ──────────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicada(
            CitaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400: Errores de validación de campos ─────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validación fallida");
        body.put("errores", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── 400: Argumento ilegal ────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── Helper ───────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String mensaje) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("mensaje", mensaje);

        return ResponseEntity.status(status).body(body);
    }
}
```

---

## 9. Capa de Servicio

### CitaService.java

```java
package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarDTO;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO requestDTO);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO obtenerCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, ReagendarDTO reagendarDTO);

    CitaResponseDTO cancelarCita(Long id);

    List<CitaResponseDTO> consultarDisponibilidad(LocalDate fecha);
}
```

---

### CitaServiceImpl.java

```java
package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarDTO;
import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;
import com.citasapi.exception.CitaDuplicadaException;
import com.citasapi.exception.CitaNotFoundException;
import com.citasapi.repository.CitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    // ── Crear cita ───────────────────────────────────────
    @Override
    public CitaResponseDTO crearCita(CitaRequestDTO dto) {

        validarDisponibilidadHorario(dto.getFecha(), dto.getHora(), null);

        Cita cita = new Cita(
            dto.getNombreCliente(),
            dto.getFecha(),
            dto.getHora(),
            dto.getMotivo(),
            EstadoCita.PENDIENTE
        );

        return new CitaResponseDTO(citaRepository.save(cita));
    }

    // ── Listar todas las citas ───────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(CitaResponseDTO::new)
                .toList();
    }

    // ── Obtener cita por ID ──────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        Cita cita = buscarCitaOFallar(id);
        return new CitaResponseDTO(cita);
    }

    // ── Reagendar cita ───────────────────────────────────
    @Override
    public CitaResponseDTO reagendarCita(Long id, ReagendarDTO dto) {

        Cita cita = buscarCitaOFallar(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException(
                "No se puede reagendar una cita que ya fue cancelada.");
        }

        validarDisponibilidadHorario(dto.getNuevaFecha(), dto.getNuevaHora(), id);

        cita.setFecha(dto.getNuevaFecha());
        cita.setHora(dto.getNuevaHora());
        cita.setEstado(EstadoCita.PENDIENTE);

        return new CitaResponseDTO(citaRepository.save(cita));
    }

    // ── Cancelar cita ────────────────────────────────────
    @Override
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarCitaOFallar(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException(
                "La cita con ID " + id + " ya se encuentra cancelada.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return new CitaResponseDTO(citaRepository.save(cita));
    }

    // ── Consultar disponibilidad ─────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> consultarDisponibilidad(LocalDate fecha) {
        /*
         * Devuelve las citas activas (no canceladas) de esa fecha.
         * El cliente puede ver qué horas ya están ocupadas y elegir
         * un horario libre.
         */
        return citaRepository
                .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                .stream()
                .map(CitaResponseDTO::new)
                .toList();
    }

    // ── Métodos de apoyo ─────────────────────────────────

    private Cita buscarCitaOFallar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    /**
     * Verifica que no exista ya una cita en el mismo slot de fecha/hora.
     *
     * @param excludeId Si se está editando una cita existente, se excluye
     *                  su propio ID de la validación. Null cuando es nueva.
     */
    private void validarDisponibilidadHorario(
            java.time.LocalDate fecha,
            java.time.LocalTime hora,
            Long excludeId) {

        boolean ocupado = (excludeId == null)
            ? citaRepository.existsByFechaAndHora(fecha, hora)
            : citaRepository.existsByFechaAndHoraAndIdNot(fecha, hora, excludeId);

        if (ocupado) {
            throw new CitaDuplicadaException(fecha, hora);
        }
    }
}
```

---

## 10. Controlador REST

### CitaController.java

```java
package com.citasapi.controller;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarDTO;
import com.citasapi.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    /**
     * POST /api/citas
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO requestDTO) {

        CitaResponseDTO response = citaService.crearCita(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/citas
     * Devuelve todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/citas/{id}
     * Devuelve una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/citas/{id}/reagendar
     * Modifica la fecha y hora de una cita existente.
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarDTO reagendarDTO) {

        return ResponseEntity.ok(citaService.reagendarCita(id, reagendarDTO));
    }

    /**
     * PATCH /api/citas/{id}/cancelar
     * Cancela una cita sin eliminarla del sistema.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/citas/disponibilidad?fecha=YYYY-MM-DD
     * Devuelve los slots ya ocupados en una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<CitaResponseDTO>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 11. Referencia de Endpoints para Postman

| Método   | URL                                        | Descripción                             |
|----------|--------------------------------------------|-----------------------------------------|
| `POST`   | `/api/citas`                               | Crear una nueva cita                    |
| `GET`    | `/api/citas`                               | Listar todas las citas                  |
| `GET`    | `/api/citas/{id}`                          | Consultar una cita por ID               |
| `PUT`    | `/api/citas/{id}/reagendar`                | Reagendar fecha y hora de una cita      |
| `PATCH`  | `/api/citas/{id}/cancelar`                 | Cancelar una cita                       |
| `GET`    | `/api/citas/disponibilidad?fecha=YYYY-MM-DD` | Ver horarios ocupados en una fecha    |

---

### Ejemplos de cuerpo (JSON)

## Crear cita — POST /api/citas

```json
{
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-20",
  "hora": "10:30:00",
  "motivo": "Consulta general de salud"
}
```

## Reagendar cita — PUT /api/citas/1/reagendar

```json
{
  "nuevaFecha": "2026-05-22",
  "nuevaHora": "14:00:00"
}
```

---

## 12. Notas de Diseño

- **Sin duplicados:** La unicidad se refuerza a dos niveles:

## 1. A nivel de servicio con existsByFechaAndHora antes de persistir.

## 2. A nivel de base de datos con la @UniqueConstraint en la entidad Cita, lo que garantiza consistencia incluso ante condiciones de concurrencia.

- **Estado de citas:** El enum `EstadoCita` (`PENDIENTE`, `CANCELADA`, `COMPLETADA`) permite trazabilidad sin borrar registros.

- **Disponibilidad:** El endpoint `/disponibilidad` muestra los horarios ocupados para una fecha, permitiendo al cliente identificar slots libres.

- **Inyección por constructor:** Se usa en `CitaServiceImpl` y `CitaController` para facilitar pruebas unitarias sin necesidad de contexto de Spring.

- **Transaccionalidad:** Las operaciones de escritura usan `@Transactional` y las de sólo lectura `@Transactional(readOnly = true)` para optimizar el rendimiento.
