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

## Dependencias — pom.xml
`pom.xml`

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
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.usercrud</groupId>
    <artifactId>user-crud</artifactId>
    <version>1.0.0</version>
    <name>user-crud</name>
    <description>CRUD de Usuarios con Spring MVC y PostgreSQL</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring Web (MVC) -->
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

        <!-- Lombok (reduce boilerplate) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
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

## Configuración — application.properties
`src/main/resources/application.properties`

# ── Datasource ──────────────────────────────────────────────

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ── JPA / Hibernate ─────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# ── Servidor ────────────────────────────────────────────────
server.port=8080

```

⚠️ Importante: Reemplaza username y password con las credenciales reales de tu instancia PostgreSQL.

### Clase Principal
`src/main/java/com/usercrud/UserCrudApplication.java`

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

## Capa model.entity
`src/main/java/com/usercrud/model/entity/User.java`

```java
package com.usercrud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(nullable = false)
    private String contrasena;
}

```

Detalle de validaciones aplicadas
| Campo | Anotación | Regla |
| --- | --- | --- |
| nombre | @NotBlank | No puede ser nulo ni vacío |
| email | @NotBlank + @Email | No vacío y formato de email válido |
| contrasena | @NotBlank + @Size(min = 8) | No vacío y mínimo 8 caracteres |

## Capa repository
`src/main/java/com/usercrud/repository/UserRepository.java`

```java
package com.usercrud.repository;

import com.usercrud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su dirección de email.
     *
     * @param email dirección de correo a buscar
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario registrado con el email dado.
     *
     * @param email dirección de correo a verificar
     * @return true si ya está en uso
     */
    boolean existsByEmail(String email);
}

```

Capa service

### Interfaz
`src/main/java/com/usercrud/service/UserService.java`

```java
package com.usercrud.service;

import com.usercrud.model.entity.User;

import java.util.List;

public interface UserService {

    /**
     * Devuelve todos los usuarios registrados.
     */
    List<User> findAll();

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return usuario encontrado
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    User findById(Long id);

    /**
     * Crea un nuevo usuario validando que el email no esté en uso.
     *
     * @param user datos del nuevo usuario
     * @return usuario persistido
     */
    User create(User user);

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id   identificador del usuario a actualizar
     * @param user nuevos datos
     * @return usuario actualizado
     */
    User update(Long id, User user);

    /**
     * Elimina un usuario por su ID.
     *
     * @param id identificador del usuario a eliminar
     */
    void delete(Long id);
}

```

### Implementación — service.impl
`src/main/java/com/usercrud/service/impl/UserServiceImpl.java`

```java
package com.usercrud.service.impl;

import com.usercrud.model.entity.User;
import com.usercrud.repository.UserRepository;
import com.usercrud.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ─── READ ALL ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ─── READ ONE ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con ID: " + id));
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con el email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User update(Long id, User user) {
        User existing = findById(id);

        // Si el email cambia, verificar que el nuevo no esté en uso
        if (!existing.getEmail().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "El email ya está en uso: " + user.getEmail());
        }

        existing.setNombre(user.getNombre());
        existing.setEmail(user.getEmail());
        existing.setContrasena(user.getContrasena());

        return userRepository.save(existing);
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "No se puede eliminar: usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
```

## Capa controller
`src/main/java/com/usercrud/controller/UserController.java`

```java
package com.usercrud.controller;

import com.usercrud.model.entity.User;
import com.usercrud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── GET ALL ─────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ─── GET BY ID ───────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─── UPDATE ──────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // ─── DELETE ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

```

## Manejo Global de Errores
`src/main/java/com/usercrud/controller/GlobalExceptionHandler.java`

```java
package com.usercrud.controller;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── 404: Entidad no encontrada ───────────────────────────────────────────

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── 400: Argumento ilegal (email duplicado, etc.) ────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ─── 422: Errores de validación de Bean Validation ───────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("error", "Validation Failed");
        body.put("fields", fieldErrors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}

```

## Resumen de Endpoints
| Método | Endpoint | Descripción | Código éxito |
| --- | --- | --- | --- |
| GET | /api/users | Lista todos los usuarios | 200 OK |
| GET | /api/users/{id} | Obtiene un usuario por ID | 200 OK |
| POST | /api/users | Crea un nuevo usuario | 201 Created |
| PUT | /api/users/{id} | Actualiza un usuario existente | 200 OK |
| DELETE | /api/users/{id} | Elimina un usuario por ID | 204 No Content |
Ejemplos de Peticiones JSON

## Crear usuario — POST /api/users
`create-user.json`

```json
{
  "nombre": "María García",
  "email": "maria.garcia@email.com",
  "contrasena": "segura1234"
}

```

## Actualizar usuario — PUT /api/users/1
`update-user.json`

```json
{
  "nombre": "María García López",
  "email": "maria.garcia@email.com",
  "contrasena": "nuevaClave2025"
}

```

## Respuesta de error de validación — 422
`validation-error-response.json`

```json
{
  "timestamp": "2026-05-02T10:15:30.123",
  "status": 422,
  "error": "Validation Failed",
  "fields": {
    "email": "El email no puede estar vacío",
    "contrasena": "La contraseña debe tener al menos 8 caracteres"
  }
}

```

## Notas Importantes
Contraseñas: Este ejemplo persiste la contraseña en texto plano con fines didácticos. En producción, utiliza BCrypt mediante spring-security-crypto o Spring Security completo.
DDL Auto: update crea o migra la tabla users automáticamente al arrancar. Para producción se recomienda Flyway o Liquibase.
Puerto PostgreSQL: Se usa el puerto 5433 según la configuración proporcionada; asegúrate de que tu instancia escuche en ese puerto.
Lombok: Requiere el plugin de Lombok habilitado en tu IDE (IntelliJ IDEA: File → Settings → Plugins → Lombok).
