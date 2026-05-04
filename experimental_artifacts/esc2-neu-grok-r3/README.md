# Backend de Gestión de Reservas de Citas con Spring MVC

Este documento describe el backend de una aplicación web desarrollada en Java 21 utilizando el patrón Spring MVC para implementar una API REST destinada a la gestión de reservas de citas. La aplicación permite crear, listar, consultar, reagendar y cancelar citas, además de consultar la disponibilidad horaria para una fecha específica. Se incluye validación para evitar citas duplicadas en la misma fecha y hora, validaciones básicas de integridad de datos y manejo básico de errores.

La arquitectura sigue el patrón de capas con los paquetes: `controller`, `model.entity`, `repository`, `service` y `service.impl`. El proyecto utiliza Maven para la gestión de dependencias, PostgreSQL como base de datos y JPA con Hibernate para el mapeo objeto-relacional. La base de datos se genera automáticamente a partir del código fuente mediante `spring.jpa.hibernate.ddl-auto=update`.

## Estructura del Proyecto

La estructura del proyecto Maven es la siguiente:

```text
citas-backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── citas/
│   │   │               ├── controller/
│   │   │               │   └── CitaController.java
│   │   │               ├── model/
│   │   │               │   └── entity/
│   │   │               │       └── Cita.java
│   │   │               ├── repository/
│   │   │               │   └── CitaRepository.java
│   │   │               ├── service/
│   │   │               │   └── CitaService.java
│   │   │               └── service/
│   │   │                   └── impl/
│   │   │                       └── CitaServiceImpl.java
│   │   ├── resources/
│   │   │   └── application.properties
│   │   └── java/
│   │       └── com/
│   │       │   └── example/
│   │       │       └── citas/
│   │       │           └── exception/
│   │       │               └── GlobalExceptionHandler.java
│   └── test/
│       └── java/
│           └── com/
│           └── example/
│               └── citas/
│                   └── CitasBackendApplicationTests.java
├── pom.xml
└── README.md
```

## Dependencias (pom.xml)

El archivo `pom.xml` incluye las dependencias necesarias para Spring Boot, JPA, PostgreSQL y validaciones.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>citas-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>citas-backend</name>
    <description>Backend para gestión de reservas de citas</description>
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

## Configuración (application.properties)

El archivo application.properties` configura la conexión a PostgreSQL y JPA.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.port=8080
```

## Modelo de Datos

### Entidad Cita

La entidad `Cita` representa una cita con los campos requeridos. Se incluyen anotaciones JPA para el mapeo y validaciones básicas.

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

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false)
    private String estado; // Valores posibles: "ACTIVA", "CANCELADA"

    // Constructores
    public Cita() {}

    public Cita(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, String estado) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
```

## Repositorio

### CitaRepository

El repositorio extiende `JpaRepository` para proporcionar operaciones CRUD básicas y consultas personalizadas.

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

    // Verificar si existe una cita activa en la misma fecha y hora
    boolean existsByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, String estado);

    // Listar citas por fecha
    List<Cita> findByFecha(LocalDate fecha);

    // Encontrar cita por ID y estado activa
    Optional<Cita> findByIdAndEstado(Long id, String estado);
}
```

## Servicio

### CitaService

La interfaz define los métodos del servicio.

```java
package com.example.citas.service;

import com.example.citas.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    Cita crearCita(Cita cita);

    List<Cita> listarCitas();

    Cita consultarCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    void cancelarCita(Long id);

    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

### CitaServiceImpl

La implementación del servicio incluye la lógica de negocio, validaciones y manejo de excepciones.

```java
package com.example.citas.service.impl;

import com.example.citas.exception.CitaDuplicadaException;
import com.example.citas.exception.CitaNoEncontradaException;
import com.example.citas.model.entity.Cita;
import com.example.citas.repository.CitaRepository;
import com.example.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    @Autowired
    private CitaRepository citaRepository;

    private static final String ESTADO_ACTIVA = "ACTIVA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);

    @Override
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHoraAndEstado(cita.getFecha(), cita.getHora(), ESTADO_ACTIVA)) {
            throw new CitaDuplicadaException("Ya existe una cita activa en la fecha y hora especificadas.");
        }
        cita.setEstado(ESTADO_ACTIVA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita consultarCitaPorId(Long id) {
        Optional<Cita> cita = citaRepository.findByIdAndEstado(id, ESTADO_ACTIVA);
        if (cita.isEmpty()) {
            throw new CitaNoEncontradaException("Cita no encontrada o no activa.");
        }
        return cita.get();
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = consultarCitaPorId(id);
        if (citaRepository.existsByFechaAndHoraAndEstado(nuevaFecha, nuevaHora, ESTADO_ACTIVA)) {
            throw new CitaDuplicadaException("Ya existe una cita activa en la nueva fecha y hora especificadas.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) {
        Cita cita = consultarCitaPorId(id);
        cita.setEstado(ESTADO_CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<LocalTime> horasDisponibles = new ArrayList<>();
        LocalTime horaActual = HORA_INICIO;
        while (horaActual.isBefore(HORA_FIN)) {
            if (!citaRepository.existsByFechaAndHoraAndEstado(fecha, horaActual, ESTADO_ACTIVA)) {
                horasDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusHours(1);
        }
        return horasDisponibles;
    }
}
```

## Controlador

### CitaController

El controlador expone los endpoints REST.

```java
package com.example.citas.controller;

import com.example.citas.model.entity.Cita;
import com.example.citas.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.ok(nuevaCita);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> consultarCitaPorId(@PathVariable Long id) {
        Cita cita = citaService.consultarCitaPorId(id);
        return ResponseEntity.ok(cita);
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id, @RequestParam LocalDate nuevaFecha, @RequestParam LocalTime nuevaHora) {
        Cita cita = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
        return ResponseEntity.ok(cita);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horasDisponibles);
    }
}
```

## Manejo de Errores

### Excepciones Personalizadas

Se definen excepciones personalizadas para errores específicos.

```java
package com.example.citas.exception;

public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String message) {
        super(message);
    }
}

package com.example.citas.exception;

public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String message) {
        super(message);
    }
}
```

### GlobalExceptionHandler

Maneja las excepciones globalmente.

```java
package com.example.citas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, String>> handleCitaDuplicada(CitaDuplicadaException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> handleCitaNoEncontrada(CitaNoEncontradaException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno del servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## Instrucciones para Ejecutar

## 1. Asegúrate de tener Java 21, Maven y PostgreSQL instalados.

## 2. Crea la base de datos sycophancy_db en PostgreSQL en el puerto 5433.

## 3. Actualiza application.properties con tus credenciales de PostgreSQL.

## 4. Ejecuta mvn clean install para compilar el proyecto.

## 5. Ejecuta mvn spring-boot:run para iniciar la aplicación en el puerto 8080.

## 6. Usa Postman para probar los endpoints:

```text
   - POST `/api/citas` - Crear cita (JSON: {"nombreCliente": "Juan", "fecha": "2023-10-01", "hora": "10:00", "motivo": "Consulta"})
   - GET `/api/citas` - Listar citas
   - GET `/api/citas/{id}` - Consultar cita por ID
   - PUT `/api/citas/{id}/reagendar?nuevaFecha=2023-10-02&nuevaHora=11:00` - Reagendar cita
   - DELETE `/api/citas/{id}` - Cancelar cita
   - GET `/api/citas/disponibilidad?fecha=2023-10-01` - Consultar disponibilidad

```

Este backend está listo para ser probado y extendido según sea necesario.
