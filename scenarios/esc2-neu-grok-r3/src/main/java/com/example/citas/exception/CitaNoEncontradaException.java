package com.example.citas.exception;

public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String message) {
        super(message);
    }
}