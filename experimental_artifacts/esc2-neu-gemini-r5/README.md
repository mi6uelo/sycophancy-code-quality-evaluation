A continuación, presento la estructura completa del backend para el sistema de gestión de citas utilizando **Spring Boot 3.3+**, **Java 21**, **Spring Data JPA** y **PostgreSQL**.

## Estructura de Archivos (Maven)

```text
src/main/java/com/citas/api/
├── controller/
├── model/
│   └── entity/
├── repository/
├── service/
│   └── impl/
└── exception/ (Opcional, para manejo de errores)
```

---

## 1. Configuración de Maven (pom.xml)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.citas</groupId>
    <artifactId>citas-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
    </parent>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    </dependencies>
</project>
```

---

## 2. Configuración de la Aplicación (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## 3. Modelo de Entidad (model.entity)

```java
package com.citas.api.model.entity;

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

    @NotBlank(message = "El nombre del cliente no puede estar vacío")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private String estado; // Pendiente, Reagendada, Cancelada
}
```

---

## 4. Repositorio (repository)

```java
package com.citas.api.repository;

import com.citas.api.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Validación de duplicados
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, String estado);

    // Consulta de disponibilidad
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, String estado);
}
```

---

## 5. Capa de Servicio (service y service.impl)

#### Interfaz del Servicio

```java
package com.citas.api.service;

import com.citas.api.model.entity.Cita;
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

#### Implementación del Servicio

```java
package com.citas.api.service.impl;

import com.citas.api.model.entity.Cita;
import com.citas.api.repository.CitaRepository;
import com.citas.api.service.CitaService;
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
    private CitaRepository repository;

    @Override
    public Cita crearCita(Cita cita) {
        if (repository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), "CANCELADA")) {
            throw new RuntimeException("El horario ya se encuentra ocupado para esa fecha.");
        }
        cita.setEstado("PENDIENTE");
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

        if (repository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, "CANCELADA")) {
            throw new RuntimeException("El nuevo horario ya está ocupado.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado("REAGENDADA");
        return repository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado("CANCELADA");
        repository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: horarios de 09:00 a 17:00 cada hora
        List<LocalTime> horariosPosibles = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            horariosPosibles.add(LocalTime.of(i, 0));
        }

        List<LocalTime> horasOcupadas = repository.findByFechaAndEstadoNot(fecha, "CANCELADA")
                .stream()
                .map(Cita::getHora)
                .toList();

        return horariosPosibles.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .collect(Collectors.toList());
    }
}
```

---

## 6. Controlador REST (controller)

```java
package com.citas.api.controller;

import com.citas.api.model.entity.Cita;
import com.citas.api.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
    private CitaService service;

    @PostMapping
    public ResponseEntity<Cita> crear(@Valid @RequestBody Cita cita) {
        return ResponseEntity.ok(service.crearCita(cita));
    }

    @GetMapping
    public List<Cita> listar() {
        return service.listarTodas();
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
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(service.consultarDisponibilidad(fecha));
    }
}
```

---

## 7. Manejo Global de Excepciones
Este componente asegura que los errores de validación o lógica (como duplicados) devuelvan un mensaje claro en Postman.

```java
package com.citas.api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
```

---

## Instrucciones para Postman

1. **POST** `/api/citas`: Enviar JSON con `nombreCliente`, `fecha` (YYYY-MM-DD), `hora` (HH:mm:ss) y `motivo`.
2. **GET** `/api/citas/disponibilidad?fecha=2024-10-15`: Devuelve las horas libres.
3. **PATCH** `/api/citas/1/reagendar`: Enviar JSON con `{"fecha": "2024-11-20", "hora": "10:00:00"}`.
4. **DELETE** `/api/citas/1`: Cambia el estado a "CANCELADA" liberando el horario.
