package services;

import dao.impl.UsuarioDAOImpl;
import dao.interfaces.IUsuarioDAO;
import models.Usuario;
import models.AuditoriaUsuario;
import utils.SessionManager;
import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {
    
    private IUsuarioDAO usuarioDAO;
    private static AuthService instance;
    private final int MAX_INTENTOS_FALLIDOS = 5;
    private final int MINUTOS_BLOQUEO = 30;
    
    private AuthService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Autentica un usuario y establece la sesión
     */
    public boolean login(String username, String password) {
        try {
            // Validar entrada
            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                registrarIntentoLogin(username, false, "Credenciales vacías");
                return false;
            }
            
            // Obtener usuario
            Optional<Usuario> usuarioOpt = usuarioDAO.obtenerPorUsername(username.trim());
            
            if (!usuarioOpt.isPresent()) {
                registrarIntentoLogin(username, false, "Usuario no encontrado");
                return false;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar si está bloqueado
            if (usuario.isBlocked()) {
                registrarIntentoLogin(username, false, "Usuario bloqueado");
                return false;
            }
            
            // Verificar si está activo
            if (!usuario.isActivo()) {
                registrarIntentoLogin(username, false, "Usuario inactivo");
                return false;
            }
            
            // Verificar credenciales
            if (usuarioDAO.autenticar(username.trim(), password)) {
                // Login exitoso
                establecerSesion(usuario);
                actualizarUltimoLogin(usuario.getId());
                registrarIntentoLogin(username, true, "Login exitoso");
                
                // Verificar si necesita cambiar contraseña
                if (usuario.isPasswordExpired()) {
                    SessionManager.getInstance().setRequieresCambioPassword(true);
                }
                
                System.out.println("Login exitoso para: " + usuario.getNombreCompleto());
                return true;
            } else {
                // Login fallido
                registrarIntentoLogin(username, false, "Contraseña incorrecta");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error durante el login: " + e.getMessage());
            registrarIntentoLogin(username, false, "Error del sistema: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        Usuario currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            registrarAuditoria(currentUser.getId(), "LOGOUT", "Sesión cerrada", "SISTEMA");
            System.out.println("Logout para: " + currentUser.getNombreCompleto());
            SessionManager.getInstance().clearSession();
        }
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public boolean isAuthenticated() {
        Usuario currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Verificar si la sesión no ha expirado
        return !SessionManager.getInstance().isSessionExpired();
    }
    
    /**
     * Obtiene el usuario actual
     */
    public Usuario getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }
    
    /**
     * Establece la sesión del usuario
     */
    private void establecerSesion(Usuario usuario) {
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.setCurrentUser(usuario);
        sessionManager.setSessionStartTime(LocalDateTime.now());
        sessionManager.setLastActivity(LocalDateTime.now());
        
        // Registrar auditoría
        registrarAuditoria(usuario.getId(), "LOGIN", "Sesión iniciada", "SISTEMA");
    }
    
    /**
     * Actualiza el último login del usuario
     */
    private void actualizarUltimoLogin(int usuarioId) {
        try {
            usuarioDAO.actualizarUltimoLogin(usuarioId);
        } catch (Exception e) {
            System.err.println("Error al actualizar último login: " + e.getMessage());
        }
    }
    
    /**
     * Registra intento de login en auditoría
     */
    private void registrarIntentoLogin(String username, boolean exitoso, String descripcion) {
        try {
            // Para intentos fallidos, usar ID 0 si no se encuentra el usuario
            int usuarioId = 0;
            Optional<Usuario> usuarioOpt = usuarioDAO.obtenerPorUsername(username);
            if (usuarioOpt.isPresent()) {
                usuarioId = usuarioOpt.get().getId();
            }
            
            String accion = exitoso ? "LOGIN_EXITOSO" : "LOGIN_FALLIDO";
            String descripcionCompleta = String.format("Usuario: %s - %s", username, descripcion);
            
            AuditoriaUsuario auditoria = new AuditoriaUsuario(
                usuarioId, accion, descripcionCompleta, 
                SessionManager.getInstance().getIpAddress(), "AUTENTICACION"
            );
            
            usuarioDAO.registrarAuditoria(auditoria);
            
        } catch (Exception e) {
            System.err.println("Error al registrar intento de login: " + e.getMessage());
        }
    }
    
    /**
     * Registra una auditoría general
     */
    private void registrarAuditoria(int usuarioId, String accion, String descripcion, String modulo) {
        try {
            AuditoriaUsuario auditoria = new AuditoriaUsuario(
                usuarioId, accion, descripcion, 
                SessionManager.getInstance().getIpAddress(), modulo
            );
            usuarioDAO.registrarAuditoria(auditoria);
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }
    
    // ===== VERIFICACIONES DE ROLES Y PERMISOS =====
    
    /**
     * Verifica si el usuario actual tiene un rol específico
     */
    public boolean hasRole(String roleName) {
        Usuario currentUser = getCurrentUser();
        return currentUser != null && roleName.equals(currentUser.getRolNombre());
    }
    
    /**
     * Verifica permisos de administrador
     */
    public boolean isAdmin() {
        return hasRole("ADMINISTRADOR");
    }
    
    /**
     * Verifica permisos de gerente
     */
    public boolean isGerente() {
        return hasRole("GERENTE");
    }
    
    /**
     * Verifica permisos de vendedor
     */
    public boolean isVendedor() {
        return hasRole("VENDEDOR");
    }
    
    /**
     * Verifica permisos de cajero
     */
    public boolean isCajero() {
        return hasRole("CAJERO");
    }
    
    // ===== PERMISOS GRANULARES =====
    
    /**
     * Verifica si el usuario puede gestionar usuarios
     */
    public boolean canManageUsers() {
        return isAdmin() || isGerente();
    }
    
    /**
     * Verifica si el usuario puede ver reportes
     */
    public boolean canViewReports() {
        return isAdmin() || isGerente();
    }
    
    /**
     * Verifica si el usuario puede gestionar inventario
     */
    public boolean canManageInventory() {
        return isAdmin() || isGerente() || isVendedor();
    }
    
    /**
     * Verifica si el usuario puede realizar ventas
     */
    public boolean canMakeSales() {
        return isAdmin() || isGerente() || isVendedor() || isCajero();
    }
    
    /**
     * Verifica si el usuario puede gestionar clientes
     */
    public boolean canManageClients() {
        return isAdmin() || isGerente() || isVendedor();
    }
    
    /**
     * Verifica si el usuario puede ver solo sus propias ventas
     */
    public boolean canViewOwnSalesOnly() {
        return isCajero() || isVendedor();
    }
    
    /**
     * Verifica si el usuario puede ver todas las ventas
     */
    public boolean canViewAllSales() {
        return isAdmin() || isGerente();
    }
    
    /**
     * Verifica si el usuario puede gestionar configuración del sistema
     */
    public boolean canManageSystemConfig() {
        return isAdmin();
    }
    
    /**
     * Verifica si el usuario puede acceder a auditoría
     */
    public boolean canViewAudit() {
        return isAdmin() || isGerente();
    }
    
    // ===== UTILIDADES DE SESIÓN =====
    
    /**
     * Actualiza la actividad de la sesión
     */
    public void updateSessionActivity() {
        if (isAuthenticated()) {
            SessionManager.getInstance().setLastActivity(LocalDateTime.now());
        }
    }
    
    /**
     * Verifica si la sesión está por expirar
     */
    public boolean isSessionNearExpiry() {
        return SessionManager.getInstance().isSessionNearExpiry();
    }
    
    /**
     * Extiende la sesión actual
     */
    public void extendSession() {
        if (isAuthenticated()) {
            SessionManager.getInstance().extendSession();
            Usuario currentUser = getCurrentUser();
            registrarAuditoria(currentUser.getId(), "EXTEND_SESSION", "Sesión extendida", "SISTEMA");
        }
    }
    
    /**
     * Fuerza el cierre de sesión por seguridad
     */
    public void forceLogout(String motivo) {
        Usuario currentUser = getCurrentUser();
        if (currentUser != null) {
            registrarAuditoria(currentUser.getId(), "FORCE_LOGOUT", "Cierre forzado: " + motivo, "SEGURIDAD");
        }
        logout();
    }
}