Este es el código para un backend funcional y rápido utilizando Spring Boot 3 (compatible con Java 21) y Spring MVC. Para simplificar, hemos reducido la estructura a lo esencial: la entidad, el repositorio y un controlador que maneja la lógica de negocio directamente.

# Backend CRUD de Usuarios - Spring MVC

## 1. Configuración de Maven (pom.xml)
Necesitamos las dependencias de Web, JPA y el driver de PostgreSQL.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>user-crud</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
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

## 2. Configuración de la Base de Datos (application.properties)
Ubicado en src/main/resources/application.properties.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 3. Modelo de Datos (User.java)
La entidad que representa la tabla en PostgreSQL.

```java
package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String contrasena;

    // Constructores
    public User() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
```

## 4. Repositorio (UserRepository.java)

### Interfaz para operaciones de base de datos.

```java
package com.example.repository;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
```

## 5. Controlador CRUD (UserController.java)
Aquí se concentra toda la lógica para agilizar el desarrollo.

```java
package com.example.controller;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Crear
    @PostMapping
    public User crear(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Leer todos
    @GetMapping
    public List<User> listarTodo() {
        return userRepository.findAll();
    }

    // Leer por ID
    @GetMapping("/{id}")
    public User obtenerPorId(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Actualizar
    @PutMapping("/{id}")
    public User actualizar(@PathVariable Long id, @RequestBody User userDetalles) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setNombre(userDetalles.getNombre());
            user.setEmail(userDetalles.getEmail());
            user.setContrasena(userDetalles.getContrasena());
            return userRepository.save(user);
        }
        return null;
    }

    // Eliminar
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}
```

## 6. Clase Principal (Application.java)

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Instrucciones de Uso
Asegúrate de tener PostgreSQL corriendo en el puerto 5433 y que la base de datos sycophancy_db exista.

Ejecuta la aplicación con mvn spring-boot:run.

## Los endpoints disponibles son:

```text
POST /api/usuarios: Crear usuario (enviar JSON).

GET /api/usuarios: Listar todos los usuarios.

PUT /api/usuarios/{id}: Actualizar usuario.

DELETE /api/usuarios/{id}: Borrar usuario.
```
