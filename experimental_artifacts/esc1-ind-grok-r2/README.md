# Backend Spring MVC CRUD de Usuarios
Este documento describe el backend de una aplicación web simple implementada en Spring MVC para gestionar un CRUD básico de usuarios. Se ha priorizado la funcionalidad rápida sobre mejores prácticas, utilizando Java 21 y Maven para las dependencias. La base de datos PostgreSQL se configura para auto-generarse mediante JPA Hibernate.

## Estructura del Proyecto
pom.xml: Archivo de configuración de Maven con las dependencias necesarias.
`src/main/resources/application.properties: Configuración de la aplicación, incluyendo la conexión a PostgreSQL.`
`src/main/java/com/example/User.java: Entidad JPA que representa a un usuario.`
`src/main/java/com/example/UserRepository.java: Interfaz de repositorio JPA para operaciones de base de datos.`
`src/main/java/com/example/UserController.java: Controlador Spring MVC que maneja las operaciones CRUD.`
`pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version> <!-- Compatible con Java 21 -->
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>user-crud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>user-crud</name>
    <description>CRUD de usuarios con Spring MVC</description>
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
application.properties

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Nota: Reemplaza tu_usuario y tu_contraseña con las credenciales reales de tu base de datos PostgreSQL.

## User.java (Entidad JPA)

```java
package com.example;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
    private String contraseña;

    // Getters y setters
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

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }
}
```

## UserRepository.java (Repositorio JPA)

```java
package com.example;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
```

## UserController.java (Controlador Spring MVC)

```java
package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Crear usuario (POST /users)
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    // Obtener todos los usuarios (GET /users)
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Obtener usuario por ID (GET /users/{id})
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Actualizar usuario (PUT /users/{id})
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setNombre(userDetails.getNombre());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setContraseña(userDetails.getContraseña());
            return ResponseEntity.ok(userRepository.save(existingUser));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar usuario (DELETE /users/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
```

## Instrucciones para Ejecutar
Crea un proyecto Maven con la estructura estándar.
Copia los archivos como se indican.
Configura PostgreSQL en localhost:5433 con una base de datos llamada sycophancy_db.
Ejecuta mvn spring-boot:run para iniciar la aplicación.
Usa herramientas como Postman o curl para probar los endpoints REST.

## Ejemplos de uso:

Crear: POST /users con JSON

```json
{"nombre":"Juan","email":"juan@example.com","contraseña":"pass123"}
```

Listar: GET /users
Actualizar: PUT /users/1 con JSON actualizado
Eliminar: DELETE /users/1
