package models;

import java.time.LocalDateTime;

public class Usuario {
    
    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private String username;
    private String password;
    private String telefono;
    private String direccion;
    private int rolId;
    private String rolNombre;
    private boolean activo;
    private LocalDateTime ultimoLogin;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor vacío
    public Usuario() {}
    
    // Constructor para crear nuevo usuario
    public Usuario(String nombre, String apellido, String email, String username, 
                   String password, String telefono, String direccion, int rolId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.username = username;
        this.password = password;
        this.telefono = telefono;
        this.direccion = direccion;
        this.rolId = rolId;
        this.activo = true;
    }
    
    // Constructor completo
    public Usuario(int id, String nombre, String apellido, String email, String username,
                   String password, String telefono, String direccion, int rolId, 
                   String rolNombre, boolean activo, LocalDateTime ultimoLogin,
                   LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.username = username;
        this.password = password;
        this.telefono = telefono;
        this.direccion = direccion;
        this.rolId = rolId;
        this.rolNombre = rolNombre;
        this.activo = activo;
        this.ultimoLogin = ultimoLogin;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }
    
    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    // Métodos de utilidad
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public boolean esAdministrador() {
        return "ADMINISTRADOR".equals(rolNombre);
    }
    
    public boolean esGerente() {
        return "GERENTE".equals(rolNombre);
    }
    
    public boolean esVendedor() {
        return "VENDEDOR".equals(rolNombre);
    }
    
    public boolean esCajero() {
        return "CAJERO".equals(rolNombre);
    }
    
    @Override
    public String toString() {
        return String.format("Usuario{id=%d, username='%s', nombre='%s', rol='%s', activo=%s}", 
                           id, username, getNombreCompleto(), rolNombre, activo);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return id == usuario.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}