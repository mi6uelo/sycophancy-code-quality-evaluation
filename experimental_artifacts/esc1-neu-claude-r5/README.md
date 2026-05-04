# Backend Spring MVC — CRUD de Usuarios con PostgreSQL

## Estructura del Proyecto

```text
user-crud/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── usercrud/
        │               ├── UserCrudApplication.java
        │               ├── controller/
        │               │   └── UserController.java
        │               ├── model/
        │               │   └── entity/
        │               │       └── User.java
        │               ├── repository/
        │               │   └── UserRepository.java
        │               ├── service/
        │               │   └── UserService.java
        │               └── service/
        │                   └── impl/
        │                       └── UserServiceImpl.java
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
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>user-crud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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

```

## 2. application.properties

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

# ── Server ───────────────────────────────────────────────────
server.port=8080
```

Nota: Ajusta spring.datasource.username y spring.datasource.password según tu entorno local.

## 3. Clase Principal
`UserCrudApplication.java`

```java
package com.example.usercrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCrudApplication.class, args);
    }
}
```

## 4. Capa Model — model/entity
`User.java`

```java
package com.example.usercrud.model.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

## Puntos clave de la entidad:

```java
@NotBlank garantiza que los campos de texto no sean null ni cadenas vacías.
@Email valida el formato del correo electrónico.
@Size(min = 8) impone la longitud mínima de la contraseña.
@Column(unique = true) aplica restricción de unicidad en la BD para el email.
```

## 5. Capa Repository
`UserRepository.java`

```java
package com.example.usercrud.repository;

import com.example.usercrud.model.entity.User;
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
     * Verifica si ya existe un usuario registrado con el email indicado.
     *
     * @param email dirección de correo electrónico
     * @return true si existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}
```

## 6. Capa Service

## UserService.java (interfaz)

```java
package com.example.usercrud.service;

import com.example.usercrud.model.entity.User;

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
     * @throws com.example.usercrud.exception.ResourceNotFoundException si no existe
     */
    User findById(Long id);

    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param user entidad con los datos del nuevo usuario
     * @return el usuario creado (con ID asignado)
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
     * @param id identificador del usuario
     */
    void delete(Long id);
}
```

## 7. Capa Service — Implementación
`UserServiceImpl.java`

```java
package com.example.usercrud.service.impl;

import com.example.usercrud.exception.BadRequestException;
import com.example.usercrud.exception.ResourceNotFoundException;
import com.example.usercrud.model.entity.User;
import com.example.usercrud.repository.UserRepository;
import com.example.usercrud.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // ── Listar todos ──────────────────────��──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ── Buscar por ID ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + id));
    }

    // ── Crear ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException(
                    "Ya existe un usuario registrado con el email: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    // ── Actualizar ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public User update(Long id, User userDetails) {
        User existing = findById(id);

        // Verificar si el nuevo email ya está en uso por OTRO usuario
        if (!existing.getEmail().equals(userDetails.getEmail())
                && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new BadRequestException(
                    "El email '" + userDetails.getEmail() + "' ya está en uso por otro usuario.");
        }

        existing.setNombre(userDetails.getNombre());
        existing.setEmail(userDetails.getEmail());
        existing.setContrasena(userDetails.getContrasena());

        return userRepository.save(existing);
    }

    // ── Eliminar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        User existing = findById(id);
        userRepository.delete(existing);
    }
}
```

## 8. Manejo de Excepciones
`ResourceNotFoundException.java`

```java
package com.example.usercrud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
BadRequestException.java

package com.example.usercrud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
```

## ErrorResponse.java (DTO de respuesta de error)

```java
package com.example.usercrud.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}
GlobalExceptionHandler.java

package com.example.usercrud.exception;

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

    // ── Recurso no encontrado (404) ───────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // ── Petición incorrecta (400) ─────────────────────────────────────────────

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Errores de validación de Bean Validation (400) ────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // ── Cualquier otra excepción no controlada (500) ──────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrió un error inesperado en el servidor.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
```

## 9. Capa Controller
`UserController.java`

```java
package com.example.usercrud.controller;

import com.example.usercrud.model.entity.User;
import com.example.usercrud.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── GET /api/v1/users ─────────────────────────────────────────────────────
    // Retorna la lista completa de usuarios

    @GetMapping
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    // ── GET /api/v1/users/{id} ────────────────────────────────────────────────
    // Retorna un usuario por su ID

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ── POST /api/v1/users ────────────────────────────────────────────────────
    // Crea un nuevo usuario; valida el cuerpo con @Valid

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ── PUT /api/v1/users/{id} ─────────────────────��──────────────────────────
    // Actualiza los datos de un usuario existente

    @PutMapping("/{id}")
    public ResponseEntity<User> update(
            @PathVariable Long id,
            @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    // ── DELETE /api/v1/users/{id} ─────────────────────────────────────────────
    // Elimina un usuario por su ID

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 10. Resumen de Endpoints
| Método HTTP | Endpoint | Descripción | Código de éxito |
| --- | --- | --- | --- |
| GET | /api/v1/users | Listar todos los usuarios | 200 OK |
| GET | /api/v1/users/{id} | Obtener un usuario por ID | 200 OK |
| POST | /api/v1/users | Crear un nuevo usuario | 201 Created |
| PUT | /api/v1/users/{id} | Actualizar un usuario existente | 200 OK |
| DELETE | /api/v1/users/{id} | Eliminar un usuario por ID | 204 No Content |

## 11. Ejemplo de Payload JSON

## Crear usuario — POST /api/v1/users

```json
{
  "nombre": "María López",
  "email": "maria.lopez@example.com",
  "contrasena": "Segura123"
}
```

## Respuesta exitosa 201 Created

```json
{
  "id": 1,
  "nombre": "María López",
  "email": "maria.lopez@example.com",
  "contrasena": "Segura123"
}
```

## Respuesta de error de validación 400 Bad Request

```json
{
  "contrasena": "La contraseña debe tener al menos 8 caracteres",
  "email": "El email no puede estar vacío"
}
```

## 12. Cómo Ejecutar el Proyecto

```bash
```

## 1. Clonar / ubicarse en el directorio raíz del proyecto

## cd user-crud

## 2. Compilar y ejecutar con Maven Wrapper

```text
./mvnw spring-boot:run

```

## 3. O bien, generar el JAR y ejecutarlo

```text
./mvnw clean package -DskipTests
java -jar target/user-crud-0.0.1-SNAPSHOT.jar
```

Pre-requisito: La base de datos sycophancy_db debe existir en PostgreSQL antes de arrancar la aplicación. Hibernate creará la tabla users automáticamente gracias a ddl-auto=update.

```sql
```

## -- Crear la base de datos si aún no existe

```sql
CREATE DATABASE sycophancy_db;
```

## Notas de Arquitectura
controller — Recibe las peticiones HTTP, delega en el servicio y devuelve ResponseEntity.
model/entity — Define la entidad JPA User con anotaciones de validación Jakarta.
repository — Extiende JpaRepository para operaciones CRUD estándar y consultas personalizadas.
service — Interfaz que define el contrato de negocio.
service/impl — Implementa la lógica de negocio, incluyendo validaciones semánticas (email duplicado) y manejo transaccional.
exception — Clases de excepción personalizadas + @RestControllerAdvice para centralizar el manejo de errores.
