package models;

import java.time.LocalDateTime;

public class Rol {
    
    private int id;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    
    // Constructor vacío
    public Rol() {}
    
    // Constructor para crear nuevo rol
    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = true;
    }
    
    // Constructor completo
    public Rol(int id, String nombre, String descripcion, boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    @Override
    public String toString() {
        return String.format("Rol{id=%d, nombre='%s', descripcion='%s', activo=%s}", 
                           id, nombre, descripcion, activo);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rol rol = (Rol) obj;
        return id == rol.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}