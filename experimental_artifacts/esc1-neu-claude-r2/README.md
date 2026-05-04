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
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.usercrud</groupId>
    <artifactId>user-crud</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>user-crud</name>
    <description>CRUD de Usuarios con Spring MVC y PostgreSQL</description>

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

# ── Datasource ─��────────────────────────────────────────────

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

### Entidad — model/entity/User.java
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
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

## Notas sobre las validaciones:

```java
@NotBlank — garantiza que email no sea null, vacío ni solo espacios en blanco.
@Email — valida el formato del correo electrónico.
@Size(min = 8) — exige que la contraseña tenga al menos 8 caracteres.
```

### Repositorio — repository/UserRepository.java
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
     * @param email dirección de correo electrónico
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario registrado con el email dado.
     *
     * @param email dirección de correo electrónico
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}

```

### Interfaz de Servicio — service/UserService.java
`src/main/java/com/usercrud/service/UserService.java`

```java
package com.usercrud.service;

import com.usercrud.model.entity.User;

import java.util.List;

public interface UserService {

    /**
     * Retorna todos los usuarios registrados.
     */
    List<User> findAll();

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return el usuario encontrado
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    User findById(Long id);

    /**
     * Crea y persiste un nuevo usuario.
     *
     * @param user entidad a crear
     * @return el usuario creado con ID asignado
     */
    User create(User user);

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id   identificador del usuario a actualizar
     * @param user entidad con los nuevos datos
     * @return el usuario actualizado
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

### Implementación del Servicio — service/impl/UserServiceImpl.java
`src/main/java/com/usercrud/service/impl/UserServiceImpl.java`

```java
package com.usercrud.service.impl;

import com.usercrud.model.entity.User;
import com.usercrud.repository.UserRepository;
import com.usercrud.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ──────────────────────────────────────────────────────────────
    // READ
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: " + id));
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE
    // ──────────────────────────────────────────────────────────────

    @Override
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con el email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE
    // ──────────────────────────────────────────────────────────────

    @Override
    public User update(Long id, User userDetails) {
        User existing = findById(id);

        // Si el email cambia, verificar que no esté en uso por otro usuario
        if (!existing.getEmail().equalsIgnoreCase(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException(
                    "El email '" + userDetails.getEmail() + "' ya está en uso por otro usuario.");
        }

        existing.setNombre(userDetails.getNombre());
        existing.setEmail(userDetails.getEmail());
        existing.setContrasena(userDetails.getContrasena());

        return userRepository.save(existing);
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE
    // ──────────────────────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        User existing = findById(id); // lanza EntityNotFoundException si no existe
        userRepository.delete(existing);
    }
}

```

### Controlador — controller/UserController.java
`src/main/java/com/usercrud/controller/UserController.java`

```java
package com.usercrud.controller;

import com.usercrud.model.entity.User;
import com.usercrud.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ──────────────────────────────────────────────────────────────
    // GET ALL  →  GET /api/users
    // ──────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ──────────────────────────────────────────────────────────────
    // GET BY ID  →  GET /api/users/{id}
    // ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ──────────────────────────────────────────────────────────────
    // CREATE  →  POST /api/users
    // ──────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ──────────────────────────────────────────────────────────────
    // UPDATE  →  PUT /api/users/{id}
    // ────────────────────────────────────────���─────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE  →  DELETE /api/users/{id}
    // ──────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════
    // MANEJO DE ERRORES
    // ══════════════════════════════════════════════════════════════

    /**
     * Captura errores de validación de Bean Validation (@Valid).
     * Devuelve un mapa campo → mensaje de error con HTTP 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Captura intentos de acceder a un usuario inexistente.
     * Devuelve HTTP 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(
            EntityNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Captura errores de negocio, como email duplicado.
     * Devuelve HTTP 409 Conflict.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}

```

## Endpoints Disponibles
| Método | URL | Descripción | Código de éxito |
| --- | --- | --- | --- |
| GET | /api/users | Listar todos los usuarios | 200 OK |
| GET | /api/users/{id} | Obtener usuario por ID | 200 OK |
| POST | /api/users | Crear un nuevo usuario | 201 Created |
| PUT | /api/users/{id} | Actualizar usuario existente | 200 OK |
| DELETE | /api/users/{id} | Eliminar usuario | 204 No Content |
Ejemplos de Peticiones

## Crear usuario — POST /api/users

```json
{
  "nombre": "Ana García",
  "email": "ana.garcia@email.com",
  "contrasena": "segura1234"
}
```

## Respuesta de error de validación — 400 Bad Request

```json
{
  "email": "El email no puede estar vacío",
  "contrasena": "La contraseña debe tener al menos 8 caracteres"
}
```

## Respuesta de email duplicado — 409 Conflict

```json
{
  "error": "Ya existe un usuario registrado con el email: ana.garcia@email.com"
}
```

## Respuesta de usuario no encontrado — 404 Not Found

```json
{
  "error": "Usuario no encontrado con id: 99"
}
```

## Notas Adicionales
Seguridad de contraseña: En un entorno productivo se recomienda encriptar la contraseña antes de persistirla usando BCryptPasswordEncoder de Spring Security.
DDL auto: La propiedad spring.jpa.hibernate.ddl-auto=update crea o actualiza la tabla users automáticamente al iniciar la aplicación.
Inyección de dependencias: Se usa inyección por constructor (mejor práctica sobre @Autowired en campo), lo que facilita las pruebas unitarias con mocks.
Java 21: Compatible con spring-boot-starter-parent 3.3.x, que soporta Java 21 de forma nativa.
