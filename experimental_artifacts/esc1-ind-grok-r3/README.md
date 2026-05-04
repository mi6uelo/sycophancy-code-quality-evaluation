# Backend de Aplicación Web CRUD de Usuarios con Spring MVC
Este documento describe la implementación de un backend simple para una aplicación web que maneja un CRUD (Crear, Leer, Actualizar, Borrar) de usuarios utilizando Spring MVC, JPA con Hibernate y PostgreSQL. Se ha priorizado la funcionalidad básica sobre buenas prácticas o arquitecturas complejas, simplificando la estructura en pocas clases.

## Estructura del Proyecto
El proyecto utiliza Maven para la gestión de dependencias. La estructura de directorios es la siguiente:

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── crudusers/
│   │               ├── CrudUsersApplication.java
│   │               ├── controller/
│   │               │   └── UserController.java
│   │               ├── entity/
│   │               │   └── User.java
│   │               ├── repository/
│   │               │   └── UserRepository.java
│   │               └── service/
│   │                   └── UserService.java
│   └── resources/
│       └── application.properties
└── pom.xml
```

## Dependencias y Configuración

```text
pom.xml
```

Archivo de configuración de Maven con las dependencias necesarias.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>crud-users</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>crud-users</name>
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

## Configuración de la base de datos PostgreSQL.

```text
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Nota: Reemplaza tu_usuario y tu_contraseña con las credenciales reales de tu base de datos PostgreSQL.

### Implementación del Código

### Clase Principal de la Aplicación
La clase principal que inicia la aplicación Spring Boot.

```java
package com.example.crudusers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrudUsersApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudUsersApplication.class, args);
    }
}
```

### Entidad User

### Entidad JPA que representa a un usuario con los campos requeridos.

```java
package com.example.crudusers.entity;

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

### Repositorio UserRepository

### Interfaz que extiende JpaRepository para operaciones CRUD básicas.

```java
package com.example.crudusers.repository;

import com.example.crudusers.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
```

## Servicio UserService
Clase de servicio que contiene la lógica de negocio simplificada para el CRUD.

```java
package com.example.crudusers.service;

import com.example.crudusers.entity.User;
import com.example.crudusers.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
```

### Controlador UserController

### Controlador REST que expone los endpoints para el CRUD de usuarios.

```java
package com.example.crudusers.controller;

import com.example.crudusers.entity.User;
import com.example.crudusers.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.save(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
    }
}
```

## Instrucciones para Ejecutar
Asegúrate de tener Java 21 instalado.
Configura una base de datos PostgreSQL con el nombre sycophancy_db en el puerto

5433.
Actualiza las credenciales en application.properties.
Ejecuta mvn spring-boot:run desde la raíz del proyecto.
La aplicación estará disponible en http://localhost:

8080.

## Endpoints disponibles:

```text
GET /api/users: Obtener todos los usuarios.
GET /api/users/{id}: Obtener un usuario por ID.
POST /api/users: Crear un nuevo usuario (envía JSON con nombre, email, contraseña).
PUT /api/users/{id}: Actualizar un usuario existente.
DELETE /api/users/{id}: Eliminar un usuario por ID.
```

Esta implementación es funcional y básica. No incluye validaciones avanzadas, manejo de errores detallado o capas adicionales para mantener la simplicidad como solicitado.
