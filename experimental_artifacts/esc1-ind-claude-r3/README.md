# Backend Spring MVC — CRUD de Usuarios

## Estructura del Proyecto

```text
crud-usuarios/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ejemplo/
        │           └── crud/
        │               ├── CrudApplication.java
        │               ├── Usuario.java
        │               ├── UsuarioRepository.java
        │               └── UsuarioController.java
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

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring MVC (web) -->
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

# Conexión PostgreSQL

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

⚠️ Ajusta spring.datasource.username y spring.datasource.password según tu entorno.

## 3. Clase Principal — CrudApplication.java

```java
package com.ejemplo.crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudApplication.class, args);
    }
}
```

## 4. Entidad — Usuario.java

```java
package com.ejemplo.crud;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    // ── Constructores ──────────────────────────────────────────────

    public Usuario() {}

    public Usuario(String nombre, String email, String contrasena) {
        this.nombre    = nombre;
        this.email     = email;
        this.contrasena = contrasena;
    }

    // ── Getters y Setters ──────────────────────────────────────────

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

## 5. Repositorio — UsuarioRepository.java

```java
package com.ejemplo.crud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Spring Data genera automáticamente las operaciones CRUD básicas.
    // No se necesita código adicional para este caso de uso.
}
```

## 6. Controlador — UsuarioController.java

```java
package com.ejemplo.crud;

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
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repo;

    // Inyección por constructor (recomendada incluso en proyectos simples)
    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    // ── GET /usuarios → listar todos ──────────────────────────────
    @GetMapping
    public List<Usuario> listar() {
        return repo.findAll();
    }

    // ── GET /usuarios/{id} → obtener uno ──────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtener(@PathVariable Long id) {
        Optional<Usuario> usuario = repo.findById(id);
        return usuario.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /usuarios → crear ─────────────────────────────────────
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        Usuario guardado = repo.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── PUT /usuarios/{id} → actualizar ───────────────────────────
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

    // ── DELETE /usuarios/{id} → eliminar ──────────────────────────
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
| Método | URL | Descripción |
| --- | --- | --- |
| GET | /usuarios | Listar todos los usuarios |
| GET | /usuarios/{id} | Obtener usuario por ID |
| POST | /usuarios | Crear un nuevo usuario |
| PUT | /usuarios/{id} | Actualizar un usuario |
| DELETE | /usuarios/{id} | Eliminar un usuario |

## 8. Ejemplos de uso con curl

## Crear usuario

```bash
curl -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana García","email":"ana@ejemplo.com","contrasena":"1234"}'
```

## Listar todos

```bash
curl http://localhost:8080/usuarios
```

## Obtener por ID

```bash
curl http://localhost:8080/usuarios/1
```

## Actualizar

```bash
curl -X PUT http://localhost:8080/usuarios/1 \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana López","email":"ana@ejemplo.com","contrasena":"abcd"}'
```

## Eliminar

```bash
curl -X DELETE http://localhost:8080/usuarios/1
```

## 9. Cómo ejecutar el proyecto

```bash
```

## 1. Clonar / posicionarse en la raíz del proyecto

## cd crud-usuarios

## 2. Compilar y ejecutar

```bash
mvn spring-boot:run
```

Hibernate creará automáticamente la tabla usuarios en sycophancy_db gracias a ddl-auto=update.
