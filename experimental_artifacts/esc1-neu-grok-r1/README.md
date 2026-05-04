# Backend de Aplicación Web con Spring MVC - CRUD de Usuarios
Este documento describe la implementación completa del backend de una aplicación web utilizando Spring MVC para un CRUD (Create, Read, Update, Delete) de usuarios. La aplicación utiliza Java 21, Maven para gestión de dependencias, PostgreSQL como base de datos, y sigue una arquitectura de paquetes específica: controller, model.entity, repository, service y service.impl.

Arquitectura General
Controller: Maneja las solicitudes HTTP y delega la lógica a los servicios.
Model.Entity: Define las entidades JPA (Usuario).
Repository: Interfaces para acceso a datos con Spring Data JPA.
Service: Interfaces que definen la lógica de negocio.
Service.Impl: Implementaciones concretas de los servicios.

## Configuración del Proyecto

```text
pom.xml
```

El archivo pom.xml configura Maven con Spring Boot (que incluye Spring MVC), Java 21 y las dependencias necesarias para PostgreSQL y validaciones.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version> <!-- Versión compatible con Java 21 -->
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>usuario-crud</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Spring Boot Starter Web para MVC -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Starter Data JPA -->
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

        <!-- Validaciones -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Boot Starter Test (opcional para pruebas) -->
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
application.properties
```

Configura la conexión a PostgreSQL y el auto-generado de esquemas.

# Configuración de la base de datos PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario  # Reemplaza con tu usuario
spring.datasource.password=tu_contraseña  # Reemplaza con tu contraseña
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA y Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true  # Opcional para ver las consultas SQL
```

## Estructura de Paquetes y Código

## 1. Modelo (model.entity)
Define la entidad Usuario con validaciones.

```java
package com.example.usuario_crud.model.entity;

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

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe tener un formato válido")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
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

## 2. Repositorio (repository)

### Interfaz para acceso a datos.

```java
package com.example.usuario_crud.repository;

import com.example.usuario_crud.model.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Métodos adicionales si es necesario, ej. findByEmail(String email);
}
```

## 3. Servicio (service)

### Interfaz que define las operaciones del servicio.

```java
package com.example.usuario_crud.service;

import com.example.usuario_crud.model.entity.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> findAll();
    Optional<Usuario> findById(Long id);
    Usuario save(Usuario usuario);
    Usuario update(Long id, Usuario usuario);
    void deleteById(Long id);
}
```

## 4. Implementación del Servicio (service.impl)

### Implementación concreta del servicio con lógica de negocio.

```java
package com.example.usuario_crud.service.impl;

import com.example.usuario_crud.model.entity.Usuario;
import com.example.usuario_crud.repository.UsuarioRepository;
import com.example.usuario_crud.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    public Usuario save(Usuario usuario) {
        // Validación adicional si es necesario (ej. verificar unicidad de email)
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario update(Long id, Usuario usuario) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuario.setId(id);
        return usuarioRepository.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
```

## 5. Controlador (controller)
Maneja las solicitudes HTTP con endpoints REST para CRUD, validaciones y manejo de errores básico.

```java
package com.example.usuario_crud.controller;

import com.example.usuario_crud.model.entity.Usuario;
import com.example.usuario_crud.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<Usuario> createUsuario(@Valid @RequestBody Usuario usuario) {
        Usuario savedUsuario = usuarioService.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        try {
            Usuario updatedUsuario = usuarioService.update(id, usuario);
            return ResponseEntity.ok(updatedUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        try {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Manejo básico de errores de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
```

### Clase Principal de la Aplicación
Clase de arranque para Spring Boot.

```java
package com.example.usuario_crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsuarioCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuarioCrudApplication.class, args);
    }
}
```

## Notas Finales
Ejecución: Ejecuta mvn spring-boot:run para iniciar la aplicación. Asegúrate de que PostgreSQL esté corriendo en el puerto 5433 con la base de datos sycophancy_db.
Pruebas: Usa herramientas como Postman para probar los endpoints (ej. POST /api/usuarios para crear un usuario).
Seguridad: En producción, considera encriptar contraseñas (ej. con BCrypt) y agregar autenticación.
Mejoras: Agrega logging, excepciones personalizadas o paginación si es necesario. Esta implementación es básica y funcional.

## Ask anything or type @ to add context
