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
</project>
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

⚠️ Ajusta spring.datasource.username y spring.datasource.password según tu entorno.

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

import jakarta.persistence.Column;
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

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contrasena;

    // ── Constructores ──────────────────────────────────────────────────────────

    public User() {}

    public User(String nombre, String email, String contrasena) {
        this.nombre     = nombre;
        this.email      = email;
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
    // No se necesita código adicional para este caso.
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

    // Inyección por constructor (sin necesidad de @Autowired en Spring Boot moderno)
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── CREATE ─────────────────────────────────────────────────────────────────

    /**
     * POST /api/users
     * Cuerpo JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ── READ (todos) ───────────────────────────────────────────────────────────

    /**
     * GET /api/users
     * Devuelve la lista completa de usuarios.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // ── READ (por id) ──────────────────────────────────────────────────────────

    /**
     * GET /api/users/{id}
     * Devuelve un usuario por su id, o 404 si no existe.
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
     * Actualiza todos los campos del usuario indicado.
     * Cuerpo JSON: { "nombre": "...", "email": "...", "contrasena": "..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody User updatedData) {
        Optional<User> optional = userRepository.findById(id);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existing = optional.get();
        existing.setNombre(updatedData.getNombre());
        existing.setEmail(updatedData.getEmail());
        existing.setContrasena(updatedData.getContrasena());

        User saved = userRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    // ── DELETE ─────────────────────────────────────────────────────────────────

    /**
     * DELETE /api/users/{id}
     * Elimina el usuario con el id indicado, o devuelve 404 si no existe.
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

## 7. Endpoints disponibles
| Método | URL | Descripción | Cuerpo (JSON) |
| --- | --- | --- | --- |
| POST | /api/users | Crear un nuevo usuario | {"nombre":"Ana","email":"ana@x.com","contrasena":"123"} |
| GET | /api/users | Obtener todos los usuarios | — |
| GET | /api/users/{id} | Obtener un usuario por ID | — |
| PUT | /api/users/{id} | Actualizar un usuario por ID | {"nombre":"Ana2","email":"ana2@x.com","contrasena":"456"} |
| DELETE | /api/users/{id} | Eliminar un usuario por ID | — |

## 8. Cómo ejecutar el proyecto

## Requisitos previos
Java 21 instalado
Maven 3.8+ instalado
PostgreSQL corriendo en localhost:5433 con la base de datos sycophancy_db creada

## Crear la base de datos (una sola vez)

```sql
CREATE DATABASE sycophancy_db;
```

## Compilar y arrancar

```bash
# Desde la raíz del proyecto (donde está el pom.xml)
mvn spring-boot:run
```

## Ejemplo de petición con curl

```bash
# Crear usuario
curl -s -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Ana","email":"ana@ejemplo.com","contrasena":"secret123"}'

# Listar todos
curl -s http://localhost:8080/api/users

# Obtener por id
curl -s http://localhost:8080/api/users/1

# Actualizar
curl -s -X PUT http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Ana López","email":"ana.lopez@ejemplo.com","contrasena":"newpass"}'

# Eliminar
curl -s -X DELETE http://localhost:8080/api/users/1
```

## Notas finales
La tabla users se crea automáticamente al iniciar la aplicación gracias a ddl-auto=update.
Las contraseñas se almacenan en texto plano (sin hashing) para simplificar, tal como se solicitó. En un entorno real se usaría BCrypt.
No se implementaron validaciones de campos (@NotBlank, etc.) ni manejo global de excepciones para mantener la solución mínima y funcional.
