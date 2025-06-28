package controllers;

import services.UsuarioService;
import services.AuthService;
import models.Usuario;
import models.Rol;
import views.usuarios.UsuarioPanel;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class UsuarioController {
    
    private UsuarioService usuarioService;
    private AuthService authService;
    private UsuarioPanel usuarioPanel;
    
    public UsuarioController() {
        this.usuarioService = UsuarioService.getInstance();
        this.authService = AuthService.getInstance();
    }
    
    public void setView(UsuarioPanel usuarioPanel) {
        this.usuarioPanel = usuarioPanel;
        cargarDatos();
    }
    
    /**
     * Carga los datos iniciales en la vista
     */
    public void cargarDatos() {
        if (usuarioPanel != null) {
            cargarUsuarios();
            cargarRoles();
            actualizarEstadisticas();
        }
    }
    
    /**
     * Carga la lista de usuarios
     */
    public void cargarUsuarios() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            if (usuarioPanel != null) {
                usuarioPanel.cargarUsuarios(usuarios);
            }
        } catch (Exception e) {
            showError("Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Carga la lista de roles
     */
    public void cargarRoles() {
        try {
            List<Rol> roles = usuarioService.obtenerTodosLosRoles();
            if (usuarioPanel != null) {
                usuarioPanel.cargarRoles(roles);
            }
        } catch (Exception e) {
            showError("Error al cargar roles: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la creación de un nuevo usuario
     */
    public void crearUsuario(Usuario usuario) {
        try {
            // Verificar permisos
            if (!authService.canManageUsers()) {
                showError("No tiene permisos para crear usuarios");
                return;
            }
            
            boolean resultado = usuarioService.crearUsuario(usuario);
            
            if (resultado) {
                showInfo("Usuario creado exitosamente");
                cargarUsuarios();
                actualizarEstadisticas();
            } else {
                showError("Error al crear el usuario");
            }
            
        } catch (Exception e) {
            showError("Error al crear usuario: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la actualización de un usuario
     */
    public void actualizarUsuario(Usuario usuario) {
        try {
            boolean resultado = usuarioService.actualizarUsuario(usuario);
            
            if (resultado) {
                showInfo("Usuario actualizado exitosamente");
                cargarUsuarios();
            } else {
                showError("Error al actualizar el usuario");
            }
            
        } catch (Exception e) {
            showError("Error al actualizar usuario: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el cambio de contraseña
     */
    public void cambiarPassword(int usuarioId, String passwordActual, String passwordNueva) {
        try {
            boolean resultado = usuarioService.cambiarPassword(usuarioId, passwordActual, passwordNueva);
            
            if (resultado) {
                showInfo("Contraseña cambiada exitosamente");
            } else {
                showError("Error al cambiar la contraseña");
            }
            
        } catch (Exception e) {
            showError("Error al cambiar contraseña: " + e.getMessage());
        }
    }
    
    /**
     * Activa un usuario
     */
    public void activarUsuario(int usuarioId) {
        try {
            if (!authService.canManageUsers()) {
                showError("No tiene permisos para activar usuarios");
                return;
            }
            
            boolean resultado = usuarioService.activarUsuario(usuarioId);
            
            if (resultado) {
                showInfo("Usuario activado exitosamente");
                cargarUsuarios();
                actualizarEstadisticas();
            } else {
                showError("Error al activar el usuario");
            }
            
        } catch (Exception e) {
            showError("Error al activar usuario: " + e.getMessage());
        }
    }
    
    /**
     * Desactiva un usuario
     */
    public void desactivarUsuario(int usuarioId) {
        try {
            if (!authService.canManageUsers()) {
                showError("No tiene permisos para desactivar usuarios");
                return;
            }
            
            // Confirmar acción
            int confirmacion = JOptionPane.showConfirmDialog(
                usuarioPanel,
                "¿Está seguro de que desea desactivar este usuario?",
                "Confirmar desactivación",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                boolean resultado = usuarioService.desactivarUsuario(usuarioId);
                
                if (resultado) {
                    showInfo("Usuario desactivado exitosamente");
                    cargarUsuarios();
                    actualizarEstadisticas();
                } else {
                    showError("Error al desactivar el usuario");
                }
            }
            
        } catch (Exception e) {
            showError("Error al desactivar usuario: " + e.getMessage());
        }
    }
    
    /**
     * Busca usuarios por nombre
     */
    public void buscarUsuarios(String termino) {
        try {
            List<Usuario> todosUsuarios = usuarioService.obtenerTodosLosUsuarios();
            
            List<Usuario> usuariosFiltrados = todosUsuarios.stream()
                .filter(u -> u.getNombreCompleto().toLowerCase().contains(termino.toLowerCase()) ||
                           u.getUsername().toLowerCase().contains(termino.toLowerCase()) ||
                           u.getEmail().toLowerCase().contains(termino.toLowerCase()))
                .toList();
            
            if (usuarioPanel != null) {
                usuarioPanel.cargarUsuarios(usuariosFiltrados);
            }
            
        } catch (Exception e) {
            showError("Error al buscar usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene un usuario por ID
     */
    public Optional<Usuario> obtenerUsuario(int id) {
        return usuarioService.obtenerUsuarioPorId(id);
    }
    
    /**
     * Actualiza las estadísticas en la vista
     */
    private void actualizarEstadisticas() {
        if (usuarioPanel != null) {
            String estadisticas = obtenerEstadisticas();
            usuarioPanel.actualizarEstadisticas(estadisticas);
        }
    }
    
    /**
     * Obtiene estadísticas de usuarios
     */
    public String obtenerEstadisticas() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            List<Usuario> activos = usuarioService.obtenerUsuariosActivos();
            
            long totalUsuarios = usuarios.size();
            long usuariosActivos = activos.size();
            long usuariosInactivos = totalUsuarios - usuariosActivos;
            
            return String.format(
                "Total: %d | Activos: %d | Inactivos: %d",
                totalUsuarios, usuariosActivos, usuariosInactivos
            );
            
        } catch (Exception e) {
            return "Error al obtener estadísticas";
        }
    }
    
    /**
     * Validaciones de formulario
     */
    public boolean validarDatosUsuario(Usuario usuario, boolean esCreacion) {
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            showError("El nombre es obligatorio");
            return false;
        }
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            showError("El email es obligatorio");
            return false;
        }
        
        if (!validarFormatoEmail(usuario.getEmail())) {
            showError("El formato del email es inválido");
            return false;
        }
        
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            showError("El username es obligatorio");
            return false;
        }
        
        if (usuario.getUsername().length() < 3) {
            showError("El username debe tener al menos 3 caracteres");
            return false;
        }
        
        if (esCreacion && (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty())) {
            showError("La contraseña es obligatoria");
            return false;
        }
        
        if (usuario.getRolId() <= 0) {
            showError("Debe seleccionar un rol");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida formato de email
     */
    private boolean validarFormatoEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * Obtiene tipos de roles para ComboBox
     */
    public List<Rol> obtenerRoles() {
        return usuarioService.obtenerTodosLosRoles();
    }
    
    // ===== MENSAJES =====
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            usuarioPanel,
            message,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            usuarioPanel,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
            usuarioPanel,
            message,
            "Advertencia",
            JOptionPane.WARNING_MESSAGE
        );
    }
}