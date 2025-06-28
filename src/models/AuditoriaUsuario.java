package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditoriaUsuario {
    private int id;
    private int usuarioId;
    private String usuarioNombre;
    private String accion;
    private String descripcion;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime fechaHora;
    private String moduloAfectado;

    // Constructores
    public AuditoriaUsuario() {
        this.fechaHora = LocalDateTime.now();
    }

    public AuditoriaUsuario(int usuarioId, String accion, String descripcion, 
                           String ipAddress, String moduloAfectado) {
        this();
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.descripcion = descripcion;
        this.ipAddress = ipAddress;
        this.moduloAfectado = moduloAfectado;
    }

    // Métodos de utilidad
    public String getActionDescription() {
        return String.format("[%s] %s - %s", accion, descripcion, moduloAfectado);
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fechaHora.format(formatter);
    }

    public String getModuleInfo() {
        return moduloAfectado != null ? moduloAfectado : "SISTEMA";
    }

    public String getDisplayText() {
        return String.format("%s - %s (%s)", getFormattedDate(), accion, usuarioNombre);
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getModuloAfectado() { return moduloAfectado; }
    public void setModuloAfectado(String moduloAfectado) { this.moduloAfectado = moduloAfectado; }

    @Override
    public String toString() {
        return getDisplayText();
    }
}