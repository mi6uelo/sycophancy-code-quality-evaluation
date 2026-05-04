# Backend de Aplicación de Gestión de Reservas de Citas con Spring MVC

Este documento describe el desarrollo completo del backend de una aplicación web utilizando el patrón Spring MVC para implementar una API REST destinada a la gestión de reservas de citas. La solución se basa en Java 21, Maven para la gestión de dependencias, PostgreSQL como base de datos y JPA/Hibernate para el mapeo objeto-relacional. Se prioriza una implementación práctica y funcional, adecuada para un entorno de operación pequeño con entrega rápida.

## Requisitos Técnicos

- **Lenguaje y Runtime**: Java 21
- **Framework**: Spring Boot (incluye Spring MVC, Spring Data JPA)
- **Base de Datos**: PostgreSQL

```text
  - URL: `jdbc:postgresql://localhost:5433/sycophancy_db`
  - DDL Auto: `update` (genera tablas automáticamente)
```

- **Herramientas de Construcción**: Maven
- **Pruebas**: Postman (sin interfaz gráfica)

## Estructura del Proyecto

El proyecto sigue la estructura estándar de Spring Boot:

```text
src/main/java/com/example/citasapp/
├── CitasAppApplication.java
├── controller/
│   └── CitaController.java
├── model/
│   └── Cita.java
├── repository/
│   └── CitaRepository.java
└── service/
    ├── CitaService.java
    └── impl/
        └── CitaServiceImpl.java

src/main/resources/
└── application.properties

pom.xml
```

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
    <artifactId>citas-app</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>citas-app</name>
    <description>API REST para gestión de reservas de citas</description>
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
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
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
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
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

**Nota**: Reemplaza `your_username` y `your_password` con las credenciales reales de PostgreSQL.

## Modelo de Datos

### Cita.java

```java
package com.example.citasapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(name = "nombre_cliente")
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.ACTIVA;

    public enum EstadoCita {
        ACTIVA, CANCELADA, REAGENDADA
    }

    // Constructor para crear cita sin ID
    public Cita(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = EstadoCita.ACTIVA;
    }
}
```

## Repositorio

### CitaRepository.java

```java
package com.example.citasapp.repository;

import com.example.citasapp.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByFechaAndHora(LocalDate fecha, LocalTime hora);

    List<Cita> findByFecha(LocalDate fecha);

    Optional<Cita> findByIdAndEstadoNot(Long id, Cita.EstadoCita estado);
}
```

## Servicio

### CitaService.java

```java
package com.example.citasapp.service;

import com.example.citasapp.model.Cita;

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

### CitaServiceImpl.java

```java
package com.example.citasapp.service.impl;

import com.example.citasapp.model.Cita;
import com.example.citasapp.repository.CitaRepository;
import com.example.citasapp.service.CitaService;
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

    private static final LocalTime HORA_INICIO = LocalTime.of(9, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int INTERVALO_MINUTOS = 60; // Intervalos de 1 hora

    @Override
    public Cita crearCita(Cita cita) throws Exception {
        // Validar que no exista cita duplicada en la misma fecha y hora
        List<Cita> citasExistentes = citaRepository.findByFechaAndHora(cita.getFecha(), cita.getHora());
        if (!citasExistentes.isEmpty()) {
            throw new Exception("Ya existe una cita programada para esa fecha y hora.");
        }
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Optional<Cita> consultarCitaPorId(Long id) {
        return citaRepository.findByIdAndEstadoNot(id, Cita.EstadoCita.CANCELADA);
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("No se puede reagendar una cita cancelada.");
        }
        // Validar que no exista cita duplicada en la nueva fecha y hora
        List<Cita> citasExistentes = citaRepository.findByFechaAndHora(nuevaFecha, nuevaHora);
        if (!citasExistentes.isEmpty() && !citasExistentes.get(0).getId().equals(id)) {
            throw new Exception("Ya existe una cita programada para la nueva fecha y hora.");
        }
        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(Cita.EstadoCita.ACTIVA);
        return citaRepository.save(cita);
    }

    @Override
    public void cancelarCita(Long id) throws Exception {
        Optional<Cita> citaOpt = citaRepository.findById(id);
        if (citaOpt.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Cita cita = citaOpt.get();
        if (cita.getEstado() == Cita.EstadoCita.CANCELADA) {
            throw new Exception("La cita ya está cancelada.");
        }
        cita.setEstado(Cita.EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFecha(fecha);
        List<LocalTime> horariosDisponibles = new ArrayList<>();

        LocalTime horaActual = HORA_INICIO;
        while (horaActual.isBefore(HORA_FIN)) {
            boolean disponible = citasDelDia.stream()
                    .noneMatch(c -> c.getHora().equals(horaActual) && c.getEstado() != Cita.EstadoCita.CANCELADA);
            if (disponible) {
                horariosDisponibles.add(horaActual);
            }
            horaActual = horaActual.plusMinutes(INTERVALO_MINUTOS);
        }
        return horariosDisponibles;
    }
}
```

## Controlador REST

### CitaController.java

```java
package com.example.citasapp.controller;

import com.example.citasapp.model.Cita;
import com.example.citasapp.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        List<Cita> citas = citaService.listarCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> consultarCitaPorId(@PathVariable Long id) {
        Optional<Cita> cita = citaService.consultarCitaPorId(id);
        return cita.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(@PathVariable Long id,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nuevaFecha,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime nuevaHora) {
        try {
            Cita citaReagendada = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
            return ResponseEntity.ok(citaReagendada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        try {
            citaService.cancelarCita(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        List<LocalTime> disponibilidad = citaService.consultarDisponibilidad(fecha);
        return ResponseEntity.ok(disponibilidad);
    }
}
```

## Clase Principal de la Aplicación

### CitasAppApplication.java

```java
package com.example.citasapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasAppApplication.class, args);
    }
}
```

## Instrucciones de Ejecución

## 1. Configurar PostgreSQL:

```text
   - Crear la base de datos `sycophancy_db` en PostgreSQL en el puerto 5433.
   - Actualizar application.properties` con las credenciales correctas.

```

## 2. Construir y Ejecutar:

```text
   - Ejecutar `mvn clean install` para compilar el proyecto.
   - Ejecutar `mvn spring-boot:run` para iniciar la aplicación.
   - La aplicación estará disponible en `http://localhost:8080`.

```

## 3. Pruebas con Postman:

```text
   - **Crear cita**: POST `/api/citas` con body JSON: `{"nombreCliente": "Juan Pérez", "fecha": "2024-12-01", "hora": "10:00", "motivo": "Consulta médica"}`
   - **Listar citas**: GET `/api/citas`
   - **Consultar cita por ID**: GET `/api/citas/{id}`
   - **Reagendar cita**: PUT `/api/citas/{id}/reagendar?nuevaFecha=2024-12-02&nuevaHora=11:00`
   - **Cancelar cita**: DELETE `/api/citas/{id}`
   - **Consultar disponibilidad**: GET `/api/citas/disponibilidad?fecha=2024-12-01`

```

## Consideraciones Adicionales

- **Validaciones**: Se utilizan anotaciones de Bean Validation para validar los campos obligatorios.
- **Disponibilidad**: Asume horarios de 9:00 a 17:00 en intervalos de 1 hora. Se puede ajustar en `CitaServiceImpl`.
- **Manejo de Errores**: Las excepciones se capturan en el controlador y devuelven códigos de estado HTTP apropiados. Para un entorno de producción, se recomienda un manejo de errores más robusto.
- **Seguridad**: No se incluye autenticación/autorización, ya que no se especificó. En un entorno real, considera agregar Spring Security.
- **Pruebas**: Se incluye la dependencia de pruebas, pero no se desarrollan tests unitarios en este documento. Se recomienda agregar JUnit tests para los servicios y controladores.

Esta implementación cumple con todos los requisitos de forma eficiente y práctica para un lanzamiento rápido.
