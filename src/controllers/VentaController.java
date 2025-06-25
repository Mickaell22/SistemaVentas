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
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public void cargarDatos() {
        if (ventaPanel != null) {
            try {
                List<Venta> ventas = ventaService.obtenerTodasLasVentas();
                ventaPanel.actualizarTablaVentas(ventas);
                actualizarEstadisticas();
            } catch (Exception e) {
                showError("Error al cargar datos: " + e.getMessage());
            }
        }
    }

    public void buscarVentas(String termino) {
        if (ventaPanel != null) {
            try {
                List<Venta> ventas;
                if (termino == null || termino.trim().isEmpty()) {
                    ventas = ventaService.obtenerTodasLasVentas();
                } else {
                    ventas = ventaService.buscarVentas(termino.trim());
                }
                ventaPanel.actualizarTablaVentas(ventas);
            } catch (Exception e) {
                showError("Error en la búsqueda: " + e.getMessage());
            }
        }
    }

    public void mostrarFormularioNuevaVenta() {
        try {
            if (!authService.canMakeSales()) {
                showError("No tiene permisos para realizar ventas");
                return;
            }

            inicializarNuevaVenta();
            VentaFormDialog dialog = new VentaFormDialog(null, this, true);
            dialog.setVisible(true);
        } catch (Exception e) {
            showError("Error al abrir formulario de venta: " + e.getMessage());
        }
    }

    // ===== GESTIÓN DEL CARRITO =====

    public void inicializarNuevaVenta() {
        try {
            this.ventaActual = new Venta();

            String numeroFactura = generarNumeroFacturaSimple();
            this.ventaActual.setNumeroFactura(numeroFactura);

            this.ventaActual.setUsuarioId(authService.getCurrentUser().getId());
            this.ventaActual.setFechaVenta(LocalDateTime.now());
            this.ventaActual.setEstado("PENDIENTE");
            this.ventaActual.setMetodoPago("EFECTIVO");
            this.ventaActual.setDescuento(BigDecimal.ZERO);
            this.ventaActual.setImpuestos(BigDecimal.ZERO);
            this.ventaActual.setSubtotal(BigDecimal.ZERO);
            this.ventaActual.setTotal(BigDecimal.ZERO);

            this.carritoTemporal = new ArrayList<>();
            this.ventaActual.setDetalles(this.carritoTemporal);
        } catch (Exception e) {
            showError("Error al inicializar nueva venta: " + e.getMessage());
        }
    }

    private String generarNumeroFacturaSimple() {
        try {
            return ventaService.generarNumeroFactura();
        } catch (Exception e) {
            long timestamp = System.currentTimeMillis();
            return "FACT" + String.format("%06d", timestamp % 1000000);
        }
    }

    public boolean agregarProductoAlCarrito(String codigoProducto, int cantidad) {
        try {
            Optional<Producto> productoOpt = productoService.obtenerProductoPorCodigo(codigoProducto);
            if (!productoOpt.isPresent()) {
                return false;
            }

            Producto producto = productoOpt.get();

            if (!producto.isActivo()) {
                showError("El producto está inactivo");
                return false;
            }

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

                    showInfo("Cantidad actualizada para: " + producto.getNombre());
                    return true;
                }
            }

            // Crear nuevo detalle
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProductoId(producto.getId());
            detalle.setProductoNombre(producto.getNombre());
            detalle.setProductoCodigo(producto.getCodigo());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.calcularSubtotal();

            carritoTemporal.add(detalle);
            actualizarTotalesCarrito();

            showInfo("Producto agregado: " + producto.getNombre());
            return true;

        } catch (Exception e) {
            showError("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }

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

    public boolean actualizarCantidadEnCarrito(int index, int nuevaCantidad) {
        try {
            if (index < 0 || index >= carritoTemporal.size()) {
                return false;
            }

            if (nuevaCantidad <= 0) {
                return removerProductoDelCarrito(index);
            }

            DetalleVenta detalle = carritoTemporal.get(index);

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

    public void limpiarCarrito() {
        try {
            carritoTemporal.clear();
            actualizarTotalesCarrito();
        } catch (Exception e) {
            showError("Error al limpiar carrito: " + e.getMessage());
        }
    }

    private void actualizarTotalesCarrito() {
        try {
            if (ventaActual != null) {
                recalcularTotalesVenta();
            }
        } catch (Exception e) {
            showError("Error al calcular totales: " + e.getMessage());
        }
    }

    private void recalcularTotalesVenta() {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (DetalleVenta detalle : carritoTemporal) {
            if (detalle.getSubtotal() != null) {
                subtotal = subtotal.add(detalle.getSubtotal());
            }
        }

        ventaActual.setSubtotal(subtotal);

        BigDecimal descuento = ventaActual.getDescuento() != null ? ventaActual.getDescuento() : BigDecimal.ZERO;
        BigDecimal impuestos = ventaActual.getImpuestos() != null ? ventaActual.getImpuestos() : BigDecimal.ZERO;

        BigDecimal total = subtotal.subtract(descuento).add(impuestos);
        ventaActual.setTotal(total);
    }

    public boolean aplicarDescuento(BigDecimal descuento) {
        try {
            if (ventaActual == null) {
                return false;
            }

            if (descuento.compareTo(BigDecimal.ZERO) < 0) {
                showError("El descuento no puede ser negativo");
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

    public boolean aplicarImpuestos(BigDecimal impuestos) {
        try {
            if (ventaActual == null) {
                return false;
            }

            if (impuestos.compareTo(BigDecimal.ZERO) < 0) {
                showError("Los impuestos no pueden ser negativos");
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

    public BigDecimal calcularIVA() {
        try {
            if (ventaActual != null && ventaActual.getSubtotal() != null) {
                BigDecimal porcentajeIVA = new BigDecimal("0.15");
                return ventaActual.getSubtotal().multiply(porcentajeIVA);
            }
            return BigDecimal.ZERO;
        } catch (Exception e) {
            showError("Error al calcular IVA: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ===== OPERACIONES DEL FORMULARIO =====

    public boolean guardarVenta(Venta venta, boolean esNueva) {
        try {
            if (carritoTemporal.isEmpty()) {
                showError("La venta debe tener al menos un producto");
                return false;
            }

            boolean tieneCliente = false;

            if (ventaActual != null && ventaActual.getClienteId() > 0) {
                venta.setClienteId(ventaActual.getClienteId());
                venta.setClienteNombre(ventaActual.getClienteNombre());
                venta.setClienteDocumento(ventaActual.getClienteDocumento());
                tieneCliente = true;
            }

            if (!tieneCliente && venta.getClienteId() > 0) {
                tieneCliente = true;
            }

            if (!tieneCliente) {
                showError("Debe seleccionar un cliente para la venta");
                return false;
            }

            if (venta.getUsuarioId() <= 0 && ventaActual != null) {
                venta.setUsuarioId(ventaActual.getUsuarioId());
            }

            if (venta.getFechaVenta() == null) {
                venta.setFechaVenta(LocalDateTime.now());
            }

            if (venta.getEstado() == null || venta.getEstado().trim().isEmpty()) {
                venta.setEstado("PENDIENTE");
            }

            if (venta.getNumeroFactura() == null || venta.getNumeroFactura().trim().isEmpty()) {
                venta.setNumeroFactura(
                        ventaActual != null ? ventaActual.getNumeroFactura() : generarNumeroFacturaSimple());
            }

            venta.setDetalles(new ArrayList<>(carritoTemporal));

            BigDecimal subtotal = BigDecimal.ZERO;
            for (DetalleVenta detalle : carritoTemporal) {
                detalle.calcularSubtotal();
                subtotal = subtotal.add(detalle.getSubtotal());
            }

            venta.setSubtotal(subtotal);

            BigDecimal descuento = venta.getDescuento() != null ? venta.getDescuento() : BigDecimal.ZERO;
            BigDecimal impuestos = venta.getImpuestos() != null ? venta.getImpuestos() : BigDecimal.ZERO;
            BigDecimal total = subtotal.subtract(descuento).add(impuestos);
            venta.setTotal(total);

            boolean resultado;
            try {
                if (esNueva) {
                    resultado = ventaService.crearVenta(venta);
                    if (resultado) {
                        showInfo("Venta creada exitosamente: " + venta.getNumeroFactura());
                        limpiarCarrito();
                        cargarDatos();
                    }
                } else {
                    resultado = ventaService.actualizarVenta(venta);
                    if (resultado) {
                        showInfo("Venta actualizada exitosamente: " + venta.getNumeroFactura());
                        cargarDatos();
                    }
                }

                if (!resultado) {
                    showError("Error: No se pudo guardar la venta. Verifique los datos.");
                }

            } catch (Exception serviceError) {
                showError("Error del servicio: " + serviceError.getMessage());
                System.err.println("Error detallado al guardar venta: " + serviceError.toString());
                serviceError.printStackTrace();
                resultado = false;
            }

            return resultado;

        } catch (Exception e) {
            showError("Error inesperado al guardar: " + e.getMessage());
            System.err.println("Error inesperado: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

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

    public Optional<Cliente> buscarClientePorDocumento(String numeroDocumento) {
        try {
            if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
                return Optional.empty();
            }
            return clienteService.obtenerClientePorDocumento(numeroDocumento.trim());
        } catch (Exception e) {
            showError("Error al buscar cliente: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===== BÚSQUEDA DE PRODUCTOS =====

    public List<Producto> buscarProductosParaVenta(String termino) {
        try {
            if (termino == null || termino.trim().isEmpty()) {
                return new ArrayList<>();
            }

            List<Producto> todosLosProductos = productoService.obtenerTodosLosProductos();
            List<Producto> productosEncontrados = new ArrayList<>();

            String terminoBusqueda = termino.trim().toLowerCase();

            for (Producto producto : todosLosProductos) {
                boolean coincideNombre = producto.getNombre() != null &&
                        producto.getNombre().toLowerCase().contains(terminoBusqueda);
                boolean coincideCodigo = producto.getCodigo() != null &&
                        producto.getCodigo().toLowerCase().contains(terminoBusqueda);

                if (coincideNombre || coincideCodigo) {
                    productosEncontrados.add(producto);
                }
            }

            return productosEncontrados;

        } catch (Exception e) {
            showError("Error al buscar productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ===== GESTIÓN DE ESTADOS DE VENTA =====

    /**
     * Completa una venta (cambia estado de PENDIENTE a COMPLETADA)
     */
    public boolean completarVenta(int ventaId) {
        try {
            if (!authService.canMakeSales()) {
                showError("No tiene permisos para completar ventas");
                return false;
            }

            Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                showError("Venta no encontrada");
                return false;
            }

            Venta venta = ventaOpt.get();

            if (!ventaService.sePuedeCompletar(venta)) {
                if ("COMPLETADA".equals(venta.getEstado())) {
                    showError("La venta ya está completada");
                } else if ("CANCELADA".equals(venta.getEstado())) {
                    showError("No se puede completar una venta cancelada");
                } else if (!venta.tieneProductos()) {
                    showError("La venta no tiene productos");
                } else {
                    showError("La venta no se puede completar en su estado actual");
                }
                return false;
            }

            boolean resultado = ventaService.completarVenta(ventaId);

            if (resultado) {
                showInfo("Venta completada exitosamente\nFactura: " + venta.getNumeroFactura());
                cargarDatos();
                return true;
            } else {
                showError("Error al completar la venta. Verifique el stock disponible.");
                return false;
            }

        } catch (Exception e) {
            showError("Error inesperado al completar venta: " + e.getMessage());
            System.err.println("Error en completarVenta(): " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancela una venta
     */
    public boolean cancelarVenta(int ventaId) {
        try {
            if (!authService.canMakeSales()) {
                showError("No tiene permisos para cancelar ventas");
                return false;
            }

            Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                showError("Venta no encontrada");
                return false;
            }

            Venta venta = ventaOpt.get();

            if (!ventaService.sePuedeCancelar(venta)) {
                if ("CANCELADA".equals(venta.getEstado())) {
                    showError("La venta ya está cancelada");
                } else {
                    showError("La venta no se puede cancelar");
                }
                return false;
            }

            boolean resultado = ventaService.cancelarVenta(ventaId);

            if (resultado) {
                showInfo("Venta cancelada exitosamente\nFactura: " + venta.getNumeroFactura());
                cargarDatos();
                return true;
            } else {
                showError("Error al cancelar la venta");
                return false;
            }

        } catch (Exception e) {
            showError("Error inesperado al cancelar venta: " + e.getMessage());
            System.err.println("Error en cancelarVenta(): " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifica si una venta se puede completar
     */
    public boolean sePuedeCompletar(int ventaId) {
        try {
            Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                return false;
            }
            return ventaService.sePuedeCompletar(ventaOpt.get());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si una venta se puede cancelar
     */
    public boolean sePuedeCancelar(int ventaId) {
        try {
            Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                return false;
            }
            return ventaService.sePuedeCancelar(ventaOpt.get());
        } catch (Exception e) {
            return false;
        }
    }

    // ===== MÉTODOS DE CONSULTA =====

    public Optional<Venta> obtenerVentaPorId(int ventaId) {
        try {
            return ventaService.obtenerVentaPorId(ventaId);
        } catch (Exception e) {
            System.err.println("Error al obtener venta por ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    public List<Venta> obtenerTodasLasVentas() {
        try {
            return ventaService.obtenerTodasLasVentas();
        } catch (Exception e) {
            System.err.println("Error al obtener todas las ventas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ===== MÉTODOS DE FORMATEO =====

    public String formatearMonto(BigDecimal monto) {
        try {
            if (monto == null) {
                return "$0.00";
            }
            return String.format("$%.2f", monto);
        } catch (Exception e) {
            return "$0.00";
        }
    }

    public String formatearFecha(LocalDateTime fecha) {
        try {
            return ventaService.formatearFecha(fecha);
        } catch (Exception e) {
            return "";
        }
    }

    public String formatearFechaCorta(LocalDateTime fecha) {
        try {
            return ventaService.formatearFechaCorta(fecha);
        } catch (Exception e) {
            return "";
        }
    }

    public String convertirEstadoADisplay(String estadoDB) {
        return ventaService.convertirEstadoADisplay(estadoDB);
    }

    // ===== UTILIDADES =====

    public String[] getMetodosPago() {
        return new String[] { "Efectivo", "Tarjeta de Crédito", "Tarjeta de Débito", "Transferencia", "Cheque" };
    }

    public String convertirMetodoPagoADB(String metodoDisplay) {
        if (metodoDisplay == null)
            return "EFECTIVO";

        switch (metodoDisplay) {
            case "Efectivo":
                return "EFECTIVO";
            case "Tarjeta de Crédito":
                return "TARJETA_CREDITO";
            case "Tarjeta de Débito":
                return "TARJETA_DEBITO";
            case "Transferencia":
                return "TRANSFERENCIA";
            case "Cheque":
                return "CHEQUE";
            default:
                return "EFECTIVO";
        }
    }

    public String convertirMetodoPagoADisplay(String metodoDB) {
        if (metodoDB == null)
            return "Efectivo";

        switch (metodoDB) {
            case "EFECTIVO":
                return "Efectivo";
            case "TARJETA_CREDITO":
                return "Tarjeta de Crédito";
            case "TARJETA_DEBITO":
                return "Tarjeta de Débito";
            case "TRANSFERENCIA":
                return "Transferencia";
            case "CHEQUE":
                return "Cheque";
            default:
                return "Efectivo";
        }
    }

    public String[] getEstadosVenta() {
        return new String[] { "TODOS", "PENDIENTE", "COMPLETADA", "CANCELADA" };
    }

    public Venta getVentaActual() {
        return ventaActual;
    }

    public List<DetalleVenta> getCarritoTemporal() {
        return new ArrayList<>(carritoTemporal);
    }

    public boolean hayProductosEnCarrito() {
        return !carritoTemporal.isEmpty();
    }

    public int getCantidadTotalCarrito() {
        return carritoTemporal.stream().mapToInt(DetalleVenta::getCantidad).sum();
    }

    // ===== MÉTODOS AUXILIARES =====

    public void actualizarEstadisticas() {
        if (ventaPanel != null) {
            try {
                String estadisticas = ventaService.getEstadisticasVentas();
                ventaPanel.actualizarEstadisticas(estadisticas);
            } catch (Exception e) {
                System.err.println("Error al actualizar estadísticas: " + e.getMessage());
            }
        }
    }

    public void filtrarPorEstado(String estado) {
        if (ventaPanel != null) {
            try {
                List<Venta> ventas;
                if (estado == null || estado.equals("TODOS")) {
                    ventas = ventaService.obtenerTodasLasVentas();
                } else {
                    ventas = ventaService.obtenerVentasPorEstado(estado);
                }
                ventaPanel.actualizarTablaVentas(ventas);
            } catch (Exception e) {
                showError("Error al filtrar por estado: " + e.getMessage());
            }
        }
    }

    public void mostrarVentasDelDia() {
        if (ventaPanel != null) {
            try {
                List<Venta> ventas = ventaService.obtenerVentasDelDia();
                ventaPanel.actualizarTablaVentas(ventas);

                if (!ventas.isEmpty()) {
                    showInfo("Mostrando " + ventas.size() + " ventas del día");
                }
            } catch (Exception e) {
                showError("Error al obtener ventas del día: " + e.getMessage());
            }
        }
    }

    public void mostrarVentasPendientes() {
        if (ventaPanel != null) {
            try {
                List<Venta> ventas = ventaService.obtenerVentasPendientes();
                ventaPanel.actualizarTablaVentas(ventas);
            } catch (Exception e) {
                showError("Error al obtener ventas pendientes: " + e.getMessage());
            }
        }
    }

    // ===== MENSAJES =====

    private void showInfo(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    ventaPanel,
                    message,
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    ventaPanel,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }
}