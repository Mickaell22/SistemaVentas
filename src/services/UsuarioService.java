package services;

import dao.impl.UsuarioDAOImpl;
import dao.interfaces.IUsuarioDAO;
import models.Usuario;
import models.Rol;
import utils.PasswordUtils;
import java.util.List;
import java.util.Optional;

public class UsuarioService {
    
    private IUsuarioDAO usuarioDAO;
    private static UsuarioService instance;
    
    private UsuarioService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }
    
    public static UsuarioService getInstance() {
        if (instance == null) {
            instance = new UsuarioService();
        }
        return instance;
    }
    
    /**
     * Crea un nuevo usuario con validaciones
     */
    public boolean crearUsuario(Usuario usuario) {
        try {
            // Validaciones
            String validationResult = validarUsuario(usuario);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Verificar username único
            if (usuarioDAO.existeUsername(usuario.getUsername())) {
                System.err.println("El username ya existe: " + usuario.getUsername());
                return false;
            }
            
            // Verificar email único
            if (usuarioDAO.existeEmail(usuario.getEmail())) {
                System.err.println("El email ya existe: " + usuario.getEmail());
                return false;
            }
            
            // Validar contraseña
            if (!PasswordUtils.isValidPassword(usuario.getPassword())) {
                System.err.println("Contraseña no cumple los requisitos");
                return false;
            }
            
            // Crear usuario
            boolean resultado = usuarioDAO.crear(usuario);
            if (resultado) {
                System.out.println("Usuario creado exitosamente: " + usuario.getUsername());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza un usuario existente
     */
    public boolean actualizarUsuario(Usuario usuario) {
        try {
            // Validaciones básicas
            if (usuario.getId() <= 0) {
                System.err.println("ID de usuario inválido");
                return false;
            }
            
            String validationResult = validarUsuarioParaActualizacion(usuario);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            boolean resultado = usuarioDAO.actualizar(usuario);
            if (resultado) {
                System.out.println("Usuario actualizado: " + usuario.getUsername());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina (desactiva) un usuario
     */
    public boolean eliminarUsuario(int id) {
        try {
            // Verificar que no sea el usuario actual
            Usuario currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getId() == id) {
                System.err.println("No se puede eliminar el usuario actual");
                return false;
            }
            
            boolean resultado = usuarioDAO.eliminar(id);
            if (resultado) {
                System.out.println("Usuario eliminado: " + id);
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cambia la contraseña de un usuario
     */
    public boolean cambiarPassword(int usuarioId, String passwordActual, String passwordNueva) {
        try {
            // Validar contraseña nueva
            if (!PasswordUtils.isValidPassword(passwordNueva)) {
                System.err.println("La nueva contraseña no cumple los requisitos");
                return false;
            }
            
            // Obtener usuario
            Optional<Usuario> usuarioOpt = usuarioDAO.obtenerPorId(usuarioId);
            if (!usuarioOpt.isPresent()) {
                System.err.println("Usuario no encontrado");
                return false;
            }
            
            // Verificar contraseña actual
            if (!PasswordUtils.verifyPassword(passwordActual, usuarioOpt.get().getPassword())) {
                System.err.println("Contraseña actual incorrecta");
                return false;
            }
            
            // Cambiar contraseña
            boolean resultado = usuarioDAO.cambiarPassword(usuarioId, passwordNueva);
            if (resultado) {
                System.out.println("Contraseña cambiada para usuario: " + usuarioId);
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene todos los usuarios
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioDAO.obtenerTodos();
    }
    
    /**
     * Obtiene usuarios activos
     */
    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioDAO.obtenerActivos();
    }
    
    /**
     * Obtiene un usuario por ID
     */
    public Optional<Usuario> obtenerUsuarioPorId(int id) {
        return usuarioDAO.obtenerPorId(id);
    }
    
    /**
     * Obtiene todos los roles disponibles
     */
    public List<Rol> obtenerTodosLosRoles() {
        return usuarioDAO.obtenerTodosLosRoles();
    }
    
    /**
     * Activa un usuario
     */
    public boolean activarUsuario(int id) {
        boolean resultado = usuarioDAO.activar(id);
        if (resultado) {
            System.out.println("Usuario activado: " + id);
        }
        return resultado;
    }
    
    /**
     * Desactiva un usuario
     */
    public boolean desactivarUsuario(int id) {
        // Verificar que no sea el usuario actual
        Usuario currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId() == id) {
            System.err.println("No se puede desactivar el usuario actual");
            return false;
        }
        
        boolean resultado = usuarioDAO.desactivar(id);
        if (resultado) {
            System.out.println("Usuario desactivado: " + id);
        }
        return resultado;
    }
    
    // Métodos de validación privados
    private String validarUsuario(Usuario usuario) {
        if (usuario == null) {
            return "Usuario no puede ser null";
        }
        
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return "Nombre es requerido";
        }
        
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            return "Apellido es requerido";
        }
        
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            return "Username es requerido";
        }
        
        if (usuario.getUsername().length() < 3) {
            return "Username debe tener al menos 3 caracteres";
        }
        
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            return "Email válido es requerido";
        }
        
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            return "Contraseña es requerida";
        }
        
        if (usuario.getRolId() <= 0) {
            return "Rol válido es requerido";
        }
        
        return "OK";
    }
    
    private String validarUsuarioParaActualizacion(Usuario usuario) {
        if (usuario == null) {
            return "Usuario no puede ser null";
        }
        
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return "Nombre es requerido";
        }
        
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            return "Apellido es requerido";
        }
        
        if (usuario.getEmail() == null || !usuario.getEmail().contains("@")) {
            return "Email válido es requerido";
        }
        
        if (usuario.getRolId() <= 0) {
            return "Rol válido es requerido";
        }
        
        return "OK";
    }
}