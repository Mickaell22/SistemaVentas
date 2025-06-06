package services;

import dao.impl.ProductoDAOImpl;
import dao.interfaces.IProductoDAO;
import models.Producto;
import models.Categoria;
import models.Proveedor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductoService {
    
    private IProductoDAO productoDAO;
    private static ProductoService instance;
    
    private ProductoService() {
        this.productoDAO = new ProductoDAOImpl();
    }
    
    public static ProductoService getInstance() {
        if (instance == null) {
            instance = new ProductoService();
        }
        return instance;
    }
    
    // ===== OPERACIONES CON PRODUCTOS =====
    
    public boolean crearProducto(Producto producto) {
        try {
            // Validaciones
            String validationResult = validarProducto(producto);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Verificar código único
            if (productoDAO.existeCodigo(producto.getCodigo())) {
                System.err.println("El código ya existe: " + producto.getCodigo());
                return false;
            }
            
            // Crear producto
            boolean resultado = productoDAO.crear(producto);
            if (resultado) {
                System.out.println("Producto creado exitosamente: " + producto.getCodigo());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        }
    }
    
    public boolean actualizarProducto(Producto producto) {
        try {
            // Validaciones
            if (producto.getId() <= 0) {
                System.err.println("ID de producto inválido");
                return false;
            }
            
            String validationResult = validarProductoParaActualizacion(producto);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            boolean resultado = productoDAO.actualizar(producto);
            if (resultado) {
                System.out.println("Producto actualizado: " + producto.getCodigo());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }
    
    public boolean eliminarProducto(int id) {
        try {
            boolean resultado = productoDAO.eliminar(id);
            if (resultado) {
                System.out.println("Producto eliminado: " + id);
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            return false;
        }
    }
    
    public List<Producto> obtenerTodosLosProductos() {
        return productoDAO.obtenerTodos();
    }
    
    public List<Producto> obtenerProductosActivos() {
        return productoDAO.obtenerActivos();
    }
    
    public Optional<Producto> obtenerProductoPorId(int id) {
        return productoDAO.obtenerPorId(id);
    }
    
    public Optional<Producto> obtenerProductoPorCodigo(String codigo) {
        return productoDAO.obtenerPorCodigo(codigo);
    }
    
    public List<Producto> buscarProductosPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return obtenerProductosActivos();
        }
        return productoDAO.buscarPorNombre(nombre.trim());
    }
    
    // ===== GESTIÓN DE STOCK =====
    
    public List<Producto> obtenerProductosConStockBajo() {
        return productoDAO.obtenerConStockBajo();
    }
    
    public List<Producto> obtenerProductosConStockCritico() {
        return productoDAO.obtenerConStockCritico();
    }
    
    public boolean actualizarStock(int productoId, int nuevoStock) {
        if (nuevoStock < 0) {
            System.err.println("El stock no puede ser negativo");
            return false;
        }
        
        boolean resultado = productoDAO.actualizarStock(productoId, nuevoStock);
        if (resultado) {
            System.out.println("Stock actualizado para producto ID: " + productoId + " -> " + nuevoStock);
        }
        
        return resultado;
    }
    
    public String generarCodigoProducto() {
        // Generar código único basado en timestamp
        long timestamp = System.currentTimeMillis();
        String codigo = "PROD" + String.valueOf(timestamp).substring(6);
        
        // Verificar que no exista
        while (productoDAO.existeCodigo(codigo)) {
            timestamp++;
            codigo = "PROD" + String.valueOf(timestamp).substring(6);
        }
        
        return codigo;
    }
    
    // ===== OPERACIONES CON CATEGORÍAS =====
    
    public List<Categoria> obtenerTodasLasCategorias() {
        return productoDAO.obtenerTodasLasCategorias();
    }
    
    public boolean crearCategoria(Categoria categoria) {
        try {
            // Validaciones
            if (categoria.getNombre() == null || categoria.getNombre().trim().isEmpty()) {
                System.err.println("El nombre de la categoría es requerido");
                return false;
            }
            
            boolean resultado = productoDAO.crearCategoria(categoria);
            if (resultado) {
                System.out.println("Categoría creada: " + categoria.getNombre());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            return false;
        }
    }
    
    // ===== OPERACIONES CON PROVEEDORES =====
    
    public List<Proveedor> obtenerTodosLosProveedores() {
        return productoDAO.obtenerTodosLosProveedores();
    }
    
    public boolean crearProveedor(Proveedor proveedor) {
        try {
            // Validaciones
            String validationResult = validarProveedor(proveedor);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            boolean resultado = productoDAO.crearProveedor(proveedor);
            if (resultado) {
                System.out.println("Proveedor creado: " + proveedor.getNombre());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al crear proveedor: " + e.getMessage());
            return false;
        }
    }
    
    // ===== MÉTODOS DE VALIDACIÓN =====
    
    private String validarProducto(Producto producto) {
        if (producto == null) {
            return "Producto no puede ser null";
        }
        
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            return "Código es requerido";
        }
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            return "Nombre es requerido";
        }
        
        if (producto.getCategoriaId() <= 0) {
            return "Categoría es requerida";
        }
        
        if (producto.getProveedorId() <= 0) {
            return "Proveedor es requerido";
        }
        
        if (producto.getPrecioCompra() == null || producto.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0) {
            return "Precio de compra debe ser mayor o igual a 0";
        }
        
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            return "Precio de venta debe ser mayor a 0";
        }
        
        if (producto.getPrecioVenta().compareTo(producto.getPrecioCompra()) < 0) {
            return "Precio de venta debe ser mayor o igual al precio de compra";
        }
        
        if (producto.getStockActual() < 0) {
            return "Stock actual no puede ser negativo";
        }
        
        if (producto.getStockMinimo() < 0) {
            return "Stock mínimo no puede ser negativo";
        }
        
        return "OK";
    }
    
    private String validarProductoParaActualizacion(Producto producto) {
        if (producto == null) {
            return "Producto no puede ser null";
        }
        
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            return "Nombre es requerido";
        }
        
        if (producto.getCategoriaId() <= 0) {
            return "Categoría es requerida";
        }
        
        if (producto.getProveedorId() <= 0) {
            return "Proveedor es requerido";
        }
        
        if (producto.getPrecioCompra() == null || producto.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0) {
            return "Precio de compra debe ser mayor o igual a 0";
        }
        
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            return "Precio de venta debe ser mayor a 0";
        }
        
        if (producto.getPrecioVenta().compareTo(producto.getPrecioCompra()) < 0) {
            return "Precio de venta debe ser mayor o igual al precio de compra";
        }
        
        if (producto.getStockActual() < 0) {
            return "Stock actual no puede ser negativo";
        }
        
        if (producto.getStockMinimo() < 0) {
            return "Stock mínimo no puede ser negativo";
        }
        
        return "OK";
    }
    
    private String validarProveedor(Proveedor proveedor) {
        if (proveedor == null) {
            return "Proveedor no puede ser null";
        }
        
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            return "Nombre es requerido";
        }
        
        if (proveedor.getEmail() != null && !proveedor.getEmail().trim().isEmpty() && !proveedor.getEmail().contains("@")) {
            return "Email debe tener formato válido";
        }
        
        return "OK";
    }
    
    // ===== MÉTODOS DE UTILIDAD =====
    
    public String getEstadisticasProductos() {
        List<Producto> todos = obtenerTodosLosProductos();
        List<Producto> activos = obtenerProductosActivos();
        List<Producto> stockBajo = obtenerProductosConStockBajo();
        List<Producto> stockCritico = obtenerProductosConStockCritico();
        
        return String.format("Productos: %d total, %d activos, %d stock bajo, %d stock crítico",
                           todos.size(), activos.size(), stockBajo.size(), stockCritico.size());
    }
    
    public boolean tieneProductosConStockBajo() {
        return !obtenerProductosConStockBajo().isEmpty();
    }
    
    public boolean tieneProductosConStockCritico() {
        return !obtenerProductosConStockCritico().isEmpty();
    }
}