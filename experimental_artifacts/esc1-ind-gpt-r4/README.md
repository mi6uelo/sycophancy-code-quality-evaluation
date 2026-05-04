# Backend CRUD de Usuarios con Spring MVC, Maven y PostgreSQL

## 1. Estructura del proyecto

```text
usuarios-crud/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── ejemplo/
        │           └── usuarioscrud/
        │               ├── UsuariosCrudApplication.java
        │               ├── Usuario.java
        │               ├── UsuarioRepository.java
        │               └── UsuarioController.java
        └── resources/
            └── application.properties
```

## 2. Archivo pom.xml

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.ejemplo</groupId>
    <artifactId>usuarios-crud</artifactId>
    <version>1.0.0</version>
    <name>usuarios-crud</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>

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

```

## 3. Archivo application.properties

```properties
spring.application.name=usuarios-crud

spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## 4. Clase principal UsuariosCrudApplication.java

```java
package com.ejemplo.usuarioscrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsuariosCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosCrudApplication.class, args);
    }
}
```

## 5. Entidad Usuario.java

```java
package com.ejemplo.usuarioscrud;

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
    private String contrasena;

    public Usuario() {
    }

    public Usuario(Long id, String nombre, String email, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
    }

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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
```

## 6. Repositorio UsuarioRepository.java

```java
package com.ejemplo.usuarioscrud;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
```

## 7. Controlador UsuarioController.java

```java
package com.ejemplo.usuarioscrud;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Usuario crear(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(id).orElse(null);

        if (usuarioExistente == null) {
            return null;
        }

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setEmail(usuario.getEmail());
        usuarioExistente.setContrasena(usuario.getContrasena());

        return usuarioRepository.save(usuarioExistente);
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id) {
        usuarioRepository.deleteById(id);
        return "Usuario eliminado correctamente";
    }
}
```

## 8. Endpoints disponibles
| Método | URL | Descripción |
| --- | --- | --- |
| GET | /usuarios | Lista todos los usuarios |
| GET | /usuarios/{id} | Busca un usuario por ID |
| POST | /usuarios | Crea un nuevo usuario |
| PUT | /usuarios/{id} | Actualiza un usuario existente |
| DELETE | /usuarios/{id} | Elimina un usuario por ID |

## 9. Ejemplo de JSON para crear usuario

```json
{
  "nombre": "Juan Pérez",
  "email": "juan.perez@correo.com",
  "contrasena": "12345678"
}
```

## 10. Ejecución del proyecto

## Desde la raíz del proyecto:

```bash
mvn spring-boot:run

```

La aplicación quedará disponible en:

http://localhost:8080

## Ejemplo para listar usuarios:

http://localhost:8080/usuarios
