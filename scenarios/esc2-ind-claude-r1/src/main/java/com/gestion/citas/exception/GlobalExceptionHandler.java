package com.gestion.citas.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* Intercepta excepciones lanzadas desde cualquier controlador
* y las convierte en respuestas JSON estructuradas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404: cita no encontrada ───────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(CitaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409: horario ya ocupado ───────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicada(CitaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400: fallos de validación Bean Validation ─────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult()
          .getFieldErrors()
          .forEach(fe -> errores.put(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status",    HttpStatus.BAD_REQUEST.value());
        body.put("error",     "Validación fallida");
        body.put("detalles",  errores);

        return ResponseEntity.badRequest().body(body);
    }

    // ── 400: estado inválido u otras reglas de negocio ────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 500: cualquier otra excepción no controlada ───────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ─── Helper ──────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("mensaje",   mensaje);
        return ResponseEntity.status(status).body(body);
    }
}