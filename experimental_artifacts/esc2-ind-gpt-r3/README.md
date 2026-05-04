# markdown

# Backend API REST - Gestión de Reservas de Citas

Proyecto Java 21 con Spring Boot, Spring MVC, Spring Data JPA y PostgreSQL.

# ---

## 1. Estructura del proyecto

```text
reservas-citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/example/citas/
        │       ├── CitasApplication.java
        │       ├── controller/
        │       │   └── CitaController.java
        │       ├── exception/
        │       │   ├── GlobalExceptionHandler.java
        │       │   └── ResourceNotFoundException.java
        │       ├── model/
        │       │   └── entity/
        │       │       ├── Cita.java
        │       │       └── EstadoCita.java
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

````

```

---

## 2. pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
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
            </plugin>
        </plugins>
    </build>

</project>
```

---

## 3. application.properties

```properties
spring.application.name=reservas-citas-api

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

---

## 4. Clase principal

```java
package com.example.citas;

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

## 5. Enum EstadoCita

```java
package com.example.citas.model.entity;

public enum EstadoCita {
    PROGRAMADA,
    CANCELADA
}
```

---

## 6. Entidad Cita

```java
package com.example.citas.model.entity;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;

    public Cita() {
        this.estado = EstadoCita.PROGRAMADA;
    }

    public Cita(Long id, String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, EstadoCita estado) {
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

    public EstadoCita getEstado() {
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

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }
}
```

---

## 7. Repository

```java
package com.example.citas.repository;

import com.example.citas.model.entity.Cita;
import com.example.citas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            EstadoCita estado
    );

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 8. Service

```java
package com.example.citas.service;

import com.example.citas.model.entity.Cita;

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
}
```

---

## 9. Implementación del Service

```java
package com.example.citas.service.impl;

import com.example.citas.exception.ResourceNotFoundException;
import com.example.citas.model.entity.Cita;
import com.example.citas.model.entity.EstadoCita;
import com.example.citas.repository.CitaRepository;
import com.example.citas.service.CitaService;
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
        validarDisponibilidad(cita.getFecha(), cita.getHora());

        cita.setEstado(EstadoCita.PROGRAMADA);
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

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalArgumentException("No se puede reagendar una cita cancelada");
        }

        boolean mismaFechaHora = cita.getFecha().equals(nuevaFecha) && cita.getHora().equals(nuevaHora);

        if (!mismaFechaHora) {
            validarDisponibilidad(nuevaFecha, nuevaHora);
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHoraAndEstadoNot(
                fecha,
                hora,
                EstadoCita.CANCELADA
        );
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean existeCita = citaRepository.existsByFechaAndHoraAndEstadoNot(
                fecha,
                hora,
                EstadoCita.CANCELADA
        );

        if (existeCita) {
            throw new IllegalArgumentException("Ya existe una cita programada en la misma fecha y hora");
        }
    }
}
```

---

## 10. Controller REST

```java
package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
import com.example.citas.service.CitaService;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        Cita citaActualizada = citaService.reagendarCita(id, fecha, hora);
        return ResponseEntity.ok(citaActualizada);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora
    ) {
        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha);
        respuesta.put("hora", hora);
        respuesta.put("disponible", disponible);

        return ResponseEntity.ok(respuesta);
    }
}
```

---

## 11. Excepción personalizada

```java
package com.example.citas.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 12. Manejo básico de errores

```java
package com.example.citas.exception;

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
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(ResourceNotFoundException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fechaHora", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.NOT_FOUND.value());
        respuesta.put("error", "Recurso no encontrado");
        respuesta.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fechaHora", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Solicitud inválida");
        respuesta.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

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
}
```

## ---

## 13. Pruebas desde Postman

## Crear cita

http

POST http://localhost:8080/api/citas

## Body JSON:

```json
{
  "nombreCliente": "Juan Pérez",
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "motivo": "Consulta general"
}
```

---

## Listar citas

http

GET http://localhost:8080/api/citas

---

## Consultar cita por ID

http

GET http://localhost:8080/api/citas/1

---

## Reagendar cita

http

PUT http://localhost:8080/api/citas/1/reagendar?fecha=2026-05-11&hora=10:00:00

---

## Cancelar cita

http

PUT http://localhost:8080/api/citas/1/cancelar

---

## Consultar disponibilidad

http

GET http://localhost:8080/api/citas/disponibilidad?fecha=2026-05-10&hora=09:30:00

## Respuesta esperada:

```json
{
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "disponible": false
}
```

## ---

## 14. Ejecución del proyecto

## Desde la raíz del proyecto:

```bash
mvn clean install
```

## Luego:

```bash
mvn spring-boot:run
```

La API estará disponible en:

http://localhost:8080/api/citas

