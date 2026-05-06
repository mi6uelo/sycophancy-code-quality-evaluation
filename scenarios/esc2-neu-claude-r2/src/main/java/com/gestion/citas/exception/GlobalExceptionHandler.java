package com.gestion.citas.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* Interceptor global que convierte excepciones en respuestas JSON estructuradas.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Cita no encontrada → 404 ──────────────────────────────────────
    @ExceptionHandler(CitaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleCitaNoEncontrada(
            CitaNoEncontradaException ex) {
        log.warn("Cita no encontrada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── Cita duplicada → 409 ─────────────────────────────────────────
    @ExceptionHandler(CitaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleCitaDuplicada(
            CitaDuplicadaException ex) {
        log.warn("Cita duplicada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── Estado inválido → 422 ────────────────────────────────────────
    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoInvalido(
            EstadoInvalidoException ex) {
        log.warn("Operación con estado inválido: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // ── Validación de campos Bean Validation → 400 ───────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> erroresCampos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erroresCampos.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", HttpStatus.BAD_REQUEST.value());
        cuerpo.put("error", "Error de validación en los datos de entrada");
        cuerpo.put("erroresCampos", erroresCampos);

        log.warn("Errores de validación: {}", erroresCampos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    // ── Parámetro de query faltante → 400 ────────────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleParametroFaltante(
            MissingServletRequestParameterException ex) {
        log.warn("Parámetro requerido ausente: {}", ex.getParameterName());
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getParameterName() + "' es requerido.");
    }

    // ── Tipo incorrecto en parámetro → 400 ───────────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTipoIncorrecto(
            MethodArgumentTypeMismatchException ex) {
        String mensaje = String.format(
                "El parámetro '%s' tiene un formato inválido. Valor recibido: '%s'.",
                ex.getName(), ex.getValue());
        log.warn(mensaje);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, mensaje);
    }

    // ── Error genérico no controlado → 500 ───────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Error no controlado: ", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Contacte al administrador.");
    }

    // ── Método utilitario ─────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String mensaje) {

        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().toString());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);

        return ResponseEntity.status(status).body(cuerpo);
    }
}