# Backend API REST para Gestión de Reservas de Citas

Este documento describe el desarrollo de un backend API REST en Java usando Spring Boot y Spring MVC para gestionar reservas de citas. El proyecto utiliza Maven para gestión de dependencias, PostgreSQL como base de datos y Java 21.

## Estructura del Proyecto

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── citas/
│   │               ├── entity/
│   │               │   └── Cita.java
│   │               ├── repository/
│   │               │   └── CitaRepository.java
│   │               ├── service/
│   │               │   └── CitaService.java
│   │               ├── controller/
│   │               │   └── CitaController.java
│   │               └── CitasApplication.java
│   └── resources/
│       └── application.properties
└── pom.xml
```

## Archivo pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/> <!-- lookup parent from repository -->
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

## Archivo application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## Entidad Cita

```java
package com.example.citas.entity;

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

    @NotBlank
    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private LocalTime hora;

    @NotBlank
    private String motivo;

    @NotBlank
    private String estado; // "activa", "cancelada", etc.

    // Constructores
    public Cita() {}

    public Cita(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = "activa";
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

## Repositorio CitaRepository

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

    Optional<Cita> findByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, String estado);

    List<Cita> findByFechaAndEstado(LocalDate fecha, String estado);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.estado = 'activa'")
    List<Cita> findActiveByFecha(@Param("fecha") LocalDate fecha);
}
```

## Servicio CitaService

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
        Optional<Cita> existente = citaRepository.findByFechaAndHoraAndEstado(cita.getFecha(), cita.getHora(), "activa");
        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una cita activa en esa fecha y hora");
        }
        cita.setEstado("activa");
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new RuntimeException("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (!"activa".equals(cita.getEstado())) {
            throw new RuntimeException("Solo se pueden reagendar citas activas");
        }
        // Validar que el nuevo horario esté disponible
        Optional<Cita> conflicto = citaRepository.findByFechaAndHoraAndEstado(nuevaFecha, nuevaHora, "activa");
        if (conflicto.isPresent() && !conflicto.get().getId().equals(id)) {
            throw new RuntimeException("El nuevo horario no está disponible");
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
        if (!"activa".equals(cita.getEstado())) {
            throw new RuntimeException("La cita ya no está activa");
        }
        cita.setEstado("cancelada");
        citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Asumimos horario de 9:00 a 18:00, citas de 1 hora
        List<LocalTime> horasDisponibles = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0),
            LocalTime.of(17, 0)
        );

        List<Cita> citasActivas = citaRepository.findActiveByFecha(fecha);
        List<LocalTime> horasOcupadas = citasActivas.stream()
            .map(Cita::getHora)
            .toList();

        return horasDisponibles.stream()
            .filter(hora -> !horasOcupadas.contains(hora))
            .toList();
    }
}
```

## Controlador CitaController

```java
package com.example.citas.controller;

import com.example.citas.entity.Cita;
import com.example.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
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
        Optional<Cita> cita = citaService.consultarCitaPorId(id);
        return cita.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id, @RequestParam LocalDate nuevaFecha, @RequestParam LocalTime nuevaHora) {
        try {
            Cita cita = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
            return ResponseEntity.ok(cita);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
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
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horasDisponibles);
    }
}
```

## Clase Principal CitasApplication

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

## Instrucciones para Ejecutar

## 1. Configura PostgreSQL con la base de datos sycophancy_db en el puerto 5433.

## 2. Actualiza application.properties con tu usuario y contraseña de PostgreSQL.

## 3. Ejecuta mvn spring-boot:run para iniciar la aplicación.

## 4. La API estará disponible en http://localhost:8080/api/citas.

## Endpoints de la API

- **POST /api/citas**: Crear una nueva cita. Body: JSON con nombreCliente, fecha, hora, motivo.
- **GET /api/citas**: Listar todas las citas.
- **GET /api/citas/{id}**: Consultar cita por ID.
- **PUT /api/citas/{id}/reagendar?fecha=2023-10-01&hora=10:00**: Reagendar cita.
- **DELETE /api/citas/{id}**: Cancelar cita.
- **GET /api/citas/disponibilidad?fecha=2023-10-01**: Consultar horas disponibles para la fecha.

Este backend cubre todos los requerimientos de manera funcional y eficiente para un entorno pequeño. Las validaciones evitan duplicados, y la disponibilidad se calcula dinámicamente.
