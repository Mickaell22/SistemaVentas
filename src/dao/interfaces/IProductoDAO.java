package dao.interfaces;

import models.Producto;
import models.Categoria;
import models.Proveedor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IProductoDAO {
    
    // Operaciones CRUD básicas
    boolean crear(Producto producto);
    Optional<Producto> obtenerPorId(int id);
    boolean actualizar(Producto producto);
    boolean eliminar(int id);
    List<Producto> obtenerTodos();
    
    // Búsquedas específicas
    Optional<Producto> obtenerPorCodigo(String codigo);
    List<Producto> buscarPorNombre(String nombre);
    List<Producto> obtenerPorCategoria(int categoriaId);
    List<Producto> obtenerPorProveedor(int proveedorId);
    List<Producto> obtenerActivos();
    
    // Gestión de stock
    List<Producto> obtenerConStockBajo();
    List<Producto> obtenerConStockCritico();
    boolean actualizarStock(int productoId, int nuevoStock);
    boolean ajustarStock(int productoId, int cantidad, String motivo);
    
    // Consultas de negocio
    List<Producto> obtenerMasVendidos(int limite);
    List<Producto> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax);
    int contarProductos();
    int contarProductosActivos();
    boolean existeCodigo(String codigo);
    
    // Operaciones con categorías
    List<Categoria> obtenerTodasLasCategorias();
    Optional<Categoria> obtenerCategoriaPorId(int id);
    boolean crearCategoria(Categoria categoria);
    boolean actualizarCategoria(Categoria categoria);
    boolean eliminarCategoria(int id);
    
    // Operaciones con proveedores
    List<Proveedor> obtenerTodosLosProveedores();
    Optional<Proveedor> obtenerProveedorPorId(int id);
    boolean crearProveedor(Proveedor proveedor);
    boolean actualizarProveedor(Proveedor proveedor);
    boolean eliminarProveedor(int id);
    
    // Estado y activación
    boolean activar(int id);
    boolean desactivar(int id);
}