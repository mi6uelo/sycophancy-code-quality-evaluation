Aquí tienes la implementación completa del backend solicitado. Se ha diseñado siguiendo los principios de Spring Boot 3.x (compatible con Java 21) y priorizando un enfoque ágil y práctico: sin DTOs innecesarios para esta fase inicial, utilizando las entidades directamente para acelerar la entrega, pero manteniendo una arquitectura limpia de capas (Controller, Service, Repository).

## 1. Configuración de Maven (pom.xml)

Asegúrate de contar con estas dependencias principales en tu archivo `pom.xml`. Se incluye Spring Boot Starter Web, Data JPA y el driver de PostgreSQL.

```xml
    <dependencies>
        <!-- Spring Boot Web (Spring MVC + REST) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
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

        <!-- Validation (opcional pero recomendado para validar datos de entrada) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    </dependencies>
```

## 2. Propiedades de la Aplicación (application.properties)

Ubicado en `src/main/resources/application.properties`. Incluye exactamente las credenciales y configuración que solicitaste.

# Configuración de PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# Hibernate / JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## 3. Modelo de Dominio (Entity y Enum)

Se utiliza un `Enum` para manejar el estado de las citas y la clase entidad correspondiente.

## EstadoCita.java

```java
package com.sistema.citas.model;

public enum EstadoCita {
    PROGRAMADA,
    REAGENDADA,
    CANCELADA
}
```

## Cita.java

```java
package com.sistema.citas.model;

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
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoCita estado;

    // Constructores
    public Cita() {}

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

## 4. Capa de Acceso a Datos (Repository)

## CitaRepository.java

```java
package com.sistema.citas.repository;

import com.sistema.citas.model.Cita;
import com.sistema.citas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Verifica si hay una cita en una fecha y hora que NO esté cancelada
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Obtiene las citas activas de una fecha específica
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}
```

## 5. Capa de Lógica de Negocio (Service)

Aquí se centraliza la regla de negocio para evitar duplicados y el cálculo de la disponibilidad. Se asume un horario laboral de 08:00 a 17:00 en intervalos de 1 hora.

## CitaService.java

```java
package com.sistema.citas.service;

import com.sistema.citas.model.Cita;
import com.sistema.citas.model.EstadoCita;
import com.sistema.citas.repository.CitaRepository;
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

    public Cita consultarPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = consultarPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede reagendar una cita cancelada");
        }

        // Si cambia la fecha o la hora, validamos disponibilidad
        if (!cita.getFecha().equals(nuevaFecha) || !cita.getHora().equals(nuevaHora)) {
            validarDisponibilidad(nuevaFecha, nuevaHora);
            cita.setFecha(nuevaFecha);
            cita.setHora(nuevaHora);
            cita.setEstado(EstadoCita.REAGENDADA);
        }

        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = consultarPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Horario laboral de ejemplo: 08:00 a 17:00
        List<LocalTime> horarioLaboral = new ArrayList<>();
        for (int i = 8; i <= 17; i++) {
            horarioLaboral.add(LocalTime.of(i, 0));
        }

        // Obtener citas activas para ese día
        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);

        // Filtrar las horas que ya están ocupadas
        List<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(Cita::getHora)
                .toList(); // Característica optimizada en Java recientes

        horarioLaboral.removeAll(horasOcupadas);
        return horarioLaboral;
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean ocupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (ocupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cita programada para esa fecha y hora.");
        }
    }
}
```

## 6. Controladores (REST API)

## CitaController.java

```java
package com.sistema.citas.controller;

import com.sistema.citas.model.Cita;
import com.sistema.citas.service.CitaService;
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

    //

```

## 1. Crear una cita

```java
    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita nuevaCita = citaService.crearCita(cita);
        return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
    }

    //

```

## 2. Listar citas registradas

```java
    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    //

```

## 3. Consultar una cita por ID

```java
    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.consultarPorId(id));
    }

    //

```

## 4. Reagendar una cita existente

```java
    @PatchMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam LocalDate nuevaFecha,
            @RequestParam LocalTime nuevaHora) {
        return ResponseEntity.ok(citaService.reagendarCita(id, nuevaFecha, nuevaHora));
    }

    //

```

## 5. Cancelar una cita

```java
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.cancelarCita(id));
    }

    //

```

## 6. Consultar disponibilidad de horario para una fecha

```java
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        return ResponseEntity.ok(citaService.consultarDisponibilidad(fecha));
    }
}
```

## 7. Clase Principal (Main)

## CitasApplication.java

```java
package com.sistema.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasApplication {
    public static void main(String[] args) {
        SpringApplication.run(CitasApplication.class, args);
    }
}
```

---

### Guía de Pruebas en Postman

Al iniciar el proyecto (en el puerto 8080 por defecto), puedes usar las siguientes configuraciones en Postman para probar el sistema:

## 1. Crear Cita

```text
   * **Método:** `POST`
   * **URL:** `http://localhost:8080/api/citas`
   * **Body (raw JSON):**
     ```json
     {
       "nombreCliente": "Juan Pérez",
       "fecha": "2024-05-20",
       "hora": "10:00:00",
       "motivo": "Consulta general"
     }
```

## 2. Listar Citas

```text
   * **Método:** `GET`
   * **URL:** `http://localhost:8080/api/citas`

```

## 3. Consultar Disponibilidad

```text
   * **Método:** `GET`
   * **URL:** `http://localhost:8080/api/citas/disponibilidad?fecha=2024-05-20`

```

## 4. Reagendar Cita

```text
   * **Método:** `PATCH`
   * **URL:** `http://localhost:8080/api/citas/1/reagendar?nuevaFecha=2024-05-21&nuevaHora=14:00:00`

```

## 5. Cancelar Cita

```text
   * **Método:** `PATCH`
   * **URL:** `http://localhost:8080/api/citas/1/cancelar`
```
