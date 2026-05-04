¡Hola! Como desarrollador Java, he diseñado la estructura de este backend siguiendo las mejores prácticas de **Spring Boot 3**, utilizando **Java 21** y el patrón **Spring MVC**.

A continuación, presento la implementación completa del sistema de gestión de citas.

# ---

# Backend de Gestión de Citas (Spring Boot & PostgreSQL)

## 1. Configuración de Maven (pom.xml)

Utilizamos las dependencias necesarias para JPA, Validación, Web y el driver de PostgreSQL.

```xml
<project xmlns="http://maven.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.gestion.citas</groupId>
    <artifactId>citas-api</artifactId>
    <version>1.0.0</version>
    <name>Citas API</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
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

## 2. Configuración de la Aplicación (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

---

## 3. Modelo de Entidad (model.entity)

```java
package com.gestion.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    // Constructores, Getters y Setters
    public enum EstadoCita {
        PROGRAMADA, REAGENDADA, CANCELADA
    }

    public Cita() {}

    // Getters y Setters omitidos por brevedad, se recomienda usar Lombok @Data
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
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

## 4. Repositorio (repository)

```java
package com.gestion.citas.repository;

import com.gestion.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Validación de duplicados
    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    // Consulta de disponibilidad
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, Cita.EstadoCita estado);
}
```

---

## 5. Capa de Servicio (service y service.impl)

### Interfaz de Servicio

```java
package com.gestion.citas.service;

import com.gestion.citas.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ICitaService {
    Cita crearCita(Cita cita);
    List<Cita> listarTodas();
    Cita obtenerPorId(Long id);
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);
    void cancelarCita(Long id);
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### Implementación del Servicio

```java
package com.gestion.citas.service.impl;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.repository.CitaRepository;
import com.gestion.citas.service.ICitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements ICitaService {

    @Autowired
    private CitaRepository repository;

    @Override
    public Cita crearCita(Cita cita) {
        if (repository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }
        cita.setEstado(Cita.EstadoCita.PROGRAMADA);
        return repository.save(cita);
    }

    @Override
    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    @Override
    public Cita obtenerPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);

        if (repository.existsByFechaAndHora(nuevaFecha, nuevaHora)) {
            throw new RuntimeException("El nuevo horario solicitado no está disponible.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.REAGENDADA);
        return repository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        repository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: Horarios de 08:00 a 16:00
        List<LocalTime> horariosPosibles = new ArrayList<>();
        for (int h = 8; h <= 16; h++) {
            horariosPosibles.add(LocalTime.of(h, 0));
        }

        List<LocalTime> horasOcupadas = repository.findByFechaAndEstadoNot(fecha, Cita.EstadoCita.CANCELADA)
                .stream()
                .map(Cita::getHora)
                .collect(Collectors.toList());

        horariosPosibles.removeAll(horasOcupadas);
        return horariosPosibles;
    }
}
```

---

## 6. Controlador REST (controller)

```java
package com.gestion.citas.controller;

import com.gestion.citas.model.entity.Cita;
import com.gestion.citas.service.ICitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private ICitaService service;

    @PostMapping
    public ResponseEntity<Cita> crear(@Valid @RequestBody Cita cita) {
        return new ResponseEntity<>(service.crearCita(cita), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendar(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        LocalDate fecha = LocalDate.parse((String) payload.get("fecha"));
        LocalTime hora = LocalTime.parse((String) payload.get("hora"));

        return ResponseEntity.ok(service.reagendarCita(id, fecha, hora));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.consultarDisponibilidad(fecha));
    }
}
```

---

## 7. Manejo de Errores Global

He incluido un `RestControllerAdvice` para capturar excepciones y devolver respuestas limpias en formato JSON.

```java
package com.gestion.citas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
```

---

## Notas de Implementación
*   **Validación de Duplicados:** Se implementó en la capa de servicio mediante el método `existsByFechaAndHora` para asegurar que el recurso esté libre antes de guardar.
*   **Java 21:** Se aprovecha la inferencia de tipos y las mejoras en colecciones de la versión más reciente.
*   **Postman:** Para probar "Reagendar", usa un `PATCH` con un JSON que contenga `"fecha": "2026-05-15"` y `"hora": "10:00:00"`.
