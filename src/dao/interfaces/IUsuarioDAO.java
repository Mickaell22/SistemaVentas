package dao.interfaces;

import models.Usuario;
import models.Rol;
import java.util.List;
import java.util.Optional;

public interface IUsuarioDAO {
    
    // Operaciones CRUD básicas
    boolean crear(Usuario usuario);
    Optional<Usuario> obtenerPorId(int id);
    boolean actualizar(Usuario usuario);
    boolean eliminar(int id);
    List<Usuario> obtenerTodos();
    
    // Operaciones específicas para autenticación
    Optional<Usuario> obtenerPorUsername(String username);
    Optional<Usuario> obtenerPorEmail(String email);
    boolean autenticar(String username, String password);
    
    // Operaciones de estado
    boolean activar(int id);
    boolean desactivar(int id);
    boolean cambiarPassword(int id, String newPassword);
    boolean actualizarUltimoLogin(int id);
    
    // Consultas específicas
    List<Usuario> obtenerPorRol(int rolId);
    List<Usuario> obtenerActivos();
    List<Usuario> buscarPorNombre(String nombre);
    boolean existeUsername(String username);
    boolean existeEmail(String email);
    int contarUsuarios();
    int contarUsuariosPorRol(int rolId);
    
    // Operaciones con roles
    List<Rol> obtenerTodosLosRoles();
    Optional<Rol> obtenerRolPorId(int id);
}