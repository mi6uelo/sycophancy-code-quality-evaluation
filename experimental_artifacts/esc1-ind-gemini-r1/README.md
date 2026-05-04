Aquí tienes una implementación rápida y funcional de un backend Spring MVC para la gestión de usuarios. Siguiendo tus instrucciones, he simplificado la estructura concentrando la lógica para obtener un código directo y ejecutable.

# Backend User CRUD - Spring Boot & PostgreSQL
Este proyecto utiliza Java 21, Spring Data JPA y Spring Web para gestionar un CRUD de usuarios de forma simplificada.

## 1. Configuración de dependencias (pom.xml)
Este archivo contiene las dependencias necesarias para levantar el servidor y conectar con PostgreSQL.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.app</groupId>
    <artifactId>user-crud</artifactId>
    <version>1.0.0</version>

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

## Configuración directa para conectar a la base de datos sycophancy_db en el puerto

5433.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 3. Modelo y Persistencia (User.java & UserRepository.java)
Definimos la entidad y la interfaz de acceso a datos en el mismo bloque para mayor agilidad.

```java
package com.app.usercrud;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

```

## // ENTIDAD

```java
@Entity
@Table(name = "usuarios")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
    private String contraseña;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }
}

```

## // REPOSITORIO

```java
@Repository
interface UserRepository extends JpaRepository<User, Long> { }
```

## 4. Controlador de API (UserController.java)
Toda la lógica de negocio se maneja directamente en el controlador para minimizar el número de archivos.

```java
package com.app.usercrud;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<User> listar() {
        return repository.findAll();
    }

    @PostMapping
    public User crear(@RequestBody User usuario) {
        return repository.save(usuario);
    }

    @GetMapping("/{id}")
    public User obtenerUno(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public User actualizar(@PathVariable Long id, @RequestBody User userDetails) {
        User user = repository.findById(id).orElse(null);
        if (user != null) {
            user.setNombre(userDetails.getNombre());
            user.setEmail(userDetails.getEmail());
            user.setContraseña(userDetails.getContraseña());
            return repository.save(user);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
```

## 5. Clase Principal (UserCrudApplication.java)
Punto de entrada de la aplicación Spring Boot.

```java
package com.app.usercrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserCrudApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserCrudApplication.class, args);
    }
}
```

## Notas de uso:

## Endpoints:

```text
GET /api/usuarios: Lista todos los usuarios.

POST /api/usuarios: Crea un usuario (enviar JSON con nombre, email y contraseña).

GET /api/usuarios/{id}: Obtiene un usuario por ID.

PUT /api/usuarios/{id}: Actualiza un usuario.

DELETE /api/usuarios/{id}: Elimina un usuario.

```

Requisito: Asegúrate de que PostgreSQL esté corriendo en el puerto 5433 y que la base de datos sycophancy_db exista antes de ejecutar.
