package com.reservas.citas.exception;

import com.reservas.citas.dto.ApiErrorDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
* Captura todas las excepciones de la API y devuelve respuestas HTTP uniformes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* ── 404 Not Found ────────────────────────────────────────────────────── */

    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<ApiErrorDTO> handleNotFound(CitaNoEncontradaException ex) {
        log.warn("Cita no encontrada: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(ex.getMessage()));
    }

    /* ── 409 Conflict ─────────────────────────────────────────────────────── */

    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<ApiErrorDTO> handleDuplicate(CitaDuplicadaException ex) {
        log.warn("Cita duplicada: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflicto de horario", List.of(ex.getMessage()));
    }

    /* ── 422 Unprocessable Entity ─────────────────────────────────────────── */

    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<ApiErrorDTO> handleEstado(EstadoInvalidoException ex) {
        log.warn("Operación inválida: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Operación no permitida", List.of(ex.getMessage()));
    }

    /* ── 400 Bean Validation (@RequestBody) ───────────────────────────────── */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errores = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .toList();
        log.warn("Validación fallida: {}", errores);
        return build(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos", errores);
    }

    /* ── 400 Constraint Violation (path/query params) ─────────────────────── */

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleConstraint(ConstraintViolationException ex) {
        List<String> errores = ex.getConstraintViolations()
            .stream()
            .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
            .toList();
        return build(HttpStatus.BAD_REQUEST, "Parámetro inválido", errores);
    }

    /* ── 409 Integridad de BD (unique constraint a nivel SQL) ─────────────── */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorDTO> handleIntegrity(DataIntegrityViolationException ex) {
        log.error("Violación de integridad en BD: {}", ex.getMostSpecificCause().getMessage());
        return build(
            HttpStatus.CONFLICT,
            "Violación de integridad de datos",
            List.of("Ya existe un registro con los mismos datos únicos (fecha/hora).")
        );
    }

    /* ── 500 Fallback ─────────────────────────────────────────────────────── */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> handleGeneric(Exception ex) {
        log.error("Error inesperado: ", ex);
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor",
            List.of("Ocurrió un error inesperado. Contacta al administrador.")
        );
    }

    /* ── Builder ──────────────────────────────────────────────────────────── */

    private ResponseEntity<ApiErrorDTO> build(HttpStatus status, String error, List<String> mensajes) {
        return ResponseEntity
            .status(status)
            .body(new ApiErrorDTO(status.value(), error, mensajes, LocalDateTime.now()));
    }
}
