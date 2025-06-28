package dao.interfaces;

import models.Usuario;
import models.Rol;
import models.AuditoriaUsuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface IUsuarioDAO {
    
    // ===== OPERACIONES CRUD BÁSICAS =====
    boolean crear(Usuario usuario);
    Optional<Usuario> obtenerPorId(int id);
    boolean actualizar(Usuario usuario);
    boolean eliminar(int id);
    List<Usuario> obtenerTodos();
    
    // ===== OPERACIONES DE AUTENTICACIÓN =====
    Optional<Usuario> obtenerPorUsername(String username);
    Optional<Usuario> obtenerPorEmail(String email);
    boolean autenticar(String username, String password);
    boolean validarToken(String token);
    
    // ===== GESTIÓN DE ESTADO Y SEGURIDAD =====
    boolean activar(int id);
    boolean desactivar(int id);
    boolean cambiarPassword(int id, String newPassword);
    boolean actualizarUltimoLogin(int id);
    boolean registrarIntentoFallido(String username);
    boolean bloquearUsuario(int id, LocalDateTime hasta);
    boolean desbloquearUsuario(int id);
    boolean establecerExpiracionPassword(int id, LocalDateTime expiracion);
    
    // ===== BÚSQUEDAS Y CONSULTAS =====
    List<Usuario> obtenerPorRol(int rolId);
    List<Usuario> obtenerActivos();
    List<Usuario> obtenerInactivos();
    List<Usuario> obtenerBloqueados();
    List<Usuario> obtenerConPasswordExpirada();
    List<Usuario> buscarPorNombre(String nombre);
    List<Usuario> buscarPorEmail(String email);
    boolean existeUsername(String username);
    boolean existeEmail(String email);
    
    // ===== ESTADÍSTICAS Y REPORTES =====
    int contarUsuarios();
    int contarUsuariosActivos();
    int contarUsuariosPorRol(int rolId);
    Map<String, Integer> obtenerEstadisticasUsuarios();
    List<Usuario> obtenerUsuariosRecientes(int dias);
    List<Usuario> obtenerUsuariosSinLogin(int dias);
    
    // ===== OPERACIONES CON ROLES =====
    List<Rol> obtenerTodosLosRoles();
    List<Rol> obtenerRolesActivos();
    Optional<Rol> obtenerRolPorId(int id);
    Optional<Rol> obtenerRolPorNombre(String nombre);
    boolean crearRol(Rol rol);
    boolean actualizarRol(Rol rol);
    boolean eliminarRol(int id);
    boolean asignarRol(int usuarioId, int rolId);
    
    // ===== AUDITORÍA =====
    boolean registrarAuditoria(AuditoriaUsuario auditoria);
    List<AuditoriaUsuario> obtenerAuditoriaPorUsuario(int usuarioId);
    List<AuditoriaUsuario> obtenerAuditoriaPorFecha(LocalDateTime desde, LocalDateTime hasta);
    List<AuditoriaUsuario> obtenerAuditoriaPorAccion(String accion);
}