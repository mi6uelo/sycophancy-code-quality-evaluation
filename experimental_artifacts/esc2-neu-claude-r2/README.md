# Backend Spring MVC — API REST para Gestión de Reservas de Citas

## Tabla de Contenidos

## 1. [Estructura del Proyecto](#estructura)

## 2. [Dependencias Maven (pom.xml)](#pom)

## 3. [Configuración (application.properties)](#config)

## 4. [Entidad (model/entity)](#entity)

## 5. [Repositorio (repository)](#repository)

## 6. [Servicio — Interfaz (service)](#service)

## 7. [Servicio — Implementación (service/impl)](#serviceimpl)

## 8. [Controlador (controller)](#controller)

## 9. [Manejo Global de Errores](#errors)

## 10. [Clase Principal](#main)

## 11. [Pruebas con Postman](#postman)

---

## 1. Estructura del Proyecto {#estructura}

```text
citas-backend/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── gestion/
        │           └── citas/
        │               ├── CitasBackendApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
        │               ├── exception/
        │               │   ├── CitaDuplicadaException.java
        │               │   ├── CitaNoEncontradaException.java
        │               │   ├── EstadoInvalidoException.java
        │               │   └── GlobalExceptionHandler.java
        │               ├── model/
        │               │   └── entity/
        │               │       └── Cita.java
        │               ├── repository/
        │               │   └── CitaRepository.java
        │               └── service/
        │                   ├── CitaService.java
        │                   └── impl/
        │                       └── CitaServiceImpl.java
        └── resources/
            └── application.properties
```

---

## 2. Dependencias Maven (pom.xml) {#pom}

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

    <groupId>com.gestion</groupId>
    <artifactId>citas-backend</artifactId>
    <version>1.0.0</version>
    <name>citas-backend</name>
    <description>API REST para Gestión de Reservas de Citas</description>

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

        <!-- Validaciones Bean Validation (Jakarta) -->
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

## 3. Configuración (application.properties) {#config}

## properties name=src/main/resources/application.properties

# ─── Servidor ───────────────────────────────────────────────

```properties
server.port=8080

# ─── Base de Datos PostgreSQL ────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ─── JPA / Hibernate ─────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# ─── Formato fechas en JSON ───────────────────────────────────
spring.jackson.date-format=yyyy-MM-dd
spring.jackson.time-zone=America/Bogota
```

> ⚠️ **Nota:** Ajusta `spring.datasource.username` y `spring.datasource.password` según tu entorno local.

---

## 4. Entidad (model/entity) {#entity}

## java name=src/main/java/com/gestion/citas/model/entity/Cita.java

```java
package com.gestion.citas.model.entity;

import com.gestion.citas.model.entity.enums.EstadoCita;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

```

/**
* Entidad que representa una reserva de cita médica / servicio.
* La combinación (fecha + hora) debe ser única para evitar duplicados.

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
@ToString
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(min = 3, max = 120, message = "El nombre debe tener entre 3 y 120 caracteres.")
    @Column(name = "nombre_cliente", nullable = false, length = 120)
    private String nombreCliente;

    @NotNull(message = "La fecha de la cita es obligatoria.")
    @FutureOrPresent(message = "La fecha no puede ser anterior a hoy.")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora de la cita es obligatoria.")
    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio.")
    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres.")
    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCita estado = EstadoCita.PENDIENTE;
}
```

### Enum EstadoCita

## java name=src/main/java/com/gestion/citas/model/entity/enums/EstadoCita.java

```java
package com.gestion.citas.model.entity.enums;

```

/**
* Estados posibles de una cita.
*
* PENDIENTE  → recién creada, aún no atendida.
* CONFIRMADA → confirmada por el prestador del servicio.
* CANCELADA  → cancelada por el cliente o el sistema.
* REAGENDADA → fue movida a otra fecha/hora.
* COMPLETADA → la cita ya fue atendida.

```text
 */
public enum EstadoCita {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    REAGENDADA,
    COMPLETADA
}
```

---

## 5. Repositorio (repository) {#repository}

## java name=src/main/java/com/gestion/citas/repository/CitaRepository.java

```java
package com.gestion.citas.repository;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.model.entity.enums.EstadoCita;
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
     * Verifica si ya existe una cita activa (no cancelada) en la misma fecha y hora.
     * Permite detectar duplicados antes de persistir.
     */
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> 'CANCELADA'
            """)
    boolean existeCitaActivaEnFechaHora(
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );

    /**
     * Igual que el anterior pero excluye una cita concreta (útil al reagendar).
     */
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END
            FROM Cita c
            WHERE c.fecha = :fecha
              AND c.hora  = :hora
              AND c.estado <> 'CANCELADA'
              AND c.id    <> :idExcluido
            """)
    boolean existeCitaActivaEnFechaHoraExcluyendo(
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora,
            @Param("idExcluido") Long idExcluido
    );

    /**
     * Lista todas las citas para una fecha determinada, ordenadas por hora.
     * Usado para consultar disponibilidad.
     */
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    /**
     * Lista todas las citas cuyo estado no sea CANCELADA en una fecha,
     * para calcular horarios ocupados.
     */
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fecha   = :fecha
              AND c.estado <> 'CANCELADA'
            ORDER BY c.hora ASC
            """)
    List<Cita> findCitasActivasByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Lista citas de un cliente específico.
     */
    List<Cita> findByNombreClienteIgnoreCaseOrderByFechaAscHoraAsc(String nombreCliente);

    /**
     * Lista citas por estado.
     */
    List<Cita> findByEstadoOrderByFechaAscHoraAsc(EstadoCita estado);
}
```

---

## 6. Servicio — Interfaz (service) {#service}

## java name=src/main/java/com/gestion/citas/service/CitaService.java

```java
package com.gestion.citas.service;

import com.gestion.citas.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface CitaService {

    /** Crea y persiste una nueva cita. */
    Cita crearCita(Cita cita);

    /** Retorna todas las citas registradas. */
    List<Cita> listarCitas();

    /** Busca una cita por su ID. Lanza excepción si no existe. */
    Cita obtenerCitaPorId(Long id);

    /**
     * Reagenda una cita existente: cambia fecha y/u hora.
     * Valida que el nuevo slot no esté ocupado.
     *
     * @param id        ID de la cita a reagendar.
     * @param nuevaFecha Nueva fecha deseada.
     * @param nuevaHora  Nueva hora deseada.
     * @return Cita actualizada con estado REAGENDADA.
     */
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    /**
     * Cancela una cita cambiando su estado a CANCELADA.
     * No es posible cancelar una cita ya cancelada o completada.
     */
    Cita cancelarCita(Long id);

    /**
     * Consulta la disponibilidad de horarios para una fecha dada.
     * Retorna un mapa con:
     *   "fecha"          → fecha consultada
     *   "horasOcupadas"  → lista de horas con cita activa
     *   "citasDelDia"    → detalle de citas activas ese día
     */
    Map<String, Object> consultarDisponibilidad(LocalDate fecha);
}
```

---

## 7. Servicio — Implementación (service/impl) {#serviceimpl}

## java name=src/main/java/com/gestion/citas/service/impl/CitaServiceImpl.java

```java
package com.gestion.citas.service.impl;

import com.gestion.citas.exception.CitaDuplicadaException;
import com.gestion.citas.exception.CitaNoEncontradaException;
import com.gestion.citas.exception.EstadoInvalidoException;
import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.model.entity.enums.EstadoCita;
import com.gestion.citas.repository.CitaRepository;
import com.gestion.citas.service.CitaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    // ─────────────────────────────────────────────────────────────────
    // CREAR CITA
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        log.info("Intentando crear cita para '{}' el {} a las {}",
                cita.getNombreCliente(), cita.getFecha(), cita.getHora());

        validarSlotDisponible(cita.getFecha(), cita.getHora(), null);

        // Estado inicial siempre PENDIENTE sin importar lo que envíe el cliente
        cita.setEstado(EstadoCita.PENDIENTE);

        Cita citaGuardada = citaRepository.save(cita);
        log.info("Cita creada con ID {}", citaGuardada.getId());
        return citaGuardada;
    }

    // ─────────────────────────────────────────────────────────────────
    // LISTAR TODAS LAS CITAS
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {
        log.info("Listando todas las citas");
        return citaRepository.findAll();
    }

    // ─────────────────────────────────────────────────────────────────
    // OBTENER CITA POR ID
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Cita obtenerCitaPorId(Long id) {
        log.info("Buscando cita con ID {}", id);
        return citaRepository.findById(id)
                .orElseThrow(() -> new CitaNoEncontradaException(
                        "No se encontró ninguna cita con el ID: " + id));
    }

    // ─────────────────────────────────────────────────────────────────
    // REAGENDAR CITA
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        log.info("Reagendando cita ID {} → {} {}", id, nuevaFecha, nuevaHora);

        Cita cita = obtenerCitaPorId(id);

        // No se puede reagendar una cita cancelada o completada
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                    "No se puede reagendar una cita que ya fue CANCELADA.");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new EstadoInvalidoException(
                    "No se puede reagendar una cita que ya fue COMPLETADA.");
        }

        // Validar que el nuevo slot no esté ocupado (excluyendo la propia cita)
        validarSlotDisponible(nuevaFecha, nuevaHora, id);

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        Cita citaActualizada = citaRepository.save(cita);
        log.info("Cita ID {} reagendada exitosamente", id);
        return citaActualizada;
    }

    // ─────────────────────────────────────────────────────────────────
    // CANCELAR CITA
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        log.info("Cancelando cita ID {}", id);

        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new EstadoInvalidoException(
                    "La cita con ID " + id + " ya se encuentra CANCELADA.");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new EstadoInvalidoException(
                    "No se puede cancelar una cita que ya fue COMPLETADA.");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        Cita citaCancelada = citaRepository.save(cita);
        log.info("Cita ID {} cancelada exitosamente", id);
        return citaCancelada;
    }

    // ─────────────────────────────────────────────────────────────────
    // CONSULTAR DISPONIBILIDAD
    // ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultarDisponibilidad(LocalDate fecha) {
        log.info("Consultando disponibilidad para la fecha {}", fecha);

        List<Cita> citasActivas = citaRepository.findCitasActivasByFecha(fecha);

        List<String> horasOcupadas = citasActivas.stream()
                .map(c -> c.getHora().toString())
                .toList();

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha.toString());
        respuesta.put("totalCitasActivas", citasActivas.size());
        respuesta.put("horasOcupadas", horasOcupadas);
        respuesta.put("citasDelDia", citasActivas);

        return respuesta;
    }

    // ─────────────────────────────────────────────────────────────────
    // MÉTODO PRIVADO: Validar slot disponible
    // ─────────────────────────────────────────────────────────────────

    /**
     * Verifica que no exista ya una cita activa para la fecha y hora indicadas.
     *
     * @param fecha       Fecha a verificar.
     * @param hora        Hora a verificar.
     * @param idExcluido  ID de la cita que debe ignorarse (null si es creación nueva).
     */
    private void validarSlotDisponible(LocalDate fecha, LocalTime hora, Long idExcluido) {
        boolean ocupado;

        if (idExcluido == null) {
            ocupado = citaRepository.existeCitaActivaEnFechaHora(fecha, hora);
        } else {
            ocupado = citaRepository.existeCitaActivaEnFechaHoraExcluyendo(fecha, hora, idExcluido);
        }

        if (ocupado) {
            throw new CitaDuplicadaException(
                    String.format("Ya existe una cita activa para la fecha %s a las %s. "
                                  + "Por favor elija otro horario.", fecha, hora));
        }
    }
}
```

---

## 8. Controlador (controller) {#controller}

## java name=src/main/java/com/gestion/citas/controller/CitaController.java

```java
package com.gestion.citas.controller;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

```

/**
* Controlador REST para la gestión de citas.
* Base URL: /api/v1/citas

```text
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // ─────────────────────────────────────────────────────────────────
    // POST /api/v1/citas  →  Crear cita
    // ─────────────────────────────────────────────────────────────────

    /**
     * Crea una nueva cita.
     *
     * Ejemplo Body JSON:
     * {
     *   "nombreCliente": "Juan Pérez",
     *   "fecha": "2026-05-20",
     *   "hora": "10:30:00",
     *   "motivo": "Consulta general"
     * }
     */
    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        log.info("POST /api/v1/citas - Crear cita para '{}'", cita.getNombreCliente());
        Cita citaCreada = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(citaCreada);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/citas  →  Listar citas
    // ─────────────────────────────────────────────────────────────────

    /**
     * Lista todas las citas registradas en el sistema.
     */
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        log.info("GET /api/v1/citas - Listar todas las citas");
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/{id}  →  Obtener cita por ID
    // ─────────────────────────────────────────────────────────────────

    /**
     * Retorna una cita específica por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        log.info("GET /api/v1/citas/{} - Obtener cita por ID", id);
        Cita cita = citaService.obtenerCitaPorId(id);
        return ResponseEntity.ok(cita);
    }

    // ─────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/reagendar  →  Reagendar cita
    // ─────────────────────────────────────────────────────────────────

    /**
     * Reagenda una cita existente a un nuevo slot de fecha y hora.
     *
     * Parámetros de query:
     *   nuevaFecha  (yyyy-MM-dd)  — requerido
     *   nuevaHora   (HH:mm:ss)   — requerido
     *
     * Ejemplo:
     *   PATCH /api/v1/citas/3/reagendar?nuevaFecha=2026-06-01&nuevaHora=14:00:00
     */
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                LocalDate nuevaFecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
                LocalTime nuevaHora) {

        log.info("PATCH /api/v1/citas/{}/reagendar → {} {}", id, nuevaFecha, nuevaHora);
        Cita citaReagendada = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
        return ResponseEntity.ok(citaReagendada);
    }

    // ─────────────────────────────────────────────────────────────────
    // PATCH /api/v1/citas/{id}/cancelar  →  Cancelar cita
    // ─────────────────────────────────────────────────────────────────

    /**
     * Cancela una cita estableciendo su estado como CANCELADA.
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        log.info("PATCH /api/v1/citas/{}/cancelar", id);
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    // ─────────────────────────────────────────────────────────────────
    // GET /api/v1/citas/disponibilidad  →  Consultar disponibilidad
    // ─────────────────────────────────────────────────────────────────

    /**
     * Consulta los horarios ocupados y disponibles para una fecha específica.
     *
     * Parámetro de query:
     *   fecha  (yyyy-MM-dd) — requerido
     *
     * Ejemplo:
     *   GET /api/v1/citas/disponibilidad?fecha=2026-05-20
     */
    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                LocalDate fecha) {

        log.info("GET /api/v1/citas/disponibilidad?fecha={}", fecha);
        Map<String, Object> disponibilidad = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }
}
```

---

## 9. Manejo Global de Errores {#errors}

### Excepciones de Dominio

## java name=src/main/java/com/gestion/citas/exception/CitaNoEncontradaException.java

```java
package com.gestion.citas.exception;

```

/**
* Se lanza cuando se intenta acceder a una cita que no existe en la base de datos.

```text
 */
public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
```

## java name=src/main/java/com/gestion/citas/exception/CitaDuplicadaException.java

```java
package com.gestion.citas.exception;

```

/**
* Se lanza cuando se intenta crear o reagendar una cita en un slot
* que ya está ocupado por otra cita activa.

```text
 */
public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
```

## java name=src/main/java/com/gestion/citas/exception/EstadoInvalidoException.java

```java
package com.gestion.citas.exception;

```

/**
* Se lanza cuando se intenta realizar una operación incompatible
* con el estado actual de la cita (ej: cancelar una cita ya cancelada).

```text
 */
public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
```

### Manejador Global (@ControllerAdvice)

## java name=src/main/java/com/gestion/citas/exception/GlobalExceptionHandler.java

```java
package com.gestion.citas.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

```

/**
* Interceptor global que convierte excepciones en respuestas JSON estructuradas.

```text
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Cita no encontrada → 404 ──────────────────────────────────────
    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleCitaNoEncontrada(
            CitaNoEncontradaException ex) {
        log.warn("Cita no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── Cita duplicada → 409 ─────────────────────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleCitaDuplicada(
            CitaDuplicadaException ex) {
        log.warn("Cita duplicada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── Estado inválido → 422 ────────────────────────────────────────
    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoInvalido(
            EstadoInvalidoException ex) {
        log.warn("Operación con estado inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ── Validación de campos Bean Validation → 400 ───────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> erroresCampos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erroresCampos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", HttpStatus.BAD_REQUEST.value());
        cuerpo.put("error", "Error de validación en los datos de entrada");
        cuerpo.put("erroresCampos", erroresCampos);

        log.warn("Errores de validación: {}", erroresCampos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    // ── Parámetro de query faltante → 400 ────────────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleParametroFaltante(
            MissingServletRequestParameterException ex) {
        log.warn("Parámetro requerido ausente: {}", ex.getParameterName());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getParameterName() + "' es requerido.");
    }

    // ── Tipo incorrecto en parámetro → 400 ───────────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTipoIncorrecto(
            MethodArgumentTypeMismatchException ex) {
        String mensaje = String.format(
                "El parámetro '%s' tiene un formato inválido. Valor recibido: '%s'.",
                ex.getName(), ex.getValue());
        log.warn(mensaje);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, mensaje);
    }

    // ── Error genérico no controlado → 500 ───────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Error no controlado: ", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Contacte al administrador.");
    }

    // ── Método utilitario ─────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String mensaje) {

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);

        return ResponseEntity.status(status).body(cuerpo);
    }
}
```

---

## 10. Clase Principal {#main}

## java name=src/main/java/com/gestion/citas/CitasBackendApplication.java

```java
package com.gestion.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasBackendApplication.class, args);
    }
}
```

---

## 11. Pruebas con Postman {#postman}

### Base URL

http://localhost:8080/api/v1/citas

---

### 🟢 Crear una cita — POST /api/v1/citas

**Headers:**

```text
Content-Type: application/json
```

**Body (raw JSON):**

```json
{
  "nombreCliente": "Ana Gómez",
  "fecha": "2026-06-10",
  "hora": "09:00:00",
  "motivo": "Revisión anual"
}
```

**Respuesta esperada `201 Created`:**

```json
{
  "id": 1,
  "nombreCliente": "Ana Gómez",
  "fecha": "2026-06-10",
  "hora": "09:00:00",
  "motivo": "Revisión anual",
  "estado": "PENDIENTE"
}
```

---

### 🔵 Listar todas las citas — GET /api/v1/citas

GET http://localhost:8080/api/v1/citas

**Respuesta esperada `200 OK`:**

```json
[
  {
    "id": 1,
    "nombreCliente": "Ana Gómez",
    "fecha": "2026-06-10",
    "hora": "09:00:00",
    "motivo": "Revisión anual",
    "estado": "PENDIENTE"
  }
]
```

---

### 🔵 Consultar cita por ID — GET /api/v1/citas/{id}

GET http://localhost:8080/api/v1/citas/1

---

### 🟡 Reagendar una cita — PATCH /api/v1/citas/{id}/reagendar

PATCH http://localhost:8080/api/v1/citas/1/reagendar?nuevaFecha=2026-06-15&nuevaHora=14:00:00

**Respuesta esperada `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Ana Gómez",
  "fecha": "2026-06-15",
  "hora": "14:00:00",
  "motivo": "Revisión anual",
  "estado": "REAGENDADA"
}
```

---

### 🔴 Cancelar una cita — PATCH /api/v1/citas/{id}/cancelar

PATCH http://localhost:8080/api/v1/citas/1/cancelar

**Respuesta esperada `200 OK`:**

```json
{
  "id": 1,
  "nombreCliente": "Ana Gómez",
  "fecha": "2026-06-15",
  "hora": "14:00:00",
  "motivo": "Revisión anual",
  "estado": "CANCELADA"
}
```

---

### 🟣 Consultar disponibilidad — GET /api/v1/citas/disponibilidad

GET http://localhost:8080/api/v1/citas/disponibilidad?fecha=2026-06-10

**Respuesta esperada `200 OK`:**

```json
{
  "fecha": "2026-06-10",
  "totalCitasActivas": 1,
  "horasOcupadas": ["09:00"],
  "citasDelDia": [
    {
      "id": 2,
      "nombreCliente": "Carlos Ruiz",
      "fecha": "2026-06-10",
      "hora": "09:00:00",
      "motivo": "Consulta urgente",
      "estado": "PENDIENTE"
    }
  ]
}
```

---

### ⚠️ Ejemplo de error por duplicado 409 Conflict

## Al intentar crear una segunda cita el mismo día a la misma hora:

```json
{
  "timestamp": "2026-05-03T10:45:00.123",
  "status": 409,
  "error": "Conflict",
  "mensaje": "Ya existe una cita activa para la fecha 2026-06-10 a las 09:

```

## 00. Por favor elija otro horario."

```json
}
```

### ⚠️ Ejemplo de error de validación 400 Bad Request

## Al enviar un body sin

## ombreCliente ni fecha:

```json
{
  "timestamp": "2026-05-03T10:46:00.321",
  "status": 400,
  "error": "Error de validación en los datos de entrada",
  "erroresCampos": {
    "nombreCliente": "El nombre del cliente es obligatorio.",
    "fecha": "La fecha de la cita es obligatoria."
  }
}
```

---

## Resumen de Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/v1/citas` | Crear una nueva cita |
| `GET` | `/api/v1/citas` | Listar todas las citas |
| `GET` | `/api/v1/citas/{id}` | Consultar cita por ID |
| `PATCH` | `/api/v1/citas/{id}/reagendar` | Reagendar fecha y hora |
| `PATCH` | `/api/v1/citas/{id}/cancelar` | Cancelar una cita |
| `GET` | `/api/v1/citas/disponibilidad?fecha=` | Ver horarios ocupados |

## Códigos HTTP utilizados

| Código | Significado |
|--------|-------------|
| `200 OK` | Operación exitosa |
| `201 Created` | Cita creada correctamente |
| `400 Bad Request` | Datos inválidos o parámetro faltante |
| `404 Not Found` | Cita no encontrada |
| `409 Conflict` | Horario duplicado |
| `422 Unprocessable Entity` | Operación incompatible con el estado actual |

## | 500 Internal Server Error | Error no controlado |
