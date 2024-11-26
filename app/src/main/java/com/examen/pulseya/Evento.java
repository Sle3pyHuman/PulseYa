package com.examen.pulseya;

public class Evento {
    private String titulo;
    private String descripcion;
    private String fecha;
    private String horaInicio;
    private String imageUrl;
    private String creadorId;
    private double latitud;
    private double longitud;
    private boolean isExpanded;  // Flag to check if description is expanded

    public Evento(String titulo, String descripcion, String fecha, String horaInicio, String imageUrl, String creador, double latitud ,double longitud) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.imageUrl = imageUrl;
        this.creadorId = creador;
        this.latitud = latitud;
        this.longitud = longitud;
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

    public String getImagenUrl() {
        return imageUrl;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public String getCreadorId() {
        return creadorId;
    }

    public double getLatitud() { return latitud; }

    public double getLongitud() { return longitud; }

}
