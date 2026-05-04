# Backend de Aplicación Web con Spring MVC - CRUD de Usuarios
Este documento describe la implementación completa de un backend para una aplicación web utilizando Spring MVC, que implementa operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para entidades de Usuario. La aplicación utiliza Java 21, Maven para la gestión de dependencias y PostgreSQL como base de datos.

Arquitectura del Proyecto
La estructura del proyecto sigue el patrón MVC con separación de responsabilidades en capas:

controller: Controladores REST para manejar las solicitudes HTTP.
model.entity: Entidades JPA que representan las tablas de la base de datos.
repository: Interfaces de repositorio para acceder a los datos.
service: Interfaces de servicio que definen la lógica de negocio.
service.impl: Implementaciones concretas de los servicios.

## Estructura de Directorios

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── sycophancy/
│   │               ├── controller/
│   │               ├── model/
│   │               │   └── entity/
│   │               ├── repository/
│   │               ├── service/
│   │               │   └── impl/
│   │               └── SycophancyApplication.java
│   └── resources/
│       └── application.properties
```

└��─ pom.xml

### Archivo pom.xml
El archivo pom.xml configura las dependencias necesarias para Spring Boot, JPA y PostgreSQL.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>sycophancy</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>sycophancy</name>
    <description>Backend CRUD de Usuarios con Spring MVC</description>

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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
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
            </plugin>
        </plugins>
    </build>
</project>
```

### Archivo application.properties

## Configuración de la base de datos y JPA.

```text
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Clase Principal
La clase principal de Spring Boot.

```java
package com.example.sycophancy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SycophancyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SycophancyApplication.class, args);
    }
}
```

### Modelo de Datos - Usuario (model.entity)

### Entidad JPA que representa la tabla de usuarios en la base de datos.

```java
package com.example.sycophancy.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(nullable = false)
    private String contraseña;

    // Constructores
    public Usuario() {}

    public Usuario(String nombre, String email, String contraseña) {
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
    }

    // Getters y Setters
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

### Repositorio (repository)

### Interfaz de repositorio que extiende JpaRepository para operaciones CRUD básicas.

```java
package com.example.sycophancy.repository;

import com.example.sycophancy.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Métodos adicionales si son necesarios, por ejemplo:
    // Optional<Usuario> findByEmail(String email);
}
```

## Servicio (service)

### Interfaz que define las operaciones de negocio para usuarios.

```java
package com.example.sycophancy.service;

import com.example.sycophancy.model.entity.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Usuario save(Usuario usuario);
    Usuario update(Usuario usuario);
    void deleteById(Long id);
}
```

### Implementación del Servicio (service.impl)

### Implementación concreta del servicio, que maneja la lógica de negocio y validaciones.

```java
package com.example.sycophancy.service.impl;

import com.example.sycophancy.model.entity.Usuario;
import com.example.sycophancy.repository.UsuarioRepository;
import com.example.sycophancy.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    public Usuario save(@Valid Usuario usuario) {
        // Aquí se pueden agregar validaciones adicionales si es necesario
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario update(@Valid Usuario usuario) {
        if (!usuarioRepository.existsById(usuario.getId())) {
            throw new RuntimeException("Usuario no encontrado con id: " + usuario.getId());
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
```

### Controlador (controller)

### Controlador REST que expone los endpoints para las operaciones CRUD.

```java
package com.example.sycophancy.controller;

import com.example.sycophancy.model.entity.Usuario;
import com.example.sycophancy.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            return ResponseEntity.ok(usuario.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createUsuario(@Valid @RequestBody Usuario usuario, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Errores de validación: " + result.getAllErrors());
        }
        try {
            Usuario savedUsuario = usuarioService.save(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable Long id, @Valid @RequestBody Usuario usuario, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Errores de validación: " + result.getAllErrors());
        }
        usuario.setId(id);
        try {
            Usuario updatedUsuario = usuarioService.update(usuario);
            return ResponseEntity.ok(updatedUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable Long id) {
        try {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar usuario: " + e.getMessage());
        }
    }
}
```

## Manejo de Errores Global
Para un manejo básico de errores, puedes agregar una clase de controlador de excepciones global.

```java
package com.example.sycophancy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
    }
}
```

## Instrucciones de Ejecución
Configurar la Base de Datos: Asegúrate de tener PostgreSQL corriendo en localhost:5433 con una base de datos llamada sycophancy_db. Actualiza las credenciales en application.properties.

## Ejecutar la Aplicación: Usa Maven para ejecutar la aplicación:

```text
mvn spring-boot:run
```

## Probar los Endpoints:

```text
GET /api/usuarios: Obtener todos los usuarios.
GET /api/usuarios/{id}: Obtener un usuario por ID.
POST /api/usuarios: Crear un nuevo usuario (envía JSON con nombre, email, contraseña).
PUT /api/usuarios/{id}: Actualizar un usuario existente.
DELETE /api/usuarios/{id}: Eliminar un usuario por ID.
```

Las validaciones se aplican automáticamente gracias a @Valid y las anotaciones en la entidad. Los errores se manejan en el controlador y en el manejador global de excepciones.
