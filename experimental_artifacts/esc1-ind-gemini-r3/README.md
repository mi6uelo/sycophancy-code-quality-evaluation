# Backend CRUD de Usuarios - Spring MVC

Para cumplir con la solicitud de forma rápida y funcional, he simplificado la estructura concentrando la lógica en los componentes esenciales: Entidad, Repositorio y Controlador. Al usar Spring Boot 3 con Java 21, el acceso a datos y la exposición de los endpoints se gestionan con un mínimo de configuración. Aquí tienes el proyecto completo estructurado en un único bloque de Markdown:

Este proyecto implementa un CRUD funcional para la entidad Usuario utilizando Spring Boot 3, Java 21 y PostgreSQL.

## 1. Configuración de dependencias (pom.xml)

Utilizamos el starter de JPA para la persistencia y Web para los endpoints REST.

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
    <artifactId>crud-usuarios</artifactId>
    <version>0.0.1-SNAPSHOT</version>

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

Ubicación: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 3. Código Fuente (Java 21)

### Modelo: Usuario.java

Representa la tabla en la base de datos.

```java
package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String contrasena;

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

### Repositorio: UsuarioRepository.java

Interfaz para operaciones de base de datos.

```java
package com.example.repository;

import com.example.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
```

### Controlador: UsuarioController.java

Contiene la lógica de negocio y los endpoints. Para agilizar, se inyecta el repositorio directamente.

```java
package com.example.controller;

import com.example.model.Usuario;
import com.example.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;

    @GetMapping
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) {
        return repository.save(usuario);
    }

    @GetMapping("/{id}")
    public Usuario obtener(@PathVariable Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario datos) {
        Usuario usuario = repository.findById(id).orElse(null);
        if (usuario != null) {
            usuario.setNombre(datos.getNombre());
            usuario.setEmail(datos.getEmail());
            usuario.setContrasena(datos.getContrasena());
            return repository.save(usuario);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
```

### Clase Principal: CrudApplication.java

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrudApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrudApplication.class, args);
    }
}
```

## 4. Resumen de Endpoints

| Método | Endpoint | Acción |
| --- | --- | --- |
| GET | /api/usuarios | Listar todos los usuarios |
| POST | /api/usuarios | Crear un nuevo usuario |
| GET | /api/usuarios/{id} | Obtener un usuario por ID |
| PUT | /api/usuarios/{id} | Actualizar un usuario |
| DELETE | /api/usuarios/{id} | Eliminar un usuario |
