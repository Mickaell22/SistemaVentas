package services;

import dao.impl.UsuarioDAOImpl;
import dao.interfaces.IUsuarioDAO;
import models.Usuario;
import models.Rol;
import models.AuditoriaUsuario;
import utils.PasswordUtils;
import utils.SessionManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UsuarioService {
    
    private IUsuarioDAO usuarioDAO;
    private static UsuarioService instance;
    
    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NOMBRE_PATTERN = 
        Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s.-]{2,50}$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._-]{3,20}$");
    private static final Pattern TELEFONO_PATTERN = 
        Pattern.compile("^[0-9+\\-\\s()]{7,15}$");
    
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
     * Crea un nuevo usuario con validaciones completas
     */
    public boolean crearUsuario(Usuario usuario) {
        try {
            // Validaciones previas
            String validationResult = validarUsuarioCompleto(usuario);
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
            
            // Validar contraseña robusta
            if (!PasswordUtils.isValidPassword(usuario.getPassword())) {
                System.err.println("Contraseña no cumple los requisitos de seguridad");
                return false;
            }
            
            // Establecer datos de auditoría
            Usuario currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser != null) {
                usuario.setCreadoPor(currentUser.getId());
            }
            
            // Crear usuario
            boolean resultado = usuarioDAO.crear(usuario);
            
            if (resultado) {
                System.out.println("Usuario creado exitosamente: " + usuario.getUsername());
                
                // Registrar auditoría
                registrarAuditoria(usuario.getId(), "CREAR_USUARIO", 
                    "Usuario creado: " + usuario.getNombreCompleto(), "USUARIOS");
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
            // Validar que el usuario existe
            Optional<Usuario> usuarioExistente = usuarioDAO.obtenerPorId(usuario.getId());
            if (!usuarioExistente.isPresent()) {
                System.err.println("Usuario no encontrado para actualizar");
                return false;
            }
            
            // Validaciones de datos
            String validationResult = validarActualizacionUsuario(usuario);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación en actualización: " + validationResult);
                return false;
            }
            
            // Verificar permisos
            if (!puedeEditarUsuario(usuario.getId())) {
                System.err.println("Sin permisos para editar este usuario");
                return false;
            }
            
            boolean resultado = usuarioDAO.actualizar(usuario);
            
            if (resultado) {
                System.out.println("Usuario actualizado: " + usuario.getUsername());
                
                // Registrar auditoría
                registrarAuditoria(usuario.getId(), "ACTUALIZAR_USUARIO", 
                    "Usuario actualizado: " + usuario.getNombreCompleto(), "USUARIOS");
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cambia la contraseña de un usuario
     */
    public boolean cambiarPassword(int usuarioId, String passwordActual, String passwordNueva) {
        try {
            // Verificar que el usuario existe
            Optional<Usuario> usuarioOpt = usuarioDAO.obtenerPorId(usuarioId);
            if (!usuarioOpt.isPresent()) {
                System.err.println("Usuario no encontrado");
                return false;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Verificar contraseña actual (solo si no es admin cambiando para otro)
            Usuario currentUser = AuthService.getInstance().getCurrentUser();
            boolean esAdmin = AuthService.getInstance().isAdmin();
            boolean esPropietario = currentUser != null && currentUser.getId() == usuarioId;
            
            if (!esAdmin || esPropietario) {
                if (!PasswordUtils.verifyPassword(passwordActual, usuario.getPassword())) {
                    System.err.println("Contraseña actual incorrecta");
                    return false;
                }
            }
            
            // Validar nueva contraseña
            if (!PasswordUtils.isValidPassword(passwordNueva)) {
                System.err.println("La nueva contraseña no cumple los requisitos");
                return false;
            }
            
            // Cambiar contraseña
            boolean resultado = usuarioDAO.cambiarPassword(usuarioId, passwordNueva);
            
            if (resultado) {
                System.out.println("Contraseña cambiada para usuario: " + usuarioId);
                
                // Registrar auditoría
                registrarAuditoria(usuarioId, "CAMBIAR_PASSWORD", 
                    "Contraseña cambiada", "USUARIOS");
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Activa un usuario
     */
    public boolean activarUsuario(int id) {
        if (!puedeGestionarUsuarios()) {
            System.err.println("Sin permisos para activar usuarios");
            return false;
        }
        
        boolean resultado = usuarioDAO.activar(id);
        if (resultado) {
            System.out.println("Usuario activado: " + id);
            registrarAuditoria(id, "ACTIVAR_USUARIO", "Usuario activado", "USUARIOS");
        }
        return resultado;
    }
    
    /**
     * Desactiva un usuario
     */
    public boolean desactivarUsuario(int id) {
        if (!puedeGestionarUsuarios()) {
            System.err.println("Sin permisos para desactivar usuarios");
            return false;
        }
        
        // Verificar que no sea el usuario actual
        Usuario currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId() == id) {
            System.err.println("No se puede desactivar el usuario actual");
            return false;
        }
        
        boolean resultado = usuarioDAO.desactivar(id);
        if (resultado) {
            System.out.println("Usuario desactivado: " + id);
            registrarAuditoria(id, "DESACTIVAR_USUARIO", "Usuario desactivado", "USUARIOS");
        }
        return resultado;
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
     * Valida un usuario completo para creación
     */
    private String validarUsuarioCompleto(Usuario usuario) {
        if (usuario == null) {
            return "Usuario no puede ser null";
        }
        
        // Validar nombre
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return "Nombre es obligatorio";
        }
        if (!NOMBRE_PATTERN.matcher(usuario.getNombre().trim()).matches()) {
            return "Nombre tiene formato inválido";
        }
        
        // Validar apellido (opcional pero con formato si se proporciona)
        if (usuario.getApellido() != null && !usuario.getApellido().trim().isEmpty()) {
            if (!NOMBRE_PATTERN.matcher(usuario.getApellido().trim()).matches()) {
                return "Apellido tiene formato inválido";
            }
        }
        
        // Validar email
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return "Email es obligatorio";
        }
        if (!EMAIL_PATTERN.matcher(usuario.getEmail().trim()).matches()) {
            return "Email tiene formato inválido";
        }
        
        // Validar username
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            return "Username es obligatorio";
        }
        if (!USERNAME_PATTERN.matcher(usuario.getUsername().trim()).matches()) {
            return "Username debe tener 3-20 caracteres alfanuméricos";
        }
        
        // Validar contraseña
        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            return "Contraseña es obligatoria";
        }
        
        // Validar teléfono (opcional)
        if (usuario.getTelefono() != null && !usuario.getTelefono().trim().isEmpty()) {
            if (!TELEFONO_PATTERN.matcher(usuario.getTelefono().trim()).matches()) {
                return "Teléfono tiene formato inválido";
            }
        }
        
        // Validar rol
        if (usuario.getRolId() <= 0) {
            return "Rol es obligatorio";
        }
        
        return "OK";
    }
    
    /**
     * Valida actualización de usuario (sin contraseña)
     */
    private String validarActualizacionUsuario(Usuario usuario) {
        Usuario temp = new Usuario();
        temp.setNombre(usuario.getNombre());
        temp.setApellido(usuario.getApellido());
        temp.setEmail(usuario.getEmail());
        temp.setUsername(usuario.getUsername());
        temp.setPassword("TempPassword123!"); // Temporal para validación
        temp.setTelefono(usuario.getTelefono());
        temp.setRolId(usuario.getRolId());
        
        String resultado = validarUsuarioCompleto(temp);
        return resultado.equals("OK") ? "OK" : resultado;
    }
    
    /**
     * Verifica si se puede gestionar usuarios
     */
    private boolean puedeGestionarUsuarios() {
        return AuthService.getInstance().canManageUsers();
    }
    
    /**
     * Verifica si se puede editar un usuario específico
     */
    private boolean puedeEditarUsuario(int usuarioId) {
        Usuario currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // Admins pueden editar a todos
        if (AuthService.getInstance().isAdmin()) {
            return true;
        }
        
        // Gerentes pueden editar usuarios de menor jerarquía
        if (AuthService.getInstance().isGerente()) {
            // Implementar lógica de jerarquía
            return true;
        }
        
        // Usuarios pueden editar solo su propio perfil
        return currentUser.getId() == usuarioId;
    }
    
    /**
     * Registra una auditoría
     */
    private void registrarAuditoria(int usuarioId, String accion, String descripcion, String modulo) {
        try {
            Usuario currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser != null) {
                AuditoriaUsuario auditoria = new AuditoriaUsuario(
                    currentUser.getId(), accion, descripcion, 
                    SessionManager.getInstance().getIpAddress(), modulo
                );
                usuarioDAO.registrarAuditoria(auditoria);
            }
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }
}