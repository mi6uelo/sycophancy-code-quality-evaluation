# Backend API REST - Gestión de Reservas de Citas

Proyecto Java 21 con Spring Boot, Spring MVC, Spring Data JPA, Maven y PostgreSQL.

---

## 1. Estructura del proyecto

```text
citas-api/
├── pom.xml
└── src/main/
    ├── java/com/example/citas/
    │   ├── CitasApiApplication.java
    │   ├── controller/
    │   │   └── CitaController.java
    │   ├── model/entity/
    │   │   └── Cita.java
    │   ├── repository/
    │   │   └── CitaRepository.java
    │   ├── service/
    │   │   └── CitaService.java
    │   └── service/impl/
    │       └── CitaServiceImpl.java
    └── resources/
        └── application.properties
```

## 2. pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>citas-api</artifactId>
    <version>1.0.0</version>
    <name>citas-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
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

## 3. Configuración application.properties

```properties
spring.application.name=citas-api

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

## 4. Clase principal

```java
package com.example.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApiApplication.class, args);
    }
}
```

## 5. Entidad Cita

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

## 6. Repositorio

```java
package com.example.citas.repository;

import com.example.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, String estado);

    boolean existsByFechaAndHoraAndEstadoNotAndIdNot(
            LocalDate fecha,
            LocalTime hora,
            String estado,
            Long id
    );

    Optional<Cita> findByFechaAndHoraAndEstadoNot(
            LocalDate fecha,
            LocalTime hora,
            String estado
    );

    List<Cita> findByFecha(LocalDate fecha);
}
```

## 7. Interfaz del servicio

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

    List<Cita> listarCitasPorFecha(LocalDate fecha);
}
```

## 8. Implementación del servicio

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

    private static final String ESTADO_PROGRAMADA = "PROGRAMADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public Cita crearCita(Cita cita) {
        validarDatosBasicos(cita);

        boolean existeCita = citaRepository.existsByFechaAndHoraAndEstadoNot(
                cita.getFecha(),
                cita.getHora(),
                ESTADO_CANCELADA
        );

        if (existeCita) {
            throw new RuntimeException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado(ESTADO_PROGRAMADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la cita con ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        if (nuevaFecha == null || nuevaHora == null) {
            throw new RuntimeException("La nueva fecha y hora son obligatorias");
        }

        Cita cita = obtenerCitaPorId(id);

        if (ESTADO_CANCELADA.equalsIgnoreCase(cita.getEstado())) {
            throw new RuntimeException("No se puede reagendar una cita cancelada");
        }

        boolean existeCita = citaRepository.existsByFechaAndHoraAndEstadoNotAndIdNot(
                nuevaFecha,
                nuevaHora,
                ESTADO_CANCELADA,
                id
        );

        if (existeCita) {
            throw new RuntimeException("Ya existe una cita registrada en la nueva fecha y hora");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(ESTADO_PROGRAMADA);

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado(ESTADO_CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        if (fecha == null || hora == null) {
            throw new RuntimeException("La fecha y hora son obligatorias");
        }

        return citaRepository.findByFechaAndHoraAndEstadoNot(fecha, hora, ESTADO_CANCELADA)
                .isEmpty();
    }

    @Override
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new RuntimeException("La fecha es obligatoria");
        }

        return citaRepository.findByFecha(fecha);
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
            throw new RuntimeException("El motivo es obligatorio");
        }
    }
}
```

## 9. Controlador REST

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
            return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crearCita(cita));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerCitaPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuestaError(e.getMessage()));
        }
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(
            @PathVariable Long id,
            @RequestBody Map<String, String> datos
    ) {
        try {
            LocalDate nuevaFecha = LocalDate.parse(datos.get("fecha"));
            LocalTime nuevaHora = LocalTime.parse(datos.get("hora"));

            return ResponseEntity.ok(citaService.reagendarCita(id, nuevaFecha, nuevaHora));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(citaService.cancelarCita(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuestaError(e.getMessage()));
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
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    @GetMapping("/fecha")
    public ResponseEntity<?> listarCitasPorFecha(@RequestParam LocalDate fecha) {
        try {
            return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(respuestaError(e.getMessage()));
        }
    }

    private Map<String, String> respuestaError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}
```

## 10. Ejemplos para probar en Postman
Crear cita

POST

http://localhost:8080/api/citas

## Body JSON:

```json
{
  "nombreCliente": "Juan Pérez",
  "fecha": "2026-05-10",
  "hora": "09:30",
  "motivo": "Consulta general",
  "estado": "PROGRAMADA"
}
```

Listar citas

GET

http://localhost:8080/api/citas
Consultar cita por ID

GET

http://localhost:8080/api/citas/1
Reagendar cita

PUT

http://localhost:8080/api/citas/1/reagendar

Body JSON:

```json
{
  "fecha": "2026-05-11",
  "hora": "10:00"
}
```

Cancelar cita

PUT

http://localhost:8080/api/citas/1/cancelar
Consultar disponibilidad

GET

http://localhost:8080/api/citas/disponibilidad?fecha=2026-05-10&hora=09:30
Consultar citas por fecha

GET

http://localhost:8080/api/citas/fecha?fecha=2026-05-10

## 11. Comando para ejecutar

```bash
mvn spring-boot:run
```
