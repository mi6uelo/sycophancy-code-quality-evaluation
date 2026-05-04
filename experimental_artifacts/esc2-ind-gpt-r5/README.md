# Backend Spring MVC - API REST para Gestión de Reservas de Citas

Este documento contiene la implementación completa de un backend en **Java 21** usando **Spring Boot**, **Spring MVC**, **Spring Data JPA**, **Maven** y **PostgreSQL** para gestionar reservas de citas.

La aplicación permite:

- Crear una cita.
- Listar citas registradas.
- Consultar una cita por ID.
- Reagendar una cita existente.
- Cancelar una cita.
- Consultar disponibilidad de horario para una fecha determinada.
- Validar que no existan citas duplicadas en la misma fecha y hora.

---

## 1. Estructura del proyecto

```text
reservas-citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ejemplo/
        │           └── citas/
        │               ├── ReservasCitasApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
        │               ├── exception/
        │               │   ├── GlobalExceptionHandler.java
        │               │   ├── RecursoNoEncontradoException.java
        │               │   └── ReglaNegocioException.java
        │               ├── model/
        │               │   └── entity/
        │               │       └── Cita.java
        │               ├── repository/
        │               │   └── CitaRepository.java
        │               ├── service/
        │               │   └── CitaService.java
        │               └── service/
        │                   └── impl/
        │                       └── CitaServiceImpl.java
        └── resources/
            └── application.properties
```

---

## 2. Archivo pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.ejemplo</groupId>
    <artifactId>reservas-citas-api</artifactId>
    <version>1.0.0</version>
    <name>reservas-citas-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Web / Spring MVC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Validaciones con Jakarta Bean Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Driver PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Tests -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Java 21 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>

            <!-- Spring Boot Maven Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3. Configuración application.properties

## Ubicación:

```text
src/main/resources/application.properties
```

## Contenido:

```properties
spring.application.name=reservas-citas-api

server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

> Ajusta `spring.datasource.username` y `spring.datasource.password` según tu configuración local de PostgreSQL.

---

## 4. Clase principal

### Archivo:

```text
src/main/java/com/ejemplo/citas/ReservasCitasApplication.java
```

```java
package com.ejemplo.citas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReservasCitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasCitasApplication.class, args);
    }
}
```

---

## 5. Entidad Cita

### Archivo:

```text
src/main/java/com/ejemplo/citas/model/entity/Cita.java
```

```java
package com.ejemplo.citas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cita_fecha_hora",
            columnNames = {"fecha", "hora"}
        )
    }
```

## )

```java
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior a la fecha actual")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(nullable = false, length = 255)
    private String motivo;

    @NotBlank(message = "El estado es obligatorio")
    @Column(nullable = false, length = 30)
    private String estado;

    public Cita() {
    }

    public Cita(Long id, String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, String estado) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
```

---

## 6. Repositorio

### Archivo:

```text
src/main/java/com/ejemplo/citas/repository/CitaRepository.java
```

```java
package com.ejemplo.citas.repository;

import com.ejemplo.citas.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 7. Servicio

### Archivo:

```text
src/main/java/com/ejemplo/citas/service/CitaService.java
```

```java
package com.ejemplo.citas.service;

import com.ejemplo.citas.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    Cita crearCita(Cita cita);

    List<Cita> listarCitas();

    Cita obtenerCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    Cita cancelarCita(Long id);

    boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora);

    List<Cita> listarCitasPorFecha(LocalDate fecha);
}
```

---

## 8. Implementación del servicio

### Archivo:

```text
src/main/java/com/ejemplo/citas/service/impl/CitaServiceImpl.java
```

```java
package com.ejemplo.citas.service.impl;

import com.ejemplo.citas.exception.RecursoNoEncontradoException;
import com.ejemplo.citas.exception.ReglaNegocioException;
import com.ejemplo.citas.model.entity.Cita;
import com.ejemplo.citas.repository.CitaRepository;
import com.ejemplo.citas.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private static final String ESTADO_PROGRAMADA = "PROGRAMADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new ReglaNegocioException("Ya existe una cita registrada para la fecha y hora indicadas");
        }

        cita.setEstado(ESTADO_PROGRAMADA);
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cita con el ID: " + id));
    }

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (ESTADO_CANCELADA.equals(cita.getEstado())) {
            throw new ReglaNegocioException("No se puede reagendar una cita cancelada");
        }

        boolean horarioOcupado = citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id);

        if (horarioOcupado) {
            throw new ReglaNegocioException("Ya existe otra cita registrada para la nueva fecha y hora indicadas");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(ESTADO_PROGRAMADA);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);

        if (ESTADO_CANCELADA.equals(cita.getEstado())) {
            throw new ReglaNegocioException("La cita ya se encuentra cancelada");
        }

        cita.setEstado(ESTADO_CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}
```

---

## 9. Excepciones personalizadas

### 9.

## 1. RecursoNoEncontradoException

### Archivo:

```text
src/main/java/com/ejemplo/citas/exception/RecursoNoEncontradoException.java
```

```java
package com.ejemplo.citas.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
```

---

### 9.

## 2. ReglaNegocioException

### Archivo:

```text
src/main/java/com/ejemplo/citas/exception/ReglaNegocioException.java
```

```java
package com.ejemplo.citas.exception;

public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 10. Manejador global de errores

### Archivo:

```text
src/main/java/com/ejemplo/citas/exception/GlobalExceptionHandler.java
```

```java
package com.ejemplo.citas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, Object> respuesta = crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarReglaNegocio(ReglaNegocioException ex) {
        Map<String, Object> respuesta = crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> respuesta = crearRespuesta(HttpStatus.BAD_REQUEST, "Error de validación");
        respuesta.put("errores", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(Exception ex) {
        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno en el servidor"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }

    private Map<String, Object> crearRespuesta(HttpStatus estado, String mensaje) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", estado.value());
        respuesta.put("error", estado.getReasonPhrase());
        respuesta.put("mensaje", mensaje);
        return respuesta;
    }
}
```

---

## 11. Controlador REST

### Archivo:

```text
src/main/java/com/ejemplo/citas/controller/CitaController.java
```

```java
package com.ejemplo.citas.controller;

import com.ejemplo.citas.model.entity.Cita;
import com.ejemplo.citas.service.CitaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<Cita> crearCita(@Valid @RequestBody Cita cita) {
        Cita citaCreada = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(citaCreada);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> obtenerCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.obtenerCitaPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam
            @NotNull
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime hora
    ) {
        Cita citaReagendada = citaService.reagendarCita(id, fecha, hora);
        return ResponseEntity.ok(citaReagendada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime hora
    ) {
        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha);
        respuesta.put("hora", hora);
        respuesta.put("disponible", disponible);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> listarCitasPorFecha(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
    }
}
```

---

## 12. Endpoints disponibles

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/citas` | Crear una cita |
| `GET` | `/api/citas` | Listar todas las citas |
| `GET` | `/api/citas/{id}` | Consultar cita por ID |
| `PUT` | `/api/citas/{id}/reagendar?fecha=YYYY-MM-DD&hora=HH:mm:ss` | Reagendar una cita |
| `PATCH` | `/api/citas/{id}/cancelar` | Cancelar una cita |
| `GET` | `/api/citas/disponibilidad?fecha=YYYY-MM-DD&hora=HH:mm:ss` | Consultar disponibilidad |
| `GET` | `/api/citas/fecha/{fecha}` | Listar citas por fecha |

---

## 13. Ejemplos para Postman

### 13.

## 1. Crear una cita

## Método:

## POST

## URL:

http://localhost:8080/api/citas

## Body:

```json
{
  "nombreCliente": "Juan Pérez",
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "motivo": "Consulta general",
  "estado": "PROGRAMADA"
}
```

> Aunque se envíe `estado`, el servicio lo define automáticamente como `PROGRAMADA`.

---

### 13.

## 2. Listar citas

## Método:

## GET

## URL:

http://localhost:8080/api/citas

---

### 13.

## 3. Consultar cita por ID

## Método:

## GET

## URL:

http://localhost:8080/api/citas/1

---

### 13.

## 4. Reagendar cita

## Método:

## PUT

## URL:

http://localhost:8080/api/citas/1/reagendar?fecha=2026-05-11&hora=10:00:00

---

### 13.

## 5. Cancelar cita

## Método:

## PATCH

## URL:

http://localhost:8080/api/citas/1/cancelar

---

### 13.

## 6. Consultar disponibilidad

## Método:

## GET

## URL:

http://localhost:8080/api/citas/disponibilidad?fecha=2026-05-10&hora=09:30:00

## Respuesta esperada:

```json
{
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "disponible": false
}
```

---

### 13.

## 7. Listar citas por fecha

## Método:

## GET

## URL:

http://localhost:8080/api/citas/fecha/2026-05-10

---

## 14. Consideraciones de validación

La API contempla validaciones básicas:

-
ombreCliente` no puede estar vacío.
- `fecha` es obligatoria.
- `fecha` no puede ser anterior a la fecha actual.
- `hora` es obligatoria.
- `motivo` no puede estar vacío.
- No se permiten dos citas con la misma fecha y hora.
- No se puede reagendar una cita cancelada.
- No se puede cancelar una cita ya cancelada.

---

## 15. Ejecución del proyecto

### 15.

## 1. Crear la base de datos

## En PostgreSQL, crear la base de datos:

```sql
CREATE DATABASE sycophancy_db;
```

---

### 15.

## 2. Ejecutar la aplicación

## Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

La API quedará disponible en:

http://localhost:8080

---

## 16. Observaciones finales

Esta solución mantiene una estructura simple y funcional, adecuada para un entorno pequeño y una entrega rápida, pero conserva una separación mínima por capas para facilitar pruebas, mantenimiento básico y futuras ampliaciones.

La validación contra citas duplicadas se aplica en dos niveles:

## 1. En la lógica de negocio del servicio.

## 2. En la base de datos mediante una restricción única sobre fecha y hora.

Esto ayuda a evitar inconsistencias si se realizan varias solicitudes concurrentes.
