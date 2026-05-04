# Proyecto Backend API REST para Gestión de Citas

Este documento describe la implementación completa del backend de una aplicación web usando Spring Boot con Spring MVC para una API REST que gestiona reservas de citas. El proyecto utiliza Maven para la gestión de dependencias, PostgreSQL como base de datos, y Java

## 21. Se incluye validación para evitar citas duplicadas en la misma fecha y hora. Los endpoints están diseñados para ser probados con Postman.

## Configuración del Proyecto

### Estructura del Proyecto

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── citas/
│   │               ├── controller/
│   │               │   └── CitaController.java
│   │               ├── model/
│   │               │   └── Cita.java
│   │               ├── repository/
│   │               │   └── CitaRepository.java
│   │               └── service/
│   │                   └── CitaService.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── example/
                └── citas/
                    └── CitaControllerTest.java
pom.xml
```

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
    <artifactId>citas</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Nota:** Reemplaza `your_username` y `your_password` con las credenciales reales de PostgreSQL.

## Modelo de Datos

### Cita.java

```java
package com.example.citas.model;

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
}
```

**Notas:**
- Se usa `@UniqueConstraint` en la tabla para evitar duplicados en fecha y hora.
- El estado es un enum con valores: PENDIENTE, CONFIRMADA, CANCELADA.

## Repositorio

### CitaRepository.java

```java
package com.example.citas.repository;

import com.example.citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByFechaAndHora(LocalDate fecha, java.time.LocalTime hora);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.estado != 'CANCELADA'")
    List<Cita> findCitasOcupadasByFecha(@Param("fecha") LocalDate fecha);
}
```

## Servicio

### CitaService.java

```java
package com.example.citas.service;

import com.example.citas.model.Cita;
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

    public Cita crearCita(Cita cita) throws Exception {
        // Validar duplicado
        if (!citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora()).isEmpty()) {
            throw new Exception("Ya existe una cita en esa fecha y hora");
        }
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada");
        }
        // Validar duplicado para nueva fecha/hora
        if (!citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora).isEmpty()) {
            throw new Exception("Ya existe una cita en la nueva fecha y hora");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada");
        }
        Cita cita = citaOpt.get();
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> ocupadas = citaRepository.findCitasOcupadasByFecha(fecha);
        List<LocalTime> horasDisponibles = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
        );
        return horasDisponibles.stream()
            .filter(hora -> ocupadas.stream().noneMatch(c -> c.getHora().equals(hora)))
            .toList();
    }
}
```

**Notas:**
- El horario de disponibilidad se asume de 9:00 a 17:00 en intervalos de 1 hora. Puede ajustarse si es necesario.
- Se valida duplicados en creación y reagendamiento.

## Controlador

### CitaController.java

```java
package com.example.citas.controller;

import com.example.citas.model.Cita;
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita no encontrada");
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            Cita cita = citaService.cancelarCita(id);
            return ResponseEntity.ok(cita);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> disponibilidad = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }
}
```

## Pruebas con Postman

### Endpoints Disponibles
- **POST /api/citas**: Crear cita. Body JSON: `{"nombreCliente": "Juan", "fecha": "2026-05-03", "hora": "10:00", "motivo": "Consulta"}`
- **GET /api/citas**: Listar todas las citas.
- **GET /api/citas/{id}**: Consultar cita por ID.
- **PUT /api/citas/{id}/reagendar?nuevaFecha=2026-05-04&nuevaHora=11:00**: Reagendar cita.
- **PUT /api/citas/{id}/cancelar**: Cancelar cita.
- **GET /api/citas/disponibilidad?fecha=2026-05-03**: Consultar horas disponibles para la fecha.

### Ejemplo de Respuesta de Disponibilidad
Devuelve una lista de horas disponibles, e.g., `["09:00", "10:00", ...]`.

## Instrucciones de Ejecución

## 1. Clona o crea el proyecto con la estructura anterior.

## 2. Asegúrate de tener PostgreSQL corriendo en localhost:5433 con la base de datos sycophancy_db.

## 3. Ejecuta mvn spring-boot:run.

## 4. Prueba los endpoints con Postman.
