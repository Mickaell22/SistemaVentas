package models;

import java.time.LocalDateTime;

public class Cliente {
    
    private int id;
    private String tipoDocumento;  // CEDULA, RUC, PASAPORTE
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructores
    
    /**
     * Constructor vacío
     */
    public Cliente() {
        this.activo = true;
        this.tipoDocumento = "CEDULA";
    }
    
    /**
     * Constructor para crear nuevo cliente
     */
    public Cliente(String tipoDocumento, String numeroDocumento, String nombre, 
                   String apellido, String email, String telefono, String direccion) {
        this();
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }
    
    /**
     * Constructor completo
     */
    public Cliente(int id, String tipoDocumento, String numeroDocumento, String nombre,
                   String apellido, String email, String telefono, String direccion,
                   boolean activo, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
    
    // Getters y Setters
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    // Métodos de utilidad
    
    /**
     * Obtiene el nombre completo del cliente
     */
    public String getNombreCompleto() {
        if (apellido != null && !apellido.trim().isEmpty()) {
            return nombre + " " + apellido;
        }
        return nombre;
    }
    
    /**
     * Verifica si es persona natural (cédula)
     */
    public boolean esPersonaNatural() {
        return "CEDULA".equals(tipoDocumento);
    }
    
    /**
     * Verifica si es persona jurídica (RUC)
     */
    public boolean esPersonaJuridica() {
        return "RUC".equals(tipoDocumento);
    }
    
    /**
     * Verifica si es extranjero (pasaporte)
     */
    public boolean esExtranjero() {
        return "PASAPORTE".equals(tipoDocumento);
    }
    
    /**
     * Obtiene el tipo de documento formateado
     */
    public String getTipoDocumentoFormateado() {
        switch (tipoDocumento) {
            case "CEDULA": return "Cédula";
            case "RUC": return "RUC";
            case "PASAPORTE": return "Pasaporte";
            default: return tipoDocumento;
        }
    }
    
    /**
     * Obtiene una representación para mostrar en listas
     */
    public String getDisplayText() {
        return String.format("%s - %s (%s)", 
                           numeroDocumento, 
                           getNombreCompleto(), 
                           getTipoDocumentoFormateado());
    }
    
    /**
     * Valida si los datos básicos están completos
     */
    public boolean datosBasicosCompletos() {
        return numeroDocumento != null && !numeroDocumento.trim().isEmpty() &&
               nombre != null && !nombre.trim().isEmpty();
    }
    
    /**
     * Obtiene información del estado del cliente
     */
    public String getEstadoTexto() {
        return activo ? "Activo" : "Inactivo";
    }
    
    @Override
    public String toString() {
        return getDisplayText();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cliente cliente = (Cliente) obj;
        return id == cliente.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}