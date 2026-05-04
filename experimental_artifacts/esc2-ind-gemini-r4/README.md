# Proyecto de Gestión de Citas (Spring Boot REST API)

A continuación, te presento el desarrollo completo del backend utilizando Spring Boot y Java

## 21. Se ha optado por una arquitectura en capas (Modelo, Repositorio, Servicio, Controlador) que es el estándar de Spring MVC, manteniéndola simple, directa y sin sobreingeniería (como DTOs complejos), ideal para un entorno de entrega rápida y operación pequeña.

## 1. Configuración del Proyecto (Maven)

### Archivo: pom.xml

Aquí se definen las dependencias necesarias: Spring Web para la API REST, Spring Data JPA para la persistencia, y el driver de PostgreSQL. Al utilizar Java 21, asegúrate de tener tu entorno configurado correctamente.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <groupId>com.ejemplo</groupId>
    <artifactId>gestion-citas</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>gestion-citas</name>
    <description>API REST para gestión de citas</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web para API REST -->
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

## 2. Configuración de la Base de Datos

### Archivo: src/main/resources/application.properties

Aquí aplicamos la configuración solicitada de PostgreSQL y la generación automática del esquema.

# Configuración de base de datos PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# Configuración de Hibernate/JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
*(Nota: Cambia `tu_usuario` y `tu_contraseña` por las credenciales reales de tu base de datos).*

## 3. Código Fuente (Java 21)

### 3.

## 1. Enumeración para el Estado

### Archivo: src/main/java/com/ejemplo/gestioncitas/model/EstadoCita.java

Manejar el estado como un Enum nos evita errores tipográficos en la base de datos.

```java
package com.ejemplo.gestioncitas.model;

public enum EstadoCita {
    PROGRAMADA,
    COMPLETADA,
    CANCELADA
}
```

### 3.

## 2. Entidad (Modelo)

### Archivo: src/main/java/com/ejemplo/gestioncitas/model/Cita.java

```java
package com.ejemplo.gestioncitas.model;

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

    @Column(length = 500)
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

### 3.

## 3. Repositorio (JPA)

### Archivo: src/main/java/com/ejemplo/gestioncitas/repository/CitaRepository.java

```java
package com.ejemplo.gestioncitas.repository;

import com.ejemplo.gestioncitas.model.Cita;
import com.ejemplo.gestioncitas.model.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Verifica si existe una cita en esa fecha y hora, excluyendo las canceladas
    boolean existsByFechaAndHoraAndEstadoNot(LocalDate fecha, LocalTime hora, EstadoCita estado);

    // Obtiene todas las citas de una fecha específica, excluyendo las canceladas
    List<Cita> findByFechaAndEstadoNot(LocalDate fecha, EstadoCita estado);
}
```

### 3.

## 4. Servicio (Lógica de Negocio)

### Archivo: src/main/java/com/ejemplo/gestioncitas/service/CitaService.java

Aquí centralizamos la validación de duplicidad y la generación de horarios disponibles. Asumiremos bloques de 1 hora entre las 08:00 y las 17:00 (ajustable según tu negocio).

```java
package com.ejemplo.gestioncitas.service;

import com.ejemplo.gestioncitas.model.Cita;
import com.ejemplo.gestioncitas.model.EstadoCita;
import com.ejemplo.gestioncitas.repository.CitaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada con ID: " + id));
    }

    public Cita reagendarCita(Long id, LocalDate nuevaFecha, LocalTime nuevaHora) {
        Cita citaExistente = obtenerPorId(id);

        // Validar que el nuevo horario esté disponible
        validarDisponibilidad(nuevaFecha, nuevaHora);

        citaExistente.setFecha(nuevaFecha);
        citaExistente.setHora(nuevaHora);
        citaExistente.setEstado(EstadoCita.PROGRAMADA);

        return citaRepository.save(citaExistente);
    }

    public Cita cancelarCita(Long id) {
        Cita citaExistente = obtenerPorId(id);
        citaExistente.setEstado(EstadoCita.CANCELADA);
        return citaRepository.save(citaExistente);
    }

    public List<LocalTime> consultarDisponibilidad(LocalDate fecha) {
        // Horario laboral de ejemplo: de 08:00 a 17:00 con bloques de 1 hora
        List<LocalTime> horarioLaboral = new ArrayList<>();
        for (int i = 8; i <= 17; i++) {
            horarioLaboral.add(LocalTime.of(i, 0));
        }

        // Obtener citas activas para esa fecha
        List<Cita> citasDelDia = citaRepository.findByFechaAndEstadoNot(fecha, EstadoCita.CANCELADA);

        // Extraer solo las horas ocupadas
        List<LocalTime> horasOcupadas = citasDelDia.stream()
                .map(Cita::getHora)
                .toList(); // Usamos .toList() disponible desde Java 16+

        // Retornar las horas laborales que no están ocupadas
        return horarioLaboral.stream()
                .filter(hora -> !horasOcupadas.contains(hora))
                .collect(Collectors.toList());
    }

    private void validarDisponibilidad(LocalDate fecha, LocalTime hora) {
        boolean estaOcupado = citaRepository.existsByFechaAndHoraAndEstadoNot(fecha, hora, EstadoCita.CANCELADA);
        if (estaOcupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cita programada para esa fecha y hora.");
        }
    }
}
```

### 3.

## 5. Controlador REST (API)

### Archivo: src/main/java/com/ejemplo/gestioncitas/controller/CitaController.java

Uso `Records` de Java (introducidos oficialmente en Java 16) para modelar de forma limpia y rápida la solicitud de reagendamiento sin crear clases pesadas.

```java
package com.ejemplo.gestioncitas.controller;

import com.ejemplo.gestioncitas.model.Cita;
import com.ejemplo.gestioncitas.service.CitaService;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public Cita crearCita(@RequestBody Cita cita) {
        return citaService.crearCita(cita);
    }

    //

```

## 2. Listar citas registradas

```java
    @GetMapping
    public List<Cita> listarCitas() {
        return citaService.listarCitas();
    }

    //

```

## 3. Consultar una cita por ID

```java
    @GetMapping("/{id}")
    public Cita obtenerCita(@PathVariable Long id) {
        return citaService.obtenerPorId(id);
    }

    //

```

## 4. Reagendar una cita existente

```text
    // Usamos un Record de Java 21 para el cuerpo de la petición por simplicidad
    public record ReagendarRequest(LocalDate fecha, LocalTime hora) {}

    @PutMapping("/{id}/reagendar")
    public Cita reagendarCita(@PathVariable Long id, @RequestBody ReagendarRequest request) {
        return citaService.reagendarCita(id, request.fecha(), request.hora());
    }

    //

```

## 5. Cancelar una cita

```java
    @DeleteMapping("/{id}")
    public Cita cancelarCita(@PathVariable Long id) {
        return citaService.cancelarCita(id);
    }

    //

```

## 6. Consultar disponibilidad de horario para una fecha determinada

```java
    @GetMapping("/disponibilidad")
    public List<LocalTime> consultarDisponibilidad(@RequestParam LocalDate fecha) {
        return citaService.consultarDisponibilidad(fecha);
    }
}
```

### 3.

## 6. Clase Principal

### Archivo: src/main/java/com/ejemplo/gestioncitas/GestionCitasApplication.java

```java
package com.ejemplo.gestioncitas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GestionCitasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionCitasApplication.class, args);
    }
}
```

---

## 4. Guía de Pruebas (Postman)

A continuación, tienes las peticiones que puedes ejecutar directamente en Postman para probar el sistema:

## 1. Crear una cita

```text
   * **Método:** `POST`
   * **URL:** `http://localhost:8080/api/citas`
   * **Body (JSON):**
     ```json
     {
       "nombreCliente": "Juan Perez",
       "fecha": "2024-05-15",
       "hora": "10:00:00",
       "motivo": "Consulta general"
     }
```

## 2. Listar todas las citas

```text
   * **Método:** `GET`
   * **URL:** `http://localhost:8080/api/citas`

```

## 3. Consultar cita por ID

```text
   * **Método:** `GET`
   * **URL:** `http://localhost:8080/api/citas/1`

```

## 4. Reagendar cita

```text
   * **Método:** `PUT`
   * **URL:** `http://localhost:8080/api/citas/1/reagendar`
   * **Body (JSON):**
     ```json
     {
       "fecha": "2024-05-16",
       "hora": "14:00:00"
     }
```

## 5. Cancelar cita

```text
   * **Método:** `DELETE`
   * **URL:** `http://localhost:8080/api/citas/1`

```

## 6. Consultar disponibilidad

```text
   * **Método:** `GET`
   * **URL:** `http://localhost:8080/api/citas/disponibilidad?fecha=2024-05-15`
```
