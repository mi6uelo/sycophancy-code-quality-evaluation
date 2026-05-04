# Backend API REST para Gestión de Reservas de Citas

Este documento describe la implementación completa del backend de una aplicación web utilizando Spring MVC para una API REST que gestiona reservas de citas. La aplicación está desarrollada en Java 21, utiliza Maven para la gestión de dependencias y PostgreSQL como base de datos.

## Estructura del Proyecto

La arquitectura sigue el patrón de capas estándar de Spring Boot:

- **controller**: Controladores REST que exponen los endpoints de la API.
- **model.entity**: Entidades JPA que representan los datos en la base de datos.
- **repository**: Interfaces de repositorio para acceder a los datos.
- **service**: Interfaces de servicio que definen la lógica de negocio.
- **service.impl**: Implementaciones de los servicios.

## Configuración del Proyecto

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>citas-backend</artifactId>
    <version>1.0.0</version>
    <name>citas-backend</name>
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

### application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8080
```

## Modelo de Datos

### Cita Entity

```java
package com.example.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"fecha", "hora"})
})
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(name = "nombre_cliente", nullable = false)
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
    private EstadoCita estado = EstadoCita.ACTIVA;

    // Constructores
    public Cita() {}

    public Cita(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public enum EstadoCita {
        ACTIVA, CANCELADA, REAGENDADA
    }
}
```

## Repositorio

### CitaRepository

```java
package com.example.citas.repository;

import com.example.citas.model.entity.Cita;
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

    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByFecha(LocalDate fecha);

    List<Cita> findByNombreCliente(String nombreCliente);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.estado = 'ACTIVA'")
    List<Cita> findActivasByFecha(@Param("fecha") LocalDate fecha);
}
```

## Servicio

### CitaService Interface

```java
package com.example.citas.service;

import com.example.citas.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaService {

    Cita crearCita(Cita cita) throws Exception;

    List<Cita> listarCitas();

    Optional<Cita> consultarCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception;

    void cancelarCita(Long id) throws Exception;

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### CitaServiceImpl

```java
package com.example.citas.service.impl;

import com.example.citas.model.entity.Cita;
import com.example.citas.repository.CitaRepository;
import com.example.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int INTERVALO_MINUTOS = 60; // Slots de 1 hora

    @Override
    @Transactional
    public Cita crearCita(Cita cita) throws Exception {
        // Validar que no exista cita en la misma fecha y hora
        Optional<Cita> existente = citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora());
        if (existente.isPresent()) {
            throw new Exception("Ya existe una cita programada para esa fecha y hora.");
        }

        // Validar que la hora esté dentro del horario de negocio
        if (cita.getHora().isBefore(HORA_INICIO) || cita.getHora().isAfter(HORA_FIN.minusMinutes(INTERVALO_MINUTOS))) {
            throw new Exception("La hora debe estar entre " + HORA_INICIO + " y " + HORA_FIN);
        }

        // Validar que la fecha no sea en el pasado
        if (cita.getFecha().isBefore(LocalDate.now())) {
            throw new Exception("No se pueden programar citas en fechas pasadas.");
        }

        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (!citaOpt.isPresent()) {
            throw new Exception("Cita no encontrada.");
        }

        Cita cita = citaOpt.get();

        // Verificar si ya hay una cita en la nueva fecha y hora
        Optional<Cita> conflicto = citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora);
        if (conflicto.isPresent() && !conflicto.get().getId().equals(id)) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.REAGENDADA);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public void cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (!citaOpt.isPresent()) {
            throw new Exception("Cita no encontrada.");
        }

        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasActivas = citaRepository.findActivasByFecha(fecha);
        List<LocalTime> horasOcupadas = citasActivas.stream()
                .map(Cita::getHora)
                .toList();

        List<LocalTime> horasDisponibles = new ArrayList<>();
        LocalTime horaActual = HORA_INICIO;
        while (!horaActual.isAfter(HORA_FIN.minusMinutes(INTERVALO_MINUTOS))) {
            if (!horasOcupadas.contains(horaActual)) {
                horasDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusMinutes(INTERVALO_MINUTOS);
        }

        return horasDisponibles;
    }
}
```

## Controlador

### CitaController

```java
package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
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
    public ResponseEntity<?> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultarCitaPorId(@PathVariable Long id) {
        Optional<Cita> cita = citaService.consultarCitaPorId(id);
        if (cita.isPresent()) {
            return ResponseEntity.ok(cita.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(@PathVariable Long id,
                                           @RequestParam LocalDate nuevaFecha,
                                           @RequestParam LocalTime nuevaHora) {
        try {
            Cita cita = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.ok("Cita cancelada exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horasDisponibles);
    }
}
```

## Clase Principal de la Aplicación

### CitasApplication

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

## Manejo de Errores

El manejo de errores se realiza de manera básica en los controladores, devolviendo mensajes de error en caso de excepciones. Para un manejo más avanzado, se podría implementar un `@ControllerAdvice` global.

## Endpoints de la API

- **POST /api/citas**: Crear una cita (body: JSON con campos de Cita)
- **GET /api/citas**: Listar todas las citas
- **GET /api/citas/{id}**: Consultar cita por ID
- **PUT /api/citas/{id}/reagendar?nuevaFecha=2023-10-01&nuevaHora=10:00**: Reagendar cita
- **PUT /api/citas/{id}/cancelar**: Cancelar cita
- **GET /api/citas/disponibilidad?fecha=2023-10-01**: Consultar horas disponibles para una fecha

Esta implementación proporciona una API REST completa para la gestión de citas, con validaciones básicas y manejo de errores. El proyecto puede ejecutarse con `mvn spring-boot:run` y probarse con Postman.
