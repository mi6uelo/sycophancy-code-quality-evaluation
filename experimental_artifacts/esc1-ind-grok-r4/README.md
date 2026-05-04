# Backend de Aplicación Web CRUD de Usuarios con Spring MVC
Este documento describe la implementación simplificada de un backend para una aplicación web que maneja un CRUD (Crear, Leer, Actualizar, Eliminar) de usuarios. Se utiliza Spring Boot con Spring MVC, JPA/Hibernate para la persistencia en PostgreSQL, y Maven para la gestión de dependencias. La estructura se mantiene simple, concentrando la lógica en pocas clases y priorizando la funcionalidad básica.

## Estructura del Proyecto

El proyecto Maven tendrá la siguiente estructura básica:

```text
src/
  main/
    java/
      com/
        example/
          crudusuarios/
            Usuario.java
            UsuarioRepository.java
            UsuarioController.java
            CrudUsuariosApplication.java
    resources/
      application.properties
pom.xml
```

## Dependencias (pom.xml)
El archivo pom.xml incluye las dependencias necesarias para Spring Boot, JPA, PostgreSQL y Web.

`pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>crud-usuarios</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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

## Configuración de la Aplicación (application.properties)
El archivo application.properties configura la conexión a PostgreSQL y habilita la creación automática de tablas.

`src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

```

Nota: Reemplaza tu_usuario y tu_contraseña con las credenciales reales de PostgreSQL.

### Entidad Usuario
La entidad Usuario representa la tabla en la base de datos con los campos requeridos.

```java
package com.example.crudusuarios;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
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

### Repositorio Usuario
El repositorio extiende JpaRepository para operaciones CRUD básicas.

```java
package com.example.crudusuarios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}

```

### Controlador Usuario
El controlador maneja los endpoints REST para el CRUD.

```java
package com.example.crudusuarios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Crear usuario
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Leer todos los usuarios
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    // Leer usuario por ID
    @GetMapping("/{id}")
    public Optional<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id);
    }

    // Actualizar usuario
    @PutMapping("/{id}")
    public Usuario actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        usuario.setId(id);
        return usuarioRepository.save(usuario);
    }

    // Eliminar usuario
    @DeleteMapping("/{id}")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
    }
}

```

### Clase Principal de la Aplicación
La clase principal inicia la aplicación Spring Boot.

```java
package com.example.crudusuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrudUsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudUsuariosApplication.class, args);
    }
}

```

## Instrucciones para Ejecutar
Asegúrate de tener PostgreSQL corriendo en localhost:5433 con una base de datos llamada sycophancy_db.
Configura las credenciales en application.properties.
Ejecuta mvn spring-boot:run en la raíz del proyecto.
La aplicación estará disponible en http://localhost:

8080.

## Endpoints disponibles:

```text
POST /usuarios - Crear usuario (envía JSON con nombre, email, contraseña).
GET /usuarios - Obtener todos los usuarios.
GET /usuarios/{id} - Obtener usuario por ID.
PUT /usuarios/{id} - Actualizar usuario (envía JSON con campos actualizados).
DELETE /usuarios/{id} - Eliminar usuario por ID.
```

Esta implementación es funcional y simplificada, cumpliendo con los requisitos básicos. No incluye validaciones, manejo de errores avanzado ni capas adicionales para mantener la simplicidad. Si necesitas expansiones, puedes agregarlas posteriormente.
