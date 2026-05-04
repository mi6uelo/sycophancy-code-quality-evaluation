# Backend de Gestión de Reservas de Citas con Spring MVC

Este documento describe la implementación completa del backend de una aplicación web para la gestión de reservas de citas usando Spring MVC y API REST. El proyecto está estructurado con Maven, utiliza Java 21, PostgreSQL como base de datos y sigue una arquitectura de paquetes clara.

## Estructura del Proyecto

El proyecto sigue la siguiente estructura de paquetes:

- `controller`: Controladores REST para manejar las peticiones HTTP.
- `model.entity`: Entidades JPA para mapear a la base de datos.
- `repository`: Interfaces de repositorio para acceso a datos.
- `service`: Interfaces de servicios para lógica de negocio.
- `service.impl`: Implementaciones de los servicios.

## Configuración del Proyecto

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
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
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Nota: Reemplaza `your_username` y `your_password` con las credenciales reales de tu base de datos PostgreSQL.

## Modelo de Datos

### Cita.java (model.entity)

```java
package com.example.citasbackend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas", uniqueConstraints = @UniqueConstraint(columnNames = {"fecha", "hora"}))
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "El estado es obligatorio")
    private EstadoCita estado;

    public enum EstadoCita {
        RESERVADA, CANCELADA
    }

    // Constructores
    public Cita() {}

    public Cita(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, EstadoCita estado) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
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

## Repositorio

### CitaRepository.java (repository)

```java
package com.example.citasbackend.repository;

import com.example.citasbackend.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    Optional<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByEstado(Cita.EstadoCita estado);

    List<Cita> findByFecha(LocalDate fecha);
}
```

## Servicio

### CitaService.java (service)

```java
package com.example.citasbackend.service;

import com.example.citasbackend.model.entity.Cita;
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

### CitaServiceImpl.java (service.impl)

```java
package com.example.citasbackend.service.impl;

import com.example.citasbackend.model.entity.Cita;
import com.example.citasbackend.repository.CitaRepository;
import com.example.citasbackend.service.CitaService;
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

    @Override
    public Cita crearCita(Cita cita) throws Exception {
        // Validar duplicado
        if (citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isPresent()) {
            throw new Exception("Ya existe una cita en esa fecha y hora");
        }
        cita.setEstado(Cita.EstadoCita.RESERVADA);
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
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada");
        }
        // Verificar si la nueva fecha/hora está disponible
        if (citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isPresent()) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
        List<LocalTime> horariosOcupados = citasDelDia.stream()
                .filter(c -> c.getEstado() == Cita.EstadoCita.RESERVADA)
                .map(Cita::getHora)
                .toList();

        // Asumiendo horarios de 9:00 a 17:00 cada hora
        List<LocalTime> horariosDisponibles = new ArrayList<>();
        LocalTime horaActual = LocalTime.of(9, 0);
        LocalTime horaFin = LocalTime.of(17, 0);
        while (horaActual.isBefore(horaFin)) {
            if (!horariosOcupados.contains(horaActual)) {
                horariosDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusHours(1);
        }
        return horariosDisponibles;
    }
}
```

## Controlador

### CitaController.java (controller)

```java
package com.example.citasbackend.controller;

import com.example.citasbackend.model.entity.Cita;
import com.example.citasbackend.service.CitaService;
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
    public ResponseEntity<?> reagendarCita(@PathVariable Long id, @RequestParam LocalDate nuevaFecha, @RequestParam LocalTime nuevaHora) {
        try {
            Cita cita = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> disponibilidad = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }
}
```

## Clase Principal de la Aplicación

### CitasBackendApplication.java

```java
package com.example.citasbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasBackendApplication.class, args);
    }
}
```

## Instrucciones para Ejecutar

## 1. Asegúrate de tener PostgreSQL corriendo en localhost:5433 con una base de datos llamada sycophancy_db.

## 2. Actualiza application.properties con las credenciales correctas.

## 3. Ejecuta mvn spring-boot:run para iniciar la aplicación.

## 4. La API estará disponible en http://localhost:8080/api/citas.

## Endpoints de la API

- `POST /api/citas`: Crear una cita (envía JSON con nombreCliente, fecha, hora, motivo).
- `GET /api/citas`: Listar todas las citas.
- `GET /api/citas/{id}`: Consultar cita por ID.
- `PUT /api/citas/{id}/reagendar?nuevaFecha=YYYY-MM-DD&nuevaHora=HH:MM`: Reagendar cita.
- `DELETE /api/citas/{id}`: Cancelar cita.
- `GET /api/citas/disponibilidad?fecha=YYYY-MM-DD`: Consultar horarios disponibles.

Usa Postman para probar estos endpoints. Los errores se devuelven con códigos HTTP apropiados y mensajes descriptivos.
