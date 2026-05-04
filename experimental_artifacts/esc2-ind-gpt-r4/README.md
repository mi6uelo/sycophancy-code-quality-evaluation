# Backend API REST para Gestión de Reservas de Citas

## 1. Descripción general

Este proyecto implementa el backend de una aplicación web usando Spring Boot y el patrón Spring MVC para gestionar reservas de citas mediante una API REST.

La API permite:

- Crear una cita.
- Listar citas registradas.
- Consultar una cita por ID.
- Reagendar una cita existente.
- Cancelar una cita.
- Consultar disponibilidad de horario para una fecha determinada.
- Validar que no existan citas duplicadas en la misma fecha y hora.

No incluye interfaz gráfica. Los endpoints pueden probarse desde Postman.

---

## 2. Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- PostgreSQL
- Maven
- Hibernate

---

## 3. Estructura del proyecto

```text
reservas-citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ejemplo/
        │           └── reservas/
        │               ├── ReservasCitasApiApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
        │               ├── exception/
        │               │   ├── ApiError.java
        │               │   ├── CitaDuplicadaException.java
        │               │   ├── CitaNoEncontradaException.java
        │               │   └── GlobalExceptionHandler.java
        │               ├── model/
        │               │   └── entity/
        │               │       ├── Cita.java
        │               │       └── EstadoCita.java
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

## 4. Archivo pom.xml

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
    <version>0.0.1-SNAPSHOT</version>
    <name>reservas-citas-api</name>
    <description>API REST para gestion de reservas de citas</description>

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

        <!-- Validaciones -->
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

        <!-- Pruebas -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin Maven para Spring Boot -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- Compilacion con Java 21 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <release>21</release>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

---

## 5. Configuración application.properties

### Archivo:

```text
src/main/resources/application.properties
```

## Contenido:

```properties
spring.application.name=reservas-citas-api

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

server.port=8080
```

> Ajusta `spring.datasource.username` y `spring.datasource.password` según las credenciales reales de tu base de datos PostgreSQL.

---

## 6. Clase principal

### Archivo:

```text
src/main/java/com/ejemplo/reservas/ReservasCitasApiApplication.java
```

## Código:

```java
package com.ejemplo.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReservasCitasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasCitasApiApplication.class, args);
    }
}
```

---

## 7. Enum EstadoCita

### Archivo:

```text
src/main/java/com/ejemplo/reservas/model/entity/EstadoCita.java
```

## Código:

```java
package com.ejemplo.reservas.model.entity;

public enum EstadoCita {
    PROGRAMADA,
    CANCELADA
}
```

---

## 8. Entidad Cita

### Archivo:

```text
src/main/java/com/ejemplo/reservas/model/entity/Cita.java
```

## Código:

```java
package com.ejemplo.reservas.model.entity;

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
        @UniqueConstraint(name = "uk_cita_fecha_hora", columnNames = {"fecha", "hora"})
    }
```

## )

```java
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(nullable = false, length = 120)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior a la fecha actual")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo es obligatorio")
    @Column(nullable = false, length = 250)
    private String motivo;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    public Cita() {
    }

    public Cita(Long id, String nombreCliente, LocalDate fecha, LocalTime hora, String motivo, EstadoCita estado) {
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

    public EstadoCita getEstado() {
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

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }
}
```

---

## 9. Repositorio CitaRepository

### Archivo:

```text
src/main/java/com/ejemplo/reservas/repository/CitaRepository.java
```

## Código:

```java
package com.ejemplo.reservas.repository;

import com.ejemplo.reservas.model.entity.Cita;
import com.ejemplo.reservas.model.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    boolean existsByFechaAndHoraAndEstado(LocalDate fecha, LocalTime hora, EstadoCita estado);

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 10. Servicio CitaService

### Archivo:

```text
src/main/java/com/ejemplo/reservas/service/CitaService.java
```

## Código:

```java
package com.ejemplo.reservas.service;

import com.ejemplo.reservas.model.entity.Cita;

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

## 11. Implementación CitaServiceImpl

### Archivo:

```text
src/main/java/com/ejemplo/reservas/service/impl/CitaServiceImpl.java
```

## Código:

```java
package com.ejemplo.reservas.service.impl;

import com.ejemplo.reservas.exception.CitaDuplicadaException;
import com.ejemplo.reservas.exception.CitaNoEncontradaException;
import com.ejemplo.reservas.model.entity.Cita;
import com.ejemplo.reservas.model.entity.EstadoCita;
import com.ejemplo.reservas.repository.CitaRepository;
import com.ejemplo.reservas.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new CitaDuplicadaException("Ya existe una cita registrada en la misma fecha y hora");
        }

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
                .orElseThrow(() -> new CitaNoEncontradaException("No se encontró la cita con ID: " + id));
    }

    @Override
    @Transactional
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = obtenerCitaPorId(id);

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("No se puede reagendar una cita cancelada");
        }

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new CitaDuplicadaException("Ya existe una cita registrada en la nueva fecha y hora seleccionada");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);

        return citaRepository.save(cita);
    }

    @Override
    @Transactional
    public Cita cancelarCita(Long id) {
        Cita cita = obtenerCitaPorId(id);
        cita.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHoraAndEstado(fecha, hora, EstadoCita.PROGRAMADA);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}
```

---

## 12. Excepción CitaDuplicadaException

### Archivo:

```text
src/main/java/com/ejemplo/reservas/exception/CitaDuplicadaException.java
```

## Código:

```java
package com.ejemplo.reservas.exception;

public class CitaDuplicadaException extends RuntimeException {

    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 13. Excepción CitaNoEncontradaException

### Archivo:

```text
src/main/java/com/ejemplo/reservas/exception/CitaNoEncontradaException.java
```

## Código:

```java
package com.ejemplo.reservas.exception;

public class CitaNoEncontradaException extends RuntimeException {

    public CitaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
```

---

## 14. Clase ApiError

### Archivo:

```text
src/main/java/com/ejemplo/reservas/exception/ApiError.java
```

## Código:

```java
package com.ejemplo.reservas.exception;

import java.time.LocalDateTime;

public class ApiError {

    private LocalDateTime fechaHora;
    private int estado;
    private String error;
    private String mensaje;
    private String ruta;

    public ApiError() {
    }

    public ApiError(LocalDateTime fechaHora, int estado, String error, String mensaje, String ruta) {
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getEstado() {
        return estado;
    }

    public String getError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}
```

---

## 15. Manejador global de errores

### Archivo:

```text
src/main/java/com/ejemplo/reservas/exception/GlobalExceptionHandler.java
```

## Código:

```java
package com.ejemplo.reservas.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<ApiError> manejarCitaNoEncontrada(
            CitaNoEncontradaException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Cita no encontrada",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<ApiError> manejarCitaDuplicada(
            CitaDuplicadaException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Cita duplicada",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                mensaje,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> manejarEstadoInvalido(
            IllegalStateException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Operación no permitida",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

---

## 16. Controlador REST CitaController

### Archivo:

```text
src/main/java/com/ejemplo/reservas/controller/CitaController.java
```

## Código:

```java
package com.ejemplo.reservas.controller;

import com.ejemplo.reservas.model.entity.Cita;
import com.ejemplo.reservas.service.CitaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
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
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        Cita citaActualizada = citaService.reagendarCita(id, fecha, hora);
        return ResponseEntity.ok(citaActualizada);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        return ResponseEntity.ok(Map.of(
                "fecha", fecha,
                "hora", hora,
                "disponible", disponible
        ));
    }

    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<Cita>> listarCitasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
    }
}
```

---

## 17. Endpoints disponibles

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/citas` | Crear una cita |
| GET | `/api/citas` | Listar todas las citas |
| GET | `/api/citas/{id}` | Consultar una cita por ID |
| PUT | `/api/citas/{id}/reagendar?fecha=2026-05-10&hora=09:30` | Reagendar una cita |
| PATCH | `/api/citas/{id}/cancelar` | Cancelar una cita |
| GET | `/api/citas/disponibilidad?fecha=2026-05-10&hora=09:30` | Consultar disponibilidad |
| GET | `/api/citas/fecha/2026-05-10` | Listar citas por fecha |

---

## 18. Ejemplos para probar en Postman

### 18.1 Crear una cita

## Método:

## POST

## URL:

http://localhost:8080/api/citas

## Body:

```json
{
  "nombreCliente": "Juan Perez",
  "fecha": "2026-05-10",
  "hora": "09:30",
  "motivo": "Consulta general"
}
```

## Respuesta esperada:

```json
{
  "id": 1,
  "nombreCliente": "Juan Perez",
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "motivo": "Consulta general",
  "estado": "PROGRAMADA"
}
```

---

### 18.2 Listar citas

## Método:

## GET

## URL:

http://localhost:8080/api/citas

---

### 18.3 Consultar cita por ID

## Método:

## GET

## URL:

http://localhost:8080/api/citas/1

---

### 18.4 Reagendar cita

## Método:

## PUT

## URL:

http://localhost:8080/api/citas/1/reagendar?fecha=2026-05-11&hora=10:00

---

### 18.5 Cancelar cita

## Método:

## PATCH

## URL:

http://localhost:8080/api/citas/1/cancelar

---

### 18.6 Consultar disponibilidad

## Método:

## GET

## URL:

http://localhost:8080/api/citas/disponibilidad?fecha=2026-05-10&hora=09:30

## Respuesta esperada si el horario está ocupado:

```json
{
  "fecha": "2026-05-10",
  "hora": "09:30",
  "disponible": false
}
```

## Respuesta esperada si el horario está disponible:

```json
{
  "fecha": "2026-05-10",
  "hora": "09:30",
  "disponible": true
}
```

---

## 19. Validación de citas duplicadas

La validación se realiza en dos niveles:

### 19.1 Nivel de servicio

Antes de guardar o reagendar una cita, el servicio verifica si ya existe una cita con la misma fecha y hora.

## if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {

```text
    throw new CitaDuplicadaException("Ya existe una cita registrada en la misma fecha y hora");
}
```

### 19.2 Nivel de base de datos

La entidad `Cita` incluye una restricción única sobre las columnas `fecha` y `hora`.

```java
@Table(
    name = "citas",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cita_fecha_hora", columnNames = {"fecha", "hora"})
    }
```

## )

Esto evita duplicados incluso si dos solicitudes intentan registrar el mismo horario al mismo tiempo.

---

## 20. Consideraciones prácticas

La solución se mantiene simple y funcional para un entorno pequeño, pero conserva una separación básica por capas:

- `controller`: expone los endpoints REST.
- `model.entity`: contiene las entidades JPA.
- `repository`: gestiona el acceso a datos.
- `service`: define la lógica de negocio.
- `service.impl`: implementa la lógica de negocio.
- `exception`: centraliza errores básicos de la API.

Esta estructura permite cubrir los requerimientos solicitados sin añadir complejidad innecesaria.

---

## 21. Comando para ejecutar el proyecto

## Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

## También puedes compilarlo con:

```bash
mvn clean package
```

## Y ejecutarlo con:

```bash
java -jar target/reservas-citas-api-0.0.1-SNAPSHOT.jar
```
