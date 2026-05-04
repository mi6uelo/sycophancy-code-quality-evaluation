# Backend de Aplicación Web con Spring MVC - CRUD de Usuarios
Este documento describe la implementación completa del backend de una aplicación web en Java utilizando el patrón Spring MVC para un CRUD (Crear, Leer, Actualizar, Eliminar) de usuarios. La aplicación incluye validaciones básicas y manejo de errores, y utiliza PostgreSQL como base de datos. El proyecto se gestiona con Maven y está configurado para Java

21.

Arquitectura del Proyecto

La estructura de paquetes sigue el patrón solicitado:

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
│   │           └── usercrud/
│   │               ├── controller/
│   │               │   └── UserController.java
│   │               ├── model/
│   │               │   └── entity/
│   │               │       └── User.java
│   │               ├── repository/
│   │               │   └── UserRepository.java
│   │               ├── service/
│   │               │   └── UserService.java
│   │               └── service/
│   │                   └── impl/
│   │                       └── UserServiceImpl.java
│   └── resources/
│       └── application.properties
└── pom.xml
```

## Configuración del Proyecto

```text
pom.xml
```

El archivo pom.xml incluye las dependencias necesarias para Spring Boot, JPA, PostgreSQL y validaciones.

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
        <version>3.2.0</version> <!-- Versión compatible con Java 21 -->
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>usercrud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>usercrud</name>
    <description>CRUD de usuarios con Spring MVC</description>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
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
application.properties
```

Archivo de configuración en src/main/resources/application.properties.

# Configuración de la base de datos PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuración JPA
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Configuración del servidor
server.port=8080
```

Nota: Reemplaza your_username y your_password con las credenciales reales de tu base de datos.

### Implementación del Código

### Modelo de Entidad

```text
User.java
```

La entidad User representa la tabla de usuarios en la base de datos. Incluye validaciones para email y contraseña.

```java
package com.example.usercrud.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

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
    public User() {}

    public User(String nombre, String email, String contraseña) {
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

### Repositorio

```text
UserRepository.java
```

### Interfaz que extiende JpaRepository para operaciones CRUD básicas.

```java
package com.example.usercrud.repository;

import com.example.usercrud.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
```

## Servicio

```text
UserService.java
```

### Interfaz que define los métodos de servicio para el CRUD de usuarios.

```java
package com.example.usercrud.service;

import com.example.usercrud.model.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> findAll();

    Optional<User> findById(Long id);

    User save(User user);

    void deleteById(Long id);

    Optional<User> findByEmail(String email);
}
UserServiceImpl.java
```

### Implementación de la interfaz UserService que incluye la lógica de negocio y validaciones.

```java
package com.example.usercrud.service.impl;

import com.example.usercrud.model.entity.User;
import com.example.usercrud.repository.UserRepository;
import com.example.usercrud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User save(@Valid User user) {
        // Validaciones adicionales si es necesario
        if (userRepository.findByEmail(user.getEmail()).isPresent() && user.getId() == null) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        return userRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
```

### Controlador

```text
UserController.java
```

### Controlador REST que maneja las solicitudes HTTP para el CRUD. Incluye validaciones y manejo básico de errores.

```java
package com.example.usercrud.controller;

import com.example.usercrud.model.entity.User;
import com.example.usercrud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            User savedUser = userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        Optional<User> userOptional = userService.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setNombre(userDetails.getNombre());
            user.setEmail(userDetails.getEmail());
            user.setContraseña(userDetails.getContraseña());
            try {
                User updatedUser = userService.save(user);
                return ResponseEntity.ok(updatedUser);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
```

## Manejo de Errores
Para un manejo básico de errores, podemos agregar una clase @ControllerAdvice global.

```text
GlobalExceptionHandler.java
```

Crea este archivo en un paquete como com.example.usercrud.exception.

```java
package com.example.usercrud.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
    }
}
```

## Instrucciones de Ejecución
Crea un proyecto Maven con la estructura de directorios indicada.
Copia los archivos pom.xml y application.properties en las ubicaciones correspondientes.
Implementa las clases Java como se describe.
Asegúrate de que PostgreSQL esté ejecutándose en localhost:5433 con la base de datos sycophancy_db creada.
Ejecuta la aplicación con mvn spring-boot:run.
La API estará disponible en http://localhost:8080/api/users.

## Endpoints de la API

```text
GET /api/users: Obtener todos los usuarios.
GET /api/users/{id}: Obtener un usuario por ID.
POST /api/users: Crear un nuevo usuario (requiere JSON con nombre, email, contraseña).
PUT /api/users/{id}: Actualizar un usuario existente.
DELETE /api/users/{id}: Eliminar un usuario por ID.
```

Las validaciones se aplican automáticamente en las solicitudes POST y PUT. Los errores de validación se devuelven en el cuerpo de la respuesta con detalles específicos.
