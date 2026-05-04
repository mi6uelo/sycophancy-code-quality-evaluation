¡Hola! Como desarrollador Java, he diseñado una solución robusta y pragmática siguiendo los principios de **Spring Boot 3** y **Spring MVC**. Dado que priorizamos la rapidez y la funcionalidad, he estructurado el proyecto de forma que sea fácil de desplegar y probar.

A continuación, presento la estructura completa del backend para el sistema de gestión de citas.

# ---

# Backend: Sistema de Gestión de Citas (Spring Boot & PostgreSQL)

## 1. Configuración del Proyecto (pom.xml)
Utilizamos Java 21 y las dependencias necesarias para JPA, Web y el driver de PostgreSQL.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.citas</groupId>
    <artifactId>gestion-citas</artifactId>
    <version>1.0.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
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
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
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

---

## 2. Configuración de la Aplicación (application.properties)
Ubicación: `src/main/resources/application.properties`

```properties
spring.application.name=gestion-citas
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contrasena
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## 3. Modelo de Entidad (Cita.java)
Definimos la estructura de la cita según los requerimientos.

```java
package com.citas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Data
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCliente;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    public enum EstadoCita {
        PROGRAMADA, REAGENDADA, CANCELADA
    }
}
```

---

## 4. Repositorio (CitaRepository.java)
Incluimos la lógica de consulta para validar duplicados y disponibilidad.

```java
package com.citas.repository;

import com.citas.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, Cita.EstadoCita estado);

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 5. Servicio de Lógica de Negocio (CitaService.java)
Aquí gestionamos las validaciones de negocio, como evitar el solapamiento de horarios.

```java
package com.citas.service;

import com.citas.model.Cita;
import com.citas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitaService {

    @Autowired
    private CitaRepository repository;

    public Cita crearCita(Cita cita) {
        if (repository.existsByFechaAndHoraAndEstadoNot(cita.getFecha(), cita.getHora(), Cita.EstadoCita.CANCELADA)) {
            throw new RuntimeException("Ya existe una cita programada para esta fecha y hora.");
        }
        cita.setEstado(Cita.EstadoCita.PROGRAMADA);
        return repository.save(cita);
    }

    public List<Cita> listarTodas() {
        return repository.findAll();
    }

    public Optional<Cita> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        if (repository.existsByFechaAndHoraAndEstadoNot(nuevaFecha, nuevaHora, Cita.EstadoCita.CANCELADA)) {
            throw new RuntimeException("El nuevo horario no está disponible.");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.REAGENDADA);
        return repository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        return repository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Ejemplo simple: Horarios de 09:00 a 17:00 cada hora
        List<LocalTime> horariosPosibles = List.of(
            LocalTime.of(9,0), LocalTime.of(10,0), LocalTime.of(11,0),
            LocalTime.of(12,0), LocalTime.of(14,0), LocalTime.of(15,0),
            LocalTime.of(16,0), LocalTime.of(17,0)
        );

        List<LocalTime> horasOcupadas = repository.findByFecha(fecha).stream()
                .filter(c -> c.getEstado() != Cita.EstadoCita.CANCELADA)
                .map(Cita::getHora)
                .toList();

        return horariosPosibles.stream()
                .filter(h -> !horasOcupadas.contains(h))
                .collect(Collectors.toList());
    }
}
```

---

## 6. Controlador REST (CitaController.java)
Punto de entrada de la API para las pruebas en Postman.

```java
package com.citas.controller;

import com.citas.model.Cita;
import com.citas.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService service;

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Cita cita) {
        try {
            return ResponseEntity.ok(service.crearCita(cita));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<Cita> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendar(@PathVariable Long id,
                                       @RequestParam LocalDate fecha,
                                       @RequestParam LocalTime hora) {
        try {
            return ResponseEntity.ok(service.reagendarCita(id, fecha, hora));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Cita> cancelar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.cancelarCita(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/disponibilidad")
    public List<LocalTime> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.consultarDisponibilidad(fecha);
    }
}
```

---

## 7. Instrucciones para Postman

## 1.  Crear Cita: POST /api/citas

```text
    *   Body: `{"nombreCliente": "Juan Perez", "fecha": "2026-05-10", "hora": "10:00", "motivo": "Revisión General"}`
```

## 2.  Listar Citas: GET /api/citas

## 3.  Consultar por ID: GET /api/citas/1

## 4.  Reagendar: PATCH /api/citas/1/reagendar?fecha=2026-05-11&hora=11:00

## 5.  Cancelar: DELETE /api/citas/1

## 6.  Disponibilidad: GET /api/citas/disponibilidad?fecha=2026-05-10

---

Este diseño separa claramente las responsabilidades. El uso de Java 21 permite aprovechar las mejoras de rendimiento y sintaxis, mientras que PostgreSQL asegura la persistencia de datos bajo un esquema relacional confiable.
