package dao.impl;

import dao.interfaces.IClienteDAO;
import models.Cliente;
import config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDAOImpl implements IClienteDAO {
    
    private Connection connection;
    
    public ClienteDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    // ===== OPERACIONES CRUD BÁSICAS =====
    
    @Override
    public boolean crear(Cliente cliente) {
        String sql = "INSERT INTO clientes (tipo_documento, numero_documento, nombre, apellido, " +
                    "email, telefono, direccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, cliente.getTipoDocumento());
            stmt.setString(2, cliente.getNumeroDocumento());
            stmt.setString(3, cliente.getNombre());
            stmt.setString(4, cliente.getApellido());
            stmt.setString(5, cliente.getEmail());
            stmt.setString(6, cliente.getTelefono());
            stmt.setString(7, cliente.getDireccion());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    cliente.setId(rs.getInt(1));
                }
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public Optional<Cliente> obtenerPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET tipo_documento = ?, numero_documento = ?, nombre = ?, " +
                    "apellido = ?, email = ?, telefono = ?, direccion = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cliente.getTipoDocumento());
            stmt.setString(2, cliente.getNumeroDocumento());
            stmt.setString(3, cliente.getNombre());
            stmt.setString(4, cliente.getApellido());
            stmt.setString(5, cliente.getEmail());
            stmt.setString(6, cliente.getTelefono());
            stmt.setString(7, cliente.getDireccion());
            stmt.setInt(8, cliente.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
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
        String sql = "UPDATE clientes SET activo = false WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public List<Cliente> obtenerTodos() {
        String sql = "SELECT * FROM clientes ORDER BY nombre, apellido";
        return ejecutarConsultaClientes(sql);
    }
    
    // ===== BÚSQUEDAS ESPECÍFICAS =====
    
    @Override
    public Optional<Cliente> obtenerPorDocumento(String numeroDocumento) {
        String sql = "SELECT * FROM clientes WHERE numero_documento = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por documento: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<Cliente> obtenerPorEmail(String email) {
        String sql = "SELECT * FROM clientes WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por email: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public List<Cliente> buscarPorNombre(String nombre) {
        String sql = "SELECT * FROM clientes WHERE nombre LIKE ? ORDER BY nombre, apellido";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes por nombre: " + e.getMessage());
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> buscarPorApellido(String apellido) {
        String sql = "SELECT * FROM clientes WHERE apellido LIKE ? ORDER BY apellido, nombre";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + apellido + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes por apellido: " + e.getMessage());
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> buscarPorNombreCompleto(String termino) {
        String sql = "SELECT * FROM clientes WHERE " +
                    "(nombre LIKE ? OR apellido LIKE ? OR numero_documento LIKE ? OR email LIKE ?) " +
                    "ORDER BY nombre, apellido";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String patron = "%" + termino + "%";
            stmt.setString(1, patron);
            stmt.setString(2, patron);
            stmt.setString(3, patron);
            stmt.setString(4, patron);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes por nombre completo: " + e.getMessage());
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> obtenerPorTipoDocumento(String tipoDocumento) {
        String sql = "SELECT * FROM clientes WHERE tipo_documento = ? ORDER BY nombre, apellido";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tipoDocumento);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes por tipo documento: " + e.getMessage());
        }
        
        return clientes;
    }
    
    // ===== OPERACIONES DE ESTADO =====
    
    @Override
    public List<Cliente> obtenerActivos() {
        String sql = "SELECT * FROM clientes WHERE activo = true ORDER BY nombre, apellido";
        return ejecutarConsultaClientes(sql);
    }
    
    @Override
    public List<Cliente> obtenerInactivos() {
        String sql = "SELECT * FROM clientes WHERE activo = false ORDER BY nombre, apellido";
        return ejecutarConsultaClientes(sql);
    }
    
    @Override
    public boolean activar(int id) {
        String sql = "UPDATE clientes SET activo = true WHERE id = ?";
        return ejecutarActualizacion(sql, id);
    }
    
    @Override
    public boolean desactivar(int id) {
        String sql = "UPDATE clientes SET activo = false WHERE id = ?";
        return ejecutarActualizacion(sql, id);
    }
    
    // ===== VALIDACIONES =====
    
    @Override
    public boolean existeDocumento(String numeroDocumento) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE numero_documento = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al verificar documento: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean existeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM clientes WHERE email = ?";
        
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
    public boolean existeDocumentoEnOtroCliente(String numeroDocumento, int clienteId) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE numero_documento = ? AND id != ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento);
            stmt.setInt(2, clienteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al verificar documento en otro cliente: " + e.getMessage());
        }
        return false;
    }
    
    @Override
    public boolean existeEmailEnOtroCliente(String email, int clienteId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM clientes WHERE email = ? AND id != ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, clienteId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al verificar email en otro cliente: " + e.getMessage());
        }
        return false;
    }
    
    // ===== ESTADÍSTICAS Y CONTEOS =====
    
    @Override
    public int contarClientes() {
        return ejecutarConteo("SELECT COUNT(*) FROM clientes");
    }
    
    @Override
    public int contarClientesActivos() {
        return ejecutarConteo("SELECT COUNT(*) FROM clientes WHERE activo = true");
    }
    
    @Override
    public int contarClientesInactivos() {
        return ejecutarConteo("SELECT COUNT(*) FROM clientes WHERE activo = false");
    }
    
    @Override
    public int contarPorTipoDocumento(String tipoDocumento) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE tipo_documento = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tipoDocumento);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al contar por tipo documento: " + e.getMessage());
        }
        return 0;
    }
    
    // ===== BÚSQUEDAS AVANZADAS =====
    
    @Override
    public List<Cliente> buscarConFiltros(String termino, String tipoDocumento, boolean soloActivos) {
        StringBuilder sql = new StringBuilder("SELECT * FROM clientes WHERE 1=1");
        List<Object> parametros = new ArrayList<>();
        
        // Filtro por término de búsqueda
        if (termino != null && !termino.trim().isEmpty()) {
            sql.append(" AND (nombre LIKE ? OR apellido LIKE ? OR numero_documento LIKE ? OR email LIKE ?)");
            String patron = "%" + termino.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }
        
        // Filtro por tipo de documento
        if (tipoDocumento != null && !tipoDocumento.trim().isEmpty()) {
            sql.append(" AND tipo_documento = ?");
            parametros.add(tipoDocumento);
        }
        
        // Filtro por estado activo
        if (soloActivos) {
            sql.append(" AND activo = true");
        }
        
        sql.append(" ORDER BY nombre, apellido");
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            // Establecer parámetros
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error en búsqueda con filtros: " + e.getMessage());
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> obtenerUltimosCreados(int limite) {
        String sql = "SELECT * FROM clientes ORDER BY fecha_creacion DESC LIMIT ?";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener últimos creados: " + e.getMessage());
        }
        
        return clientes;
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Ejecuta una consulta que retorna lista de clientes
     */
    private List<Cliente> ejecutarConsultaClientes(String sql) {
        List<Cliente> clientes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error en consulta de clientes: " + e.getMessage());
        }
        
        return clientes;
    }
    
    /**
     * Ejecuta una actualización simple con un parámetro entero
     */
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
    
    /**
     * Ejecuta una consulta de conteo
     */
    private int ejecutarConteo(String sql) {
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error en conteo: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Mapea un ResultSet a un objeto Cliente
     */
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setTipoDocumento(rs.getString("tipo_documento"));
        cliente.setNumeroDocumento(rs.getString("numero_documento"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setEmail(rs.getString("email"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            cliente.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            cliente.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }
        
        return cliente;
    }
}