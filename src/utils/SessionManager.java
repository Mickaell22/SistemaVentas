package utils;

import models.Usuario;
import java.time.LocalDateTime;

public class SessionManager {
    
    private static SessionManager instance;
    private Usuario currentUser;
    private LocalDateTime loginTime;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Establece el usuario actual en la sesión
     */
    public void setCurrentUser(Usuario user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
    }
    
    /**
     * Obtiene el usuario actual
     */
    public Usuario getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Obtiene la hora de login
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public boolean isSessionActive() {
        return currentUser != null;
    }
    
    /**
     * Limpia la sesión actual
     */
    public void clearSession() {
        this.currentUser = null;
        this.loginTime = null;
    }
    
    /**
     * Obtiene información de la sesión actual
     */
    public String getSessionInfo() {
        if (currentUser != null) {
            return String.format("Usuario: %s | Rol: %s | Login: %s", 
                               currentUser.getNombreCompleto(),
                               currentUser.getRolNombre(),
                               loginTime != null ? loginTime.toString() : "N/A");
        }
        return "Sin sesión activa";
    }
}