# Backend Spring MVC — API REST para Gestión de Reservas de Citas

---

## 📁 Estructura de Paquetes

```text
src/main/java/com/reservas/citas/
├── controller/
│   └── CitaController.java
├── model/
│   └── entity/
│       └── Cita.java
├── repository/
│   └── CitaRepository.java
├── service/
│   └── CitaService.java
└── service/
    └── impl/
        └── CitaServiceImpl.java
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
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.reservas</groupId>
    <artifactId>citas</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>citas</name>
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

        <!-- Validaciones (Bean Validation / Jakarta) -->
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

## 2. application.properties

## properties name=src/main/resources/application.properties

# ─── Servidor ─────────────────────────────────────────────

```properties
server.port=8080

# ─── Base de datos PostgreSQL ─────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ─── JPA / Hibernate ──────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# ─── Serialización de fechas (ISO 8601) ───────────────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

---

## 3. Clase Principal

## java name=src/main/java/com/reservas/citas/CitasApplication.java

```java
package com.reservas.citas;

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

## 4. Entidad — model/entity/Cita.java

## java name=src/main/java/com/reservas/citas/model/entity/Cita.java

```java
package com.reservas.citas.model.entity;

import com.reservas.citas.model.enums.EstadoCita;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    // Restricción de unicidad compuesta a nivel de base de datos
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
    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior al día de hoy.")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria.")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Column(nullable = false, length = 300)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;
}
```

---

## 5. Enumerado — model/enums/EstadoCita.java

## java name=src/main/java/com/reservas/citas/model/enums/EstadoCita.java

```java
package com.reservas.citas.model.enums;

public enum EstadoCita {
    PROGRAMADA,
    REAGENDADA,
    CANCELADA
}
```

---

## 6. DTOs

### CitaRequestDTO.java

## java name=src/main/java/com/reservas/citas/model/dto/CitaRequestDTO.java

```java
package com.reservas.citas.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String nombreCliente;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior al día de hoy.")
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    private String motivo;
}
```

### ReagendarDTO.java

## java name=src/main/java/com/reservas/citas/model/dto/ReagendarDTO.java

```java
package com.reservas.citas.model.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagendarDTO {

    @NotNull(message = "La nueva fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior al día de hoy.")
    private LocalDate nuevaFecha;

    @NotNull(message = "La nueva hora es obligatoria.")
    private LocalTime nuevaHora;
}
```

### CitaResponseDTO.java

## java name=src/main/java/com/reservas/citas/model/dto/CitaResponseDTO.java

```java
package com.reservas.citas.model.dto;

import com.reservas.citas.model.enums.EstadoCita;
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

## 7. Excepciones Personalizadas

### CitaNotFoundException.java

## java name=src/main/java/com/reservas/citas/exception/CitaNotFoundException.java

```java
package com.reservas.citas.exception;

public class CitaNotFoundException extends RuntimeException {

    public CitaNotFoundException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}
```

### CitaDuplicadaException.java

## java name=src/main/java/com/reservas/citas/exception/CitaDuplicadaException.java

```java
package com.reservas.citas.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita registrada para la fecha "
              + fecha + " a las " + hora + ".");
    }
}
```

### CitaCanceladaException.java

## java name=src/main/java/com/reservas/citas/exception/CitaCanceladaException.java

```java
package com.reservas.citas.exception;

public class CitaCanceladaException extends RuntimeException {

    public CitaCanceladaException(Long id) {
        super("La cita con ID " + id + " ya está cancelada y no puede modificarse.");
    }
}
```

### GlobalExceptionHandler.java

## java name=src/main/java/com/reservas/citas/exception/GlobalExceptionHandler.java

```java
package com.reservas.citas.exception;

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

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 ──────────────────────────────────────────────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CitaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 Duplicado ────────────────────────────────────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicated(CitaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 409 Cancelada ────────────────────────────────────────────────────────
    @ExceptionHandler(CitaCanceladaException.class)
    public ResponseEntity<Map<String, Object>> handleCancelled(CitaCanceladaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400 Validaciones Bean Validation ─────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validación de datos fallida");
        body.put("detalles", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── 500 Genérico ─────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", message);
        return ResponseEntity.status(status).body(body);
    }
}
```

---

## 8. Repository — CitaRepository.java

## java name=src/main/java/com/reservas/citas/repository/CitaRepository.java

```java
package com.reservas.citas.repository;

import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.enums.EstadoCita;
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
     * Verifica si ya existe una cita en esa fecha/hora (excluyendo un ID en caso
     * de reagendado: si el ID es null, no se excluye ningún registro).
     */
    @Query("""
           SELECT COUNT(c) > 0
           FROM Cita c
           WHERE c.fecha = :fecha
             AND c.hora  = :hora
             AND (:excludeId IS NULL OR c.id <> :excludeId)
             AND c.estado <> 'CANCELADA'
           """)
    boolean existsByFechaAndHoraExcludingId(
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora,
            @Param("excludeId") Long excludeId);

    /**
     * Lista todas las citas de una fecha determinada que NO estén canceladas,
     * ordenadas por hora ascendente.
     */
    List<Cita> findByFechaAndEstadoNotOrderByHoraAsc(LocalDate fecha, EstadoCita estado);

    /**
     * Retorna todas las horas ocupadas (activas) para una fecha dada.
     */
    @Query("""
           SELECT c.hora
           FROM Cita c
           WHERE c.fecha = :fecha
             AND c.estado <> 'CANCELADA'
           ORDER BY c.hora ASC
           """)
    List<LocalTime> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);
}
```

---

## 9. Service (Interfaz) — CitaService.java

## java name=src/main/java/com/reservas/citas/service/CitaService.java

```java
package com.reservas.citas.service;

import com.reservas.citas.model.dto.CitaRequestDTO;
import com.reservas.citas.model.dto.CitaResponseDTO;
import com.reservas.citas.model.dto.ReagendarDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    /** Crea una nueva cita y la persiste en estado PROGRAMADA. */
    CitaResponseDTO crearCita(CitaRequestDTO request);

    /** Retorna todas las citas registradas. */
    List<CitaResponseDTO> listarCitas();

    /** Busca una cita por su ID. */
    CitaResponseDTO obtenerCitaPorId(Long id);

    /** Reagenda una cita existente con nueva fecha y hora. */
    CitaResponseDTO reagendarCita(Long id, ReagendarDTO dto);

    /** Cancela una cita existente. */
    CitaResponseDTO cancelarCita(Long id);

    /**
     * Consulta las horas disponibles para una fecha dada, dentro de un rango
     * de atención (09:00 – 17:00) con intervalos de 30 minutos.
     */
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

---

## 10. Service (Implementación) — CitaServiceImpl.java

## java name=src/main/java/com/reservas/citas/service/impl/CitaServiceImpl.java

```java
package com.reservas.citas.service.impl;

import com.reservas.citas.exception.CitaCanceladaException;
import com.reservas.citas.exception.CitaDuplicadaException;
import com.reservas.citas.exception.CitaNotFoundException;
import com.reservas.citas.model.dto.CitaRequestDTO;
import com.reservas.citas.model.dto.CitaResponseDTO;
import com.reservas.citas.model.dto.ReagendarDTO;
import com.reservas.citas.model.entity.Cita;
import com.reservas.citas.model.enums.EstadoCita;
import com.reservas.citas.repository.CitaRepository;
import com.reservas.citas.service.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private static final LocalTime HORA_INICIO  = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 0);
    private static final int       INTERVALO_MIN = 30;

    private final CitaRepository citaRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Crear
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO crearCita(CitaRequestDTO request) {

        validarDisponibilidadHorario(request.getFecha(), request.getHora(), null);

        Cita cita = Cita.builder()
                .nombreCliente(request.getNombreCliente())
                .fecha(request.getFecha())
                .hora(request.getHora())
                .motivo(request.getMotivo())
                .estado(EstadoCita.PROGRAMADA)
                .build();

        return toDTO(citaRepository.save(cita));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Listar
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Obtener por ID
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarCitaOLanzarExcepcion(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reagendar
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, ReagendarDTO dto) {

        Cita cita = buscarCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new CitaCanceladaException(id);
        }

        validarDisponibilidadHorario(dto.getNuevaFecha(), dto.getNuevaHora(), id);

        cita.setFecha(dto.getNuevaFecha());
        cita.setHora(dto.getNuevaHora());
        cita.setEstado(EstadoCita.REAGENDADA);

        return toDTO(citaRepository.save(cita));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Cancelar
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {

        Cita cita = buscarCitaOLanzarExcepcion(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new CitaCanceladaException(id);
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Disponibilidad
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {

        List<LocalTime> horasOcupadas = citaRepository.findHorasOcupadasByFecha(fecha);
        List<LocalTime> disponibles   = new ArrayList<>();

        LocalTime slot = HORA_INICIO;
        while (!slot.isAfter(HORA_FIN.minusMinutes(INTERVALO_MIN))) {
            if (!horasOcupadas.contains(slot)) {
                disponibles.add(slot);
            }
            slot = slot.plusMinutes(INTERVALO_MIN);
        }

        return disponibles;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────────────────────────────────

    private Cita buscarCitaOLanzarExcepcion(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNotFoundException(id));
    }

    /**
     * Verifica que el slot fecha/hora no esté ocupado por otra cita activa.
     *
     * @param excludeId ID de la cita que se está reagendando (null si es nueva).
     */
    private void validarDisponibilidadHorario(LocalDate fecha,
                                               LocalTime hora,
                                               Long excludeId) {
        boolean ocupado = citaRepository
                .existsByFechaAndHoraExcludingId(fecha, hora, excludeId);
        if (ocupado) {
            throw new CitaDuplicadaException(fecha, hora);
        }
    }

    /** Convierte una entidad Cita en su DTO de respuesta. */
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

## 11. Controller — CitaController.java

## java name=src/main/java/com/reservas/citas/controller/CitaController.java

```java
package com.reservas.citas.controller;

import com.reservas.citas.model.dto.CitaRequestDTO;
import com.reservas.citas.model.dto.CitaResponseDTO;
import com.reservas.citas.model.dto.ReagendarDTO;
import com.reservas.citas.service.CitaService;
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
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ── POST /api/v1/citas ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── GET /api/v1/citas ─────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    // ── GET /api/v1/citas/{id} ────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    // ── PUT /api/v1/citas/{id}/reagendar ──────────────────────────────────────
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody ReagendarDTO dto) {

        return ResponseEntity.ok(citaService.reagendarCita(id, dto));
    }

    // ── PATCH /api/v1/citas/{id}/cancelar ────────────────────────────────────
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    // ── GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD ────────────────────
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 12. Resumen de Endpoints para Postman

| Método | URL | Body / Params | Descripción |
|--------|-----|---------------|-------------|
| `POST` | `/api/v1/citas` | JSON `CitaRequestDTO` | Crear una cita |
| `GET` | `/api/v1/citas` | — | Listar todas las citas |
| `GET` | `/api/v1/citas/{id}` | — | Consultar cita por ID |
| `PUT` | `/api/v1/citas/{id}/reagendar` | JSON `ReagendarDTO` | Reagendar cita |
| `PATCH` | `/api/v1/citas/{id}/cancelar` | — | Cancelar cita |
| `GET` | `/api/v1/citas/disponibilidad?fecha=2026-05-10` | Query param `fecha` | Ver horas disponibles |

---

### Ejemplo de body para POST /api/v1/citas

## json name=crear-cita.json

```json
{
  "nombreCliente": "Laura Gómez",
  "fecha": "2026-05-10",
  "hora": "10:30:00",
  "motivo": "Consulta general"
}
```

### Ejemplo de body para PUT /api/v1/citas/{id}/reagendar

## json name=reagendar-cita.json

```json
{
  "nuevaFecha": "2026-05-15",
  "nuevaHora": "14:00:00"
}
```

---

## 📝 Notas de Diseño

- **Unicidad:** la validación de duplicados se realiza en dos capas: a nivel de servicio con la query `existsByFechaAndHoraExcludingId` y a nivel de base de datos con la `@UniqueConstraint`, garantizando consistencia incluso bajo concurrencia.
- **Reagendado:** al reagendar, se excluye el propio ID de la cita del chequeo de duplicados, permitiendo confirmar la misma hora solo si la cita original ya estaba en ese slot.
- **Disponibilidad:** se generan slots de 30 minutos entre las 09:00 y las 16:30; cualquier hora que ya tenga una cita activa (no cancelada) es eliminada de la lista de disponibles.
- **Mapeo manual (sin MapStruct):** se usa un método `toDTO()` privado en el servicio para mantener la dependencia al mínimo.
- **Transacciones:** las operaciones de escritura están anotadas con `@Transactional` y las de solo lectura con `@Transactional(readOnly = true)` para optimizar el uso de conexiones.
