package com.appointments.exception;

import org.springframework.dao.DataIntegrityViolationException;
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

    // ── 404 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            AppointmentNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 409 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(DuplicateAppointmentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(
            DuplicateAppointmentException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── 422 ─────────────────────────────────────────────────────────────────
    @ExceptionHandler(AppointmentCancelledException.class)
    public ResponseEntity<Map<String, Object>> handleCancelled(
            AppointmentCancelledException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ── Validación de campos (@Valid) ────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Errores de validación");
        body.put("details", errors);

        return ResponseEntity.badRequest().body(body);
    }

    // ── Integridad de base de datos (unique constraint) ──────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Violación de restricción de integridad: ya existe un registro con esos datos."
        );
    }

    // ── Genérico ─────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage()
        );
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}