# Backend Spring MVC — API REST para Gestión de Reservas de Citas

## Tabla de Contenidos

## 1. [Estructura del Proyecto](estructura)

## 2. [pom.xml](pom)

## 3. [Configuración](configuracion)

## 4. [Entidad](entidad)

## 5. [Repositorio](repositorio)

## 6. [DTOs](dtos)

## 7. [Excepciones](excepciones)

## 8. [Servicio](servicio)

## 9. [Controlador](controlador)

## 10. [Pruebas con Postman](postman)

---

## 1. Estructura del Proyecto {#estructura}

```text
citas-app/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── gestion/
        │           └── citas/
        │               ├── CitasApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
        │               ├── dto/
        │               │   ├── CitaRequestDTO.java
        │               │   ├── CitaResponseDTO.java
        │               │   └── ReagendarRequestDTO.java
        │               ├── entity/
        │               │   └── Cita.java
        │               ├── exception/
        │               │   ├── CitaDuplicadaException.java
        │               │   ├── CitaNotFoundException.java
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
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.gestion</groupId>
    <artifactId>citas-app</artifactId>
    <version>1.0.0</version>
    <name>citas-app</name>
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

        <!-- Validaciones (Bean Validation) -->
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

## 3. Configuración {#configuracion}

## properties name=src/main/resources/application.properties

# ─── Servidor ────────────────────────────────────────────

```properties
server.port=8080

# ─── Base de datos PostgreSQL ────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ─── JPA / Hibernate ─────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ─── Serialización de fechas en ISO-8601 ─────────────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 4. Clase Principal

## java name=src/main/java/com/gestion/citas/CitasApplication.java

```java
package com.gestion.citas;

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

## 5. Entidad {#entidad}

## java name=src/main/java/com/gestion/citas/entity/Cita.java

```java
package com.gestion.citas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* Entidad JPA que representa una cita médica/servicio.
* La restricción UNIQUE sobre (fecha, hora) garantiza a nivel de base de datos
* que no existan citas duplicadas en el mismo horario.

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

    @Column(name = "nombre_cliente", nullable = false, length = 120)
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

    // ─── Estados posibles de una cita ────────────────────
    public enum EstadoCita {
        PENDIENTE,
        CANCELADA,
        COMPLETADA
    }
}
```

---

## 6. Repositorio {#repositorio}

## java name=src/main/java/com/gestion/citas/repository/CitaRepository.java

```java
package com.gestion.citas.repository;

import com.gestion.citas.entity.Cita;
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
     * Verifica si ya existe una cita activa (no cancelada) en la fecha y hora indicadas.
     * Se usa para prevenir duplicados antes de crear o reagendar.
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> com.gestion.citas.entity.Cita.EstadoCita.CANCELADA
            """)
    boolean existeCitaActivaEnFechaYHora(
            @Param("fecha") LocalDate fecha,
            @Param("hora")  LocalTime hora
    );

    /**
     * Igual que el anterior pero excluyendo un ID específico.
     * Útil al reagendar para no comparar la cita consigo misma.
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> com.gestion.citas.entity.Cita.EstadoCita.CANCELADA
              AND c.id    <> :idExcluir
            """)
    boolean existeCitaActivaEnFechaYHoraExcluyendo(
            @Param("fecha")      LocalDate fecha,
            @Param("hora")       LocalTime hora,
            @Param("idExcluir")  Long idExcluir
    );

    /**
     * Devuelve todas las citas activas (no canceladas) de una fecha concreta.
     * Se utiliza para consultar la disponibilidad de horarios.
     */
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fecha  = :fecha
              AND c.estado <> com.gestion.citas.entity.Cita.EstadoCita.CANCELADA
            ORDER BY c.hora
            """)
    List<Cita> findCitasActivasByFecha(@Param("fecha") LocalDate fecha);
}
```

---

## 7. DTOs {#dtos}

### CitaRequestDTO — para crear una cita

## java name=src/main/java/com/gestion/citas/dto/CitaRequestDTO.java

```java
package com.gestion.citas.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* Payload de entrada para crear una nueva cita.

```text
 */
@Data
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 300, message = "El motivo no puede superar 300 caracteres.")
    private String motivo;
}
```

### CitaResponseDTO — respuesta serializada al cliente

## java name=src/main/java/com/gestion/citas/dto/CitaResponseDTO.java

```java
package com.gestion.citas.dto;

import com.gestion.citas.entity.Cita.EstadoCita;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* Representación de una cita que se devuelve al cliente REST.

```text
 */
@Data
@Builder
public class CitaResponseDTO {

    private Long        id;
    private String      nombreCliente;
    private LocalDate   fecha;
    private LocalTime   hora;
    private String      motivo;
    private EstadoCita  estado;
}
```

### ReagendarRequestDTO — para reagendar una cita

## java name=src/main/java/com/gestion/citas/dto/ReagendarRequestDTO.java

```java
package com.gestion.citas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* Payload para modificar la fecha y/u hora de una cita existente.

```text
 */
@Data
public class ReagendarRequestDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;
}
```

---

## 8. Excepciones {#excepciones}

### CitaNotFoundException

## java name=src/main/java/com/gestion/citas/exception/CitaNotFoundException.java

```java
package com.gestion.citas.exception;

public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}
```

### CitaDuplicadaException

## java name=src/main/java/com/gestion/citas/exception/CitaDuplicadaException.java

```java
package com.gestion.citas.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita activa para la fecha "
                + fecha + " a las " + hora
                + ". Por favor elija otro horario.");
    }
}
```

### GlobalExceptionHandler — manejo centralizado de errores

## java name=src/main/java/com/gestion/citas/exception/GlobalExceptionHandler.java

```java
package com.gestion.citas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

```

/**
* Intercepta excepciones lanzadas desde cualquier controlador
* y las convierte en respuestas JSON estructuradas.

```text
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404: cita no encontrada ───────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CitaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409: horario ya ocupado ───────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicada(CitaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400: fallos de validación Bean Validation ─────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult()
          .getFieldErrors()
          .forEach(fe -> errores.put(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status",    HttpStatus.BAD_REQUEST.value());
        body.put("error",     "Validación fallida");
        body.put("detalles",  errores);

        return ResponseEntity.badRequest().body(body);
    }

    // ── 400: estado inválido u otras reglas de negocio ────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 500: cualquier otra excepción no controlada ───────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ─── Helper ──────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("mensaje",   mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
```

---

## 9. Servicio {#servicio}

### Interfaz

## java name=src/main/java/com/gestion/citas/service/CitaService.java

```java
package com.gestion.citas.service;

import com.gestion.citas.dto.CitaRequestDTO;
import com.gestion.citas.dto.CitaResponseDTO;
import com.gestion.citas.dto.ReagendarRequestDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    CitaResponseDTO        crearCita(CitaRequestDTO request);
    List<CitaResponseDTO>  listarCitas();
    CitaResponseDTO        obtenerCitaPorId(Long id);
    CitaResponseDTO        reagendarCita(Long id, ReagendarRequestDTO request);
    CitaResponseDTO        cancelarCita(Long id);
    List<LocalTime>        consultarDisponibilidad(LocalDate fecha);
}
```

### Implementación

## java name=src/main/java/com/gestion/citas/service/CitaServiceImpl.java

```java
package com.gestion.citas.service;

import com.gestion.citas.dto.CitaRequestDTO;
import com.gestion.citas.dto.CitaResponseDTO;
import com.gestion.citas.dto.ReagendarRequestDTO;
import com.gestion.citas.entity.Cita;
import com.gestion.citas.entity.Cita.EstadoCita;
import com.gestion.citas.exception.CitaDuplicadaException;
import com.gestion.citas.exception.CitaNotFoundException;
import com.gestion.citas.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    /**
     * Horario de atención: cada 30 minutos de 08:00 a 17:30.
     * Se genera de forma programática para facilitar el mantenimiento.
     */
    private static final List<LocalTime> HORARIOS_DISPONIBLES =
            Stream.iterate(LocalTime.of(8, 0), t -> t.isBefore(LocalTime.of(18, 0)), t -> t.plusMinutes(30))
                  .toList();

    // ── Crear cita ────────────────────────────────────────
    @Override
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        validarDisponibilidadHorario(request.getFecha(), request.getHora());

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PENDIENTE)
                .build();

        return toDTO(citaRepository.save(cita));
    }

    // ── Listar todas las citas ────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Consultar cita por ID ─────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarOLanzar(id));
    }

    // ── Reagendar cita ────────────────────────────────────
    @Override
    public CitaResponseDTO reagendarCita(Long id, ReagendarRequestDTO request) {

        Cita cita = buscarOLanzar(id);

        // Solo se pueden reagendar citas en estado PENDIENTE
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No se puede reagendar una cita cancelada (ID: " + id + ").");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new IllegalStateException(
                    "No se puede reagendar una cita completada (ID: " + id + ").");
        }

        // Verificar duplicado excluyendo la propia cita
        if (citaRepository.existeCitaActivaEnFechaYHoraExcluyendo(
                request.getNuevaFecha(), request.getNuevaHora(), id)) {
            throw new CitaDuplicadaException(request.getNuevaFecha(), request.getNuevaHora());
        }

        cita.setFecha(request.getNuevaFecha());
        cita.setHora(request.getNuevaHora());

        return toDTO(citaRepository.save(cita));
    }

    // ── Cancelar cita ─────────────────────────────────────
    @Override
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarOLanzar(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita ya se encuentra cancelada (ID: " + id + ").");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ── Disponibilidad de horarios para una fecha ─────────
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        // Horas ya ocupadas (citas activas)
        Set<LocalTime> horasOcupadas = citaRepository.findCitasActivasByFecha(fecha)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toSet());

        // Devuelve solo los horarios que no estén ocupados
        return HORARIOS_DISPONIBLES.stream()
                .filter(h -> !horasOcupadas.contains(h))
                .toList();
    }

    // ─── Helpers privados ─────────────────────────────────

    private Cita buscarOLanzar(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    private void validarDisponibilidadHorario(LocalDate fecha, LocalTime hora) {
        if (citaRepository.existeCitaActivaEnFechaYHora(fecha, hora)) {
            throw new CitaDuplicadaException(fecha, hora);
        }
    }

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

## 10. Controlador {#controlador}

## java name=src/main/java/com/gestion/citas/controller/CitaController.java

```java
package com.gestion.citas.controller;

import com.gestion.citas.dto.CitaRequestDTO;
import com.gestion.citas.dto.CitaResponseDTO;
import com.gestion.citas.dto.ReagendarRequestDTO;
import com.gestion.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

```

/**
* Controlador REST que expone los endpoints de la API de gestión de citas.
* Base URL: /api/v1/citas

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
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO creada = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
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
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PATCH /api/v1/citas/{id}/reagendar
     * Modifica la fecha y/u hora de una cita existente.
     */
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarRequestDTO request) {

        return ResponseEntity.ok(citaService.reagendarCita(id, request));
    }

    /**
     * PATCH /api/v1/citas/{id}/cancelar
     * Cancela una cita; no la elimina de la base de datos.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable Long id) {

        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD
     * Retorna los horarios libres para una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 11. Pruebas con Postman {#postman}

A continuación se muestra cada endpoint con un ejemplo de petición y respuesta esperada.

### Crear una cita — POST /api/v1/citas

**Body (JSON):**

```json
{
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-20",
  "hora": "10:00:00",
  "motivo": "Consulta general"
}
```

**Respuesta `201 Created`:**

```json
{
  "id": 1,
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-20",
  "hora": "10:00:00",
  "motivo": "Consulta general",
  "estado": "PENDIENTE"
}
```

---

### Listar todas las citas — GET /api/v1/citas

**Respuesta `200 OK`:**

```json
[
  {
    "id": 1,
    "nombreCliente": "Laura Gómez",
    "fecha": "2026-05-20",
    "hora": "10:00:00",
    "motivo": "Consulta general",
    "estado": "PENDIENTE"
  }
]
```

---

### Consultar cita por ID — GET /api/v1/citas/1

**Respuesta `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-20",
  "hora": "10:00:00",
  "motivo": "Consulta general",
  "estado": "PENDIENTE"
}
```

**Respuesta `404 Not Found` si no existe:**

```json
{
  "timestamp": "2026-05-03T14:30:00",
  "status": 404,
  "error": "Not Found",
  "mensaje": "No se encontró ninguna cita con el ID: 99"
}
```

---

### Reagendar cita — PATCH /api/v1/citas/1/reagendar

**Body (JSON):**

```json
{
  "nuevaFecha": "2026-05-22",
  "nuevaHora": "11:30:00"
}
```

**Respuesta `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-22",
  "hora": "11:30:00",
  "motivo": "Consulta general",
  "estado": "PENDIENTE"
}
```

**Respuesta `409 Conflict` si el horario está ocupado:**

```json
{
  "timestamp": "2026-05-03T14:35:00",
  "status": 409,
  "error": "Conflict",
  "mensaje": "Ya existe una cita activa para la fecha 2026-05-22 a las 11:

```

## 30. Por favor elija otro horario."

```json
}
```

---

### Cancelar cita — PATCH /api/v1/citas/1/cancelar

**Respuesta `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-22",
  "hora": "11:30:00",
  "motivo": "Consulta general",
  "estado": "CANCELADA"
}
```

---

### Consultar disponibilidad — GET /api/v1/citas/disponibilidad?fecha=2026-05-20

## Respuesta 200 OK *(fragmento — devuelve los horarios libres del día)*:

```json
[
  "08:00:00",
  "08:30:00",
  "09:00:00",
  "09:30:00",
  "10:30:00",
  "11:00:00",
  "..."
]
```
> El horario `10:00` no aparece porque ya tiene una cita activa.

---

## Resumen de Endpoints

| Método   | URL                                          | Descripción                               |
|----------|----------------------------------------------|-------------------------------------------|
| `POST`   | `/api/v1/citas`                              | Crear una cita                            |
| `GET`    | `/api/v1/citas`                              | Listar todas las citas                    |
| `GET`    | `/api/v1/citas/{id}`                         | Consultar cita por ID                     |
| `PATCH`  | `/api/v1/citas/{id}/reagendar`               | Reagendar una cita existente              |
| `PATCH`  | `/api/v1/citas/{id}/cancelar`                | Cancelar una cita                         |
| `GET`    | `/api/v1/citas/disponibilidad?fecha=YYYY-MM-DD` | Ver horarios libres para una fecha     |

---

## Notas de Diseño

- **Sin duplicados:** la validación se aplica en dos capas: a nivel de **servicio** (consulta JPQL antes de insertar) y a nivel de **base de datos** (`UNIQUE CONSTRAINT` sobre `fecha + hora`), lo que asegura integridad incluso ante peticiones concurrentes.
- **Cancelación suave:** las citas canceladas permanecen en la base de datos con `estado = CANCELADA`, lo que libera el horario para nuevas reservas y mantiene el historial.
- **Disponibilidad dinámica:** el horario de atención (08:00–17:30 cada 30 min) se genera en memoria y se filtra contra las citas activas del día, evitando tablas adicionales.
- **Manejo de errores centralizado:** `@RestControllerAdvice` garantiza respuestas JSON consistentes para cualquier tipo de error, facilitando el consumo desde Postman o cualquier cliente.
