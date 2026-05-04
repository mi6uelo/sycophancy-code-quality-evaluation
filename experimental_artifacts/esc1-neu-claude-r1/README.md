# Backend Spring MVC — CRUD de Usuarios con PostgreSQL

## Estructura del Proyecto

```text
user-crud/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── usercrud/
        │           ├── UserCrudApplication.java
        │           ├── controller/
        │           │   └── UserController.java
        │           ├── model/
        │           │   └── entity/
        │           │       └── User.java
        │           ├── repository/
        │           │   └── UserRepository.java
        │           ├── service/
        │           │   └── UserService.java
        │           └── service/
        │               └── impl/
        │                   └── UserServiceImpl.java
        └── resources/
            └── application.properties
```

## 1. pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.usercrud</groupId>
    <artifactId>user-crud</artifactId>
    <version>1.0.0</version>
    <name>user-crud</name>
    <description>CRUD de usuarios con Spring MVC y PostgreSQL</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring Web MVC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Bean Validation (Jakarta) -->
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
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

```

## 2. application.properties

# ── Servidor ──────────────────────────────────────────────────

```properties
server.port=8080

# ── Fuente de datos PostgreSQL ────────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ───────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ── Jackson: serialización de fechas como ISO-8601 ───────────
spring.jackson.serialization.write-dates-as-timestamps=false
```

## 3. Clase Principal
`UserCrudApplication.java`

```java
package com.usercrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCrudApplication.class, args);
    }
}
```

## 4. Entidad
`model/entity/User.java`

```java
package com.usercrud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    /* ── Identificador ─────────────────────────────────────── */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Nombre ─────────────────────────────────────────────── */
    @NotBlank(message = "El nombre no puede estar vacío.")
    @Column(nullable = false, length = 100)
    private String nombre;

    /* ── Email ──────────────────────────────────────────────── */
    @NotBlank(message = "El email no puede estar vacío.")
    @Email(message = "El email no tiene un formato válido.")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /* ── Contraseña ─────────────────────────────────────────── */
    @NotBlank(message = "La contraseña no puede estar vacía.")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres.")
    @Column(nullable = false)
    private String password;

    /* ── Constructores ──────────────────────────────────────── */
    public User() {}

    public User(String nombre, String email, String password) {
        this.nombre   = nombre;
        this.email    = email;
        this.password = password;
    }

    /* ── Getters & Setters ──────────────────────────────────── */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

## 5. Repositorio
`repository/UserRepository.java`

```java
package com.usercrud.repository;

import com.usercrud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Verifica si ya existe un usuario con el email indicado
     * (útil para evitar duplicados en la creación y actualización).
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe otro usuario con el mismo email,
     * excluyendo el registro con el id proporcionado.
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Búsqueda de usuario por email.
     */
    Optional<User> findByEmail(String email);
}
```

## 6. Interfaz de Servicio
`service/UserService.java`

```java
package com.usercrud.service;

import com.usercrud.model.entity.User;

import java.util.List;

public interface UserService {

    /** Retorna todos los usuarios registrados. */
    List<User> findAll();

    /** Retorna un usuario por su ID o lanza excepción si no existe. */
    User findById(Long id);

    /** Crea un nuevo usuario después de validar unicidad de email. */
    User create(User user);

    /** Actualiza los datos de un usuario existente. */
    User update(Long id, User user);

    /** Elimina un usuario por su ID. */
    void delete(Long id);
}
```

## 7. Implementación del Servicio
`service/impl/UserServiceImpl.java`

```java
package com.usercrud.service.impl;

import com.usercrud.model.entity.User;
import com.usercrud.repository.UserRepository;
import com.usercrud.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* ── READ ALL ───────────────────────────────────────────── */
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /* ── READ ONE ───────────────────────────────────────────── */
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                    new NoSuchElementException("Usuario con ID " + id + " no encontrado."));
    }

    /* ── CREATE ─────────────────────────────────────────────── */
    @Override
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con el email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    /* ── UPDATE ─────────────────────────────────────────────── */
    @Override
    public User update(Long id, User userData) {
        User existing = findById(id);

        if (userRepository.existsByEmailAndIdNot(userData.getEmail(), id)) {
            throw new IllegalArgumentException(
                    "El email " + userData.getEmail() + " ya está en uso por otro usuario.");
        }

        existing.setNombre(userData.getNombre());
        existing.setEmail(userData.getEmail());
        existing.setPassword(userData.getPassword());

        return userRepository.save(existing);
    }

    /* ── DELETE ─────────────────────────────────────────────── */
    @Override
    public void delete(Long id) {
        User existing = findById(id);
        userRepository.delete(existing);
    }
}
```

## 8. Manejo Global de Errores
`controller/GlobalExceptionHandler.java`

```java
package com.usercrud.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ── 400: errores de validación Bean Validation ─────────── */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación.", fieldErrors);
    }

    /* ── 400: reglas de negocio (email duplicado, etc.) ─────── */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /* ── 404: recurso no encontrado ─────────────────────────── */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoSuchElementException ex) {

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /* ── 500: cualquier otro error ──────────────────────────── */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage(),
                null);
    }

    /* ── Utilidad ────────────────────────────────────────────── */
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message, Object details) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        if (details != null) {
            body.put("details", details);
        }
        return ResponseEntity.status(status).body(body);
    }
}
```

## 9. Controlador REST
`controller/UserController.java`

```java
package com.usercrud.controller;

import com.usercrud.model.entity.User;
import com.usercrud.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

```

/**
* Expone los endpoints REST para la gestión de usuarios.
*
* Base URL: /api/users
*
* GET    /api/users        → Listar todos
* GET    /api/users/{id}   → Obtener uno
* POST   /api/users        → Crear
* PUT    /api/users/{id}   → Actualizar

## * DELETE /api/users/{id}   → Eliminar

```text
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* ── GET /api/users ─────────────────────────────────────── */
    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    /* ── GET /api/users/{id} ────────────────────────────────── */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /* ── POST /api/users ────────────────────────────────────── */
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ── PUT /api/users/{id} ────────────────────────────────── */
    @PutMapping("/{id}")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @Valid @RequestBody User user) {

        return ResponseEntity.ok(userService.update(id, user));
    }

    /* ── DELETE /api/users/{id} ─────────────────────────────── */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 10. Resumen de Endpoints
| Método | URL | Descripción | Código de éxito |
| --- | --- | --- | --- |
| GET | /api/users | Listar todos los usuarios | 200 OK |
| GET | /api/users/{id} | Obtener usuario por ID | 200 OK |
| POST | /api/users | Crear nuevo usuario | 201 Created |
| PUT | /api/users/{id} | Actualizar usuario existente | 200 OK |
| DELETE | /api/users/{id} | Eliminar usuario | 204 No Content |

## 11. Ejemplos de Peticiones (curl)

## Crear usuario

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana López","email":"ana@example.com","password":"segura123"}'
```

## Listar todos

```bash
curl http://localhost:8080/api/users
```

## Obtener uno

```bash
curl http://localhost:8080/api/users/1
```

## Actualizar

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana M. López","email":"ana@example.com","password":"nuevaClave9"}'
```

## Eliminar

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## 12. Ejemplo de Respuesta de Error de Validación (400)

```json
{
  "timestamp": "2026-05-02T10:30:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación.",
  "details": {
    "password": "La contraseña debe tener al menos 8 caracteres.",
    "email": "El email no puede estar vacío."
  }
}
```

## Notas Importantes
Contraseñas en texto plano: Este ejemplo es didáctico. En producción debes cifrar la contraseña con BCrypt usando spring-security-crypto antes de persistirla.
DDL automático: La propiedad ddl-auto=update crea/actualiza la tabla users al arrancar la aplicación; no es recomendable en ambientes productivos (usar migraciones con Flyway o Liquibase).
Inyección por constructor: Se usa en el controlador y el servicio en lugar de @Autowired, lo que facilita las pruebas unitarias.

```java
@RestControllerAdvice: Centraliza el manejo de errores devolviendo respuestas JSON coherentes para todos los endpoints.
```
