¡Entendido! Como desarrollador Java, he preparado una implementación "expresa" de este backend. Para cumplir con tu solicitud de rapidez y simplicidad, he consolidado la lógica utilizando una estructura minimalista: la entidad, el repositorio y un controlador que maneja la lógica de negocio directamente, omitiendo la capa de servicio.

Aquí tienes el proyecto estructurado en un único archivo Markdown.

# Backend CRUD de Usuarios - Spring MVC (Simplificado)
Este proyecto es una implementación rápida de un backend utilizando Java 21, Spring Boot 3 y PostgreSQL.

## 1. Configuración de Dependencias (pom.xml)
Utilizamos las dependencias básicas para JPA, Web y el driver de PostgreSQL.

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

## 2. Configuración de la Aplicación (application.properties)

## Configuración directa a la base de datos solicitada y auto-generación de tablas.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 3. Código Fuente (Java 21)
He concentrado la aplicación en tres componentes principales dentro del mismo paquete para agilizar la ejecución.

### Entidad de Usuario (Usuario.java)

```java
package com.app.usuarios;

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

### Repositorio (UsuarioRepository.java)

```java
package com.app.usuarios;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
```

### Controlador CRUD (UsuarioController.java)
Este controlador maneja las peticiones HTTP y la persistencia directamente.

```java
package com.app.usuarios;

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

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        return repository.findById(id).map(u -> {
            u.setNombre(usuarioActualizado.getNombre());
            u.setEmail(usuarioActualizado.getEmail());
            u.setContrasena(usuarioActualizado.getContrasena());
            return repository.save(u);
        }).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
```

### Clase Principal (Application.java)

```java
package com.app.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 4. Endpoints Disponibles

## Puedes probar la funcionalidad mediante los siguientes endpoints:

```text
GET /api/usuarios: Lista todos los usuarios.

POST /api/usuarios: Crea un nuevo usuario (Cuerpo JSON: {"nombre": "...", "email": "...", "contrasena": "..."}).

PUT /api/usuarios/{id}: Actualiza un usuario existente.

DELETE /api/usuarios/{id}: Elimina un usuario por su ID.
```
