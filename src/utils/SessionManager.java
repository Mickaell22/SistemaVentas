package utils;

import models.Usuario;
import java.time.LocalDateTime;
import java.time.Duration;
import java.net.InetAddress;

public class SessionManager {
    
    private static SessionManager instance;
    private Usuario currentUser;
    private LocalDateTime sessionStartTime;
    private LocalDateTime lastActivity;
    private String ipAddress;
    private String userAgent;
    private boolean requiresCambioPassword;
    
    // Configuración de sesión
    private static final int SESSION_TIMEOUT_MINUTES = 120; // 2 horas
    private static final int WARNING_BEFORE_EXPIRY_MINUTES = 10; // Aviso 10 min antes
    private static final int MAX_IDLE_MINUTES = 30; // Inactividad máxima
    
    private SessionManager() {
        initializeSessionInfo();
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Inicializa información de la sesión
     */
    private void initializeSessionInfo() {
        try {
            this.ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            this.ipAddress = "127.0.0.1";
        }
        
        this.userAgent = System.getProperty("java.version", "Unknown");
    }
    
    /**
     * Establece el usuario actual
     */
    public void setCurrentUser(Usuario user) {
        this.currentUser = user;
        this.sessionStartTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.requiresCambioPassword = false;
    }
    
    /**
     * Obtiene el usuario actual
     */
    public Usuario getCurrentUser() {
        if (isSessionExpired()) {
            clearSession();
            return null;
        }
        return currentUser;
    }
    
    /**
     * Limpia la sesión
     */
    public void clearSession() {
        this.currentUser = null;
        this.sessionStartTime = null;
        this.lastActivity = null;
        this.requiresCambioPassword = false;
    }
    
    /**
     * Verifica si la sesión ha expirado
     */
    public boolean isSessionExpired() {
        if (sessionStartTime == null || lastActivity == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // Verificar timeout total de sesión
        Duration totalDuration = Duration.between(sessionStartTime, now);
        if (totalDuration.toMinutes() > SESSION_TIMEOUT_MINUTES) {
            return true;
        }
        
        // Verificar inactividad
        Duration idleDuration = Duration.between(lastActivity, now);
        if (idleDuration.toMinutes() > MAX_IDLE_MINUTES) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica si la sesión está cerca de expirar
     */
    public boolean isSessionNearExpiry() {
        if (sessionStartTime == null) {
            return false;
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            Duration totalDuration = Duration.between(sessionStartTime, now);
            long minutesRemaining = SESSION_TIMEOUT_MINUTES - totalDuration.toMinutes();
            
            return minutesRemaining <= WARNING_BEFORE_EXPIRY_MINUTES && minutesRemaining > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extiende la sesión actual
     */
    public void extendSession() {
        if (currentUser != null) {
            this.sessionStartTime = LocalDateTime.now();
            this.lastActivity = LocalDateTime.now();
        }
    }
    
    /**
     * Actualiza la última actividad
     */
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    /**
     * Obtiene información de la sesión
     */
    public String getSessionInfo() {
        if (currentUser == null) {
            return "Sin sesión activa";
        }
        
        try {
            Duration sessionDuration = Duration.between(sessionStartTime, LocalDateTime.now());
            long hours = sessionDuration.toHours();
            long minutes = sessionDuration.toMinutes() % 60;
            
            return String.format("Usuario: %s | Duración: %dh %dm | IP: %s",
                currentUser.getNombreCompleto(),
                hours, minutes,
                ipAddress
            );
        } catch (Exception e) {
            return "Usuario: " + currentUser.getNombreCompleto() + " | Sesión activa";
        }
    }
    
    /**
     * Obtiene el tiempo restante de sesión
     */
    public String getTimeRemaining() {
        if (sessionStartTime == null) {
            return "Sin sesión";
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            Duration totalDuration = Duration.between(sessionStartTime, now);
            long minutesRemaining = SESSION_TIMEOUT_MINUTES - totalDuration.toMinutes();
            
            if (minutesRemaining <= 0) {
                return "Sesión expirada";
            }
            
            long hours = minutesRemaining / 60;
            long minutes = minutesRemaining % 60;
            
            return String.format("%dh %dm restantes", hours, minutes);
        } catch (Exception e) {
            return "Tiempo no disponible";
        }
    }
    
    /**
     * Obtiene estadísticas de la sesión
     */
    public String getSessionStats() {
        if (currentUser == null) {
            return "Sin estadísticas de sesión";
        }
        
        try {
            return String.format(
                "Sesión iniciada: %s\n" +
                "Última actividad: %s\n" +
                "Duración: %s\n" +
                "Estado: %s\n" +
                "IP: %s\n" +
                "Rol: %s",
                sessionStartTime != null ? sessionStartTime.toString() : "Desconocido",
                lastActivity != null ? lastActivity.toString() : "Desconocido",
                getTimeRemaining(),
                isSessionExpired() ? "Expirada" : "Activa",
                ipAddress,
                currentUser.getRolNombre()
            );
        } catch (Exception e) {
            return "Error al obtener estadísticas";
        }
    }
    
    // Getters y Setters
    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }
    
    public void setSessionStartTime(LocalDateTime sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public boolean isRequiresCambioPassword() {
        return requiresCambioPassword;
    }
    
    public void setRequieresCambioPassword(boolean requiresCambioPassword) {
        this.requiresCambioPassword = requiresCambioPassword;
    }
    
    // Configuración
    public static int getSessionTimeoutMinutes() {
        return SESSION_TIMEOUT_MINUTES;
    }
    
    public static int getMaxIdleMinutes() {
        return MAX_IDLE_MINUTES;
    }
    
    public static int getWarningBeforeExpiryMinutes() {
        return WARNING_BEFORE_EXPIRY_MINUTES;
    }
}