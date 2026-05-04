# Backend de Gestión de Reservas de Citas con Spring MVC

Este documento describe la implementación de un backend para una API REST de gestión de reservas de citas usando Spring Boot y Spring MVC. El proyecto está diseñado para ser funcional y eficiente, priorizando una entrega rápida sin complejidad innecesaria. Se utiliza Java 21, Maven para gestión de dependencias, y PostgreSQL como base de datos.

## Estructura del Proyecto

El proyecto sigue la estructura típica de una aplicación Spring Boot:

- `src/main/java/com/example/appointmentmanager`: Paquete principal.

```text
  - `entity/Appointment.java`: Entidad JPA.
  - `repository/AppointmentRepository.java`: Repositorio JPA.
  - `service/AppointmentService.java`: Lógica de negocio.
  - `controller/AppointmentController.java`: Controlador REST.
```

- `src/main/resources/application.properties`: Configuración de la aplicación.
- `pom.xml`: Archivo de configuración de Maven.

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
        <version>3.2.0</version> <!-- Versión compatible con Java 21 -->
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>appointment-manager</artifactId>
    <version>1.0.0</version>

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

## Configuración de la Aplicación

### application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

## Modelo de Datos

### Appointment.java

```java
package com.example.appointmentmanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombreCliente;

    @NotNull
    private LocalDate fecha;

    @NotNull
    private LocalTime hora;

    @NotBlank
    private String motivo;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Estado estado = Estado.ACTIVA;

    public enum Estado {
        ACTIVA, CANCELADA
    }

    // Constructores, getters y setters
    public Appointment() {}

    public Appointment(String nombreCliente, LocalDate fecha, LocalTime hora, String motivo) {
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
    }

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

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
}
```

## Repositorio

### AppointmentRepository.java

```java
package com.example.appointmentmanager.repository;

import com.example.appointmentmanager.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, Appointment.Estado estado);

    @Query("SELECT a.hora FROM Appointment a WHERE a.fecha = :fecha AND a.estado = 'ACTIVA'")
    List<LocalTime> findOccupiedTimesByDate(@Param("fecha") LocalDate fecha);
}
```

## Servicio

### AppointmentService.java

```java
package com.example.appointmentmanager.service;

import com.example.appointmentmanager.entity.Appointment;
import com.example.appointmentmanager.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository repository;

    public Appointment createAppointment(Appointment appointment) throws Exception {
        if (repository.findByFechaAndHoraAndEstado(appointment.getFecha(), appointment.getHora(), Appointment.Estado.ACTIVA).isPresent()) {
            throw new Exception("Cita duplicada en fecha y hora especificadas.");
        }
        return repository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return repository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return repository.findById(id);
    }

    public Appointment rescheduleAppointment(Long id, LocalDate newFecha, LocalTime newHora) throws Exception {
        Optional<Appointment> optAppointment = repository.findById(id);
        if (optAppointment.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Appointment appointment = optAppointment.get();
        if (repository.findByFechaAndHoraAndEstado(newFecha, newHora, Appointment.Estado.ACTIVA).isPresent()) {
            throw new Exception("Nueva fecha y hora ya ocupadas.");
        }
        appointment.setFecha(newFecha);
        appointment.setHora(newHora);
        return repository.save(appointment);
    }

    public void cancelAppointment(Long id) throws Exception {
        Optional<Appointment> optAppointment = repository.findById(id);
        if (optAppointment.isEmpty()) {
            throw new Exception("Cita no encontrada.");
        }
        Appointment appointment = optAppointment.get();
        appointment.setEstado(Appointment.Estado.CANCELADA);
        repository.save(appointment);
    }

    public List<LocalTime> getAvailableTimes(LocalDate fecha) {
        List<LocalTime> occupied = repository.findOccupiedTimesByDate(fecha);
        List<LocalTime> allTimes = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(12, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0),
            LocalTime.of(17, 0)
        );
        return allTimes.stream().filter(time -> !occupied.contains(time)).collect(Collectors.toList());
    }
}
```

## Controlador REST

### AppointmentController.java

```java
package com.example.appointmentmanager.controller;

import com.example.appointmentmanager.entity.Appointment;
import com.example.appointmentmanager.service.AppointmentService;
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
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment) {
        try {
            Appointment saved = service.createAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return service.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = service.getAppointmentById(id);
        return appointment.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(@PathVariable Long id, @RequestParam LocalDate fecha, @RequestParam LocalTime hora) {
        try {
            Appointment updated = service.rescheduleAppointment(id, fecha, hora);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable Long id) {
        try {
            service.cancelAppointment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/availability")
    public List<LocalTime> getAvailableTimes(@RequestParam LocalDate fecha) {
        return service.getAvailableTimes(fecha);
    }
}
```

## Notas Adicionales

- **Validación**: Se usa `@Valid` en el controlador para validar campos obligatorios. Para validaciones personalizadas (como duplicados), se maneja en el servicio.
- **Disponibilidad**: Se asume un horario fijo de 9:00 a 17:00 en intervalos de 1 hora. Puedes ajustar `getAvailableTimes` si es necesario.
- **Errores**: Las excepciones se manejan de manera básica; en producción, considera usar `@ControllerAdvice` para un manejo global de errores.
- **Pruebas**: Usa Postman para endpoints como POST /api/appointments (crear), GET /api/appointments (listar), etc.
- **Ejecución**: Ejecuta con `mvn spring-boot:run` después de configurar la BD.

Este backend cubre todos los requerimientos de forma práctica y funcional. Si necesitas ajustes, avísame.
