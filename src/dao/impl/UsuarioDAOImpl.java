package dao.impl;

import dao.interfaces.IUsuarioDAO;
import models.Usuario;
import models.Rol;
import config.DatabaseConnection;
import utils.PasswordUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAOImpl implements IUsuarioDAO {
    
    private Connection connection;
    
    public UsuarioDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, apellido, email, username, password, telefono, direccion, rol_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getUsername());
            stmt.setString(5, PasswordUtils.hashPassword(usuario.getPassword()));
            stmt.setString(6, usuario.getTelefono());
            stmt.setString(7, usuario.getDireccion());
            stmt.setInt(8, usuario.getRolId());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public Optional<Usuario> obtenerPorId(int id) {
        String sql = "SELECT u.*, r.nombre as rol_nombre FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id WHERE u.id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearUsuario(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<Usuario> obtenerPorUsername(String username) {
        String sql = "SELECT u.*, r.nombre as rol_nombre FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id WHERE u.username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearUsuario(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario por username: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public boolean autenticar(String username, String password) {
        Optional<Usuario> usuario = obtenerPorUsername(username);
        
        if (usuario.isPresent() && usuario.get().isActivo()) {
            return PasswordUtils.verifyPassword(password, usuario.get().getPassword());
        }
        return false;
    }
    
    @Override
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, telefono = ?, direccion = ?, rol_id = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getTelefono());
            stmt.setString(5, usuario.getDireccion());
            stmt.setInt(6, usuario.getRolId());
            stmt.setInt(7, usuario.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public boolean eliminar(int id) {
        String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public List<Usuario> obtenerTodos() {
        String sql = "SELECT u.*, r.nombre as rol_nombre FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id ORDER BY u.nombre";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los usuarios: " + e.getMessage());
        }
        return usuarios;
    }
    
    @Override
    public List<Usuario> obtenerActivos() {
        String sql = "SELECT u.*, r.nombre as rol_nombre FROM usuarios u LEFT JOIN roles r ON u.rol_id = r.id WHERE u.activo = true ORDER BY u.nombre";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios activos: " + e.getMessage());
        }
        return usuarios;
    }
    
    @Override
    public boolean cambiarPassword(int id, String newPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, PasswordUtils.hashPassword(newPassword));
            stmt.setInt(2, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al cambiar contraseña: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public boolean actualizarUltimoLogin(int id) {
        String sql = "UPDATE usuarios SET ultimo_login = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar último login: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al verificar username: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public List<Rol> obtenerTodosLosRoles() {
        String sql = "SELECT * FROM roles WHERE activo = true ORDER BY nombre";
        List<Rol> roles = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                roles.add(mapearRol(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener roles: " + e.getMessage());
        }
        return roles;
    }
    
    // Métodos auxiliares para mapear ResultSet a objetos
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setUsername(rs.getString("username"));
        usuario.setPassword(rs.getString("password"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setDireccion(rs.getString("direccion"));
        usuario.setRolId(rs.getInt("rol_id"));
        usuario.setRolNombre(rs.getString("rol_nombre"));
        usuario.setActivo(rs.getBoolean("activo"));
        
        Timestamp ultimoLogin = rs.getTimestamp("ultimo_login");
        if (ultimoLogin != null) {
            usuario.setUltimoLogin(ultimoLogin.toLocalDateTime());
        }
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return usuario;
    }
    
    private Rol mapearRol(ResultSet rs) throws SQLException {
        Rol rol = new Rol();
        rol.setId(rs.getInt("id"));
        rol.setNombre(rs.getString("nombre"));
        rol.setDescripcion(rs.getString("descripcion"));
        rol.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            rol.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return rol;
    }
    
    // Implementación de métodos restantes (simplificados)
    @Override
    public Optional<Usuario> obtenerPorEmail(String email) {
        // Similar a obtenerPorUsername pero con email
        return Optional.empty(); // Implementar según necesidad
    }
    
    @Override
    public boolean activar(int id) {
        String sql = "UPDATE usuarios SET activo = true WHERE id = ?";
        return ejecutarActualizacion(sql, id);
    }
    
    @Override
    public boolean desactivar(int id) {
        String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
        return ejecutarActualizacion(sql, id);
    }
    
    @Override
    public List<Usuario> obtenerPorRol(int rolId) {
        // Implementar según necesidad
        return new ArrayList<>();
    }
    
    @Override
    public List<Usuario> buscarPorNombre(String nombre) {
        // Implementar según necesidad
        return new ArrayList<>();
    }
    
    @Override
    public int contarUsuarios() {
        return 0; // Implementar según necesidad
    }
    
    @Override
    public int contarUsuariosPorRol(int rolId) {
        return 0; // Implementar según necesidad
    }
    
    @Override
    public Optional<Rol> obtenerRolPorId(int id) {
        return Optional.empty(); // Implementar según necesidad
    }
    
    private boolean ejecutarActualizacion(String sql, int id) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error en actualización: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
}