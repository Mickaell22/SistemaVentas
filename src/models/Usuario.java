package models;

import java.time.LocalDateTime;
import java.util.Objects;

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
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoLogin;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;
    private LocalDateTime passwordExpira;
    private int creadoPor;
    private LocalDateTime fechaModificacion;

    // Constructores
    public Usuario() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.intentosFallidos = 0;
    }

    public Usuario(String nombre, String apellido, String email, String username, 
                   String password, String telefono, String direccion, int rolId) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.username = username;
        this.password = password;
        this.telefono = telefono;
        this.direccion = direccion;
        this.rolId = rolId;
    }

    // Métodos de utilidad
    public String getNombreCompleto() {
        if (apellido != null && !apellido.trim().isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }

    public boolean isPasswordExpired() {
        return passwordExpira != null && LocalDateTime.now().isAfter(passwordExpira);
    }

    public boolean isBlocked() {
        return bloqueadoHasta != null && LocalDateTime.now().isBefore(bloqueadoHasta);
    }

    public boolean needsPasswordChange() {
        return isPasswordExpired() || (passwordExpira != null && 
               passwordExpira.minusDays(7).isBefore(LocalDateTime.now()));
    }

    public String getSecurityStatus() {
        if (isBlocked()) return "BLOQUEADO";
        if (isPasswordExpired()) return "CONTRASEÑA_EXPIRADA";
        if (needsPasswordChange()) return "CONTRASEÑA_POR_EXPIRAR";
        if (!activo) return "INACTIVO";
        return "ACTIVO";
    }

    public String getDisplayText() {
        return String.format("%s (%s) - %s", getNombreCompleto(), username, 
                           rolNombre != null ? rolNombre : "Sin rol");
    }

    public boolean isValidForCreation() {
        return nombre != null && !nombre.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               rolId > 0;
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

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(LocalDateTime ultimoLogin) { this.ultimoLogin = ultimoLogin; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }

    public LocalDateTime getPasswordExpira() { return passwordExpira; }
    public void setPasswordExpira(LocalDateTime passwordExpira) { this.passwordExpira = passwordExpira; }

    public int getCreadoPor() { return creadoPor; }
    public void setCreadoPor(int creadoPor) { this.creadoPor = creadoPor; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    @Override
    public String toString() {
        return getDisplayText();
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
        return Objects.hash(id);
    }
}