# Backend CRUD de Usuarios con Spring MVC
Este documento describe la implementación de un backend simple para un CRUD de usuarios utilizando Spring MVC (como parte de Spring Boot). El proyecto utiliza Maven para la gestión de dependencias, PostgreSQL como base de datos, y Java

## 21. La base de datos se genera automáticamente a partir del código fuente mediante Hibernate.

## Estructura del Proyecto
pom.xml: Archivo de configuración de Maven con dependencias.
`src/main/resources/application.properties: Configuración de la aplicación y base de datos.`
`src/main/java/com/example/usercrud/entity/User.java: Entidad JPA para el usuario.`
`src/main/java/com/example/usercrud/repository/UserRepository.java: Repositorio para operaciones de base de datos.`
`src/main/java/com/example/usercrud/service/UserService.java: Servicio con lógica de negocio simplificada.`
`src/main/java/com/example/usercrud/controller/UserController.java: Controlador REST con endpoints CRUD.`

```text
src/main/java/com/example/usercrud/UserCrudApplication.java: Clase principal de Spring Boot.
```

## Dependencias y Configuración
`pom.xml`

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
    <artifactId>usercrud</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>usercrud</name>
    <description>CRUD de usuarios con Spring MVC</description>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
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

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Nota: Reemplaza tu_usuario y tu_contraseña con las credenciales reales de PostgreSQL.

## Código Fuente

## User.java (Entidad)

```java
package com.example.usercrud.entity;

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

## UserRepository.java (Repositorio)

```java
package com.example.usercrud.repository;

import com.example.usercrud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
```

## UserService.java (Servicio)

```java
package com.example.usercrud.service;

import com.example.usercrud.entity.User;
import com.example.usercrud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setNombre(userDetails.getNombre());
            user.setEmail(userDetails.getEmail());
            user.setContraseña(userDetails.getContraseña());
            return userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

## UserController.java (Controlador)

```java
package com.example.usercrud.controller;

import com.example.usercrud.entity.User;
import com.example.usercrud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

## UserCrudApplication.java (Clase Principal)

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

## Instrucciones de Ejecución
Asegúrate de tener PostgreSQL corriendo en localhost:5433 con una base de datos llamada sycophancy_db.
Actualiza las credenciales en application.properties.
Ejecuta mvn spring-boot:run en el directorio raíz del proyecto.
Los endpoints REST estarán disponibles en http://localhost:8080/api/users:

```text
GET /api/users: Obtener todos los usuarios.
GET /api/users/{id}: Obtener un usuario por ID.
POST /api/users: Crear un nuevo usuario (envía JSON con nombre, email, contraseña).
PUT /api/users/{id}: Actualizar un usuario.
DELETE /api/users/{id}: Eliminar un usuario.
```

## Notas
La contraseña se almacena en texto plano; en un entorno real, se debería encriptar.
No se incluyen validaciones extensas ni manejo de errores detallado para mantener la simplicidad.
La base de datos se crea automáticamente con la tabla user al iniciar la aplicación.
