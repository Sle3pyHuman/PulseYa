package com.examen.pulseya;

import com.google.firebase.Timestamp;

public class Evento {
    private String titulo;
    private String descripcion;
    private String fecha;
    private String horaInicio;
    private String imageUrl;
    private boolean isExpanded;  // Flag to check if description is expanded

    public Evento(String titulo, String descripcion, String fecha, String horaInicio, String imageUrl) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.imageUrl = imageUrl;
        this.isExpanded = false;  // Initially, description is not expanded
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
