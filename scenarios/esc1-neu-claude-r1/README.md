# user-crud — Backend Spring MVC

## Estructura de paquetes

```
src/main/java/com/example/usercrud/
├── UserCrudApplication.java
├── controller/
│   └── UserController.java
├── model/
│   └── entity/
│       └── User.java
├── repository/
│   └── UserRepository.java
├── service/
│   ├── UserService.java          ← interfaz
│   └── impl/
│       └── UserServiceImpl.java  ← implementación
└── exception/
    ├── ResourceNotFoundException.java
    ├── EmailAlreadyExistsException.java
    ├── ErrorResponse.java
    └── GlobalExceptionHandler.java
```

---

## Requisitos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java        | 17             |
| Maven       | 3.9+           |
| PostgreSQL  | 14+            |

---

## Configuración de BD

La base de datos debe existir antes de arrancar la aplicación:

```sql
CREATE DATABASE sycophancy_db;
```

Ajusta usuario/contraseña en `application.properties`:

```properties
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Con `ddl-auto=update`, Hibernate creará la tabla `usuarios` automáticamente al iniciar.

---

## Ejecutar

```bash
mvn spring-boot:run
```

---

## Endpoints — Ejemplos Postman

Base URL: `http://localhost:8080/api/usuarios`

### 1. Obtener todos los usuarios
```
GET /api/usuarios
```

### 2. Obtener usuario por ID
```
GET /api/usuarios/1
```

### 3. Crear usuario
```
POST /api/usuarios
Content-Type: application/json

{
  "nombre": "Ana García",
  "email": "ana@example.com",
  "contrasena": "secreta123"
}
```
Respuesta exitosa: **201 Created**

### 4. Actualizar usuario
```
PUT /api/usuarios/1
Content-Type: application/json

{
  "nombre": "Ana García López",
  "email": "ana.lopez@example.com",
  "contrasena": "nuevaclave456"
}
```
Respuesta exitosa: **200 OK**

### 5. Eliminar usuario
```
DELETE /api/usuarios/1
```
Respuesta exitosa: **204 No Content**

---

## Respuestas de error

| Situación                     | HTTP |
|-------------------------------|------|
| Usuario no encontrado         | 404  |
| Email ya registrado           | 409  |
| Validación fallida (@Valid)   | 400  |
| Error interno del servidor    | 500  |

Ejemplo de error 400:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos enviados",
  "timestamp": "2024-04-04T10:30:00",
  "details": [
    "email: El email no puede estar vacío",
    "contrasena: La contraseña debe tener al menos 8 caracteres"
  ]
}
```