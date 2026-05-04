# Backend API para Gestión de Reservas de Citas

Este documento describe la implementación completa del backend de una aplicación web utilizando Spring Boot y el patrón MVC para una API REST que gestiona reservas de citas. La API permite operaciones CRUD básicas, reagendar citas, cancelar citas y consultar disponibilidad de horarios. Se utiliza Java 21, Maven para dependencias, PostgreSQL como base de datos, y JPA para el mapeo objeto-relacional.

## Arquitectura General

La aplicación sigue una arquitectura en capas típica de Spring MVC:
- **Controller**: Maneja las solicitudes HTTP y respuestas.
- **Service**: Contiene la lógica de negocio.
- **Repository**: Interacción con la base de datos.
- **Model.Entity**: Entidades JPA.

Se incluyen validaciones básicas de integridad de datos (usando Bean Validation) y manejo de errores personalizado.

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
        <version>3.2.0</version> <!-- Compatible con Java 21 -->
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>citas-api</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

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

# Configuración de la base de datos

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuración de JPA
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Puerto del servidor
server.port=8080
```

**Nota**: Reemplaza `your_username` y `your_password` con las credenciales reales de PostgreSQL.

## Arquitectura del Código

### Paquete model.entity

#### Cita.java

```java
package com.example.citasapi.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas", uniqueConstraints = @UniqueConstraint(columnNames = {"fecha", "hora"}))
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre del cliente no puede exceder 100 caracteres")
    @Column(nullable = false)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha debe ser futura")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    public enum EstadoCita {
        PENDIENTE, CONFIRMADA, CANCELADA
    }

    // Constructores
    public Cita() {}

    public Cita(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
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

## Notas sobre la entidad:
- Se usa `@UniqueConstraint` en la tabla para evitar duplicados en fecha y hora.
- Validaciones con Bean Validation para campos obligatorios y formatos.
- El estado inicial es `PENDIENTE`.

### Paquete repository

#### CitaRepository.java

```java
package com.example.citasapi.repository;

import com.example.citasapi.model.entity.Cita;
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

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.estado != 'CANCELADA'")
    List<Cita> findOcupadasByFecha(@Param("fecha") LocalDate fecha);
}
```

### Paquete service

#### CitaService.java

```java
package com.example.citasapi.service;

import com.example.citasapi.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface CitaService {

    Cita crearCita(Cita cita) throws Exception;

    List<Cita> listarCitas();

    Optional<Cita> consultarCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception;

    Cita cancelarCita(Long id) throws Exception;

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### Paquete service.impl

#### CitaServiceImpl.java

```java
package com.example.citasapi.service.impl;

import com.example.citasapi.model.entity.Cita;
import com.example.citasapi.repository.CitaRepository;
import com.example.citasapi.service.CitaService;
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

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0); // 9:00 AM
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);  // 5:00 PM
    private static final int INTERVALO_MINUTOS = 30; // Citas cada 30 minutos

    @Override
    @Transactional
    public Cita crearCita(Cita cita) throws Exception {
        if (citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isPresent()) {
            throw new Exception("Ya existe una cita en esa fecha y hora.");
        }
        cita.setEstado(Cita.EstadoCita.PENDIENTE);
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
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada.");
        }
        if (citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isPresent()) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> ocupadas = citaRepository.findOcupadasByFecha(fecha);
        List<LocalTime> disponibles = new ArrayList<>();
        LocalTime horaActual = HORA_INICIO;
        while (horaActual.isBefore(HORA_FIN)) {
            boolean ocupada = ocupadas.stream().anyMatch(c -> c.getHora().equals(horaActual));
            if (!ocupada) {
                disponibles.add(horaActual);
            }
            horaActual = horaActual.plusMinutes(INTERVALO_MINUTOS);
        }
        return disponibles;
    }
}
```

## Notas sobre el servicio:
- Validación de duplicados en creación y reagendamiento.
- Horarios disponibles: De 9:00 AM a 5:00 PM, cada 30 minutos, excluyendo ocupadas.

### Paquete controller

#### CitaController.java

```java
package com.example.citasapi.controller;

import com.example.citasapi.model.entity.Cita;
import com.example.citasapi.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/citas")
@Validated
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
    public ResponseEntity<?> reagendarCita(@PathVariable Long id, @RequestParam LocalDate fecha, @RequestParam LocalTime hora) {
        try {
            Cita cita = citaService.reagendarCita(id, fecha, hora);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            Cita cita = citaService.cancelarCita(id);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> disponibles = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(disponibles);
    }
}
```

## Notas sobre el controlador:
- Uso de `@Valid` para validaciones automáticas.
- Manejo de errores con try-catch y respuestas HTTP apropiadas.
- Endpoints RESTful.

## Manejo de Errores y Validaciones

- **Validaciones**: Implementadas con Bean Validation en la entidad y activadas en el controlador con `@Valid`.
- **Errores personalizados**: Excepciones lanzadas desde el servicio y manejadas en el controlador con mensajes descriptivos.
- **Códigos HTTP**: 201 para creación exitosa, 400 para errores de negocio, 404 para no encontrado, 200 para operaciones exitosas.

## Endpoints de la API

- **POST /api/citas**: Crear cita (body: JSON con campos de Cita).
- **GET /api/citas**: Listar todas las citas.
- **GET /api/citas/{id}**: Consultar cita por ID.
- **PUT /api/citas/{id}/reagendar?fecha=YYYY-MM-DD&hora=HH:MM**: Reagendar cita.
- **PUT /api/citas/{id}/cancelar**: Cancelar cita.
- **GET /api/citas/disponibilidad?fecha=YYYY-MM-DD**: Consultar horarios disponibles en una fecha.

## Ejecución y Pruebas

## 1. Configura PostgreSQL y crea la base de datos sycophancy_db.

## 2. Actualiza application.properties con credenciales.

## 3. Ejecuta mvn spring-boot:run.

## 4. Usa Postman para probar los endpoints.

Este backend está listo para ser extendido con autenticación, logging avanzado o integración con frontends.
