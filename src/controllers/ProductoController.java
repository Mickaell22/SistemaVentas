package controllers;

import services.ProductoService;
import services.AuthService;
import models.Producto;
import models.Categoria;
import models.Proveedor;
import views.productos.ProductoPanel;
import views.productos.ProductoFormDialog;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductoController {
    
    private ProductoService productoService;
    private AuthService authService;
    private ProductoPanel productoPanel;
    
    public ProductoController() {
        this.productoService = ProductoService.getInstance();
        this.authService = AuthService.getInstance();
    }
    
    public void setProductoPanel(ProductoPanel productoPanel) {
        this.productoPanel = productoPanel;
        cargarDatos();
    }
    
    // ===== OPERACIONES PRINCIPALES =====
    
    public void cargarDatos() {
        if (productoPanel != null) {
            List<Producto> productos = productoService.obtenerProductosActivos();
            productoPanel.actualizarTablaProductos(productos);
            
            // Mostrar alertas de stock si es necesario
            mostrarAlertasStock();
        }
    }
    
    public void buscarProductos(String termino) {
        if (productoPanel != null) {
            List<Producto> productos;
            if (termino == null || termino.trim().isEmpty()) {
                productos = productoService.obtenerProductosActivos();
            } else {
                productos = productoService.buscarProductosPorNombre(termino);
            }
            productoPanel.actualizarTablaProductos(productos);
        }
    }
    
    public void mostrarFormularioNuevoProducto() {
        if (!authService.canManageInventory()) {
            showError("No tiene permisos para crear productos");
            return;
        }
        
        ProductoFormDialog dialog = new ProductoFormDialog(null, this, true);
        dialog.setVisible(true);
    }
    
    public void mostrarFormularioEditarProducto(int productoId) {
        if (!authService.canManageInventory()) {
            showError("No tiene permisos para editar productos");
            return;
        }
        
        Optional<Producto> productoOpt = productoService.obtenerProductoPorId(productoId);
        if (productoOpt.isPresent()) {
            ProductoFormDialog dialog = new ProductoFormDialog(null, this, false);
            dialog.cargarProducto(productoOpt.get());
            dialog.setVisible(true);
        } else {
            showError("Producto no encontrado");
        }
    }
    
    public void eliminarProducto(int productoId) {
        if (!authService.canManageInventory()) {
            showError("No tiene permisos para eliminar productos");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(
            productoPanel,
            "¿Está seguro que desea eliminar este producto?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            boolean resultado = productoService.eliminarProducto(productoId);
            
            if (resultado) {
                showInfo("Producto eliminado exitosamente");
                cargarDatos();
            } else {
                showError("Error al eliminar el producto");
            }
        }
    }
    
    // ===== OPERACIONES DEL FORMULARIO =====
    
    public boolean guardarProducto(Producto producto, boolean esNuevo) {
        try {
            boolean resultado;
            
            if (esNuevo) {
                resultado = productoService.crearProducto(producto);
                if (resultado) {
                    showInfo("Producto creado exitosamente");
                }
            } else {
                resultado = productoService.actualizarProducto(producto);
                if (resultado) {
                    showInfo("Producto actualizado exitosamente");
                }
            }
            
            if (resultado) {
                cargarDatos();
            } else {
                showError("Error al guardar el producto");
            }
            
            return resultado;
            
        } catch (Exception e) {
            showError("Error inesperado: " + e.getMessage());
            return false;
        }
    }
    
    public String generarCodigoProducto() {
        return productoService.generarCodigoProducto();
    }
    
    // ===== GESTIÓN DE CATEGORÍAS =====
    
    public List<Categoria> obtenerCategorias() {
        return productoService.obtenerTodasLasCategorias();
    }
    
    public void mostrarFormularioNuevaCategoria() {
        String nombre = JOptionPane.showInputDialog(
            productoPanel,
            "Ingrese el nombre de la nueva categoría:",
            "Nueva Categoría",
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            String descripcion = JOptionPane.showInputDialog(
                productoPanel,
                "Ingrese la descripción (opcional):",
                "Descripción de Categoría",
                JOptionPane.PLAIN_MESSAGE
            );
            
            Categoria categoria = new Categoria(nombre.trim(), descripcion);
            boolean resultado = productoService.crearCategoria(categoria);
            
            if (resultado) {
                showInfo("Categoría creada exitosamente");
                // Actualizar combo de categorías si el panel lo tiene
                if (productoPanel != null) {
                    productoPanel.actualizarCategorias(obtenerCategorias());
                }
            } else {
                showError("Error al crear la categoría");
            }
        }
    }
    
    // ===== GESTIÓN DE PROVEEDORES =====
    
    public List<Proveedor> obtenerProveedores() {
        return productoService.obtenerTodosLosProveedores();
    }
    
    public void mostrarFormularioNuevoProveedor() {
        // Crear un mini formulario para proveedor
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JTextField txtNombre = new JTextField(20);
        JTextField txtContacto = new JTextField(20);
        JTextField txtTelefono = new JTextField(20);
        JTextField txtEmail = new JTextField(20);
        JTextField txtRuc = new JTextField(20);
        
        panel.add(new JLabel("Nombre:"));
        panel.add(txtNombre);
        panel.add(new JLabel("Contacto:"));
        panel.add(txtContacto);
        panel.add(new JLabel("Teléfono:"));
        panel.add(txtTelefono);
        panel.add(new JLabel("Email:"));
        panel.add(txtEmail);
        panel.add(new JLabel("RUC:"));
        panel.add(txtRuc);
        
        int option = JOptionPane.showConfirmDialog(
            productoPanel,
            panel,
            "Nuevo Proveedor",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (option == JOptionPane.OK_OPTION) {
            String nombre = txtNombre.getText().trim();
            
            if (!nombre.isEmpty()) {
                Proveedor proveedor = new Proveedor(
                    nombre,
                    txtContacto.getText().trim(),
                    txtTelefono.getText().trim(),
                    txtEmail.getText().trim(),
                    "", // dirección vacía por ahora
                    txtRuc.getText().trim()
                );
                
                boolean resultado = productoService.crearProveedor(proveedor);
                
                if (resultado) {
                    showInfo("Proveedor creado exitosamente");
                    // Actualizar combo de proveedores si el panel lo tiene
                    if (productoPanel != null) {
                        productoPanel.actualizarProveedores(obtenerProveedores());
                    }
                } else {
                    showError("Error al crear el proveedor");
                }
            } else {
                showError("El nombre del proveedor es requerido");
            }
        }
    }
    
    // ===== GESTIÓN DE STOCK =====
    
    public void mostrarProductosStockBajo() {
        List<Producto> productos = productoService.obtenerProductosConStockBajo();
        
        if (productos.isEmpty()) {
            showInfo("No hay productos con stock bajo");
        } else {
            StringBuilder mensaje = new StringBuilder("Productos con stock bajo:\n\n");
            for (Producto producto : productos) {
                mensaje.append(String.format("• %s - %s (Stock: %d, Mínimo: %d)\n",
                                            producto.getCodigo(),
                                            producto.getNombre(),
                                            producto.getStockActual(),
                                            producto.getStockMinimo()));
            }
            
            JOptionPane.showMessageDialog(
                productoPanel,
                mensaje.toString(),
                "Alertas de Stock",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
    
    public void actualizarStock(int productoId) {
        Optional<Producto> productoOpt = productoService.obtenerProductoPorId(productoId);
        
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            
            String input = JOptionPane.showInputDialog(
                productoPanel,
                String.format("Stock actual de '%s': %d\nIngrese el nuevo stock:",
                            producto.getNombre(), producto.getStockActual()),
                "Actualizar Stock",
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int nuevoStock = Integer.parseInt(input.trim());
                    
                    if (nuevoStock >= 0) {
                        boolean resultado = productoService.actualizarStock(productoId, nuevoStock);
                        
                        if (resultado) {
                            showInfo("Stock actualizado exitosamente");
                            cargarDatos();
                        } else {
                            showError("Error al actualizar el stock");
                        }
                    } else {
                        showError("El stock no puede ser negativo");
                    }
                    
                } catch (NumberFormatException e) {
                    showError("Ingrese un número válido");
                }
            }
        }
    }
    
    // ===== MÉTODOS DE UTILIDAD =====
    
    private void mostrarAlertasStock() {
        if (productoService.tieneProductosConStockCritico()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    productoPanel,
                    "¡Atención! Hay productos con stock crítico.\nRevise el inventario.",
                    "Alerta de Stock Crítico",
                    JOptionPane.WARNING_MESSAGE
                );
            });
        }
    }
    
    public String obtenerEstadisticas() {
        return productoService.getEstadisticasProductos();
    }
    
    // ===== MÉTODOS DE VALIDACIÓN =====
    
    public boolean validarPrecio(String precioStr) {
        try {
            BigDecimal precio = new BigDecimal(precioStr);
            return precio.compareTo(BigDecimal.ZERO) >= 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean validarStock(String stockStr) {
        try {
            int stock = Integer.parseInt(stockStr);
            return stock >= 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ===== MENSAJES =====
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            productoPanel,
            message,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            productoPanel,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
            productoPanel,
            message,
            "Advertencia",
            JOptionPane.WARNING_MESSAGE
        );
    }
}