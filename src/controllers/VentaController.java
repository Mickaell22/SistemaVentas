package controllers;

import services.VentaService;
import views.ventas.VentaFormDialog;
import views.ventas.VentaPanel;
import services.ClienteService;
import services.ProductoService;
import services.AuthService;
import models.Venta;
import models.DetalleVenta;
import models.Cliente;
import models.Producto;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class VentaController {
    
    private VentaService ventaService;
    private ClienteService clienteService;
    private ProductoService productoService;
    private AuthService authService;
    private VentaPanel ventaPanel;
    
    // Carrito de compras temporal
    private Venta ventaActual;
    private List<DetalleVenta> carritoTemporal;
    
    public VentaController() {
        this.ventaService = VentaService.getInstance();
        this.clienteService = ClienteService.getInstance();
        this.productoService = ProductoService.getInstance();
        this.authService = AuthService.getInstance();
        this.carritoTemporal = new ArrayList<>();
    }
    
    public void setVentaPanel(VentaPanel ventaPanel) {
        this.ventaPanel = ventaPanel;
        cargarDatos();
    }
    
    // ===== OPERACIONES PRINCIPALES =====
    
    /**
     * Carga todos los datos en el panel
     */
    public void cargarDatos() {
        if (ventaPanel != null) {
            List<Venta> ventas = ventaService.obtenerTodasLasVentas();
            ventaPanel.actualizarTablaVentas(ventas);
            actualizarEstadisticas();
        }
    }
    
    /**
     * Busca ventas por término
     */
    public void buscarVentas(String termino) {
        if (ventaPanel != null) {
            List<Venta> ventas = ventaService.buscarVentas(termino);
            ventaPanel.actualizarTablaVentas(ventas);
        }
    }
    
    /**
     * Busca ventas con filtros avanzados
     */
    public void buscarVentasConFiltros(Integer clienteId, Integer usuarioId, String estado, 
                                      String metodoPago, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (ventaPanel != null) {
            List<Venta> ventas = ventaService.obtenerVentasConFiltros(
                clienteId, usuarioId, estado, metodoPago, fechaInicio, fechaFin);
            ventaPanel.actualizarTablaVentas(ventas);
        }
    }
    
    /**
     * Muestra formulario para nueva venta
     */
    public void mostrarFormularioNuevaVenta() {
        if (!authService.canMakeSales()) {
            showError("No tiene permisos para realizar ventas");
            return;
        }
        
        // Inicializar nueva venta
        inicializarNuevaVenta();
        
        VentaFormDialog dialog = new VentaFormDialog(null, this, true);
        dialog.setVisible(true);
    }
    
    /**
     * Muestra formulario para editar venta
     */
    public void mostrarFormularioEditarVenta(int ventaId) {
        if (!authService.canMakeSales()) {
            showError("No tiene permisos para editar ventas");
            return;
        }
        
        Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
        if (ventaOpt.isPresent()) {
            Venta venta = ventaOpt.get();
            
            if (!ventaService.sePuedeModificar(venta)) {
                showError("Solo se pueden editar ventas pendientes");
                return;
            }
            
            this.ventaActual = venta;
            this.carritoTemporal = new ArrayList<>(venta.getDetalles());
            
            VentaFormDialog dialog = new VentaFormDialog(null, this, false);
            dialog.cargarVenta(venta);
            dialog.setVisible(true);
        } else {
            showError("Venta no encontrada");
        }
    }
    
    /**
     * Completa una venta
     */
    public void completarVenta(int ventaId) {
        if (!authService.canMakeSales()) {
            showError("No tiene permisos para completar ventas");
            return;
        }
        
        Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
        if (!ventaOpt.isPresent()) {
            showError("Venta no encontrada");
            return;
        }
        
        Venta venta = ventaOpt.get();
        
        if (!ventaService.sePuedeCompletar(venta)) {
            showError("La venta no se puede completar. Debe estar pendiente y tener productos.");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(
            ventaPanel,
            "¿Está seguro que desea completar la venta:\n" + 
            venta.getNumeroFactura() + " por $" + venta.getTotal() + "?\n\n" +
            "Esto actualizará el stock de los productos.",
            "Confirmar Completar Venta",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            boolean resultado = ventaService.completarVenta(ventaId);
            
            if (resultado) {
                showInfo("Venta completada exitosamente");
                cargarDatos();
            } else {
                showError("Error al completar la venta");
            }
        }
    }
    
    /**
     * Cancela una venta
     */
    public void cancelarVenta(int ventaId) {
        if (!authService.canMakeSales()) {
            showError("No tiene permisos para cancelar ventas");
            return;
        }
        
        Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
        if (!ventaOpt.isPresent()) {
            showError("Venta no encontrada");
            return;
        }
        
        Venta venta = ventaOpt.get();
        
        if (!ventaService.sePuedeCancelar(venta)) {
            showError("La venta ya está cancelada");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(
            ventaPanel,
            "¿Está seguro que desea cancelar la venta:\n" + 
            venta.getNumeroFactura() + "?\n\n" +
            "Esta acción no se puede deshacer.",
            "Confirmar Cancelar Venta",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            boolean resultado = ventaService.cancelarVenta(ventaId);
            
            if (resultado) {
                showInfo("Venta cancelada exitosamente");
                cargarDatos();
            } else {
                showError("Error al cancelar la venta");
            }
        }
    }
    
    // ===== GESTIÓN DEL CARRITO DE COMPRAS =====
    
    /**
     * Inicializa una nueva venta
     */
    public void inicializarNuevaVenta() {
        this.ventaActual = new Venta();
        this.ventaActual.setNumeroFactura(ventaService.generarNumeroFactura());
        this.ventaActual.setUsuarioId(authService.getCurrentUser().getId());
        this.ventaActual.setFechaVenta(LocalDateTime.now());
        this.ventaActual.setEstado("PENDIENTE");
        this.ventaActual.setMetodoPago("EFECTIVO");
        this.ventaActual.setDescuento(BigDecimal.ZERO);
        this.ventaActual.setImpuestos(BigDecimal.ZERO);
        
        this.carritoTemporal = new ArrayList<>();
        this.ventaActual.setDetalles(this.carritoTemporal);
    }
    
    /**
     * Agrega un producto al carrito
     */
    public boolean agregarProductoAlCarrito(String codigoProducto, int cantidad) {
        try {
            // Buscar producto por código
            Optional<Producto> productoOpt = productoService.obtenerProductoPorCodigo(codigoProducto);
            if (!productoOpt.isPresent()) {
                showError("Producto no encontrado: " + codigoProducto);
                return false;
            }
            
            Producto producto = productoOpt.get();
            
            if (!producto.isActivo()) {
                showError("El producto está inactivo");
                return false;
            }
            
            // Verificar stock
            if (cantidad > producto.getStockActual()) {
                showError("Stock insuficiente. Disponible: " + producto.getStockActual());
                return false;
            }
            
            // Verificar si ya existe en el carrito
            for (DetalleVenta detalle : carritoTemporal) {
                if (detalle.getProductoId() == producto.getId()) {
                    int nuevaCantidad = detalle.getCantidad() + cantidad;
                    if (nuevaCantidad > producto.getStockActual()) {
                        showError("Cantidad total excede el stock disponible");
                        return false;
                    }
                    detalle.setCantidad(nuevaCantidad);
                    detalle.calcularSubtotal();
                    actualizarTotalesCarrito();
                    return true;
                }
            }
            
            // Crear nuevo detalle
            DetalleVenta detalle = new DetalleVenta(producto, cantidad);
            carritoTemporal.add(detalle);
            
            actualizarTotalesCarrito();
            return true;
            
        } catch (Exception e) {
            showError("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Agrega un producto al carrito por ID
     */
    public boolean agregarProductoAlCarrito(int productoId, int cantidad) {
        try {
            Optional<Producto> productoOpt = productoService.obtenerProductoPorId(productoId);
            if (!productoOpt.isPresent()) {
                showError("Producto no encontrado");
                return false;
            }
            
            return agregarProductoAlCarrito(productoOpt.get().getCodigo(), cantidad);
            
        } catch (Exception e) {
            showError("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    public boolean actualizarCantidadEnCarrito(int index, int nuevaCantidad) {
        try {
            if (index < 0 || index >= carritoTemporal.size()) {
                return false;
            }
            
            if (nuevaCantidad <= 0) {
                return removerProductoDelCarrito(index);
            }
            
            DetalleVenta detalle = carritoTemporal.get(index);
            
            // Verificar stock
            Optional<Producto> productoOpt = productoService.obtenerProductoPorId(detalle.getProductoId());
            if (!productoOpt.isPresent()) {
                return false;
            }
            
            Producto producto = productoOpt.get();
            if (nuevaCantidad > producto.getStockActual()) {
                showError("Cantidad excede el stock disponible: " + producto.getStockActual());
                return false;
            }
            
            detalle.setCantidad(nuevaCantidad);
            detalle.calcularSubtotal();
            actualizarTotalesCarrito();
            return true;
            
        } catch (Exception e) {
            showError("Error al actualizar cantidad: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remueve un producto del carrito
     */
    public boolean removerProductoDelCarrito(int index) {
        try {
            if (index >= 0 && index < carritoTemporal.size()) {
                carritoTemporal.remove(index);
                actualizarTotalesCarrito();
                return true;
            }
            return false;
            
        } catch (Exception e) {
            showError("Error al remover producto: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Limpia el carrito completamente
     */
    public void limpiarCarrito() {
        carritoTemporal.clear();
        actualizarTotalesCarrito();
    }
    
    /**
     * Actualiza los totales del carrito
     */
    private void actualizarTotalesCarrito() {
        if (ventaActual != null) {
            ventaService.calcularTotalesVenta(ventaActual);
        }
    }
    
    /**
     * Aplica descuento a la venta actual
     */
    public boolean aplicarDescuento(BigDecimal descuento) {
        try {
            if (ventaActual == null) {
                return false;
            }
            
            ventaActual.setDescuento(descuento);
            actualizarTotalesCarrito();
            return true;
            
        } catch (Exception e) {
            showError("Error al aplicar descuento: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Aplica impuestos a la venta actual
     */
    public boolean aplicarImpuestos(BigDecimal impuestos) {
        try {
            if (ventaActual == null) {
                return false;
            }
            
            ventaActual.setImpuestos(impuestos);
            actualizarTotalesCarrito();
            return true;
            
        } catch (Exception e) {
            showError("Error al aplicar impuestos: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calcula IVA automáticamente
     */
    public BigDecimal calcularIVA() {
        if (ventaActual != null && ventaActual.getSubtotal() != null) {
            return ventaService.calcularIVA(ventaActual.getSubtotal());
        }
        return BigDecimal.ZERO;
    }
    
    // ===== OPERACIONES DEL FORMULARIO =====
    
    /**
     * Guarda una venta (crear o actualizar)
     */
    public boolean guardarVenta(Venta venta, boolean esNueva) {
        try {
            // Validar que tiene productos
            if (carritoTemporal.isEmpty()) {
                showError("La venta debe tener al menos un producto");
                return false;
            }
            
            // Asignar detalles del carrito temporal
            venta.setDetalles(new ArrayList<>(carritoTemporal));
            
            boolean resultado;
            
            if (esNueva) {
                resultado = ventaService.crearVenta(venta);
                if (resultado) {
                    showInfo("Venta creada exitosamente: " + venta.getNumeroFactura());
                    limpiarCarrito();
                }
            } else {
                resultado = ventaService.actualizarVenta(venta);
                if (resultado) {
                    showInfo("Venta actualizada exitosamente: " + venta.getNumeroFactura());
                }
            }
            
            if (resultado) {
                cargarDatos();
            } else {
                showError("Error al guardar la venta");
            }
            
            return resultado;
            
        } catch (Exception e) {
            showError("Error inesperado: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Selecciona un cliente para la venta
     */
    public boolean seleccionarCliente(int clienteId) {
        try {
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(clienteId);
            if (!clienteOpt.isPresent()) {
                showError("Cliente no encontrado");
                return false;
            }
            
            Cliente cliente = clienteOpt.get();
            if (!cliente.isActivo()) {
                showError("El cliente está inactivo");
                return false;
            }
            
            if (ventaActual != null) {
                ventaActual.setClienteId(clienteId);
                ventaActual.setClienteNombre(cliente.getNombreCompleto());
                ventaActual.setClienteDocumento(cliente.getNumeroDocumento());
            }
            
            return true;
            
        } catch (Exception e) {
            showError("Error al seleccionar cliente: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Busca cliente por documento
     */
    public Optional<Cliente> buscarClientePorDocumento(String numeroDocumento) {
        return clienteService.obtenerClientePorDocumento(numeroDocumento);
    }
    
    // ===== FILTROS Y BÚSQUEDAS =====
    
    /**
     * Muestra ventas por estado
     */
    public void filtrarPorEstado(String estado) {
        if (ventaPanel != null) {
            List<Venta> ventas;
            if (estado == null || estado.equals("TODOS")) {
                ventas = ventaService.obtenerTodasLasVentas();
            } else {
                ventas = ventaService.obtenerVentasPorEstado(estado);
            }
            ventaPanel.actualizarTablaVentas(ventas);
        }
    }
    
    /**
     * Muestra ventas del día
     */
    public void mostrarVentasDelDia() {
        if (ventaPanel != null) {
            List<Venta> ventas = ventaService.obtenerVentasDelDia();
            ventaPanel.actualizarTablaVentas(ventas);
            
            if (!ventas.isEmpty()) {
                showInfo("Mostrando " + ventas.size() + " ventas del día");
            }
        }
    }
    
    /**
     * Muestra últimas ventas
     */
    public void mostrarUltimasVentas() {
        if (ventaPanel != null) {
            List<Venta> ventas = ventaService.obtenerUltimasVentas(20);
            ventaPanel.actualizarTablaVentas(ventas);
            
            if (!ventas.isEmpty()) {
                showInfo("Mostrando las últimas " + ventas.size() + " ventas");
            }
        }
    }
    
    /**
     * Muestra ventas pendientes
     */
    public void mostrarVentasPendientes() {
        if (ventaPanel != null) {
            List<Venta> ventas = ventaService.obtenerVentasPendientes();
            ventaPanel.actualizarTablaVentas(ventas);
        }
    }
    
    // ===== VALIDACIONES PARA FORMULARIOS =====
    
    /**
     * Valida número de factura único
     */
    public boolean validarNumeroFacturaUnico(String numeroFactura, int ventaIdActual) {
        if (numeroFactura == null || numeroFactura.trim().isEmpty()) {
            return false;
        }
        
        // Para ventas existentes, verificar en otras ventas
        if (ventaIdActual > 0) {
            Optional<Venta> ventaExistente = ventaService.obtenerVentaPorNumeroFactura(numeroFactura);
            return !ventaExistente.isPresent() || ventaExistente.get().getId() == ventaIdActual;
        } else {
            // Para ventas nuevas, verificar que no exista
            return !ventaService.obtenerVentaPorNumeroFactura(numeroFactura).isPresent();
        }
    }
    
    /**
     * Valida formato de cantidad
     */
    public boolean validarCantidad(String cantidadStr) {
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            return cantidad > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Valida formato de precio/monto
     */
    public boolean validarMonto(String montoStr) {
        try {
            BigDecimal monto = new BigDecimal(montoStr);
            return monto.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // ===== UTILIDADES =====
    
    /**
     * Obtiene métodos de pago para ComboBox
     */
    public String[] getMetodosPago() {
        return ventaService.getMetodosPagoFormateados();
    }
    
    /**
     * Convierte método de pago para base de datos
     */
    public String convertirMetodoPagoADB(String metodoDisplay) {
        return ventaService.convertirMetodoPagoADB(metodoDisplay);
    }
    
    /**
     * Convierte método de pago para mostrar
     */
    public String convertirMetodoPagoADisplay(String metodoDB) {
        return ventaService.convertirMetodoPagoADisplay(metodoDB);
    }
    
    /**
     * Obtiene estados de venta
     */
    public String[] getEstadosVenta() {
        return new String[]{"TODOS", "PENDIENTE", "COMPLETADA", "CANCELADA"};
    }
    
    /**
     * Obtiene estadísticas de ventas
     */
    public String obtenerEstadisticas() {
        return ventaService.getEstadisticasVentas();
    }
    
    /**
     * Actualiza las estadísticas en el panel
     */
    private void actualizarEstadisticas() {
        if (ventaPanel != null) {
            String estadisticas = obtenerEstadisticas();
            ventaPanel.actualizarEstadisticas(estadisticas);
        }
    }
    
    /**
     * Obtiene la venta actual en edición
     */
    public Venta getVentaActual() {
        return ventaActual;
    }
    
    /**
     * Obtiene el carrito temporal
     */
    public List<DetalleVenta> getCarritoTemporal() {
        return new ArrayList<>(carritoTemporal);
    }
    
    /**
     * Verifica si hay productos en el carrito
     */
    public boolean hayProductosEnCarrito() {
        return !carritoTemporal.isEmpty();
    }
    
    /**
     * Obtiene cantidad total de productos en carrito
     */
    public int getCantidadTotalCarrito() {
        return carritoTemporal.stream().mapToInt(DetalleVenta::getCantidad).sum();
    }
    
    // ===== BÚSQUEDA DE PRODUCTOS =====
    
    /**
     * Busca productos para agregar a la venta
     */
    public List<Producto> buscarProductosParaVenta(String termino) {
        return productoService.buscarProductosPorNombre(termino);
    }
    
    /**
     * Obtiene productos activos
     */
    public List<Producto> obtenerProductosActivos() {
        return productoService.obtenerProductosActivos();
    }
    
    // ===== REPORTES Y EXPORTACIÓN =====
    
    /**
     * Genera reporte de ventas
     */
    public void generarReporteVentas() {
        if (!authService.canViewReports()) {
            showError("No tiene permisos para generar reportes");
            return;
        }
        
        // TODO: Implementar generación de reporte
        showInfo("Funcionalidad de reportes será implementada en una próxima versión");
    }
    
    /**
     * Exporta ventas a CSV
     */
    public void exportarVentasCSV() {
        if (!authService.canViewReports()) {
            showError("No tiene permisos para exportar datos");
            return;
        }
        
        // TODO: Implementar exportación
        showInfo("Funcionalidad de exportación será implementada en una próxima versión");
    }
    
    /**
     * Imprime factura
     */
    public void imprimirFactura(int ventaId) {
        // TODO: Implementar impresión de factura
        showInfo("Funcionalidad de impresión será implementada en una próxima versión");
    }
    
    // ===== MENSAJES =====
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            ventaPanel,
            message,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            ventaPanel,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
            ventaPanel,
            message,
            "Advertencia",
            JOptionPane.WARNING_MESSAGE
        );
    }
}