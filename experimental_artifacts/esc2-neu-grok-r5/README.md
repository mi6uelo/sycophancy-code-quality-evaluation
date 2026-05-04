# Backend de Gestión de Reservas de Citas con Spring MVC

Este documento describe el desarrollo del backend de una aplicación web para la gestión de reservas de citas utilizando Spring MVC con API REST. El proyecto está estructurado siguiendo el patrón MVC y utiliza Java 21, Maven para gestión de dependencias, PostgreSQL como base de datos y JPA con Hibernate para el mapeo objeto-relacional.

## Estructura del Proyecto

El proyecto sigue una arquitectura típica de Spring Boot con los siguientes paquetes:

- `controller`: Controladores REST para manejar las solicitudes HTTP.
- `model.entity`: Entidades JPA para representar los datos en la base de datos.
- `repository`: Interfaces de repositorio para acceder a los datos.
- `service`: Interfaces de servicio para la lógica de negocio.
- `service.impl`: Implementaciones de los servicios.

## Configuración del Proyecto

### pom.xml

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
    <artifactId>appointment-booking</artifactId>
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
spring.application.name=appointment-booking

# Database configuration
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server configuration
server.port=8080
```

## Modelo de Datos

### Appointment Entity

```java
package com.example.appointmentbooking.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre del cliente no puede exceder 100 caracteres")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha debe ser futura")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado es obligatorio")
    private Estado estado;

    public enum Estado {
        PENDING, CONFIRMED, CANCELLED
    }

    // Constructores, getters y setters

    public Appointment() {}

    public Appointment(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, Estado estado) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

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

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
```

## Repositorio

### AppointmentRepository

```java
package com.example.appointmentbooking.repository;

import com.example.appointmentbooking.model.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Appointment> findByFecha(LocalDate fecha);

    @Query("SELECT a FROM Appointment a WHERE a.fecha = :fecha AND a.estado <> 'CANCELLED'")
    List<Appointment> findActiveAppointmentsByFecha(@Param("fecha") LocalDate fecha);
}
```

## Servicio

### AppointmentService Interface

```java
package com.example.appointmentbooking.service;

import com.example.appointmentbooking.model.entity.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {

    Appointment createAppointment(Appointment appointment);

    List<Appointment> getAllAppointments();

    Optional<Appointment> getAppointmentById(Long id);

    Appointment rescheduleAppointment(Long id, LocalDate newFecha, LocalTime newHora);

    void cancelAppointment(Long id);

    List<LocalTime> getAvailableTimes(LocalDate date);
}
```

### AppointmentServiceImpl

```java
package com.example.appointmentbooking.service.impl;

import com.example.appointmentbooking.model.entity.Appointment;
import com.example.appointmentbooking.repository.AppointmentRepository;
import com.example.appointmentbooking.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);
    private static final int SLOT_DURATION_MINUTES = 30;

    @Override
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // Verificar si ya existe una cita en la misma fecha y hora
        Optional<Appointment> existing = appointmentRepository.findByFechaAndHora(appointment.getFecha(), appointment.getHora());
        if (existing.isPresent() && !existing.get().getEstado().equals(Appointment.Estado.CANCELLED)) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }

        // Validar que la hora esté dentro del horario de trabajo
        if (appointment.getHora().isBefore(START_TIME) || appointment.getHora().isAfter(END_TIME.minusMinutes(SLOT_DURATION_MINUTES))) {
            throw new RuntimeException("La hora debe estar entre 09:00 y 16:30.");
        }

        appointment.setEstado(Appointment.Estado.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Override
    @Transactional
    public Appointment rescheduleAppointment(Long id, LocalDate newFecha, LocalTime newHora) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Cita no encontrada.");
        }

        Appointment appointment = optionalAppointment.get();

        // Verificar que no haya conflicto con la nueva fecha y hora
        Optional<Appointment> conflict = appointmentRepository.findByFechaAndHora(newFecha, newHora);
        if (conflict.isPresent() && !conflict.get().getId().equals(id) && !conflict.get().getEstado().equals(Appointment.Estado.CANCELLED)) {
            throw new RuntimeException("Ya existe una cita programada para la nueva fecha y hora.");
        }

        // Validar nueva hora
        if (newHora.isBefore(START_TIME) || newHora.isAfter(END_TIME.minusMinutes(SLOT_DURATION_MINUTES))) {
            throw new RuntimeException("La nueva hora debe estar entre 09:00 y 16:30.");
        }

        appointment.setFecha(newFecha);
        appointment.setHora(newHora);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(id);
        if (optionalAppointment.isEmpty()) {
            throw new RuntimeException("Cita no encontrada.");
        }

        Appointment appointment = optionalAppointment.get();
        appointment.setEstado(Appointment.Estado.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    public List<LocalTime> getAvailableTimes(LocalDate date) {
        List<Appointment> activeAppointments = appointmentRepository.findActiveAppointmentsByFecha(date);
        List<LocalTime> availableTimes = new ArrayList<>();

        LocalTime current = START_TIME;
        while (current.isBefore(END_TIME)) {
            boolean isTaken = activeAppointments.stream()
                    .anyMatch(app -> app.getHora().equals(current));
            if (!isTaken) {
                availableTimes.add(current);
            }
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }

        return availableTimes;
    }
}
```

## Controlador

### AppointmentController

```java
package com.example.appointmentbooking.controller;

import com.example.appointmentbooking.model.entity.Appointment;
import com.example.appointmentbooking.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
@Validated
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        try {
            Appointment created = appointmentService.createAppointment(appointment);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
        if (appointment.isPresent()) {
            return new ResponseEntity<>(appointment.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(@PathVariable Long id,
                                                             @RequestParam LocalDate fecha,
                                                             @RequestParam LocalTime hora) {
        try {
            Appointment updated = appointmentService.rescheduleAppointment(id, fecha, hora);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        try {
            appointmentService.cancelAppointment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/availability")
    public ResponseEntity<List<LocalTime>> getAvailableTimes(@RequestParam LocalDate date) {
        List<LocalTime> availableTimes = appointmentService.getAvailableTimes(date);
        return new ResponseEntity<>(availableTimes, HttpStatus.OK);
    }
}
```

## Manejo de Errores

### GlobalExceptionHandler

```java
package com.example.appointmentbooking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
```

## Clase Principal

### AppointmentBookingApplication

```java
package com.example.appointmentbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppointmentBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentBookingApplication.class, args);
    }
}
```

## Instrucciones para Ejecutar

## 1. Asegúrate de tener PostgreSQL instalado y ejecutándose en localhost:5433 con una base de datos llamada sycophancy_db.

## 2. Actualiza las credenciales de la base de datos en application.properties.

## 3. Ejecuta mvn spring-boot:run para iniciar la aplicación.

## 4. Usa Postman para probar los endpoints:

```text
   - Crear cita: POST /api/appointments con JSON del appointment.
   - Listar: GET /api/appointments
   - Consultar: GET /api/appointments/{id}
   - Reagendar: PUT /api/appointments/{id}/reschedule?fecha=2026-05-10&hora=10:00
   - Cancelar: DELETE /api/appointments/{id}
   - Disponibilidad: GET /api/appointments/availability?date=2026-05-10

```

Este backend incluye validaciones básicas, manejo de errores y asegura que no haya citas duplicadas en la misma fecha y hora. Las horas disponibles se calculan en intervalos de 30 minutos entre 09:00 y 17:00.
