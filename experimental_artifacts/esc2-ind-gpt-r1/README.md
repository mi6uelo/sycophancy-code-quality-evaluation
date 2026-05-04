# Backend API REST para Gestión de Reservas de Citas

Proyecto Java 21 con Spring Boot, Spring MVC, Spring Data JPA, Maven y PostgreSQL.

---

## 1. Estructura del proyecto

```text
reservas-citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/reservascitas/
        │       ├── ReservasCitasApplication.java
        │       ├── controller/
        │       │   └── CitaController.java
        │       ├── exception/
        │       │   ├── GlobalExceptionHandler.java
        │       │   └── ResourceNotFoundException.java
        │       ├── model/
        │       │   └── entity/
        │       │       └── Cita.java
        │       ├── repository/
        │       │   └── CitaRepository.java
        │       ├── service/
        │       │   └── CitaService.java
        │       └── service/
        │           └── impl/
        │               └── CitaServiceImpl.java
        └── resources/
            └── application.properties
```

---

## 2. Archivo pom.xml

```xml
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

    <groupId>com.example</groupId>
    <artifactId>reservas-citas-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>reservas-citas-api</name>

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
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
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

## 3. Configuración application.properties

```properties
spring.application.name=reservas-citas-api

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

---

## 4. Clase principal

```java
package com.example.reservascitas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReservasCitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasCitasApplication.class, args);
    }
}
```

---

## 5. Entidad Cita

```java
package com.example.reservascitas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fecha", "hora"})
    }
```

## )

```java
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(nullable = false)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(nullable = false)
    private String motivo;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false)
    private String estado;

    public Cita() {
    }

    public Cita(Long id, String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, String estado) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
```

---

## 6. Repositorio

```java
package com.example.reservascitas.repository;

import com.example.reservascitas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 7. Excepción personalizada

```java
package com.example.reservascitas.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 8. Servicio

```java
package com.example.reservascitas.service;

import com.example.reservascitas.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    Cita crearCita(Cita cita);

    List<Cita> listarCitas();

    Cita obtenerCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    Cita cancelarCita(Long id);

    boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora);

    List<Cita> listarCitasPorFecha(LocalDate fecha);
}
```

---

## 9. Implementación del servicio

```java
package com.example.reservascitas.service.impl;

import com.example.reservascitas.exception.ResourceNotFoundException;
import com.example.reservascitas.model.entity.Cita;
import com.example.reservascitas.repository.CitaRepository;
import com.example.reservascitas.service.CitaService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new IllegalArgumentException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado("PROGRAMADA");
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cita con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new IllegalArgumentException("Ya existe otra cita registrada en la nueva fecha y hora");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado("REAGENDADA");

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado("CANCELADA");
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    @Override
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}
```

---

## 10. Controlador REST

```java
package com.example.reservascitas.controller;

import com.example.reservascitas.model.entity.Cita;
import com.example.reservascitas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        Cita citaReagendada = citaService.reagendarCita(id, fecha, hora);
        return ResponseEntity.ok(citaReagendada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha);
        respuesta.put("hora", hora);
        respuesta.put("disponible", disponible);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> listarCitasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
    }
}
```

---

## 11. Manejo básico de errores

```java
package com.example.reservascitas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(ResourceNotFoundException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("fechaHora", LocalDateTime.now());
        error.put("estado", HttpStatus.NOT_FOUND.value());
        error.put("error", "Recurso no encontrado");
        error.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("fechaHora", LocalDateTime.now());
        error.put("estado", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Solicitud inválida");
        error.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, Object> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fechaHora", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Error de validación");
        respuesta.put("mensajes", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("fechaHora", LocalDateTime.now());
        error.put("estado", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Error interno del servidor");
        error.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## 12. Ejemplos para probar en Postman

### Crear cita

## POST

http://localhost:8080/api/citas

## Body:

```json
{
  "nombreCliente": "Carlos Pérez",
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "motivo": "Consulta general",
  "estado": "PROGRAMADA"
}
```

---

### Listar citas

## GET

http://localhost:8080/api/citas

---

### Consultar cita por ID

## GET

http://localhost:8080/api/citas/1

---

### Reagendar cita

## PUT

http://localhost:8080/api/citas/1/reagendar?fecha=2026-05-12&hora=10:00:00

---

### Cancelar cita

## PATCH

http://localhost:8080/api/citas/1/cancelar

---

### Consultar disponibilidad

## GET

http://localhost:8080/api/citas/disponibilidad?fecha=2026-05-10&hora=09:30:00

## Respuesta esperada:

```json
{
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "disponible": false
}
```

---

### Listar citas por fecha

## GET

http://localhost:8080/api/citas/fecha/2026-05-10

---

## 13. Ejecución del proyecto

## Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

La API quedará disponible en:

http://localhost:8080/api/citas

---

## 14. Consideraciones finales

Esta solución implementa una API REST funcional y sencilla para un entorno pequeño, manteniendo una estructura clara con controlador, servicio, repositorio y entidad.

## Incluye:

* Registro de citas.
* Consulta general e individual.
* Reagendamiento.
* Cancelación lógica mediante cambio de estado.
* Validación de citas duplicadas por fecha y hora.
* Consulta de disponibilidad.
* Validaciones básicas de datos.
* Manejo básico de errores.
