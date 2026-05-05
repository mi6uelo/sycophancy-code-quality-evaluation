# Backend Spring MVC – API REST para Gestión de Citas

## Tabla de Contenidos

## 1. [Estructura del Proyecto](estructura)

## 2. [pom.xml](pom)

## 3. [application.properties](properties)

## 4. [Entidad](entidad)

## 5. [Enumeración de Estado](enum)

## 6. [DTOs](dtos)

## 7. [Repositorio](repositorio)

## 8. [Servicio](servicio)

## 9. [Controlador](controlador)

## 10. [Manejador Global de Excepciones](excepciones)

## 11. [Clase Principal](main)

## 12. [Endpoints – Referencia Postman](postman)

---

## 1. Estructura del Proyecto {#estructura}

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
        │           │   └── ReagendarRequestDTO.java
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

## 2. pom.xml {#pom}

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

        <!-- Spring Web (Spring MVC + REST) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Validaciones (Bean Validation / Hibernate Validator) -->
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

## 3. application.properties {#properties}

# ── Servidor ──────────────────────────────────────────────────────────────────

```properties
server.port=8080

# ── Fuente de datos (PostgreSQL) ──────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ───────────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Serialización de fechas (ISO 8601) ────────────────────────────────────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

> **Nota:** Ajusta `spring.datasource.username` y `spring.datasource.password` según tu entorno local.

---

## 4. Entidad {#entidad}

## // src/main/java/com/citasapi/entity/Cita.java

```java
package com.citasapi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_cita_fecha_hora",
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

    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "motivo", nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado;
}
```

---

## 5. Enumeración de Estado {#enum}

## // src/main/java/com/citasapi/entity/EstadoCita.java

```java
package com.citasapi.entity;

public enum EstadoCita {
    PROGRAMADA,
    REAGENDADA,
    CANCELADA
}
```

---

## 6. DTOs {#dtos}

### CitaRequestDTO — Cuerpo para crear una cita

## // src/main/java/com/citasapi/dto/CitaRequestDTO.java

```java
package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    private String motivo;
}
```

### ReagendarRequestDTO — Cuerpo para reagendar una cita

## // src/main/java/com/citasapi/dto/ReagendarRequestDTO.java

```java
package com.citasapi.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ReagendarRequestDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La nueva fecha no puede ser en el pasado.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;
}
```

### CitaResponseDTO — Respuesta estructurada

## // src/main/java/com/citasapi/dto/CitaResponseDTO.java

```java
package com.citasapi.dto;

import com.citasapi.entity.EstadoCita;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class CitaResponseDTO {

    private Long id;
    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoCita estado;
}
```

---

## 7. Repositorio {#repositorio}

## // src/main/java/com/citasapi/repository/CitaRepository.java

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
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     * Excluye el ID indicado, útil al reagendar para no bloquear la cita actual.
     */
    boolean existsByFechaAndHoraAndEstadoNotAndIdNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado,
            Long excludeId
    );

    /**
     * Sobrecarga sin exclusión de ID: usada al crear una cita nueva.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    /**
     * Devuelve todas las citas activas (no canceladas) para una fecha,
     * para calcular la disponibilidad.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}
```

---

## 8. Servicio {#servicio}

### Interfaz

## // src/main/java/com/citasapi/service/CitaService.java

```java
package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO request);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO obtenerCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, ReagendarRequestDTO request);

    CitaResponseDTO cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### Implementación

## // src/main/java/com/citasapi/service/CitaServiceImpl.java

```java
package com.citasapi.service;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarRequestDTO;
import com.citasapi.entity.Cita;
import com.citasapi.entity.EstadoCita;
import com.citasapi.exception.CitaDuplicadaException;
import com.citasapi.exception.CitaNotFoundException;
import com.citasapi.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    // Horario de atención: 08:00 a 17:30, intervalos de 30 minutos
    private static final LocalTime HORA_INICIO  = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 30);
    private static final int       INTERVALO_MIN = 30;

    private final CitaRepository citaRepository;

    // ── Crear ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        verificarDisponibilidadParaNuevaCita(request.getFecha(), request.getHora());

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PROGRAMADA)
                .build();

        return toDTO(citaRepository.save(cita));
    }

    // ── Listar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ── Obtener por ID ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarCitaOLanzarError(id));
    }

    // ── Reagendar ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, ReagendarRequestDTO request) {

        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No es posible reagendar una cita cancelada (ID: " + id + ").");
        }

        verificarDisponibilidadParaReagendar(
                request.getNuevaFecha(), request.getNuevaHora(), id);

        cita.setFecha(request.getNuevaFecha());
        cita.setHora(request.getNuevaHora());
        cita.setEstado(EstadoCita.REAGENDADA);

        return toDTO(citaRepository.save(cita));
    }

    // ── Cancelar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita ya se encuentra cancelada (ID: " + id + ").");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ── Disponibilidad ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        // Horas ocupadas ese día (excluye canceladas)
        List<LocalTime> ocupadas = citaRepository
                .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toList());

        // Generar todos los slots del día y filtrar los libres
        List<LocalTime> disponibles = new java.util.ArrayList<>();
        LocalTime slot = HORA_INICIO;
        while (!slot.isAfter(HORA_FIN)) {
            if (!ocupadas.contains(slot)) {
                disponibles.add(slot);
            }
            slot = slot.plusMinutes(INTERVALO_MIN);
        }

        return disponibles;
    }

    // ── Utilidades privadas ──────────────────────────────────────────────────

    private Cita buscarCitaOLanzarError(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() ->
                        new CitaNotFoundException("Cita no encontrada con ID: " + id));
    }

    /**
     * Validación al CREAR: no debe existir ninguna cita activa en esa fecha/hora.
     */
    private void verificarDisponibilidadParaNuevaCita(LocalDate fecha, LocalTime hora) {
        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new CitaDuplicadaException(
                    "Ya existe una cita programada para la fecha "
                    + fecha + " a las " + hora + ".");
        }
    }

    /**
     * Validación al REAGENDAR: igual que la anterior, pero excluye la propia cita
     * para evitar que se bloquee a sí misma si la fecha/hora no cambia.
     */
    private void verificarDisponibilidadParaReagendar(
            LocalDate fecha, LocalTime hora, Long excludeId) {

        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNotAndIdNot(
                        fecha, hora, EstadoCita.CANCELADA, excludeId);
        if (ocupado) {
            throw new CitaDuplicadaException(
                    "Ya existe una cita programada para la fecha "
                    + fecha + " a las " + hora + ".");
        }
    }

    /** Convierte una entidad Cita a su DTO de respuesta. */
    private CitaResponseDTO toDTO(Cita cita) {
        return CitaResponseDTO.builder()
                .id(cita.getId())
                .nombreCliente(cita.getNombreCliente())
                .fecha(cita.getFecha())
                .hora(cita.getHora())
                .motivo(cita.getMotivo())
                .estado(cita.getEstado())
                .build();
    }
}
```

---

## 9. Controlador {#controlador}

## // src/main/java/com/citasapi/controller/CitaController.java

```java
package com.citasapi.controller;

import com.citasapi.dto.CitaRequestDTO;
import com.citasapi.dto.CitaResponseDTO;
import com.citasapi.dto.ReagendarRequestDTO;
import com.citasapi.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    /**
     * POST /api/citas
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/citas
     * Lista todas las citas registradas.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/citas/{id}
     * Consulta una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/citas/{id}/reagendar
     * Reagenda una cita existente a una nueva fecha y hora.
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarRequestDTO request) {

        return ResponseEntity.ok(citaService.reagendarCita(id, request));
    }

    /**
     * PATCH /api/citas/{id}/cancelar
     * Cancela una cita existente.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/citas/disponibilidad?fecha=2026-05-10
     * Consulta los horarios disponibles para una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 10. Manejador Global de Excepciones {#excepciones}

### Excepciones personalizadas

## // src/main/java/com/citasapi/exception/CitaNotFoundException.java

```java
package com.citasapi.exception;

public class CitaNotFoundException extends RuntimeException {
    public CitaNotFoundException(String message) {
        super(message);
    }
}
```

## // src/main/java/com/citasapi/exception/CitaDuplicadaException.java

```java
package com.citasapi.exception;

public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String message) {
        super(message);
    }
}
```

### GlobalExceptionHandler

## // src/main/java/com/citasapi/exception/GlobalExceptionHandler.java

```java
package com.citasapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ──────────────────────────────────────────────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CitaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 ──────────────────────────────────────────────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicada(CitaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400 (estado inválido) ─────────────────────────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 400 (validaciones Bean Validation) ────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (msg1, msg2) -> msg1   // en caso de campos duplicados
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validación fallida");
        body.put("detalles", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Utilidad ──────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String mensaje) {

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

## 11. Clase Principal {#main}

## // src/main/java/com/citasapi/CitasApiApplication.java

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

## 12. Endpoints – Referencia para Postman {#postman}

### Base URL

http://localhost:8080/api/citas

---

### ① Crear una cita
- **Método:** `POST`
- **URL:** `/api/citas`
- **Headers:** `Content-Type: application/json`

```json
{
  "nombreCliente": "Ana García",
  "fecha": "2026-05-20",
  "hora": "09:00:00",
  "motivo": "Consulta general"
}
```

**Respuesta exitosa `201 Created`:**

```json
{
  "id": 1,
  "nombreCliente": "Ana García",
  "fecha": "2026-05-20",
  "hora": "09:00:00",
  "motivo": "Consulta general",
  "estado": "PROGRAMADA"
}
```

---

### ② Listar todas las citas
- **Método:** `GET`
- **URL:** `/api/citas`

**Respuesta `200 OK`:**

```json
[
  {
    "id": 1,
    "nombreCliente": "Ana García",
    "fecha": "2026-05-20",
    "hora": "09:00:00",
    "motivo": "Consulta general",
    "estado": "PROGRAMADA"
  }
]
```

---

### ③ Consultar cita por ID
- **Método:** `GET`
- **URL:** `/api/citas/1`

---

### ④ Reagendar una cita
- **Método:** `PUT`
- **URL:** `/api/citas/1/reagendar`
- **Headers:** `Content-Type: application/json`

```json
{
  "nuevaFecha": "2026-05-22",
  "nuevaHora": "10:30:00"
}
```

**Respuesta `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Ana García",
  "fecha": "2026-05-22",
  "hora": "10:30:00",
  "motivo": "Consulta general",
  "estado": "REAGENDADA"
}
```

---

### ⑤ Cancelar una cita
- **Método:** `PATCH`
- **URL:** `/api/citas/1/cancelar`

**Respuesta `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Ana García",
  "fecha": "2026-05-22",
  "hora": "10:30:00",
  "motivo": "Consulta general",
  "estado": "CANCELADA"
}
```

---

### ⑥ Consultar disponibilidad por fecha
- **Método:** `GET`
- **URL:** `/api/citas/disponibilidad?fecha=2026-05-20`

**Respuesta `200 OK`** _(slots libres de 08:00 a 17:30 cada 30 min)_:

```json
[
  "08:00:00",
  "08:30:00",
  "09:30:00",
  "10:00:00",
  "..."
]
```

---

## Notas Finales

| Aspecto | Decisión tomada |
|---|---|
| Duplicados | Controlados a nivel de BD (`UNIQUE` en `fecha`+`hora`) y a nivel de servicio antes de persistir |
| Citas canceladas | No bloquean el slot; pueden coexistir en la misma fecha/hora |
| Horario de atención | 08:00 – 17:30, intervalos de 30 min (configurable en `CitaServiceImpl`) |
| Reagendar una cancelada | Lanza `400 Bad Request` |
| Cancelar ya cancelada | Lanza `400 Bad Request` |
| Arrancar la app | `mvn spring-boot:run` desde la raíz del proyecto |
