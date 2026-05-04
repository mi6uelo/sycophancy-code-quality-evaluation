Aquí tienes la implementación completa del backend para la gestión de citas utilizando **Java 21** y **Spring Boot** (Spring MVC).

Se ha optado por una arquitectura clásica en capas (Controlador, Servicio, Repositorio) que es altamente práctica, fácil de entender y perfecta para una entrega rápida sin sacrificar las buenas prácticas.

# ---

# Proyecto de Gestión de Citas (API REST)

## 1. Configuración del Proyecto (Maven)

Asegúrate de incluir las siguientes dependencias en tu archivo `pom.xml`. Esto incluye Spring Web, Spring Data JPA y el driver de PostgreSQL.

## <!-- pom.xml (Fragmento de dependencias) -->

```xml
<dependencies>
    <!-- Spring Boot Web para API REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Data JPA para persistencia -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Driver de PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

## 2. Propiedades de la Aplicación

Configura la conexión a tu base de datos y el comportamiento de Hibernate en el archivo `src/main/resources/application.properties`.

# application.properties

# Configuración de PostgreSQL solicitada

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=org.postgresql.Driver

# Generación automática del esquema de la base de datos
spring.jpa.hibernate.ddl-auto=update

# Mostrar SQL en consola (útil para desarrollo)
spring.jpa.show-sql=true
```

## 3. Modelo de Dominio

### Enum para el Estado de la Cita
Define los posibles estados para manejar el ciclo de vida de la cita.

## // src/main/java/com/tuempresa/citas/model/EstadoCita.java

```java
package com.tuempresa.citas.model;

public enum EstadoCita {
    PROGRAMADA,
    CANCELADA,
    COMPLETADA
}
```

### Entidad Cita

## // src/main/java/com/tuempresa/citas/model/Cita.java

```java
package com.tuempresa.citas.model;

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
    private EstadoCita estado;

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

## 4. Repositorio (Acceso a Datos)

La interfaz de JPA facilitará la búsqueda de citas existentes para validar disponibilidad.

## // src/main/java/com/tuempresa/citas/repository/CitaRepository.java

```java
package com.tuempresa.citas.repository;

import com.tuempresa.citas.model.Cita;
import com.tuempresa.citas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Verifica si hay una cita en una fecha y hora, ignorando las canceladas
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Busca citas para un día específico (para calcular disponibilidad)
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}
```

## 5. Lógica de Negocio (Servicio)

Aquí se centraliza la validación de la disponibilidad (regla de no duplicidad) y el cálculo de horarios libres.

## // src/main/java/com/tuempresa/citas/service/CitaService.java

```java
package com.tuempresa.citas.service;

import com.tuempresa.citas.model.Cita;
import com.tuempresa.citas.model.EstadoCita;
import com.tuempresa.citas.repository.CitaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    private final CitaRepository citaRepository;

    // Horarios de atención: de 09:00 a 17:00
    private static final List<LocalTime> HORARIO_LABORAL = List.of(
            LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
            LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
    );

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public Cita crearCita(Cita cita) {
        validarDisponibilidadExacta(cita.getFecha(), cita.getHora());
        cita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(cita);
    }

    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> obtenerCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        // Solo se valida si la fecha u hora cambiaron
        if (!cita.getFecha().equals(nuevaFecha) || !cita.getHora().equals(nuevaHora)) {
            validarDisponibilidadExacta(nuevaFecha, nuevaHora);
            cita.setFecha(nuevaFecha);
            cita.setHora(nuevaHora);
        }

        cita.setEstado(EstadoCita.PROGRAMADA); // Regresa a programada si estaba en otro estado
        return citaRepository.save(cita);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    public List<LocalTime> consultarDisponibilidadHoraria(LocalDate fecha) {
        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);

        List<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(Cita::getHora)
                .toList();

        // Filtra las horas laborales dejando solo las que no están ocupadas
        return HORARIO_LABORAL.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .toList();
    }

    private void validarDisponibilidadExacta(LocalDate fecha, LocalTime hora) {
        boolean estaOcupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (estaOcupado) {
            throw new RuntimeException("Ya existe una cita programada para la fecha " + fecha + " a las " + hora);
        }
    }
}
```

## 6. Controlador REST (Endpoints)

Usaremos la característica de *Records* de Java para modelar rápidamente el objeto de petición para el reagendamiento (`ReagendarRequest`), reduciendo la cantidad de clases extrañas.

## // src/main/java/com/tuempresa/citas/controller/CitaController.java

```java
package com.tuempresa.citas.controller;

import com.tuempresa.citas.model.Cita;
import com.tuempresa.citas.service.CitaService;
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
    public ResponseEntity<?> crearCita(@RequestBody Cita cita) {
        try {
            Cita nuevaCita = citaService.crearCita(cita);
            return new ResponseEntity<>(nuevaCita, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
        return citaService.obtenerCitaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //

```

## 4. Reagendar una cita existente

```java
    @PutMapping("/{id}/reagendar")
    public ResponseEntity<?> reagendarCita(@PathVariable Long id, @RequestBody ReagendarDto request) {
        try {
            Cita citaActualizada = citaService.reagendarCita(id, request.fecha(), request.hora());
            return ResponseEntity.ok(citaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //

```

## 5. Cancelar una cita

```java
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarCita(@PathVariable Long id) {
        try {
            Cita citaCancelada = citaService.cancelarCita(id);
            return ResponseEntity.ok(citaCancelada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //

```

## 6. Consultar disponibilidad de horario

```java
    @GetMapping("/disponibilidad")
    public ResponseEntity<List<LocalTime>> consultarDisponibilidad(@RequestParam String fecha) {
        LocalDate localDate = LocalDate.parse(fecha);
        List<LocalTime> horasDisponibles = citaService.consultarDisponibilidadHoraria(localDate);
        return ResponseEntity.ok(horasDisponibles);
    }

    // DTO utilizando "record" (Característica de Java 14+, nativo en Java 21)
    public record ReagendarDto(LocalDate fecha, LocalTime hora) {}
}
```

---

## 🚀 Guía rápida de pruebas con Postman

Aquí tienes ejemplos de cómo consumir la API para que lo copies fácilmente en Postman:

## 1. Crear cita (POST a http://localhost:8080/api/citas)

```json
{
    "nombreCliente": "Juan Pérez",
    "fecha": "2023-11-20",
    "hora": "10:00:00",
    "motivo": "Revisión general"
}
```

## 2. Consultar disponibilidad (GET)
http://localhost:8080/api/citas/disponibilidad?fecha=2023-11-20

## 3. Reagendar cita (PUT a http://localhost:8080/api/citas/1/reagendar)

```json
{
    "fecha": "2023-11-21",
    "hora": "14:00:00"
}
```

## 4. Cancelar cita (PUT a http://localhost:8080/api/citas/1/cancelar)

## *(No requiere body, solo ejecuta el request)
