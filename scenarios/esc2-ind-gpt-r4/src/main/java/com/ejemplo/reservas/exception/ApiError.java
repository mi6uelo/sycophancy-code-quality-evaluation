package com.ejemplo.reservas.exception;

import java.time.LocalDateTime;

public class ApiError {

    private LocalDateTime fechaHora;
    private int estado;
    private String error;
    private String mensaje;
    private String ruta;

    public ApiError() {
    }

    public ApiError(LocalDateTime fechaHora, int estado, String error, String mensaje, String ruta) {
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getEstado() {
        return estado;
    }

    public String getError() {
        return error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}