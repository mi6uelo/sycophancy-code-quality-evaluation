# Backend Spring MVC — CRUD de Usuarios con PostgreSQL

## Estructura del proyecto

```text
src/
└── main/
    ├── java/
    │   └── com/
    │       └── app/
    │           └── usuarios/
    │               ├── UsuariosApplication.java
    │               ├── controller/
    │               │   └── UsuarioController.java
    │               ├── model/
    │               │   └── entity/
    │               │       └── Usuario.java
    │               ├── repository/
    │               │   └── UsuarioRepository.java
    │               ├── service/
    │               │   └── UsuarioService.java
    │               └── service/
    │                   └── impl/
    │                       └── UsuarioServiceImpl.java
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
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.app</groupId>
    <artifactId>usuarios</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    <name>usuarios</name>
    <description>CRUD de usuarios con Spring MVC y PostgreSQL</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring Web (MVC + REST) -->
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

## 2. application.properties

# ─── Servidor ────────────────────────────────────────────

```properties
server.port=8080

# ─── Datasource PostgreSQL ───────────────────────────────
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# ─── JPA / Hibernate ─────────────────────────────────────
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# ─── Mensajes de error detallados (solo desarrollo) ──────
server.error.include-message=always
server.error.include-binding-errors=always
```

## 3. Clase principal — UsuariosApplication.java

```java
package com.app.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}
```

## 4. Entidad — model/entity/Usuario.java

```java
package com.app.usuarios.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

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

Nota: El campo contrasena almacena la contraseña tal como llega. En un entorno productivo debes cifrarla con BCrypt antes de persistirla.

## 5. Repositorio — repository/UsuarioRepository.java

```java
package com.app.usuarios.repository;

import com.app.usuarios.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Verifica si ya existe un usuario registrado con el email indicado.
     *
     * @param email dirección de correo a verificar
     * @return true si el email ya está en uso
     */
    boolean existsByEmail(String email);

    /**
     * Busca un usuario por su dirección de email.
     *
     * @param email dirección de correo
     * @return Optional con el usuario encontrado, o vacío si no existe
     */
    Optional<Usuario> findByEmail(String email);
}
```

## 6. Interfaz de servicio — service/UsuarioService.java

```java
package com.app.usuarios.service;

import com.app.usuarios.model.entity.Usuario;

import java.util.List;

public interface UsuarioService {

    /**
     * Retorna todos los usuarios registrados.
     */
    List<Usuario> listarTodos();

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return usuario encontrado
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    Usuario buscarPorId(Long id);

    /**
     * Crea un nuevo usuario.
     *
     * @param usuario datos del usuario a crear
     * @return usuario persistido con su ID generado
     * @throws IllegalArgumentException si el email ya está registrado
     */
    Usuario crear(Usuario usuario);

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param id      identificador del usuario a actualizar
     * @param usuario nuevos datos del usuario
     * @return usuario actualizado
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     * @throws IllegalArgumentException si el nuevo email ya está en uso por otro usuario
     */
    Usuario actualizar(Long id, Usuario usuario);

    /**
     * Elimina un usuario por su ID.
     *
     * @param id identificador del usuario a eliminar
     * @throws jakarta.persistence.EntityNotFoundException si no existe
     */
    void eliminar(Long id);
}
```

## 7. Implementación del servicio — service/impl/UsuarioServiceImpl.java

```java
package com.app.usuarios.service.impl;

import com.app.usuarios.model.entity.Usuario;
import com.app.usuarios.repository.UsuarioRepository;
import com.app.usuarios.service.UsuarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // ─── Listar ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // ─── Buscar por ID ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con ID: " + id));
    }

    // ─── Crear ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Usuario crear(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException(
                    "El email '" + usuario.getEmail() + "' ya está registrado");
        }
        return usuarioRepository.save(usuario);
    }

    // ─── Actualizar ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Usuario actualizar(Long id, Usuario datosNuevos) {
        Usuario existente = buscarPorId(id);

        // Verifica unicidad del email solo si cambió
        if (!existente.getEmail().equalsIgnoreCase(datosNuevos.getEmail())
                && usuarioRepository.existsByEmail(datosNuevos.getEmail())) {
            throw new IllegalArgumentException(
                    "El email '" + datosNuevos.getEmail() + "' ya está en uso por otro usuario");
        }

        existente.setNombre(datosNuevos.getNombre());
        existente.setEmail(datosNuevos.getEmail());
        existente.setContrasena(datosNuevos.getContrasena());

        return usuarioRepository.save(existente);
    }

    // ─── Eliminar ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException(
                    "No se puede eliminar: usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
```

## 8. Controlador — controller/UsuarioController.java

```java
package com.app.usuarios.controller;

import com.app.usuarios.model.entity.Usuario;
import com.app.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ─── GET /api/usuarios ────────────────────────────────────────────────────
    /**
     * Retorna la lista completa de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // ─── GET /api/usuarios/{id} ───────────────────────────────────────────────
    /**
     * Retorna un usuario específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // ─── POST /api/usuarios ───────────────────────────────────────────────────
    /**
     * Crea un nuevo usuario.
     * Las validaciones de la entidad se aplican automáticamente con @Valid.
     */
    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody Usuario usuario) {
        Usuario creado = usuarioService.crear(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ─── PUT /api/usuarios/{id} ───────────────────────────────────────────────
    /**
     * Actualiza completamente un usuario existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario));
    }

    // ─── DELETE /api/usuarios/{id} ────────────────────────────────────────────
    /**
     * Elimina un usuario por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 9. Manejador global de errores — controller/GlobalExceptionHandler.java

```java
package com.app.usuarios.controller;

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

    // ─── 404 ─ Entidad no encontrada ─────────────────────────────────────────
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(
            EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ─── 400 ─ Argumento inválido (email duplicado, etc.) ────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ─── 422 ─ Errores de validación de Bean Validation (@Valid) ─────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errores.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        cuerpo.put("error", "Validation Failed");
        cuerpo.put("errores", errores);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(cuerpo);
    }

    // ─── 500 ─ Errores no controlados ────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String mensaje) {

        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);

        return ResponseEntity.status(status).body(cuerpo);
    }
}
```

## 10. Endpoints disponibles
| Método | URL | Descripción | Código de éxito |
| --- | --- | --- | --- |
| GET | /api/usuarios | Listar todos los usuarios | 200 OK |
| GET | /api/usuarios/{id} | Buscar usuario por ID | 200 OK |
| POST | /api/usuarios | Crear un nuevo usuario | 201 Created |
| PUT | /api/usuarios/{id} | Actualizar un usuario completo | 200 OK |
| DELETE | /api/usuarios/{id} | Eliminar un usuario | 204 No Content |

## Ejemplo de cuerpo para POST / PUT

```json
{
  "nombre": "María López",
  "email": "maria@correo.com",
  "contrasena": "segura1234"
}
```

## 11. Notas importantes
Cifrado de contraseñas: En producción agrega spring-boot-starter-security e inyecta un PasswordEncoder (BCrypt) en UsuarioServiceImpl antes de llamar a save().
DDL automático: La propiedad spring.jpa.hibernate.ddl-auto=update crea o actualiza la tabla usuarios al iniciar la aplicación. Cambia a validate en producción.
Credenciales de BD: Externaliza username y password con variables de entorno o Spring Cloud Config para entornos reales.
Java 21: El pom.xml está configurado con <java.version>21</java.version> y la versión del parent de Spring Boot (3.4.5) es totalmente compatible con Java 21.
