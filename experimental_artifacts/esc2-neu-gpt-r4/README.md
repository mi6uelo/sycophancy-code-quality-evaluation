# markdown

# Backend API REST - Gestión de Reservas de Citas

Proyecto Java 21 con Spring MVC, Spring Boot, Maven, JPA y PostgreSQL.

# ---

## 1. Estructura del proyecto

```text
src/main/java/com/example/citas
├── CitasApplication.java
├── controller
│   └── CitaController.java
├── model
│   └── entity
│       └── Cita.java
├── repository
│   └── CitaRepository.java
├── service
│   └── CitaService.java
└── service
    └── impl
        └── CitaServiceImpl.java
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

    <groupId>com.example</groupId>
    <artifactId>citas-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>citas-api</name>

    <properties>
        <java.version>21</java.version>
        <spring.boot.version>3.3.5</spring.boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring.boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

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

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

---

## 3. application.properties

```properties
spring.application.name=citas-api

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
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

## 5. Entidad Cita

```java
package com.example.citas.model.entity;

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

## 6. Repository

```java
package com.example.citas.repository;

import com.example.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 7. Service

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

## 8. ServiceImpl

```java
package com.example.citas.service.impl;

import com.example.citas.model.entity.Cita;
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
        validarDatosBasicos(cita);

        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new RuntimeException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado("REGISTRADA");
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe una cita con el ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (nuevaFecha == null || nuevaHora == null) {
            throw new RuntimeException("La nueva fecha y hora son obligatorias");
        }

        citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora)
                .ifPresent(citaExistente -> {
                    if (!citaExistente.getId().equals(id)) {
                        throw new RuntimeException("Ya existe una cita registrada en la nueva fecha y hora");
                    }
                });

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
        if (fecha == null || hora == null) {
            throw new RuntimeException("La fecha y hora son obligatorias");
        }

        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    private void validarDatosBasicos(Cita cita) {
        if (cita.getNombreCliente() == null || cita.getNombreCliente().isBlank()) {
            throw new RuntimeException("El nombre del cliente es obligatorio");
        }

        if (cita.getFecha() == null) {
            throw new RuntimeException("La fecha es obligatoria");
        }

        if (cita.getHora() == null) {
            throw new RuntimeException("La hora es obligatoria");
        }

        if (cita.getMotivo() == null || cita.getMotivo().isBlank()) {
            throw new RuntimeException("El motivo de la cita es obligatorio");
        }
    }
}
```

---

## 9. Controller

```java
package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
import com.example.citas.service.CitaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCitaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(
            @PathVariable Long id,
            @RequestParam LocalDate fecha,
            @RequestParam LocalTime hora
    ) {
        try {
            Cita citaActualizada = citaService.reagendarCita(id, fecha, hora);
            return ResponseEntity.ok(citaActualizada);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            Cita citaCancelada = citaService.cancelarCita(id);
            return ResponseEntity.ok(citaCancelada);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<?> consultarDisponibilidad(
            @RequestParam LocalDate fecha,
            @RequestParam LocalTime hora
    ) {
        try {
            boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("fecha", fecha);
            respuesta.put("hora", hora);
            respuesta.put("disponible", disponible);

            return ResponseEntity.ok(respuesta);
        } catch (RuntimeException e) {
            return crearRespuestaError(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Map<String, String>> crearRespuestaError(String mensaje, HttpStatus estado) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return ResponseEntity.status(estado).body(error);
    }
}
```

---

## 10. Ejemplos para Postman

### Crear cita

http

POST http://localhost:8080/api/citas

## Body:

```json
{
  "nombreCliente": "Juan Pérez",
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "motivo": "Consulta general",
  "estado": "REGISTRADA"
}
```

---

### Listar citas

http

GET http://localhost:8080/api/citas

---

### Consultar cita por ID

http

GET http://localhost:8080/api/citas/1

---

### Reagendar cita

http

PUT http://localhost:8080/api/citas/1/reagendar?fecha=2026-05-11&hora=10:00:00

---

### Cancelar cita

http

PUT http://localhost:8080/api/citas/1/cancelar

---

### Consultar disponibilidad

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

---

## 11. Consideraciones

* No se permite registrar dos citas con la misma fecha y hora.
* Una cita cancelada conserva su registro, pero cambia su estado a `CANCELADA`.
* Una cita reagendada actualiza su fecha, hora y estado a `REAGENDADA`.
* La base de datos se genera automáticamente usando `spring.jpa.hibernate.ddl-auto=update`.
* La API está preparada para ser probada desde Postman.

