# Backend API REST para Gestión de Reservas de Citas

Proyecto backend desarrollado con **Java 21**, **Spring Boot / Spring MVC**, **Spring Data JPA**, **Maven** y **PostgreSQL**.

La API permite:

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
citas-api/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── citasapi/
        │               ├── CitasApiApplication.java
        │               ├── controller/
        │               │   └── CitaController.java
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

    <groupId>com.example</groupId>
    <artifactId>citas-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>citas-api</name>
    <description>API REST para gestión de reservas de citas</description>

    <properties>
        <java.version>21</java.version>
        <spring.boot.version>3.4.5</spring.boot.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring.boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

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

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring.boot.version}</version>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>
```

---

## 3. Configuración application.properties

## Ruta:

```text
src/main/resources/application.properties
```

```properties
spring.application.name=citas-api

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

server.port=8080
```

> Ajusta `spring.datasource.username` y `spring.datasource.password` según la configuración real de PostgreSQL.

---

## 4. Clase principal

## Ruta:

```text
src/main/java/com/example/citasapi/CitasApiApplication.java
```

```java
package com.example.citasapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CitasApiApplication.class, args);
    }
}
```

---

## 5. Entidad Cita

## Ruta:

```text
src/main/java/com/example/citasapi/model/entity/Cita.java
```

```java
package com.example.citasapi.model.entity;

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
        @UniqueConstraint(columnNames = {"fecha", "hora"})
    }
```

## )

```java
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombreCliente;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior a la fecha actual")
    @Column(nullable = false)
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    @Column(nullable = false)
    private LocalTime hora;

    @NotBlank(message = "El motivo de la cita es obligatorio")
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

## Ruta:

```text
src/main/java/com/example/citasapi/repository/CitaRepository.java
```

```java
package com.example.citasapi.repository;

import com.example.citasapi.model.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);

    boolean existsByFechaAndHoraAndIdNot(LocalDate fecha, LocalTime hora, Long id);

    List<Cita> findByFecha(LocalDate fecha);
}
```

---

## 7. Interfaz de servicio

## Ruta:

```text
src/main/java/com/example/citasapi/service/CitaService.java
```

```java
package com.example.citasapi.service;

import com.example.citasapi.model.entity.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaService {

    Cita crearCita(Cita cita);

    List<Cita> listarCitas();

    Cita consultarCitaPorId(Long id);

    Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora);

    Cita cancelarCita(Long id);

    boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora);

    List<Cita> listarCitasPorFecha(LocalDate fecha);
}
```

---

## 8. Implementación del servicio

## Ruta:

```text
src/main/java/com/example/citasapi/service/impl/CitaServiceImpl.java
```

```java
package com.example.citasapi.service.impl;

import com.example.citasapi.model.entity.Cita;
import com.example.citasapi.repository.CitaRepository;
import com.example.citasapi.service.CitaService;
import org.springframework.stereotype.Service;

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
    public Cita crearCita(Cita cita) {
        if (citaRepository.existsByFechaAndHora(cita.getFecha(), cita.getHora())) {
            throw new IllegalArgumentException("Ya existe una cita registrada en la misma fecha y hora");
        }

        cita.setEstado(ESTADO_PROGRAMADA);
        return citaRepository.save(cita);
    }

    @Override
    public List<Cita> listarCitas() {
        return citaRepository.findAll();
    }

    @Override
    public Cita consultarCitaPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe una cita con el ID: " + id));
    }

    @Override
    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita cita = consultarCitaPorId(id);

        if (ESTADO_CANCELADA.equalsIgnoreCase(cita.getEstado())) {
            throw new IllegalArgumentException("No se puede reagendar una cita cancelada");
        }

        if (citaRepository.existsByFechaAndHoraAndIdNot(nuevaFecha, nuevaHora, id)) {
            throw new IllegalArgumentException("Ya existe otra cita registrada en la misma fecha y hora");
        }

        cita.setFecha(nuevaFecha);
        cita.setHora(nuevaHora);
        cita.setEstado(ESTADO_PROGRAMADA);

        return citaRepository.save(cita);
    }

    @Override
    public Cita cancelarCita(Long id) {
        Cita cita = consultarCitaPorId(id);
        cita.setEstado(ESTADO_CANCELADA);
        return citaRepository.save(cita);
    }

    @Override
    public boolean consultarDisponibilidad(LocalDate fecha, LocalTime hora) {
        return !citaRepository.existsByFechaAndHora(fecha, hora);
    }

    @Override
    public List<Cita> listarCitasPorFecha(LocalDate fecha) {
        return citaRepository.findByFecha(fecha);
    }
}
```

---

## 9. Controlador REST

## Ruta:

```text
src/main/java/com/example/citasapi/controller/CitaController.java
```

```java
package com.example.citasapi.controller;

import com.example.citasapi.model.entity.Cita;
import com.example.citasapi.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
        Cita nuevaCita = citaService.crearCita(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
    }

    @GetMapping
    public ResponseEntity<List<Cita>> listarCitas() {
        return ResponseEntity.ok(citaService.listarCitas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> consultarCitaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.consultarCitaPorId(id));
    }

    @PutMapping("/{id}/reagendar")
    public ResponseEntity<Cita> reagendarCita(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        Cita citaReagendada = citaService.reagendarCita(id, fecha, hora);
        return ResponseEntity.ok(citaReagendada);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable Long id) {
        Cita citaCancelada = citaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<Map<String, Object>> consultarDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora) {

        boolean disponible = citaService.consultarDisponibilidad(fecha, hora);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fecha", fecha);
        respuesta.put("hora", hora);
        respuesta.put("disponible", disponible);

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<Cita>> listarCitasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        return ResponseEntity.ok(citaService.listarCitasPorFecha(fecha));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errores);
    }
}
```

---

## 10. Endpoints disponibles

### Crear una cita

## http

```text
POST /api/citas
```

## Ejemplo JSON:

```json
{
  "nombreCliente": "Juan Pérez",
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "motivo": "Consulta general",
  "estado": "PROGRAMADA"
}
```

> Aunque se envíe el estado desde Postman, el backend asigna automáticamente `PROGRAMADA`.

---

### Listar citas

## http

```text
GET /api/citas
```

---

### Consultar una cita por ID

## http

```text
GET /api/citas/1
```

---

### Reagendar una cita

## http

```text
PUT /api/citas/1/reagendar?fecha=2026-05-11&hora=10:00:00
```

---

### Cancelar una cita

## http

```text
PUT /api/citas/1/cancelar
```

---

### Consultar disponibilidad

## http

```text
GET /api/citas/disponibilidad?fecha=2026-05-10&hora=09:30:00
```

## Respuesta esperada:

```json
{
  "fecha": "2026-05-10",
  "hora": "09:30:00",
  "disponible": false
}
```

---

### Listar citas por fecha

## http

```text
GET /api/citas/fecha?fecha=2026-05-10
```

---

## 11. Ejecución del proyecto

## Desde la raíz del proyecto:

```bash
mvn clean install
```

## Luego:

```bash
mvn spring-boot:run
```

La API quedará disponible en:

http://localhost:8080/api/citas

---

## 12. Consideraciones implementadas

## - Arquitectura por capas:

```text
  - `controller`
  - `model.entity`
  - `repository`
  - `service`
  - `service.impl`
```

- Validación de campos obligatorios.
- Validación de fecha actual o futura.
- Validación para evitar citas duplicadas en la misma fecha y hora.
- Restricción única a nivel de base de datos para `fecha` y `hora`.
- Manejo básico de errores mediante `@ExceptionHandler`.
- Persistencia automática con PostgreSQL usando `spring.jpa.hibernate.ddl-auto=update`.
- Proyecto sin interfaz gráfica, preparado para pruebas desde Postman.
