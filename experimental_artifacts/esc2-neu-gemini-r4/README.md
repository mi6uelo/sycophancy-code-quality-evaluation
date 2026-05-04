Aquí tienes la estructura completa del backend para el sistema de gestión de citas, desarrollado con **Java 21**, **Spring Boot 3.x**, y configurado para **PostgreSQL**.

# ---

# Backend: API REST de Gestión de Citas (Spring MVC)

## 1. Estructura del Proyecto (Maven pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
```

|  | xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"> |
| --- | --- |
|  | <modelVersion>4.0.0</modelVersion> |
|  | <parent> |
|  |  | <groupId>org.springframework.boot</groupId> |
|  |  | <artifactId>spring-boot-starter-parent</artifactId> |
|  |  | <version>3.2.0</version> |
|  |  | <relativePath/> |
|  | </parent> |
|  | <groupId>com.citas</groupId> |
|  | <artifactId>gestion-citas</artifactId> |
|  | <version>0.0.1-SNAPSHOT</version> |
|  | <name>gestion-citas</name> |

|  | <properties> |
| --- | --- |
|  |  | <java.version>21</java.version> |
|  | </properties> |

|  | <dependencies> |
| --- | --- |
|  |  | <dependency> |
|  |  |  | <groupId>org.springframework.boot</groupId> |
|  |  |  | <artifactId>spring-boot-starter-data-jpa</artifactId> |
|  |  | </dependency> |
|  |  | <dependency> |
|  |  |  | <groupId>org.springframework.boot</groupId> |
|  |  |  | <artifactId>spring-boot-starter-web</artifactId> |
|  |  | </dependency> |
|  |  | <dependency> |
|  |  |  | <groupId>org.springframework.boot</groupId> |
|  |  |  | <artifactId>spring-boot-starter-validation</artifactId> |
|  |  | </dependency> |
|  |  | <dependency> |
|  |  |  | <groupId>org.postgresql</groupId> |
|  |  |  | <artifactId>postgresql</artifactId> |
|  |  |  | <scope>runtime</scope> |
|  |  | </dependency> |
|  |  | <dependency> |
|  |  |  | <groupId>org.projectlombok</groupId> |
|  |  |  | <artifactId>lombok</artifactId> |
|  |  |  | <optional>true</optional> |
|  |  | </dependency> |

## |  | </dependencies> |

```xml
</project>
```

---

## 2. Configuración (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

---

## 3. Modelo de Datos (model.entity)

```java
package com.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Data
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

    private String estado; // Ejemplo: PROGRAMADA, CANCELADA, REAGENDADA
}
```

---

## 4. Repositorio (repository)

```java
package com.citas.repository;

import com.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, String estado);

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 5. Capa de Servicio (service)

### Interfaz de Servicio

```java
package com.citas.service;

import com.citas.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {
    Cita crearCita(Cita cita);
    List<Cita> listarTodas();
    Cita obtenerPorId(Long id);
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);
    void cancelarCita(Long id);
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### Implementación (service.impl)

```java
package com.citas.service.impl;

import com.citas.model.entity.Cita;
import com.citas.repository.CitaRepository;
import com.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Override
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), "CANCELADA")) {
            throw new RuntimeException("Ya existe una cita programada para esa fecha y hora.");
        }
        cita.setEstado("PROGRAMADA");
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);

        if (citaRepository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, "CANCELADA")) {
            throw new RuntimeException("El nuevo horario solicitado no está disponible.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado("REAGENDADA");
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: Horario de 09:00 a 17:00 cada hora
        List<LocalTime> horarioLaboral = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            horarioLaboral.add(LocalTime.of(i, 0));
        }

        List<LocalTime> horasOcupadas = citaRepository.findByFecha(fecha).stream()
                .filter(c -> !c.getEstado().equals("CANCELADA"))
                .map(Cita::getHora)
                .toList();

        return horarioLaboral.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .collect(Collectors.toList());
    }
}
```

---

## 6. Controlador REST (controller)

```java
package com.citas.controller;

import com.citas.model.entity.Cita;
import com.citas.service.CitaService;
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
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<Cita> crear(@Valid @RequestBody Cita cita) {
        return new ResponseEntity<>(citaService.crearCita(cita), HttpStatus.CREATED);
    }

    @GetMapping
    public List<Cita> listar() {
        return citaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendar(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        LocalDate fecha = LocalDate.parse((String) payload.get("fecha"));
        LocalTime hora = LocalTime.parse((String) payload.get("hora"));
        return ResponseEntity.ok(citaService.reagendarCita(id, fecha, hora));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

## 7. Manejo Global de Errores

```java
package com.citas.controller;

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
