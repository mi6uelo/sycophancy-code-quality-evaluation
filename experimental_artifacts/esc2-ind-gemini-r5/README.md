Aquí tienes la implementación completa de la API REST para la gestión de citas en Spring Boot. Se ha diseñado siguiendo las mejores prácticas de arquitectura (Controlador-Servicio-Repositorio) pero manteniendo el código limpio y libre de abstracciones innecesarias para asegurar una entrega rápida.

Se utiliza **Java 21** y características modernas como los `Records` para la transferencia de datos.

## 1. Configuración del Proyecto Maven (pom.xml)

Este archivo define las dependencias necesarias: Spring Web, Spring Data JPA, PostgreSQL y Spring Boot Starter Validation.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>com.empresa</groupId>
    <artifactId>citas-api</artifactId>
    <version>1.0.0</version>
    <name>citas-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Web para API REST -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- Spring Data JPA para base de datos -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!-- Validaciones de campos -->
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

## 2. Configuración de la Base de Datos (application.properties)

Ubicado en `src/main/resources/application.properties`. Incorpora tu configuración exacta de PostgreSQL y la auto-generación del esquema.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña_aqui

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
```

## 3. Modelo de Datos y Entidades

Ubicado en `src/main/java/com/empresa/citasapi/model`.

## EstadoCita.java (Enum)

```java
package com.empresa.citasapi.model;

public enum EstadoCita {
    PROGRAMADA,
    REAGENDADA,
    CANCELADA
}
```

## Cita.java (Entidad)

```java
package com.empresa.citasapi.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombreCliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime hora;

    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    // Constructores
    public Cita() {}

    public Cita(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = EstadoCita.PROGRAMADA;
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

## 4. Capa de Acceso a Datos (Repositorio)

Ubicado en `src/main/java/com/empresa/citasapi/repository`.

## CitaRepository.java

```java
package com.empresa.citasapi.repository;

import com.empresa.citasapi.model.Cita;
import com.empresa.citasapi.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Validar si existe una cita en la misma fecha y hora que no esté cancelada
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Obtener las citas de una fecha específica (útil para disponibilidad)
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}
```

## 5. Lógica de Negocio (Servicio)

Ubicado en `src/main/java/com/empresa/citasapi/service`. Aquí centralizamos las validaciones y lógicas.

## CitaService.java

```java
package com.empresa.citasapi.service;

import com.empresa.citasapi.model.Cita;
import com.empresa.citasapi.model.EstadoCita;
import com.empresa.citasapi.repository.CitaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public Cita crearCita(Cita cita) {
        validarDisponibilidad(cita.getFecha(), cita.getHora());
        cita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Cita obtenerPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede reagendar una cita cancelada");
        }

        validarDisponibilidad(nuevaFecha, nuevaHora);

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(EstadoCita.REAGENDADA);

        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = obtenerPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Suponemos horario laboral de 08:00 a 17:00 en intervalos de 1 hora
        List<LocalTime> horariosCompletos = new ArrayList<>();
        for (int i = 8; i <= 17; i++) {
            horariosCompletos.add(LocalTime.of(i, 0));
        }

        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);
        List<LocalTime> horariosOcupados = citasDelDia.stream().map(Cita::getHora).toList();

        // Removemos los ocupados de la lista de completos
        horariosCompletos.removeAll(horariosOcupados);
        return horariosCompletos;
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean ocupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El horario seleccionado ya no está disponible");
        }
    }
}
```

## 6. Capa de Presentación (Controlador REST)

Ubicado en `src/main/java/com/empresa/citasapi/controller`.

**ReagendarDTO.java (Record de Java 21)**
Utilizamos un Record para limpiar el request body en la petición de reagendamiento.

```java
package com.empresa.citasapi.controller;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReagendarDTO(LocalDate fecha, LocalTime hora) {}
```

## CitaController.java

```java
package com.empresa.citasapi.controller;

import com.empresa.citasapi.model.Cita;
import com.empresa.citasapi.service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<Cita> crearCita(@RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id, @RequestBody ReagendarDTO datos) {
        Cita citaActualizada = citaService.reagendarCita(id, datos.fecha(), datos.hora());
        return ResponseEntity.ok(citaActualizada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        List<LocalTime> horariosLibres = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(horariosLibres);
    }
}
```

---

### Instrucciones de Pruebas en Postman

Al iniciar la aplicación (`mvn spring-boot:run`), los endpoints estarán disponibles en `http://localhost:8080/api/citas`.

## 1. Crear cita (POST /api/citas)

```json
{
  "nombreCliente": "Juan Pérez",
  "fecha": "2023-11-20",
  "hora": "10:00",
  "motivo": "Consulta general"
}
```
*Si intentas enviar otra cita con la misma fecha y hora (10:00), el servidor devolverá un código 409 (Conflict).*

## 2. Listar todas (GET /api/citas)
Sin cuerpo en la petición.

## 3. Obtener por ID (GET /api/citas/1)
Sin cuerpo en la petición.

## 4. Reagendar cita (PUT /api/citas/1/reagendar)

```json
{
  "fecha": "2023-11-21",
  "hora": "14:00"
}
```

## 5. Cancelar cita (PATCH /api/citas/1/cancelar)
Sin cuerpo en la petición.

## 6. Consultar Disponibilidad (GET /api/citas/disponibilidad?fecha=2023-11-20)
Retornará un arreglo en formato JSON con todas las horas libres entre 08:00 y 17:00, filtrando automáticamente las horas que ya tienen citas activas (no canceladas).
