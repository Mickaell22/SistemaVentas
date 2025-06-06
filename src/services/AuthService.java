package services;

import dao.impl.UsuarioDAOImpl;
import dao.interfaces.IUsuarioDAO;
import models.Usuario;
import utils.SessionManager;
import java.util.Optional;

public class AuthService {
    
    private IUsuarioDAO usuarioDAO;
    private static AuthService instance;
    
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
                return false;
            }
            
            // Verificar credenciales
            if (usuarioDAO.autenticar(username.trim(), password)) {
                // Obtener datos completos del usuario
                Optional<Usuario> usuarioOpt = usuarioDAO.obtenerPorUsername(username.trim());
                
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    
                    // Verificar que el usuario esté activo
                    if (!usuario.isActivo()) {
                        System.out.println("Usuario inactivo: " + username);
                        return false;
                    }
                    
                    // Establecer sesión
                    SessionManager.getInstance().setCurrentUser(usuario);
                    
                    // Actualizar último login
                    usuarioDAO.actualizarUltimoLogin(usuario.getId());
                    
                    System.out.println("Login exitoso para: " + usuario.getNombreCompleto());
                    return true;
                }
            }
            
            System.out.println("Credenciales inválidas para: " + username);
            return false;
            
        } catch (Exception e) {
            System.err.println("Error durante el login: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        Usuario currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("Logout para: " + currentUser.getNombreCompleto());
            SessionManager.getInstance().clearSession();
        }
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public boolean isAuthenticated() {
        return SessionManager.getInstance().getCurrentUser() != null;
    }
    
    /**
     * Obtiene el usuario actual
     */
    public Usuario getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }
    
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
}