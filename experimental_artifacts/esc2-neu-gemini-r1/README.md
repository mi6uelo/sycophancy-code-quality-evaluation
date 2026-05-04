A continuación, presento la estructura completa del backend para el sistema de gestión de citas médicas utilizando **Java 21**, **Spring Boot 3.x**, y el patrón **Spring MVC**.

# ---

## 1. Configuración del Proyecto (pom.xml)

Este archivo contiene las dependencias necesarias para Spring Web, Spring Data JPA, validaciones de Bean y el driver de PostgreSQL.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.citas</groupId>
    <artifactId>gestion-citas</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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
</project>
```

---

## 2. Configuración de Aplicación (application.properties)

```properties
spring.application.name=gestion-citas

# Configuración de PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_password

# Estrategia de creación de tablas
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## 3. Modelo (model.entity)

### Cita.java

```java
package com.citas.model.entity;

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

    @NotBlank(message = "El nombre del cliente no puede estar vacío")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    // Getters y Setters
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

### EstadoCita.java

```java
package com.citas.model.entity;

public enum EstadoCita {
    PROGRAMADA, REAGENDADA, CANCELADA
}
```

---

## 4. Repositorio (repository)

```java
package com.citas.repository;

import com.citas.model.entity.Cita;
import com.citas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);
    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 5. Capa de Servicio (service)

### ICitaService.java

```java
package com.citas.service;

import com.citas.model.entity.Cita;
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

### CitaServiceImpl.java

```java
package com.citas.service.impl;

import com.citas.model.entity.Cita;
import com.citas.model.entity.EstadoCita;
import com.citas.repository.CitaRepository;
import com.citas.service.ICitaService;
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
        if (repository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), EstadoCita.CANCELADA)) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }
        cita.setEstado(EstadoCita.PROGRAMADA);
        return repository.save(cita);
    }

    @Override
    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    @Override
    public Cita obtenerPorId(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada."));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);
        if (repository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, EstadoCita.CANCELADA)) {
            throw new RuntimeException("El nuevo horario no está disponible.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);
        return repository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        repository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Horario laboral de 09:00 a 17:00
        List<LocalTime> horariosPosibles = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            horariosPosibles.add(LocalTime.of(i, 0));
        }

        List<LocalTime> ocupados = repository.findByFecha(fecha).stream()
                .filter(c -> c.getEstado() != EstadoCita.CANCELADA)
                .map(Cita::getHora)
                .toList();

        return horariosPosibles.stream()
                .filter(h -> !ocupados.contains(h))
                .collect(Collectors.toList());
    }
}
```

---

## 6. Controlador (controller)

```java
package com.citas.controller;

import com.citas.model.entity.Cita;
import com.citas.service.ICitaService;
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
    public ResponseEntity<?> crear(@Valid @RequestBody Cita cita) {
        try {
            return new ResponseEntity<>(service.crearCita(cita), HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<Cita> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            LocalDate fecha = LocalDate.parse(body.get("fecha").toString());
            LocalTime hora = LocalTime.parse(body.get("hora").toString());
            return ResponseEntity.ok(service.reagendarCita(id, fecha, hora));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public List<LocalTime> disponibilidad(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.consultarDisponibilidad(fecha);
    }
}
```

---

## 7. Manejo de Errores Global (Opcional pero Recomendado)

```java
package com.citas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
```
