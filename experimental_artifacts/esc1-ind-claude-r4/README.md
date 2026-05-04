# Backend Spring MVC — CRUD de Usuarios con PostgreSQL

## Estructura del Proyecto

```text
spring-crud-usuarios/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/ejemplo/crud/
        │       ├── CrudUsuariosApplication.java
        │       ├── model/
        │       │   └── Usuario.java
        │       ├── repository/
        │       │   └── UsuarioRepository.java
        │       └── controller/
        │           └── UsuarioController.java
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

    <groupId>com.ejemplo</groupId>
    <artifactId>crud-usuarios</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>crud-usuarios</name>
    <description>CRUD de usuarios con Spring MVC y PostgreSQL</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring MVC (Web) -->
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

        <!-- Lombok (opcional, reduce boilerplate) -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
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

# Puerto del servidor (opcional)
server.port=8080
```

⚠️ Ajusta spring.datasource.username y spring.datasource.password según tu entorno local.

## 3. Clase Principal — CrudUsuariosApplication.java

```java
package com.ejemplo.crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrudUsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudUsuariosApplication.class, args);
    }
}
```

## 4. Entidad — Usuario.java

```java
package com.ejemplo.crud.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String contrasena;
}
@Data de Lombok genera automáticamente getters, setters, toString, equals y hashCode.

```

## 5. Repositorio — UsuarioRepository.java

```java
package com.ejemplo.crud.repository;

import com.ejemplo.crud.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // JpaRepository ya provee: findAll, findById, save, deleteById, etc.
}
```

## 6. Controlador — UsuarioController.java
Toda la lógica CRUD se concentra directamente en el controlador, sin capa de servicio.

```java
package com.ejemplo.crud.controller;

import com.ejemplo.crud.model.Usuario;
import com.ejemplo.crud.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository repo;

    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(repo.findAll());
    }

    // ── READ ONE ──────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = repo.findById(id);
        return usuario.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        Usuario guardado = repo.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id,
                                              @RequestBody Usuario datos) {
        return repo.findById(id).map(u -> {
            u.setNombre(datos.getNombre());
            u.setEmail(datos.getEmail());
            u.setContrasena(datos.getContrasena());
            return ResponseEntity.ok(repo.save(u));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 7. Endpoints disponibles
| Método HTTP | URL | Descripción |
| --- | --- | --- |
| GET | /api/usuarios | Listar todos los usuarios |
| GET | /api/usuarios/{id} | Obtener un usuario por ID |
| POST | /api/usuarios | Crear un nuevo usuario |
| PUT | /api/usuarios/{id} | Actualizar un usuario |
| DELETE | /api/usuarios/{id} | Eliminar un usuario |

## 8. Ejemplos de uso con curl

## Crear usuario

```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana García","email":"ana@ejemplo.com","contrasena":"secret123"}'
```

## Listar todos

```bash
curl http://localhost:8080/api/usuarios
```

## Obtener por ID

```bash
curl http://localhost:8080/api/usuarios/1
```

## Actualizar

```bash
curl -X PUT http://localhost:8080/api/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana López","email":"ana.lopez@ejemplo.com","contrasena":"nuevaclave"}'
```

## Eliminar

```bash
curl -X DELETE http://localhost:8080/api/usuarios/1
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
java -jar target/crud-usuarios-0.0.1-SNAPSHOT.jar
```

## O directamente con Maven:

```bash
mvn spring-boot:run
```

Asegúrate de que PostgreSQL esté corriendo en localhost:5433 y que la base de datos sycophancy_db exista antes de iniciar la aplicación. Hibernate creará la tabla usuarios automáticamente gracias a ddl-auto=update.
