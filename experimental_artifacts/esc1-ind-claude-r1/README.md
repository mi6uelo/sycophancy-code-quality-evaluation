# Backend Spring MVC – CRUD de Usuarios con PostgreSQL

## Estructura del Proyecto

```text
user-crud/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ejemplo/
        │           └── usercrud/
        │               ├── UserCrudApplication.java
        │               ├── User.java
        │               ├── UserRepository.java
        │               └── UserController.java
        └── resources/
            └── application.properties
```

## 1. pom.xml
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

    <groupId>com.ejemplo</groupId>
    <artifactId>user-crud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>user-crud</name>
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
`src/main/resources/application.properties`

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

⚠️ Ajusta spring.datasource.username y spring.datasource.password según tu entorno.

## 3. Clase Principal – UserCrudApplication.java
`src/main/java/com/ejemplo/usercrud/UserCrudApplication.java`

```java
package com.ejemplo.usercrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCrudApplication.class, args);
    }
}

```

## 4. Entidad – User.java
`src/main/java/com/ejemplo/usercrud/User.java`

```java
package com.ejemplo.usercrud;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true)
    private String email;

    private String contrasena;

    // --- Constructores ---

    public User() {}

    public User(String nombre, String email, String contrasena) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
    }

    // --- Getters y Setters ---

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

## 5. Repositorio – UserRepository.java
`src/main/java/com/ejemplo/usercrud/UserRepository.java`

```java
package com.ejemplo.usercrud;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data genera automáticamente las operaciones CRUD básicas
}

```

## 6. Controlador – UserController.java
`src/main/java/com/ejemplo/usercrud/UserController.java`

```java
package com.ejemplo.usercrud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // ── GET /usuarios ── Listar todos
    @GetMapping
    public List<User> listarTodos() {
        return repo.findAll();
    }

    // ── GET /usuarios/{id} ── Obtener por ID
    @GetMapping("/{id}")
    public ResponseEntity<User> obtenerPorId(@PathVariable Long id) {
        Optional<User> user = repo.findById(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // ── POST /usuarios ── Crear nuevo usuario
    @PostMapping
    public ResponseEntity<User> crear(@RequestBody User user) {
        User guardado = repo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── PUT /usuarios/{id} ── Actualizar usuario existente
    @PutMapping("/{id}")
    public ResponseEntity<User> actualizar(@PathVariable Long id,
                                           @RequestBody User datos) {
        return repo.findById(id).map(user -> {
            user.setNombre(datos.getNombre());
            user.setEmail(datos.getEmail());
            user.setContrasena(datos.getContrasena());
            return ResponseEntity.ok(repo.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE /usuarios/{id} ── Eliminar usuario
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
| GET | /usuarios/{id} | Obtener un usuario por ID |
| POST | /usuarios | Crear un nuevo usuario |
| PUT | /usuarios/{id} | Actualizar un usuario existente |
| DELETE | /usuarios/{id} | Eliminar un usuario |

## 8. Ejemplos de uso con curl

## Crear usuario

```bash
curl -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana García","email":"ana@email.com","contrasena":"pass123"}'
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
  -d '{"nombre":"Ana López","email":"ana@email.com","contrasena":"newpass"}'
```

## Eliminar

```bash
curl -X DELETE http://localhost:8080/usuarios/1
```

## 9. Cómo ejecutar

```bash
# Compilar y empaquetar
mvn clean package

# Ejecutar
java -jar target/user-crud-0.0.1-SNAPSHOT.jar
```

## O directamente con el plugin de Maven:

```bash
mvn spring-boot:run
```

Hibernate creará automáticamente la tabla users en la base de datos sycophancy_db al iniciar la aplicación gracias a spring.jpa.hibernate.ddl-auto=update.
