package dao.impl;

import dao.interfaces.IProductoDAO;
import models.Producto;
import models.Categoria;
import models.Proveedor;
import config.DatabaseConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAOImpl implements IProductoDAO {
    
    private Connection connection;
    
    public ProductoDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public boolean crear(Producto producto) {
        String sql = "INSERT INTO productos (codigo, nombre, descripcion, categoria_id, proveedor_id, " +
                    "precio_compra, precio_venta, stock_actual, stock_minimo, unidad_medida) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, producto.getCodigo());
            stmt.setString(2, producto.getNombre());
            stmt.setString(3, producto.getDescripcion());
            stmt.setInt(4, producto.getCategoriaId());
            stmt.setInt(5, producto.getProveedorId());
            stmt.setBigDecimal(6, producto.getPrecioCompra());
            stmt.setBigDecimal(7, producto.getPrecioVenta());
            stmt.setInt(8, producto.getStockActual());
            stmt.setInt(9, producto.getStockMinimo());
            stmt.setString(10, producto.getUnidadMedida());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    producto.setId(rs.getInt(1));
                }
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public Optional<Producto> obtenerPorId(int id) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, pr.nombre as proveedor_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id " +
                    "WHERE p.id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearProducto(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por ID: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<Producto> obtenerPorCodigo(String codigo) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, pr.nombre as proveedor_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id " +
                    "WHERE p.codigo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapearProducto(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener producto por código: " + e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public boolean actualizar(Producto producto) {
        String sql = "UPDATE productos SET nombre = ?, descripcion = ?, categoria_id = ?, " +
                    "proveedor_id = ?, precio_compra = ?, precio_venta = ?, stock_actual = ?, " +
                    "stock_minimo = ?, unidad_medida = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, producto.getNombre());
            stmt.setString(2, producto.getDescripcion());
            stmt.setInt(3, producto.getCategoriaId());
            stmt.setInt(4, producto.getProveedorId());
            stmt.setBigDecimal(5, producto.getPrecioCompra());
            stmt.setBigDecimal(6, producto.getPrecioVenta());
            stmt.setInt(7, producto.getStockActual());
            stmt.setInt(8, producto.getStockMinimo());
            stmt.setString(9, producto.getUnidadMedida());
            stmt.setInt(10, producto.getId());
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
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
        String sql = "UPDATE productos SET activo = false WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public List<Producto> obtenerTodos() {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, pr.nombre as proveedor_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id " +
                    "ORDER BY p.nombre";
        
        return ejecutarConsultaProductos(sql);
    }
    
    @Override
    public List<Producto> obtenerActivos() {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, pr.nombre as proveedor_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id " +
                    "WHERE p.activo = true ORDER BY p.nombre";
        
        return ejecutarConsultaProductos(sql);
    }
    
    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, pr.nombre as proveedor_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id " +
                    "WHERE p.nombre LIKE ? AND p.activo = true ORDER BY p.nombre";
        
        List<Producto> productos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nombre + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar productos por nombre: " + e.getMessage());
        }
        
        return productos;
    }
    
    @Override
    public List<Producto> obtenerConStockBajo() {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, pr.nombre as proveedor_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id " +
                    "WHERE p.stock_actual <= p.stock_minimo AND p.activo = true " +
                    "ORDER BY p.stock_actual ASC";
        
        return ejecutarConsultaProductos(sql);
    }
    
    @Override
    public List<Producto> obtenerConStockCritico() {
        String sql = "SELECT p.*, c.nombre as categoria_nombre, pr.nombre as proveedor_nombre " +
                    "FROM productos p " +
                    "LEFT JOIN categorias c ON p.categoria_id = c.id " +
                    "LEFT JOIN proveedores pr ON p.proveedor_id = pr.id " +
                    "WHERE p.stock_actual <= (p.stock_minimo * 0.5) AND p.activo = true " +
                    "ORDER BY p.stock_actual ASC";
        
        return ejecutarConsultaProductos(sql);
    }
    
    @Override
    public boolean actualizarStock(int productoId, int nuevoStock) {
        String sql = "UPDATE productos SET stock_actual = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, nuevoStock);
            stmt.setInt(2, productoId);
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    @Override
    public boolean existeCodigo(String codigo) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al verificar código: " + e.getMessage());
        }
        return false;
    }
    
    // ===== OPERACIONES CON CATEGORÍAS =====
    
    @Override
    public List<Categoria> obtenerTodasLasCategorias() {
        String sql = "SELECT * FROM categorias WHERE activo = true ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener categorías: " + e.getMessage());
        }
        return categorias;
    }
    
    @Override
    public boolean crearCategoria(Categoria categoria) {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNombre());
            stmt.setString(2, categoria.getDescripcion());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    categoria.setId(rs.getInt(1));
                }
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    // ===== OPERACIONES CON PROVEEDORES =====
    
    @Override
    public List<Proveedor> obtenerTodosLosProveedores() {
        String sql = "SELECT * FROM proveedores WHERE activo = true ORDER BY nombre";
        List<Proveedor> proveedores = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                proveedores.add(mapearProveedor(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener proveedores: " + e.getMessage());
        }
        return proveedores;
    }
    
    @Override
    public boolean crearProveedor(Proveedor proveedor) {
        String sql = "INSERT INTO proveedores (nombre, contacto, telefono, email, direccion, ruc) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, proveedor.getNombre());
            stmt.setString(2, proveedor.getContacto());
            stmt.setString(3, proveedor.getTelefono());
            stmt.setString(4, proveedor.getEmail());
            stmt.setString(5, proveedor.getDireccion());
            stmt.setString(6, proveedor.getRuc());
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    proveedor.setId(rs.getInt(1));
                }
                connection.commit();
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error al crear proveedor: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private List<Producto> ejecutarConsultaProductos(String sql) {
        List<Producto> productos = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error en consulta de productos: " + e.getMessage());
        }
        
        return productos;
    }
    
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getInt("id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setCategoriaId(rs.getInt("categoria_id"));
        producto.setCategoriaNombre(rs.getString("categoria_nombre"));
        producto.setProveedorId(rs.getInt("proveedor_id"));
        producto.setProveedorNombre(rs.getString("proveedor_nombre"));
        producto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        producto.setStockActual(rs.getInt("stock_actual"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setUnidadMedida(rs.getString("unidad_medida"));
        producto.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            producto.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            producto.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }
        
        return producto;
    }
    
    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setId(rs.getInt("id"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            categoria.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return categoria;
    }
    
    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(rs.getInt("id"));
        proveedor.setNombre(rs.getString("nombre"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setEmail(rs.getString("email"));
        proveedor.setDireccion(rs.getString("direccion"));
        proveedor.setRuc(rs.getString("ruc"));
        proveedor.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            proveedor.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            proveedor.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }
        
        return proveedor;
    }
    
    // Implementaciones básicas para métodos restantes
    @Override
    public List<Producto> obtenerPorCategoria(int categoriaId) {
        // Implementar según necesidad
        return new ArrayList<>();
    }
    
    @Override
    public List<Producto> obtenerPorProveedor(int proveedorId) {
        // Implementar según necesidad
        return new ArrayList<>();
    }
    
    @Override
    public List<Producto> obtenerMasVendidos(int limite) {
        // Implementar cuando tengamos módulo de ventas
        return new ArrayList<>();
    }
    
    @Override
    public List<Producto> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        // Implementar según necesidad
        return new ArrayList<>();
    }
    
    @Override
    public boolean ajustarStock(int productoId, int cantidad, String motivo) {
        // Implementar cuando tengamos módulo de inventario
        return false;
    }
    
    @Override
    public int contarProductos() { return 0; }
    @Override
    public int contarProductosActivos() { return 0; }
    @Override
    public Optional<Categoria> obtenerCategoriaPorId(int id) { return Optional.empty(); }
    @Override
    public boolean actualizarCategoria(Categoria categoria) { return false; }
    @Override
    public boolean eliminarCategoria(int id) { return false; }
    @Override
    public Optional<Proveedor> obtenerProveedorPorId(int id) { return Optional.empty(); }
    @Override
    public boolean actualizarProveedor(Proveedor proveedor) { return false; }
    @Override
    public boolean eliminarProveedor(int id) { return false; }
    @Override
    public boolean activar(int id) { return false; }
    @Override
    public boolean desactivar(int id) { return false; }
}