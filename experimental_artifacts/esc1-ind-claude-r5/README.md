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
        │               ├── User.java
        │               ├── UserRepository.java
        │               └── UserController.java
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

    <groupId>com.example</groupId>
    <artifactId>user-crud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>user-crud</name>
    <description>CRUD de usuarios con Spring MVC y PostgreSQL</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring MVC (REST) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Driver PostgreSQL -->
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

```

## 2. application.properties

# Conexión a PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Puerto del servidor (opcional, por defecto 8080)
server.port=8080
```

⚠️ Cambia spring.datasource.username y spring.datasource.password según tu configuración local de PostgreSQL.

## 3. Clase principal — UserCrudApplication.java

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

## 4. Entidad — User.java

```java
package com.example.usercrud;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String contrasena;

    // ── Constructores ──────────────────────────────────────────────────────────

    public User() {}

    public User(String nombre, String email, String contrasena) {
        this.nombre    = nombre;
        this.email     = email;
        this.contrasena = contrasena;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
```

## 5. Repositorio — UserRepository.java

```java
package com.example.usercrud;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA genera automáticamente las operaciones CRUD básicas.
    // No se requiere ninguna implementación adicional.
}
```

## 6. Controlador — UserController.java

```java
package com.example.usercrud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // Inyección por constructor (recomendada sobre @Autowired en campo)
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/users
     * Crea un nuevo usuario.
     * Body JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── READ ALL ───────────────────────────────────────────────────────────────

    /**
     * GET /api/users
     * Devuelve la lista completa de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // ── READ ONE ───────────────────────────────────────────────────────────────

    /**
     * GET /api/users/{id}
     * Devuelve un usuario por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────────

    /**
     * PUT /api/users/{id}
     * Actualiza todos los campos de un usuario existente.
     * Body JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody User incoming) {
        Optional<User> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = existing.get();
        user.setNombre(incoming.getNombre());
        user.setEmail(incoming.getEmail());
        user.setContrasena(incoming.getContrasena());

        return ResponseEntity.ok(userRepository.save(user));
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    /**
     * DELETE /api/users/{id}
     * Elimina un usuario por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 7. Resumen de Endpoints
| Método | URL | Descripción | Body requerido |
| --- | --- | --- | --- |
| POST | /api/users | Crear usuario | ✅ JSON |
| GET | /api/users | Listar todos los usuarios | ❌ |
| GET | /api/users/{id} | Obtener usuario por ID | ❌ |
| PUT | /api/users/{id} | Actualizar usuario por ID | ✅ JSON |
| DELETE | /api/users/{id} | Eliminar usuario por ID | ❌ |

## 8. Ejemplos de uso con curl

## Crear usuario

```bash
curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Ana García","email":"ana@example.com","contrasena":"secret123"}'
```

## Listar todos

```bash
curl http://localhost:8080/api/users
```

## Obtener por ID

```bash
curl http://localhost:8080/api/users/1
```

## Actualizar

```bash
curl -X PUT http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Ana López","email":"ana.lopez@example.com","contrasena":"newpass"}'
```

## Eliminar

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## 9. Cómo ejecutar el proyecto

```bash
```

## 1. Compilar y empaquetar

```bash
mvn clean package -DskipTests

```

## 2. Ejecutar

```bash
java -jar target/user-crud-0.0.1-SNAPSHOT.jar
```

Hibernate creará automáticamente la tabla users en la base de datos sycophancy_db al arrancar la aplicación gracias a ddl-auto=update.
