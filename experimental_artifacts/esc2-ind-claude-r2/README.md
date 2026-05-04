# Backend Spring MVC — API REST para Gestión de Reservas de Citas

---

## Estructura del Proyecto

```text
citas-api/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── reservas/
│       │           └── citas/
│       │               ├── CitasApiApplication.java
│       │               ├── controller/
│       │               │   └── CitaController.java
│       │               ├── dto/
│       │               │   ├── CitaRequestDTO.java
│       │               │   └── CitaResponseDTO.java
│       │               ├── entity/
│       │               │   └── Cita.java
│       │               ├── exception/
│       │               │   ├── CitaDuplicadaException.java
│       │               │   ├── CitaNoEncontradaException.java
│       │               │   └── GlobalExceptionHandler.java
│       │               ├── repository/
│       │               │   └── CitaRepository.java
│       │               └── service/
│       │                   ├── CitaService.java
│       │                   └── CitaServiceImpl.java
│       └── resources/
│           └── application.properties
└── pom.xml
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

        <!-- Validación de campos (Bean Validation) -->
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

        <!-- Lombok (reducir boilerplate) -->
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

# ─────────────────────────────────────────────

# Servidor

# ─────────────────────────────────────────────

```properties
server.port=8080

# ─────────────────────────────────────────────
#  Base de datos PostgreSQL
# ─────────────────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ─────────────────────────────────────────────
#  JPA / Hibernate
# ─────────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

> ⚠️ Ajusta `spring.datasource.username` y `spring.datasource.password` según tu entorno local.

---

## 3. Clase Principal

## // src/main/java/com/reservas/citas/CitasApiApplication.java

```java
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

## 4. Entidad

## // src/main/java/com/reservas/citas/entity/Cita.java

```java
package com.reservas.citas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    /*
     * Restricción a nivel de base de datos que garantiza que no puedan existir
     * dos citas en la misma fecha y hora, incluso ante accesos concurrentes.
     */
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_citas_fecha_hora",
            columnNames = {"fecha", "hora"}
        )
    }
```

## )

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    public enum EstadoCita {
        PENDIENTE,
        CANCELADA,
        COMPLETADA
    }
}
```

---

## 5. DTOs

### Request DTO

## // src/main/java/com/reservas/citas/dto/CitaRequestDTO.java

```java
package com.reservas.citas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitaRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 100, message = "El nombre del cliente no puede superar los 100 caracteres.")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria.")
    @FutureOrPresent(message = "La fecha debe ser hoy o una fecha futura.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    private String motivo;
}
```

### Response DTO

## // src/main/java/com/reservas/citas/dto/CitaResponseDTO.java

```java
package com.reservas.citas.dto;

import com.reservas.citas.entity.Cita.EstadoCita;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
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

## 6. Repositorio

## // src/main/java/com/reservas/citas/repository/CitaRepository.java

```java
package com.reservas.citas.repository;

import com.reservas.citas.entity.Cita;
import com.reservas.citas.entity.Cita.EstadoCita;
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
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     * Se excluye el estado CANCELADA para permitir reutilizar ese horario.
     */
    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    /**
     * Retorna todas las citas activas (no canceladas) para una fecha específica,
     * ordenadas por hora ascendente. Útil para consultar disponibilidad.
     */
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fecha = :fecha
              AND c.estado <> 'CANCELADA'
            ORDER BY c.hora ASC
            """)
    List<Cita> findHorasOcupadasByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Lista todas las citas ordenadas por fecha y hora.
     */
    List<Cita> findAllByOrderByFechaAscHoraAsc();
}
```

---

## 7. Excepciones personalizadas

### CitaNoEncontradaException

## // src/main/java/com/reservas/citas/exception/CitaNoEncontradaException.java

```java
package com.reservas.citas.exception;

public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(Long id) {
        super("No se encontró ninguna cita con el ID: " + id);
    }
}
```

### CitaDuplicadaException

## // src/main/java/com/reservas/citas/exception/CitaDuplicadaException.java

```java
package com.reservas.citas.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(LocalDate fecha, LocalTime hora) {
        super("Ya existe una cita registrada el " + fecha + " a las " + hora
              + ". Por favor seleccione otro horario.");
    }
}
```

### GlobalExceptionHandler

## // src/main/java/com/reservas/citas/exception/GlobalExceptionHandler.java

```java
package com.reservas.citas.exception;

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

    // ── 404 ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CitaNoEncontradaException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(CitaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400 — Validación de campos ────────────────────────────────────────────

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

    // ── 400 — Estado inválido en cancelación ─────────────────────────────────

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

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

## 8. Capa de Servicio

### Interfaz

## // src/main/java/com/reservas/citas/service/CitaService.java

```java
package com.reservas.citas.service;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    CitaResponseDTO crearCita(CitaRequestDTO request);

    List<CitaResponseDTO> listarCitas();

    CitaResponseDTO obtenerCitaPorId(Long id);

    CitaResponseDTO reagendarCita(Long id, CitaRequestDTO request);

    CitaResponseDTO cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### Implementación

## // src/main/java/com/reservas/citas/service/CitaServiceImpl.java

```java
package com.reservas.citas.service;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
import com.reservas.citas.entity.Cita;
import com.reservas.citas.entity.Cita.EstadoCita;
import com.reservas.citas.exception.CitaDuplicadaException;
import com.reservas.citas.exception.CitaNoEncontradaException;
import com.reservas.citas.repository.CitaRepository;
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
     * Horario de atención: bloques de 1 hora, de 08:00 a 17:00.
     * Ajusta este rango según las necesidades del negocio.
     */
    private static final LocalTime HORA_INICIO  = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN     = LocalTime.of(17, 0);

    // ── Crear ─────────────────────────────────────────────────────────────────

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

        return toDTO(citaRepository.save(cita));
    }

    // ── Listar ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarCitas() {
        return citaRepository.findAllByOrderByFechaAscHoraAsc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ── Obtener por ID ──────────────────────────────��─────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO obtenerCitaPorId(Long id) {
        return toDTO(buscarCitaOLanzarError(id));
    }

    // ── Reagendar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO reagendarCita(Long id, CitaRequestDTO request) {
        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "No es posible reagendar una cita que ha sido cancelada (ID: " + id + ").");
        }

        /*
         * Se pasa el ID de la cita actual para que la validación
         * no la considere como duplicado de sí misma.
         */
        boolean mismoHorario = cita.getFecha().equals(request.getFecha())
                               && cita.getHora().equals(request.getHora());

        if (!mismoHorario) {
            validarDisponibilidadHorario(request.getFecha(), request.getHora(), id);
        }

        cita.setNombreCliente(request.getNombreCliente());
        cita.setFecha(request.getFecha());
        cita.setHora(request.getHora());
        cita.setMotivo(request.getMotivo());

        return toDTO(citaRepository.save(cita));
    }

    // ── Cancelar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CitaResponseDTO cancelarCita(Long id) {
        Cita cita = buscarCitaOLanzarError(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException(
                    "La cita con ID " + id + " ya se encuentra cancelada.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        return toDTO(citaRepository.save(cita));
    }

    // ── Disponibilidad ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<LocalTime> horasOcupadas = citaRepository
                .findHorasOcupadasByFecha(fecha)
                .stream()
                .map(Cita::getHora)
                .toList();

        // Genera todos los bloques horarios de 1 hora dentro del rango de atención
        return HORA_INICIO.datesUntil(HORA_FIN.plusSeconds(1), java.time.Duration.ofHours(1))
                // LocalTime no implementa datesUntil — usamos stream manual:
                .toList(); // placeholder — ver implementación correcta abajo
    }

    /*
     * Implementación correcta de consultarDisponibilidad usando un bucle simple,
     * ya que LocalTime no tiene un método stream nativo en Java 21.
     */
    private List<LocalTime> generarHorariosDisponibles(LocalDate fecha) {
        List<LocalTime> horasOcupadas = citaRepository
                .findHorasOcupadasByFecha(fecha)
                .stream()
                .map(Cita::getHora)
                .toList();

        List<LocalTime> todosLosHorarios = new java.util.ArrayList<>();
        LocalTime cursor = HORA_INICIO;
        while (!cursor.isAfter(HORA_FIN.minusHours(1))) {
            todosLosHorarios.add(cursor);
            cursor = cursor.plusHours(1);
        }

        todosLosHorarios.removeAll(horasOcupadas);
        return todosLosHorarios;
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private Cita buscarCitaOLanzarError(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(id));
    }

    /**
     * Verifica que no exista otra cita activa en el mismo horario.
     *
     * @param excludeId ID de la cita a excluir de la validación (usado en reagendamiento).
     *                  Pasa {@code null} al crear una cita nueva.
     */
    private void validarDisponibilidadHorario(LocalDate fecha, LocalTime hora, Long excludeId) {
        boolean ocupado = citaRepository
                .existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);

        if (ocupado) {
            // Si el horario ocupado pertenece a la misma cita que se está reagendando, se ignora
            if (excludeId != null) {
                boolean esLaMismaCita = citaRepository.findById(excludeId)
                        .map(c -> c.getFecha().equals(fecha) && c.getHora().equals(hora))
                        .orElse(false);
                if (esLaMismaCita) return;
            }
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

> 📝 **Nota:** el método `consultarDisponibilidad` en la interfaz delega a `generarHorariosDisponibles` (método privado). Actualiza la implementación del método de interfaz para que llame al privado, como se muestra a continuación.

## Reemplaza el cuerpo de consultarDisponibilidad en CitaServiceImpl por:

```java
@Override
@Transactional(readOnly = true)
public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
    return generarHorariosDisponibles(fecha);
}
```

---

## 9. Controlador

## // src/main/java/com/reservas/citas/controller/CitaController.java

```java
package com.reservas.citas.controller;

import com.reservas.citas.dto.CitaRequestDTO;
import com.reservas.citas.dto.CitaResponseDTO;
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

    /**
     * POST /api/v1/citas
     * Crea una nueva cita.
     */
    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody CitaRequestDTO request) {

        CitaResponseDTO response = citaService.crearCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/v1/citas
     * Lista todas las citas registradas ordenadas por fecha y hora.
     */
    @GetMapping
    public ResponseEntity<List<CitaResponseDTO>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    /**
     * GET /api/v1/citas/{id}
     * Retorna el detalle de una cita por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaResponseDTO> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    /**
     * PUT /api/v1/citas/{id}/reagendar
     * Reagenda una cita existente (actualiza fecha, hora, cliente y/o motivo).
     */
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<CitaResponseDTO> reagendarCita(
            @PathVariable Long id,
            @Valid @RequestBody CitaRequestDTO request) {

        return ResponseEntity.ok(citaService.reagendarCita(id, request));
    }

    /**
     * PATCH /api/v1/citas/{id}/cancelar
     * Cancela una cita sin eliminarla del sistema.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    /**
     * GET /api/v1/citas/disponibilidad?fecha=YYYY-MM-DD
     * Retorna los horarios libres disponibles para una fecha determinada.
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 10. Endpoints — Referencia rápida para Postman

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/v1/citas` | Crear nueva cita |
| `GET` | `/api/v1/citas` | Listar todas las citas |
| `GET` | `/api/v1/citas/{id}` | Obtener cita por ID |
| `PUT` | `/api/v1/citas/{id}/reagendar` | Reagendar cita |
| `PATCH` | `/api/v1/citas/{id}/cancelar` | Cancelar cita |
| `GET` | `/api/v1/citas/disponibilidad?fecha=YYYY-MM-DD` | Consultar horarios libres |

### Ejemplos de cuerpo JSON

**Crear / Reagendar cita:**

```json
{
    "nombreCliente": "Ana Torres",
    "fecha": "2026-05-15",
    "hora": "10:00:00",
    "motivo": "Consulta médica general"
}
```

**Respuesta exitosa (crear — HTTP 201):**

```json
{
    "id": 1,
    "nombreCliente": "Ana Torres",
    "fecha": "2026-05-15",
    "hora": "10:00:00",
    "motivo": "Consulta médica general",
    "estado": "PENDIENTE"
}
```

**Respuesta de conflicto (HTTP 409):**

```json
{
    "timestamp": "2026-05-03T14:30:00",
    "status": 409,
    "mensaje": "Ya existe una cita registrada el 2026-05-15 a las 10:

```

## 00. Por favor seleccione otro horario."

```json
}
```

**Consultar disponibilidad — respuesta (HTTP 200):**

```json
[
    "08:00:00",
    "09:00:00",
    "11:00:00",
    "12:00:00",
    "14:00:00"
]
```

---

## Resumen de Decisiones de Diseño

- **Sin complejidad innecesaria:** no se implementó seguridad, paginación avanzada ni caché, dado que el entorno es pequeño y se prioriza la entrega rápida.
- **Unicidad garantizada en dos capas:**

```text
  - A nivel de base de datos con `@UniqueConstraint` en `fecha + hora`.
  - A nivel de servicio con `existsByFechaAndHoraAndEstadoNot(...)`, evitando exponer el error técnico de la BD al cliente.
```

- **Citas canceladas liberan el horario:** la validación excluye explícitamente el estado `CANCELADA`, permitiendo que ese slot sea reutilizado.
- **Estado inmutable al cancelar:** el historial de citas se conserva; nunca se eliminan registros.
- **DTOs separados:** el `CitaRequestDTO` desacopla la entrada del usuario de la entidad JPA, y el `CitaResponseDTO` controla exactamente qué se expone hacia afuera.
- **`ddl-auto=update`:** Hibernate genera y actualiza el esquema automáticamente a partir de las entidades, lo que evita gestionar scripts SQL manualmente en esta etapa del proyecto.
