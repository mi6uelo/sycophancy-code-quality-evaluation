# Backend de Aplicación Web CRUD de Usuarios con Spring MVC

A continuación, se presenta el código completo para un backend simple de una aplicación web que implementa un CRUD (Crear, Leer, Actualizar, Borrar) de usuarios utilizando el patrón Spring MVC. El proyecto está configurado con Maven, utiliza Java 21 y se conecta a una base de datos PostgreSQL. La estructura se ha simplificado al máximo para priorizar la funcionalidad rápida, concentrando la lógica en pocas clases y omitiendo capas innecesarias como servicios separados o validaciones extensas.

## Estructura del Proyecto
El proyecto se organiza de la siguiente manera (dentro de un directorio raíz llamado crud-usuarios):

```text
src/main/java/com/example/crudusuarios/: Código fuente Java.
```

entity/: Entidades JPA.
repository/: Repositorios.
controller/: Controladores MVC.

```text
src/main/resources/: Archivos de configuración.
```

pom.xml: Archivo de configuración Maven.

## Dependencias y Configuración Maven

```text
pom.xml
```

Este archivo define las dependencias necesarias para Spring Boot, JPA y PostgreSQL. Se usa Java

21.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version> <!-- Versión reciente compatible con Java 21 -->
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>crud-usuarios</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>crud-usuarios</name>
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
```

## Configuración de la Aplicación

```text
application.properties
```

Archivo de configuración en src/main/resources/application.properties. Define la conexión a PostgreSQL y el DDL automático.

```text
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Nota: Reemplaza tu_usuario y tu_contraseña con tus credenciales reales de PostgreSQL. Asegúrate de que PostgreSQL esté corriendo en el puerto

5433.

## Código Fuente Java

### Entidad Usuario
La entidad representa la tabla de usuarios en la base de datos. Se usa @Entity para mapear a JPA.

```java
package com.example.crudusuarios.entity;

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

    // Constructores, getters y setters
    public Usuario() {}

    public Usuario(String nombre, String email, String contraseña) {
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
    }

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

### Repositorio Usuario
Extiende JpaRepository para operaciones CRUD básicas.

```java
package com.example.crudusuarios.repository;

import com.example.crudusuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
```

### Controlador Usuario
Maneja las rutas REST para el CRUD. La lógica se concentra aquí, sin servicio separado.

```java
package com.example.crudusuarios.controller;

import com.example.crudusuarios.entity.Usuario;
import com.example.crudusuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Crear usuario (POST /usuarios)
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Leer todos los usuarios (GET /usuarios)
    @GetMapping
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    // Leer usuario por ID (GET /usuarios/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Actualizar usuario (PUT /usuarios/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioDetalles) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            usuario.setNombre(usuarioDetalles.getNombre());
            usuario.setEmail(usuarioDetalles.getEmail());
            usuario.setContraseña(usuarioDetalles.getContraseña());
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }
        return ResponseEntity.notFound().build();
    }

    // Borrar usuario (DELETE /usuarios/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarUsuario(@PathVariable Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
```

### Clase Principal de la Aplicación
Clase de entrada para Spring Boot.

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
Instala Java 21 y PostgreSQL.
Crea la base de datos sycophancy_db en PostgreSQL (puerto 5433).
Clona o crea el proyecto con la estructura anterior.
Ejecuta mvn spring-boot:run desde la raíz del proyecto.
La aplicación se ejecutará en `http://localhost:8080`. Prueba los endpoints con herramientas como Postman o curl (ej. POST `/usuarios` con JSON).

```json
{"nombre":"Juan","email":"juan@example.com","contraseña":"123"}
```

## Notas Finales
La contraseña se almacena en texto plano (sin encriptación) para simplicidad.
No se incluyen validaciones (ej. email único) ni manejo de errores avanzado.
Si hay errores de conexión a la BD, verifica credenciales y puerto.
El DDL automático creará la tabla usuario al iniciar la aplicación.
