package services;

import dao.impl.UsuarioDAOImpl;
import dao.interfaces.IUsuarioDAO;
import models.Rol;
import models.AuditoriaUsuario;
import utils.SessionManager;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.ArrayList;

public class RolService {
    
    private IUsuarioDAO usuarioDAO;
    private static RolService instance;
    
    // Permisos predefinidos del sistema
    private static final List<String> PERMISOS_DISPONIBLES = Arrays.asList(
        "users.view", "users.create", "users.edit", "users.delete",
        "clients.view", "clients.create", "clients.edit", "clients.delete",
        "sales.view", "sales.create", "sales.edit", "sales.delete",
        "products.view", "products.create", "products.edit", "products.delete",
        "inventory.view", "inventory.adjust", "inventory.reports",
        "reports.sales", "reports.clients", "reports.products", "reports.financial",
        "system.config", "system.audit", "system.backup",
        "*" // Acceso total
    );
    
    private RolService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }
    
    public static RolService getInstance() {
        if (instance == null) {
            instance = new RolService();
        }
        return instance;
    }
    
    /**
     * Crea un nuevo rol
     */
    public boolean crearRol(Rol rol) {
        try {
            // Validaciones
            String validationResult = validarRol(rol);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Verificar permisos
            if (!AuthService.getInstance().canManageUsers()) {
                System.err.println("Sin permisos para crear roles");
                return false;
            }
            
            // Verificar nombre único
            Optional<Rol> rolExistente = usuarioDAO.obtenerRolPorNombre(rol.getNombre());
            if (rolExistente.isPresent()) {
                System.err.println("Ya existe un rol con ese nombre: " + rol.getNombre());
                return false;
            }
            
            boolean resultado = usuarioDAO.crearRol(rol);
            
            if (resultado) {
                System.out.println("Rol creado exitosamente: " + rol.getNombre());
                registrarAuditoria("CREAR_ROL", "Rol creado: " + rol.getNombre());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al crear rol: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza un rol existente
     */
    public boolean actualizarRol(Rol rol) {
        try {
            // Validar que el rol existe
            Optional<Rol> rolExistente = usuarioDAO.obtenerRolPorId(rol.getId());
            if (!rolExistente.isPresent()) {
                System.err.println("Rol no encontrado para actualizar");
                return false;
            }
            
            // Validaciones
            String validationResult = validarRol(rol);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Verificar permisos
            if (!AuthService.getInstance().canManageUsers()) {
                System.err.println("Sin permisos para actualizar roles");
                return false;
            }
            
            boolean resultado = usuarioDAO.actualizarRol(rol);
            
            if (resultado) {
                System.out.println("Rol actualizado: " + rol.getNombre());
                registrarAuditoria("ACTUALIZAR_ROL", "Rol actualizado: " + rol.getNombre());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar rol: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina un rol (lo desactiva)
     */
    public boolean eliminarRol(int rolId) {
        try {
            // Verificar que el rol existe
            Optional<Rol> rolExistente = usuarioDAO.obtenerRolPorId(rolId);
            if (!rolExistente.isPresent()) {
                System.err.println("Rol no encontrado para eliminar");
                return false;
            }
            
            // Verificar permisos
            if (!AuthService.getInstance().canManageUsers()) {
                System.err.println("Sin permisos para eliminar roles");
                return false;
            }
            
            // Verificar que no hay usuarios asignados a este rol
            int usuariosConRol = usuarioDAO.contarUsuariosPorRol(rolId);
            if (usuariosConRol > 0) {
                System.err.println("No se puede eliminar el rol. Hay " + usuariosConRol + " usuarios asignados");
                return false;
            }
            
            boolean resultado = usuarioDAO.eliminarRol(rolId);
            
            if (resultado) {
                Rol rol = rolExistente.get();
                System.out.println("Rol eliminado: " + rol.getNombre());
                registrarAuditoria("ELIMINAR_ROL", "Rol eliminado: " + rol.getNombre());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al eliminar rol: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene todos los roles
     */
    public List<Rol> obtenerTodosLosRoles() {
        return usuarioDAO.obtenerTodosLosRoles();
    }
    
    /**
     * Obtiene roles activos
     */
    public List<Rol> obtenerRolesActivos() {
        return usuarioDAO.obtenerRolesActivos();
    }
    
    /**
     * Obtiene un rol por ID
     */
    public Optional<Rol> obtenerRolPorId(int id) {
        return usuarioDAO.obtenerRolPorId(id);
    }
    
    /**
     * Obtiene permisos disponibles en el sistema
     */
    public List<String> getPermisosDisponibles() {
        return new ArrayList<>(PERMISOS_DISPONIBLES);
    }
    
    /**
     * Verifica si un permiso es válido
     */
    public boolean esPermisoValido(String permiso) {
        return PERMISOS_DISPONIBLES.contains(permiso) || permiso.matches("^[a-zA-Z0-9_.]+$");
    }
    
    /**
     * Obtiene descripción de un permiso
     */
    public String getDescripcionPermiso(String permiso) {
        switch (permiso) {
            case "*": return "Acceso total al sistema";
            case "users.view": return "Ver usuarios";
            case "users.create": return "Crear usuarios";
            case "users.edit": return "Editar usuarios";
            case "users.delete": return "Eliminar usuarios";
            case "clients.view": return "Ver clientes";
            case "clients.create": return "Crear clientes";
            case "clients.edit": return "Editar clientes";
            case "clients.delete": return "Eliminar clientes";
            case "sales.view": return "Ver ventas";
            case "sales.create": return "Crear ventas";
            case "sales.edit": return "Editar ventas";
            case "sales.delete": return "Anular ventas";
            case "products.view": return "Ver productos";
            case "products.create": return "Crear productos";
            case "products.edit": return "Editar productos";
            case "products.delete": return "Eliminar productos";
            case "inventory.view": return "Ver inventario";
            case "inventory.adjust": return "Ajustar inventario";
            case "inventory.reports": return "Reportes de inventario";
            case "reports.sales": return "Reportes de ventas";
            case "reports.clients": return "Reportes de clientes";
            case "reports.products": return "Reportes de productos";
            case "reports.financial": return "Reportes financieros";
            case "system.config": return "Configuración del sistema";
            case "system.audit": return "Auditoría del sistema";
            case "system.backup": return "Respaldos del sistema";
            default: return "Permiso personalizado";
        }
    }
    
    /**
     * Valida un rol antes de guardarlo
     */
    private String validarRol(Rol rol) {
        if (rol == null) {
            return "Rol no puede ser null";
        }
        
        if (rol.getNombre() == null || rol.getNombre().trim().isEmpty()) {
            return "Nombre del rol es obligatorio";
        }
        
        if (rol.getNombre().length() < 3 || rol.getNombre().length() > 50) {
            return "Nombre del rol debe tener entre 3 y 50 caracteres";
        }
        
        if (!rol.getNombre().matches("^[A-Z_]+$")) {
            return "Nombre del rol debe estar en mayúsculas y usar solo letras y guiones bajos";
        }
        
        if (!rol.isValidHierarchy()) {
            return "Nivel de jerarquía inválido (debe ser entre 1 y 10)";
        }
        
        // Validar permisos si existen
        List<String> permisos = rol.getPermissionsList();
        if (permisos != null) {
            for (String permiso : permisos) {
                if (!esPermisoValido(permiso)) {
                    return "Permiso inválido: " + permiso;
                }
            }
        }
        
        return "OK";
    }
    
    /**
     * Registra una auditoría
     */
    private void registrarAuditoria(String accion, String descripcion) {
        try {
            Usuario currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser != null) {
                AuditoriaUsuario auditoria = new AuditoriaUsuario(
                    currentUser.getId(), accion, descripcion, 
                    SessionManager.getInstance().getIpAddress(), "ROLES"
                );
                usuarioDAO.registrarAuditoria(auditoria);
            }
        } catch (Exception e) {
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }
}