# Backend API REST para Gestión de Reservas de Citas

Este documento describe la implementación completa del backend de una aplicación web usando Spring MVC para gestionar reservas de citas. La API REST permite operaciones CRUD básicas sobre citas, con validaciones para evitar duplicados en fecha y hora.

## Estructura del Proyecto

El proyecto sigue la estructura estándar de Spring Boot con Maven. Se utiliza PostgreSQL como base de datos, y las entidades se generan automáticamente mediante Hibernate.

## - src/main/java/com/example/citas

```text
  - **entity**: Contiene la entidad JPA `Cita`.
  - **repository**: Contiene el repositorio `CitaRepository`.
  - **service**: Contiene el servicio `CitaService` con la lógica de negocio.
  - **controller**: Contiene el controlador REST `CitaController`.
```

- **src/main/resources**: Contiene application.properties`.
- **pom.xml**: Archivo de configuración de Maven.

## Configuración de Maven (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>citas</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>citas</name>
    <description>API REST para gestión de citas</description>

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
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
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

## Configuración de la Aplicación (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Nota: Reemplaza `username` y `password` con las credenciales reales de tu base de datos PostgreSQL.

## Entidad Cita

La entidad representa una cita en la base de datos. Incluye validaciones para asegurar que los campos obligatorios no estén vacíos y que la fecha sea futura.

```java
package com.example.citas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @Future(message = "La fecha debe ser futura")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    public enum EstadoCita {
        PENDIENTE, CONFIRMADA, CANCELADA
    }

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

## Repositorio CitaRepository

El repositorio extiende JpaRepository para operaciones básicas de CRUD.

```java
package com.example.citas.repository;

import com.example.citas.entity.Cita;
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

    // Buscar cita por fecha y hora para validar duplicados
    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    // Listar citas por fecha (para disponibilidad)
    List<Cita> findByFecha(LocalDate fecha);

    // Buscar citas por estado
    List<Cita> findByEstado(Cita.EstadoCita estado);
}
```

## Servicio CitaService

El servicio contiene la lógica de negocio, incluyendo validaciones para evitar duplicados y gestión de estados.

```java
package com.example.citas.service;

import com.example.citas.entity.Cita;
import com.example.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    public Cita crearCita(Cita cita) {
        // Validar que no exista cita en la misma fecha y hora
        if (citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isPresent()) {
            throw new RuntimeException("Ya existe una cita en esa fecha y hora");
        }
        cita.setEstado(Cita.EstadoCita.CONFIRMADA);
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> consultarCita(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new RuntimeException("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new RuntimeException("No se puede reagendar una cita cancelada");
        }
        // Validar que la nueva fecha y hora no esté ocupada
        if (citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isPresent()) {
            throw new RuntimeException("La nueva fecha y hora ya están ocupadas");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    public void cancelarCita(Long id) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new RuntimeException("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
        // Asumir horario de 9:00 a 17:00, citas cada hora
        List<LocalTime> horasDisponibles = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
        );
        // Filtrar horas ocupadas (solo citas confirmadas)
        return horasDisponibles.stream()
            .filter(hora -> citasDelDia.stream()
                .noneMatch(c -> c.getHora().equals(hora) && c.getEstado() == Cita.EstadoCita.CONFIRMADA))
            .toList();
    }
}
```

## Controlador CitaController

El controlador define los endpoints REST para la API.

```java
package com.example.citas.controller;

import com.example.citas.entity.Cita;
import com.example.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> consultarCita(@PathVariable Long id) {
        Optional<Cita> cita = citaService.consultarCita(id);
        return cita.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id,
                                               @RequestParam LocalDate fecha,
                                               @RequestParam LocalTime hora) {
        try {
            Cita cita = citaService.reagendarCita(id, fecha, hora);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horas = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horas);
    }
}
```

## Clase Principal de la Aplicación

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

## Notas Adicionales

- **Estados de Cita**: Las citas pueden estar en estado PENDIENTE (inicial), CONFIRMADA (después de crear), o CANCELADA.
- **Horario de Disponibilidad**: Se asume un horario de 9:00 a 17:00 con citas cada hora. Puedes ajustar en el servicio si es necesario.
- **Validaciones**: Se usan anotaciones de Jakarta Validation para campos obligatorios. Para fechas futuras, se valida en la entidad.
- **Errores**: Las excepciones se manejan con RuntimeException y se devuelven códigos de estado HTTP apropiados.
- **Pruebas**: Usa Postman para probar los endpoints. Ejemplos de URLs:

```text
  - POST /api/citas (crear cita)
  - GET /api/citas (listar)
  - GET /api/citas/{id} (consultar)
  - PUT /api/citas/{id}/reagendar?fecha=2023-12-01&hora=10:00 (reagendar)
  - PUT /api/citas/{id}/cancelar (cancelar)
  - GET /api/citas/disponibilidad?fecha=2023-12-01 (disponibilidad)

```

Esta implementación es funcional y eficiente para un entorno pequeño, priorizando rapidez de entrega sin complejidad innecesaria.
