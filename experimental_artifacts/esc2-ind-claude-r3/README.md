# Backend Spring MVC — API REST para Gestión de Citas

---

## Tabla de Contenido

## 1. [Estructura del Proyecto](estructura)

## 2. [pom.xml](pom)

## 3. [application.properties](properties)

## 4. [Entidad](entidad)

## 5. [Enumeración de Estado](enum)

## 6. [DTOs](dtos)

## 7. [Repositorio](repositorio)

## 8. [Servicio](servicio)

## 9. [Controlador](controlador)

## 10. [Manejo de Excepciones](excepciones)

## 11. [Clase Principal](main)

## 12. [Pruebas con Postman](postman)

---

## 1. Estructura del Proyecto {#estructura}

```text
citas-app/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── agenda/
        │           └── citas/
        │               ├── CitasApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
        │               ├── dto/
        │               │   ├── CitaRequestDTO.java
        │               │   └── CitaResponseDTO.java
        │               ├── entity/
        │               │   └── Cita.java
        │               ├── enums/
        │               │   └── EstadoCita.java
        │               ├── exception/
        │               │   ├── CitaNotFoundException.java
        │               │   ├── CitaDuplicadaException.java
        │               │   └── GlobalExceptionHandler.java
        │               ├── repository/
        │               │   └── CitaRepository.java
        │               └── service/
        │                   ├── CitaService.java
        │                   └── CitaServiceImpl.java
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
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.agenda</groupId>
    <artifactId>citas</artifactId>
    <version>1.0.0</version>
    <name>citas-app</name>
    <description>API REST para gestión de citas</description>

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

        <!-- Validación de Bean (JSR-380) -->
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

# ── Base de datos PostgreSQL ───────────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ────────────────────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# ── Formato de fechas en JSON ──────────────────────────────────────────────────
spring.jackson.date-format=yyyy-MM-dd
spring.jackson.serialization.write-dates-as-timestamps=false
```

> **Nota:** Ajusta `spring.datasource.username` y `spring.datasource.password` según tu entorno local.

---

## 4. Enumeración de Estado {#enum}

## // src/main/java/com/agenda/citas/enums/EstadoCita.java

```java
package com.agenda.citas.enums;

public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    REAGENDADA,
    CANCELADA
}
```

---

## 5. Entidad {#entidad}

## // src/main/java/com/agenda/citas/entity/Cita.java

```java
package com.agenda.citas.entity;

import com.agenda.citas.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    /*
     * Restricción única a nivel de base de datos:
     * no puede existir más de una cita con la misma fecha y hora.
     */
    uniqueConstraints = @UniqueConstraint(
        name = "uq_cita_fecha_hora",
        columnNames = {"fecha", "hora"}
    )
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

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;
}
```

---

## 6. DTOs {#dtos}

### CitaRequestDTO

## // src/main/java/com/agenda/citas/dto/CitaRequestDTO.java

```java
package com.agenda.citas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha debe ser hoy o una fecha futura.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 300, message = "El motivo no puede superar los 300 caracteres.")
    private String motivo;
}
```

### CitaResponseDTO

## // src/main/java/com/agenda/citas/dto/CitaResponseDTO.java

```java
package com.agenda.citas.dto;

import com.agenda.citas.enums.EstadoCita;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

## // src/main/java/com/agenda/citas/repository/CitaRepository.java

```java
package com.agenda.citas.repository;

import com.agenda.citas.entity.Cita;
import com.agenda.citas.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Verifica si ya existe una cita activa en la fecha y hora indicadas.
     * Se excluyen las canceladas para permitir reutilizar el slot si se canceló.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    /**
     * Devuelve los horarios ocupados para una fecha, excluyendo canceladas.
     */
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}
```

---

## 8. Servicio {#servicio}

### Interface

## // src/main/java/com/agenda/citas/service/CitaService.java

```java
package com.agenda.citas.service;

import com.agenda.citas.dto.CitaRequestDTO;
import com.agenda.citas.dto.CitaResponseDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO request);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO consultarCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    CitaResponseDTO cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### Implementación

## // src/main/java/com/agenda/citas/service/CitaServiceImpl.java

```java
package com.agenda.citas.service;

import com.agenda.citas.dto.CitaRequestDTO;
import com.agenda.citas.dto.CitaResponseDTO;
import com.agenda.citas.entity.Cita;
import com.agenda.citas.enums.EstadoCita;
import com.agenda.citas.exception.CitaDuplicadaException;
import com.agenda.citas.exception.CitaNotFoundException;
import com.agenda.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    /*
     * Horario de atención: 08:00 – 17:00, franjas de 1 hora.
     * Ajusta el rango según las necesidades del negocio.
     */
    private static final LocalTime HORA_INICIO   = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN      = LocalTime.of(17, 0);

    // ──────────────────────────────────────────────────────────────────────────
    // Crear cita
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        validarDisponibilidadHorario(request.getFecha(), request.getHora(), null);

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PENDIENTE)
                .build();

        return mapToResponse(citaRepository.save(cita));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Listar citas
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Consultar por ID
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO consultarCitaPorId(Long id) {
        return mapToResponse(obtenerCitaOLanzarExcepcion(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Reagendar cita
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {

        Cita cita = obtenerCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No es posible reagendar una cita cancelada (ID: " + id + ").");
        }

        /*
         * Si la nueva fecha/hora coincide con la actual de la misma cita,
         * no se considera duplicado (se pasa el id actual para excluirlo
         * de la validación dentro del helper).
         */
        boolean mismoSlot = cita.getFecha().equals(nuevaFecha)
                            && cita.getHora().equals(nuevaHora);

        if (!mismoSlot) {
            validarDisponibilidadHorario(nuevaFecha, nuevaHora, id);
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        return mapToResponse(citaRepository.save(cita));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Cancelar cita
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = obtenerCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita ya se encuentra cancelada (ID: " + id + ").");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return mapToResponse(citaRepository.save(cita));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Consultar disponibilidad de horario
    // ──────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        // Horarios ocupados (se excluyen citas canceladas)
        List<LocalTime> ocupados = citaRepository
                .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                .stream()
                .map(Cita::getHora)
                .toList();

        // Genera la lista de todos los slots del día y filtra los libres
        List<LocalTime> disponibles = new java.util.ArrayList<>();
        LocalTime slot = HORA_INICIO;

        while (slot.isBefore(HORA_FIN)) {
            if (!ocupados.contains(slot)) {
                disponibles.add(slot);
            }
            slot = slot.plusHours(1);
        }

        return disponibles;
    }

    // ────────────────────────────────────────────────────────��─────────────────
    // Helpers privados
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Verifica que el slot fecha/hora no esté ocupado por otra cita activa.
     *
     * @param excludeId ID de la cita a excluir de la verificación (puede ser null).
     */
    private void validarDisponibilidadHorario(
            LocalDate fecha, LocalTime hora, Long excludeId) {

        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);

        if (ocupado) {
            /*
             * Si el slot está ocupado pero es por la misma cita que queremos
             * reagendar, lo permitimos. Para los demás casos lanzamos excepción.
             */
            if (excludeId != null) {
                Cita existente = citaRepository
                        .findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA)
                        .stream()
                        .filter(c -> c.getFecha().equals(fecha)
                                     && c.getHora().equals(hora)
                                     && c.getId().equals(excludeId))
                        .findFirst()
                        .orElse(null);

                if (existente == null) {
                    // El slot lo ocupa OTRA cita → conflicto real
                    throw new CitaDuplicadaException(fecha, hora);
                }
            } else {
                throw new CitaDuplicadaException(fecha, hora);
            }
        }
    }

    private Cita obtenerCitaOLanzarExcepcion(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    private CitaResponseDTO mapToResponse(Cita cita) {
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

## // src/main/java/com/agenda/citas/controller/CitaController.java

```java
package com.agenda.citas.controller;

import com.agenda.citas.dto.CitaRequestDTO;
import com.agenda.citas.dto.CitaResponseDTO;
import com.agenda.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/v1/citas
    // Crear una nueva cita
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/citas
    // Listar todas las citas
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/{id}
    // Consultar una cita por ID
    // ────���─────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> consultarCitaPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.consultarCitaPorId(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/reagendar
    // Reagendar una cita existente
    // Body: { "fecha": "2026-05-10", "hora": "09:00:00" }
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        LocalDate nuevaFecha = LocalDate.parse(body.get("fecha"));
        LocalTime nuevaHora  = LocalTime.parse(body.get("hora"));

        return ResponseEntity.ok(citaService.reagendarCita(id, nuevaFecha, nuevaHora));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/cancelar
    // Cancelar una cita
    // ──────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/disponibilidad?fecha=2026-05-10
    // Consultar horarios disponibles para una fecha
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 10. Manejo de Excepciones {#excepciones}

### CitaNotFoundException

## // src/main/java/com/agenda/citas/exception/CitaNotFoundException.java

```java
package com.agenda.citas.exception;

public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}
```

### CitaDuplicadaException

## // src/main/java/com/agenda/citas/exception/CitaDuplicadaException.java

```java
package com.agenda.citas.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita activa el " + fecha + " a las " + hora
              + ". Por favor elige otro horario.");
    }
}
```

### GlobalExceptionHandler

## // src/main/java/com/agenda/citas/exception/GlobalExceptionHandler.java

```java
package com.agenda.citas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ───────────────────────────────────────────────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            CitaNotFoundException ex) {

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 ───────────────────────────────────────────────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicada(
            CitaDuplicadaException ex) {

        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400 — Estado inválido ─────────────────────────────────────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex) {

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 422 — Validaciones de Bean ────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        List<String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("errores", errores);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // ── 500 — Genérico ────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ── Helper ────────────────────────────────────────────────────────────────
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

## 11. Clase Principal {#main}

## // src/main/java/com/agenda/citas/CitasApplication.java

```java
package com.agenda.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}
```

---

## 12. Pruebas con Postman {#postman}

### Base URL

http://localhost:8080/api/v1/citas

---

### Crear una cita — POST /api/v1/citas

**Body (JSON):**

```json
{
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-10",
  "hora": "09:00:00",
  "motivo": "Consulta general"
}
```

**Respuesta esperada `201 Created`:**

```json
{
  "id": 1,
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-10",
  "hora": "09:00:00",
  "motivo": "Consulta general",
  "estado": "PENDIENTE"
}
```

---

### Listar todas las citas — GET /api/v1/citas

GET http://localhost:8080/api/v1/citas

---

### Consultar cita por ID — GET /api/v1/citas/{id}

GET http://localhost:8080/api/v1/citas/1

---

### Reagendar una cita — PATCH /api/v1/citas/{id}/reagendar

**Body (JSON):**

```json
{
  "fecha": "2026-05-12",
  "hora": "11:00:00"
}
```

**Respuesta esperada `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-12",
  "hora": "11:00:00",
  "motivo": "Consulta general",
  "estado": "REAGENDADA"
}
```

---

### Cancelar una cita — PATCH /api/v1/citas/{id}/cancelar

PATCH http://localhost:8080/api/v1/citas/1/cancelar

**Respuesta esperada `200 OK`:**

```json
{
  "id": 1,
  "estado": "CANCELADA",
  ...
}
```

---

### Consultar disponibilidad — GET /api/v1/citas/disponibilidad?fecha=

GET http://localhost:8080/api/v1/citas/disponibilidad?fecha=2026-05-10

**Respuesta esperada `200 OK`:**

```json
[
  "08:00:00",
  "10:00:00",
  "11:00:00",
  "12:00:00",
  "13:00:00",
  "14:00:00",
  "15:00:00",
  "16:00:00"
]
```
> El slot `09:00:00` no aparece porque ya está ocupado.

---

## Resumen de Endpoints

| Método   | Endpoint                              | Descripción                            | Status OK |
|----------|---------------------------------------|----------------------------------------|-----------|
| `POST`   | `/api/v1/citas`                       | Crear una cita                         | `201`     |
| `GET`    | `/api/v1/citas`                       | Listar todas las citas                 | `200`     |
| `GET`    | `/api/v1/citas/{id}`                  | Consultar cita por ID                  | `200`     |
| `PATCH`  | `/api/v1/citas/{id}/reagendar`        | Reagendar fecha y hora                 | `200`     |
| `PATCH`  | `/api/v1/citas/{id}/cancelar`         | Cancelar una cita                      | `200`     |
| `GET`    | `/api/v1/citas/disponibilidad?fecha=` | Horarios libres para una fecha         | `200`     |
