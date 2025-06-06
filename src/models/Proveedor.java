package models;

import java.time.LocalDateTime;

public class Proveedor {
    
    private int id;
    private String nombre;
    private String contacto;
    private String telefono;
    private String email;
    private String direccion;
    private String ruc;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor vacío
    public Proveedor() {}
    
    // Constructor para crear nuevo proveedor
    public Proveedor(String nombre, String contacto, String telefono, String email, String direccion, String ruc) {
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.ruc = ruc;
        this.activo = true;
    }
    
    // Constructor con ID (para casos especiales como combobox)
    public Proveedor(int id, String nombre, String contacto, String telefono, String email, String direccion, String ruc) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.ruc = ruc;
        this.activo = true;
    }
    
    // Constructor completo
    public Proveedor(int id, String nombre, String contacto, String telefono, String email, 
                    String direccion, String ruc, boolean activo, LocalDateTime fechaCreacion, 
                    LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.ruc = ruc;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    @Override
    public String toString() {
        return nombre; // Para mostrar en ComboBox
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Proveedor proveedor = (Proveedor) obj;
        return id == proveedor.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}