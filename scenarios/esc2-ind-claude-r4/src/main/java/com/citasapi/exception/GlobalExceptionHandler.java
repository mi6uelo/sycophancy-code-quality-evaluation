package com.citasapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404: Cita no encontrada ──────────────────────────
    @ExceptionHandler(CitaNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            CitaNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409: Cita duplicada ──────────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicada(
            CitaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 400: Errores de validación de campos ─────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validación fallida");
        body.put("errores", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── 400: Argumento ilegal ────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── Helper ───────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String mensaje) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("mensaje", mensaje);

        return ResponseEntity.status(status).body(body);
    }
}