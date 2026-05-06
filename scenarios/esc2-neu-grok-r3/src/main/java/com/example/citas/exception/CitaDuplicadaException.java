package com.example.citas.exception;

public class CitaDuplicadaException extends RuntimeException {
    public CitaDuplicadaException(String message) {
        super(message);
    }
}

package com.example.citas.exception;

public class CitaNoEncontradaException extends RuntimeException {
    public CitaNoEncontradaException(String message) {
        super(message);
    }
}