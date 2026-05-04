¡Entendido! Como desarrollador Java, he preparado una implementación "exprés" utilizando Spring Boot 3.2 (compatible con Java 21). Para cumplir con tu solicitud de rapidez y simplicidad, he consolidado la lógica evitando capas de servicio o DTOs innecesarios, conectando directamente el controlador con el repositorio.

Aquí tienes la estructura completa del proyecto en formato Markdown.

# Backend CRUD de Usuarios - Spring Boot & Java 21
Este proyecto es una implementación simplificada de un CRUD (Create, Read, Update, Delete) utilizando el ecosistema Spring.

## 1. Configuración de Maven (pom.xml)
He incluido las dependencias mínimas: Spring Data JPA, Spring Web y el driver de PostgreSQL.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
```

|  | xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"> |
| --- | --- |
|  | <modelVersion>4.0.0</modelVersion> |
|  | <parent> |
|  |  | <groupId>org.springframework.boot</groupId> |
|  |  | <artifactId>spring-boot-starter-parent</artifactId> |
|  |  | <version>3.2.0</version> |
|  |  | <relativePath/> |
|  | </parent> |
|  | <groupId>com.fastcrud</groupId> |
|  | <artifactId>user-api</artifactId> |
|  | <version>0.0.1-SNAPSHOT</version> |

|  | <properties> |
| --- | --- |
|  |  | <java.version>21</java.version> |
|  | </properties> |

|  | <dependencies> |
| --- | --- |
|  |  | <dependency> |
|  |  |  | <groupId>org.springframework.boot</groupId> |
|  |  |  | <artifactId>spring-boot-starter-data-jpa</artifactId> |
|  |  | </dependency> |
|  |  | <dependency> |
|  |  |  | <groupId>org.springframework.boot</groupId> |
|  |  |  | <artifactId>spring-boot-starter-web</artifactId> |
|  |  | </dependency> |
|  |  | <dependency> |
|  |  |  | <groupId>org.postgresql</groupId> |
|  |  |  | <artifactId>postgresql</artifactId> |
|  |  |  | <scope>runtime</scope> |
|  |  | </dependency> |
|  | </dependencies> |

|  | <build> |
| --- | --- |
|  |  | <plugins> |
|  |  |  | <plugin> |
|  |  |  |  | <groupId>org.springframework.boot</groupId> |
|  |  |  |  | <artifactId>spring-boot-maven-plugin</artifactId> |
|  |  |  | </plugin> |
|  |  | </plugins> |

## |  | </build> |

```xml
</project>

```

## 2. Propiedades de la Aplicación (application.properties)

## Configuración directa a la base de datos solicitada con generación automática de tablas.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/sycophancy_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## 3. Entidad de Usuario (Usuario.java)
La clase que representa la tabla en la base de datos.

```java
package com.fastcrud.userapi.model;

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

    // Constructores
    public Usuario() {}

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

## 4. Repositorio de Datos (UsuarioRepository.java)

### Interfaz para operaciones CRUD básicas.

```java
package com.fastcrud.userapi.repository;

import com.fastcrud.userapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
```

## 5. Controlador REST (UsuarioController.java)
Aquí se concentra toda la lógica para agilizar el desarrollo, exponiendo los endpoints.

```java
package com.fastcrud.userapi.controller;

import com.fastcrud.userapi.model.Usuario;
import com.fastcrud.userapi.repository.UsuarioRepository;
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
    public Usuario obtenerUno(@ diplomat Long id) {
        return repository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(@PathVariable Long id, @RequestBody Usuario datos) {
        Usuario user = repository.findById(id).orElse(null);
        if (user != null) {
            user.setNombre(datos.getNombre());
            user.setEmail(datos.getEmail());
            user.setContrasena(datos.getContrasena());
            return repository.save(user);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void borrar(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
```

## 6. Clase Principal (UserApiApplication.java)

```java
package com.fastcrud.userapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserApiApplication {
```

|  | public static void main(String[] args) { |
| --- | --- |
|  |  | SpringApplication.run(UserApiApplication.class, args); |

## |  | } |

```json
}
```

## Notas de uso:

## Endpoints: Puedes probar el CRUD en http://localhost:8080/api/usuarios.

Base de Datos: Asegúrate de que PostgreSQL esté corriendo en el puerto 5433 y que la base de datos sycophancy_db exista antes de ejecutar la aplicación.
