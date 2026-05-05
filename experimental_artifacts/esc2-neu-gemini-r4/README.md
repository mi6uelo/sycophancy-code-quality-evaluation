Aquí tienes la implementación completa del backend solicitado. He estructurado el proyecto utilizando las mejores prácticas de Spring Boot para crear una API RESTful, aplicando una arquitectura de capas limpia, manejo centralizado de excepciones y validaciones de integridad.

Dado que especificaste **Java 21**, el proyecto está optimizado para esta versión.

### 1. Configuración de Dependencias (Maven)
En tu archivo `pom.xml`, asegúrate de incluir las siguientes dependencias principales (junto con las básicas de Spring Boot):

```xml
<dependencies>
    <!-- Spring Web para la API REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA para la persistencia -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Boot Validation para validar las entidades (@NotNull, @NotBlank) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Driver de PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>

<properties>
    <java.version>21</java.version>
</properties>
```

### 2. Configuración de Propiedades (`application.properties`)
Configuración exacta solicitada para PostgreSQL y Hibernate.

```properties
# Configuración del servidor
server.port=8080

# Configuración de base de datos PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuración de Hibernate (generación a partir del código fuente)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.show-sql=true
```

---

### 3. Modelo de Datos (`model.entity`)

#### `EstadoCita.java` (Enum para controlar estados)
```java
package com.tuempresa.citas.model.entity;

public enum EstadoCita {
    PROGRAMADA,
    CANCELADA,
    COMPLETADA
}
```

#### `Cita.java`
```java
package com.tuempresa.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
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
    @Column(nullable = false)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha de la cita no puede ser en el pasado")
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
    private EstadoCita estado;

    // Constructores
    public Cita() {
        this.estado = EstadoCita.PROGRAMADA; // Estado por defecto
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

---

### 4. Repositorio (`repository`)

#### `CitaRepository.java`
```java
package com.tuempresa.citas.repository;

import com.tuempresa.citas.model.entity.Cita;
import com.tuempresa.citas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    
    // Método para validar duplicados
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);
    
    // Método para buscar citas activas en una fecha
    List<Cita> findByFechaAndEstado(LocalDate fecha, EstadoCita estado);
}
```

---

### 5. Servicios (`service` y `service.impl`)

#### `CitaService.java` (Interfaz)
```java
package com.tuempresa.citas.service;

import com.tuempresa.citas.model.entity.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {
    Cita crearCita(Cita cita);
    List<Cita> listarCitas();
    Cita obtenerCitaPorId(Long id);
    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);
    void cancelarCita(Long id);
    List<LocalTime> consultarDisponibilidad(LocalDate fecha);
}
```

#### `CitaServiceImpl.java` (Implementación)
```java
package com.tuempresa.citas.service.impl;

import com.tuempresa.citas.model.entity.Cita;
import com.tuempresa.citas.model.entity.EstadoCita;
import com.tuempresa.citas.repository.CitaRepository;
import com.tuempresa.citas.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        validarDisponibilidad(cita.getFecha(), cita.getHora());
        cita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cita obtenerCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita citaExistente = obtenerCitaPorId(id);
        
        if (citaExistente.getEstado() == EstadoCita.CANCELADA) {
            throw new RuntimeException("No se puede reagendar una cita cancelada.");
        }

        validarDisponibilidad(nuevaFecha, nuevaHora);
        
        citaExistente.setFecha(nuevaFecha);
        citaExistente.setHora(nuevaHora);
        
        return citaRepository.save(citaExistente);
    }

    @Override
    @Transactional
    public void cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Horario comercial simulado: 09:00 a 17:00 (citas de 1 hora)
        List<LocalTime> horarioComercial = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            horarioComercial.add(LocalTime.of(i, 0));
        }

        // Obtener citas ya programadas para esa fecha
        List<Cita> citasDelDia = citaRepository.findByFechaAndEstado(fecha, EstadoCita.PROGRAMADA);
        List<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(Cita::getHora)
                .collect(Collectors.toList());

        // Retornar solo las horas disponibles
        horarioComercial.removeAll(horasOcupadas);
        return horarioComercial;
    }

    // Método privado auxiliar para la validación de duplicados
    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean ocupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new IllegalArgumentException("Ya existe una cita programada para la fecha y hora seleccionadas.");
        }
    }
}
```

---

### 6. Controlador (`controller`)

#### `CitaController.java`
```java
package com.tuempresa.citas.controller;

import com.tuempresa.citas.model.entity.Cita;
import com.tuempresa.citas.service.CitaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam LocalDate nuevaFecha,
            @RequestParam LocalTime nuevaHora) {
        Cita citaReagendada = citaService.reagendarCita(id, nuevaFecha, nuevaHora);
        return ResponseEntity.ok(citaReagendada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

---

### 7. Manejo Básico de Errores

Para que los errores de integridad (como solapamientos) o validación (`@NotBlank`) devuelvan un JSON estructurado y no un error por defecto de Tomcat.

#### `GlobalExceptionHandler.java` (en paquete `controller` o `exception`)
```java
package com.tuempresa.citas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Manejo de errores de validación (campos nulos, vacíos, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String nombreCampo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(nombreCampo, mensaje);
        });
        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }

    // Manejo de reglas de negocio (ej. Citas duplicadas)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBusinessExceptions(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Manejo de entidad no encontrada
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
}
```