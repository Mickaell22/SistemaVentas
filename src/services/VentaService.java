package services;

import dao.impl.VentaDAOImpl;
import dao.interfaces.IVentaDAO;
import dao.interfaces.IClienteDAO;
import dao.interfaces.IProductoDAO;
import dao.impl.ClienteDAOImpl;
import dao.impl.ProductoDAOImpl;
import models.Venta;
import models.DetalleVenta;
import models.Cliente;
import models.Producto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

public class VentaService {
    
    private IVentaDAO ventaDAO;
    private IClienteDAO clienteDAO;
    private IProductoDAO productoDAO;
    private static VentaService instance;
    
    // Constantes de negocio
    private static final BigDecimal IVA_PORCENTAJE = new BigDecimal("0.12"); // 12% IVA Ecuador
    private static final BigDecimal DESCUENTO_MAXIMO_PORCENTAJE = new BigDecimal("0.50"); // 50% máximo
    private static final int DECIMALES_PRECIO = 2;
    
    private VentaService() {
        this.ventaDAO = new VentaDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.productoDAO = new ProductoDAOImpl();
    }
    
    public static VentaService getInstance() {
        if (instance == null) {
            instance = new VentaService();
        }
        return instance;
    }
    
    // ===== OPERACIONES PRINCIPALES =====
    
    /**
     * Crea una nueva venta con validaciones completas
     */
    public boolean crearVenta(Venta venta) {
        try {
            // Validaciones de negocio
            String validationResult = validarVenta(venta);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Verificar que el cliente existe y está activo
            if (!validarCliente(venta.getClienteId())) {
                System.err.println("Cliente no válido o inactivo");
                return false;
            }
            
            // Validar y preparar detalles
            if (!validarYPrepararDetalles(venta)) {
                System.err.println("Error en validación de detalles");
                return false;
            }
            
            // Calcular totales
            calcularTotalesVenta(venta);
            
            // Crear venta
            boolean resultado = ventaDAO.crear(venta);
            if (resultado) {
                System.out.println("Venta creada exitosamente: " + venta.getNumeroFactura());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al crear venta: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza una venta existente
     */
    public boolean actualizarVenta(Venta venta) {
        try {
            // Verificar que la venta existe
            if (venta.getId() <= 0) {
                System.err.println("ID de venta inválido");
                return false;
            }
            
            Optional<Venta> ventaExistente = ventaDAO.obtenerPorId(venta.getId());
            if (!ventaExistente.isPresent()) {
                System.err.println("Venta no encontrada");
                return false;
            }
            
            // Verificar que se puede modificar (solo PENDIENTE)
            if (!"PENDIENTE".equals(ventaExistente.get().getEstado())) {
                System.err.println("No se puede modificar una venta que no está PENDIENTE");
                return false;
            }
            
            // Validaciones
            String validationResult = validarVentaParaActualizacion(venta);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Recalcular totales
            calcularTotalesVenta(venta);
            
            boolean resultado = ventaDAO.actualizar(venta);
            if (resultado) {
                System.out.println("Venta actualizada: " + venta.getNumeroFactura());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar venta: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Completa una venta (cambia estado a COMPLETADA y actualiza stock)
     */
    public boolean completarVenta(int ventaId) {
        try {
            Optional<Venta> ventaOpt = ventaDAO.obtenerPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                System.err.println("Venta no encontrada");
                return false;
            }
            
            Venta venta = ventaOpt.get();
            
            if (!"PENDIENTE".equals(venta.getEstado())) {
                System.err.println("Solo se pueden completar ventas PENDIENTES");
                return false;
            }
            
            // Verificar que tiene detalles
            if (!ventaDAO.tieneDetalles(ventaId)) {
                System.err.println("La venta no tiene productos");
                return false;
            }
            
            // Verificar stock suficiente
            if (!ventaDAO.verificarStockSuficiente(ventaId)) {
                System.err.println("No hay stock suficiente para completar la venta");
                return false;
            }
            
            boolean resultado = ventaDAO.completarVenta(ventaId);
            if (resultado) {
                System.out.println("Venta completada exitosamente: " + venta.getNumeroFactura());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al completar venta: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Cancela una venta
     */
    public boolean cancelarVenta(int ventaId) {
        try {
            Optional<Venta> ventaOpt = ventaDAO.obtenerPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                System.err.println("Venta no encontrada");
                return false;
            }
            
            Venta venta = ventaOpt.get();
            
            if ("CANCELADA".equals(venta.getEstado())) {
                System.err.println("La venta ya está cancelada");
                return false;
            }
            
            boolean resultado = ventaDAO.cancelarVenta(ventaId);
            if (resultado) {
                System.out.println("Venta cancelada: " + venta.getNumeroFactura());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al cancelar venta: " + e.getMessage());
            return false;
        }
    }
    
    // ===== GESTIÓN DE DETALLES =====
    
    /**
     * Agrega un producto a una venta
     */
    public boolean agregarProductoAVenta(int ventaId, int productoId, int cantidad) {
        try {
            // Verificar que la venta existe y está PENDIENTE
            Optional<Venta> ventaOpt = ventaDAO.obtenerPorId(ventaId);
            if (!ventaOpt.isPresent() || !"PENDIENTE".equals(ventaOpt.get().getEstado())) {
                System.err.println("Venta no encontrada o no se puede modificar");
                return false;
            }
            
            // Verificar que el producto existe
            Optional<Producto> productoOpt = productoDAO.obtenerPorId(productoId);
            if (!productoOpt.isPresent() || !productoOpt.get().isActivo()) {
                System.err.println("Producto no encontrado o inactivo");
                return false;
            }
            
            Producto producto = productoOpt.get();
            
            // Verificar stock
            if (cantidad > producto.getStockActual()) {
                System.err.println("Cantidad solicitada mayor al stock disponible");
                return false;
            }
            
            // Verificar si ya existe el producto en la venta
            List<DetalleVenta> detallesExistentes = ventaDAO.obtenerDetallesPorVenta(ventaId);
            for (DetalleVenta detalle : detallesExistentes) {
                if (detalle.getProductoId() == productoId) {
                    // Actualizar cantidad existente
                    int nuevaCantidad = detalle.getCantidad() + cantidad;
                    if (nuevaCantidad > producto.getStockActual()) {
                        System.err.println("Cantidad total mayor al stock disponible");
                        return false;
                    }
                    detalle.setCantidad(nuevaCantidad);
                    detalle.calcularSubtotal();
                    
                    boolean resultado = ventaDAO.actualizarDetalle(detalle);
                    if (resultado) {
                        ventaDAO.recalcularTotales(ventaId);
                    }
                    return resultado;
                }
            }
            
            // Crear nuevo detalle
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVentaId(ventaId);
            detalle.setProductoId(productoId);
            detalle.setProductoNombre(producto.getNombre());
            detalle.setProductoCodigo(producto.getCodigo());
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setDescuento(BigDecimal.ZERO);
            detalle.calcularSubtotal();
            
            boolean resultado = ventaDAO.agregarDetalle(detalle);
            if (resultado) {
                ventaDAO.recalcularTotales(ventaId);
            }
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al agregar producto a venta: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remueve un producto de una venta
     */
    public boolean removerProductoDeVenta(int detalleId) {
        try {
            // Obtener el detalle para verificar
            List<DetalleVenta> detalles = ventaDAO.obtenerDetallesPorVenta(0); // Necesitaríamos método para obtener por detalle ID
            
            boolean resultado = ventaDAO.eliminarDetalle(detalleId);
            if (resultado) {
                // Recalcular totales de la venta
                // ventaDAO.recalcularTotales(ventaId); // Necesitaríamos el ventaId
            }
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al remover producto de venta: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza la cantidad de un producto en la venta
     */
    public boolean actualizarCantidadProducto(int detalleId, int nuevaCantidad) {
        try {
            if (nuevaCantidad <= 0) {
                return ventaDAO.eliminarDetalle(detalleId);
            }
            
            // Aquí necesitaríamos obtener el detalle por ID para validar stock
            // Por ahora implementación básica
            
            return true; // Implementación pendiente
            
        } catch (Exception e) {
            System.err.println("Error al actualizar cantidad: " + e.getMessage());
            return false;
        }
    }
    
    // ===== CONSULTAS Y BÚSQUEDAS =====
    
    /**
     * Obtiene todas las ventas
     */
    public List<Venta> obtenerTodasLasVentas() {
        return ventaDAO.obtenerTodas();
    }
    
    /**
     * Obtiene ventas por estado
     */
    public List<Venta> obtenerVentasPorEstado(String estado) {
        return ventaDAO.obtenerPorEstado(estado);
    }
    
    /**
     * Obtiene ventas pendientes
     */
    public List<Venta> obtenerVentasPendientes() {
        return ventaDAO.obtenerPorEstado("PENDIENTE");
    }
    
    /**
     * Obtiene ventas completadas
     */
    public List<Venta> obtenerVentasCompletadas() {
        return ventaDAO.obtenerPorEstado("COMPLETADA");
    }
    
    /**
     * Obtiene una venta por ID
     */
    public Optional<Venta> obtenerVentaPorId(int id) {
        return ventaDAO.obtenerPorId(id);
    }
    
    /**
     * Obtiene una venta por número de factura
     */
    public Optional<Venta> obtenerVentaPorNumeroFactura(String numeroFactura) {
        return ventaDAO.obtenerPorNumeroFactura(numeroFactura);
    }
    
    /**
     * Busca ventas por múltiples criterios
     */
    public List<Venta> buscarVentas(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodasLasVentas();
        }
        
        // Buscar por número de factura
        List<Venta> resultados = ventaDAO.buscarPorNumeroFactura(termino.trim());
        
        // Si no encuentra por factura, buscar por cliente
        if (resultados.isEmpty()) {
            resultados = ventaDAO.buscarPorCliente(termino.trim());
        }
        
        return resultados;
    }
    
    /**
     * Obtiene ventas con filtros avanzados
     */
    public List<Venta> obtenerVentasConFiltros(Integer clienteId, Integer usuarioId, String estado, 
                                              String metodoPago, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ventaDAO.obtenerConFiltros(clienteId, usuarioId, estado, metodoPago, fechaInicio, fechaFin);
    }
    
    /**
     * Obtiene ventas del día actual
     */
    public List<Venta> obtenerVentasDelDia() {
        return ventaDAO.obtenerPorFecha(LocalDate.now());
    }
    
    /**
     * Obtiene las últimas ventas
     */
    public List<Venta> obtenerUltimasVentas(int limite) {
        return ventaDAO.obtenerUltimasVentas(limite);
    }
    
    // ===== CÁLCULOS Y UTILIDADES =====
    
    /**
     * Calcula todos los totales de una venta
     */
    public void calcularTotalesVenta(Venta venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            venta.setSubtotal(BigDecimal.ZERO);
            venta.setTotal(BigDecimal.ZERO);
            return;
        }
        
        BigDecimal subtotal = BigDecimal.ZERO;
        
        // Sumar todos los subtotales de los detalles
        for (DetalleVenta detalle : venta.getDetalles()) {
            detalle.calcularSubtotal(); // Asegurar que el subtotal está actualizado
            subtotal = subtotal.add(detalle.getSubtotal());
        }
        
        venta.setSubtotal(subtotal);
        
        // Calcular total = subtotal - descuento + impuestos
        BigDecimal descuento = venta.getDescuento() != null ? venta.getDescuento() : BigDecimal.ZERO;
        BigDecimal impuestos = venta.getImpuestos() != null ? venta.getImpuestos() : BigDecimal.ZERO;
        
        BigDecimal total = subtotal.subtract(descuento).add(impuestos);
        venta.setTotal(total.max(BigDecimal.ZERO)); // No permitir totales negativos
    }
    
    /**
     * Calcula el IVA para una venta
     */
    public BigDecimal calcularIVA(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(IVA_PORCENTAJE).setScale(DECIMALES_PRECIO, RoundingMode.HALF_UP);
    }
    
    /**
     * Aplica descuento a una venta
     */
    public boolean aplicarDescuentoVenta(int ventaId, BigDecimal descuento) {
        try {
            Optional<Venta> ventaOpt = ventaDAO.obtenerPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                return false;
            }
            
            Venta venta = ventaOpt.get();
            
            // Validar descuento
            if (!validarDescuento(venta.getSubtotal(), descuento)) {
                System.err.println("Descuento no válido");
                return false;
            }
            
            venta.setDescuento(descuento);
            calcularTotalesVenta(venta);
            
            return ventaDAO.actualizar(venta);
            
        } catch (Exception e) {
            System.err.println("Error al aplicar descuento: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Genera número de factura único
     */
    public String generarNumeroFactura() {
        return ventaDAO.generarNumeroFactura();
    }
    
    // ===== ESTADÍSTICAS Y REPORTES =====
    
    /**
     * Obtiene estadísticas generales de ventas
     */
    public String getEstadisticasVentas() {
        int totalVentas = ventaDAO.contarVentas();
        int ventasPendientes = ventaDAO.contarVentasPorEstado("PENDIENTE");
        int ventasCompletadas = ventaDAO.contarVentasPorEstado("COMPLETADA");
        int ventasCanceladas = ventaDAO.contarVentasPorEstado("CANCELADA");
        
        BigDecimal totalHoy = ventaDAO.obtenerTotalVentasHoy();
        
        return String.format("Ventas: %d total (%d pendientes, %d completadas, %d canceladas) | Hoy: $%.2f",
                           totalVentas, ventasPendientes, ventasCompletadas, ventasCanceladas, totalHoy);
    }
    
    /**
     * Obtiene total de ventas en un período
     */
    public BigDecimal obtenerTotalVentasPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        return ventaDAO.obtenerTotalVentasPorPeriodo(inicio, fin);
    }
    
    /**
     * Obtiene productos más vendidos
     */
    public List<Object[]> obtenerProductosMasVendidos(LocalDateTime inicio, LocalDateTime fin, int limite) {
        return ventaDAO.obtenerProductosMasVendidos(inicio, fin, limite);
    }
    
    /**
     * Obtiene clientes que más compran
     */
    public List<Object[]> obtenerClientesTopCompradores(int limite) {
        return ventaDAO.obtenerClientesMasCompradores(limite);
    }
    
    // ===== MÉTODOS DE VALIDACIÓN =====
    
    /**
     * Valida una venta para creación
     */
    private String validarVenta(Venta venta) {
        if (venta == null) {
            return "Venta no puede ser null";
        }
        
        if (venta.getClienteId() <= 0) {
            return "Cliente es requerido";
        }
        
        if (venta.getUsuarioId() <= 0) {
            return "Usuario vendedor es requerido";
        }
        
        if (venta.getFechaVenta() == null) {
            return "Fecha de venta es requerida";
        }
        
        if (venta.getFechaVenta().isAfter(LocalDateTime.now())) {
            return "La fecha de venta no puede ser futura";
        }
        
        if (venta.getMetodoPago() == null || venta.getMetodoPago().trim().isEmpty()) {
            return "Método de pago es requerido";
        }
        
        if (!esMetodoPagoValido(venta.getMetodoPago())) {
            return "Método de pago no válido";
        }
        
        if (venta.getEstado() != null && !esEstadoValido(venta.getEstado())) {
            return "Estado de venta no válido";
        }
        
        return "OK";
    }
    
    /**
     * Valida una venta para actualización
     */
    private String validarVentaParaActualizacion(Venta venta) {
        String validacionBasica = validarVenta(venta);
        if (!validacionBasica.equals("OK")) {
            return validacionBasica;
        }
        
        if (venta.getId() <= 0) {
            return "ID de venta es requerido para actualización";
        }
        
        return "OK";
    }
    
    /**
     * Valida que un cliente existe y está activo
     */
    private boolean validarCliente(int clienteId) {
        Optional<Cliente> clienteOpt = clienteDAO.obtenerPorId(clienteId);
        return clienteOpt.isPresent() && clienteOpt.get().isActivo();
    }
    
    /**
     * Valida y prepara los detalles de una venta
     */
    private boolean validarYPrepararDetalles(Venta venta) {
        if (venta.getDetalles() == null) {
            venta.setDetalles(new ArrayList<>());
            return true;
        }
        
        for (DetalleVenta detalle : venta.getDetalles()) {
            // Validar producto
            Optional<Producto> productoOpt = productoDAO.obtenerPorId(detalle.getProductoId());
            if (!productoOpt.isPresent() || !productoOpt.get().isActivo()) {
                System.err.println("Producto no válido: " + detalle.getProductoId());
                return false;
            }
            
            Producto producto = productoOpt.get();
            
            // Validar stock
            if (detalle.getCantidad() > producto.getStockActual()) {
                System.err.println("Stock insuficiente para producto: " + producto.getNombre());
                return false;
            }
            
            // Establecer precio actual del producto si no está definido
            if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                detalle.setPrecioUnitario(producto.getPrecioVenta());
            }
            
            // Calcular subtotal
            detalle.calcularSubtotal();
        }
        
        return true;
    }
    
    /**
     * Valida un descuento
     */
    private boolean validarDescuento(BigDecimal subtotal, BigDecimal descuento) {
        if (descuento == null) {
            return true;
        }
        
        if (descuento.compareTo(BigDecimal.ZERO) < 0) {
            return false; // No descuentos negativos
        }
        
        if (subtotal != null && descuento.compareTo(subtotal) > 0) {
            return false; // Descuento no puede ser mayor al subtotal
        }
        
        // Verificar porcentaje máximo
        if (subtotal != null && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentaje = descuento.divide(subtotal, 4, RoundingMode.HALF_UP);
            if (porcentaje.compareTo(DESCUENTO_MAXIMO_PORCENTAJE) > 0) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Verifica si un método de pago es válido
     */
    private boolean esMetodoPagoValido(String metodoPago) {
        return "EFECTIVO".equals(metodoPago) || 
               "TARJETA".equals(metodoPago) || 
               "TRANSFERENCIA".equals(metodoPago);
    }
    
    /**
     * Verifica si un estado es válido
     */
    private boolean esEstadoValido(String estado) {
        return "PENDIENTE".equals(estado) || 
               "COMPLETADA".equals(estado) || 
               "CANCELADA".equals(estado);
    }
    
    // ===== MÉTODOS DE UTILIDAD =====
    
    /**
     * Obtiene los métodos de pago disponibles
     */
    public String[] getMetodosPago() {
        return new String[]{"EFECTIVO", "TARJETA", "TRANSFERENCIA"};
    }
    
    /**
     * Obtiene los métodos de pago formateados para mostrar
     */
    public String[] getMetodosPagoFormateados() {
        return new String[]{"Efectivo", "Tarjeta", "Transferencia"};
    }
    
    /**
     * Convierte método de pago de formato display a base de datos
     */
    public String convertirMetodoPagoADB(String metodoDisplay) {
        switch (metodoDisplay) {
            case "Efectivo": return "EFECTIVO";
            case "Tarjeta": return "TARJETA";
            case "Transferencia": return "TRANSFERENCIA";
            default: return metodoDisplay;
        }
    }
    
    /**
     * Convierte método de pago de base de datos a formato display
     */
    public String convertirMetodoPagoADisplay(String metodoDB) {
        switch (metodoDB) {
            case "EFECTIVO": return "Efectivo";
            case "TARJETA": return "Tarjeta";
            case "TRANSFERENCIA": return "Transferencia";
            default: return metodoDB;
        }
    }
    
    /**
     * Obtiene los estados de venta disponibles
     */
    public String[] getEstadosVenta() {
        return new String[]{"PENDIENTE", "COMPLETADA", "CANCELADA"};
    }
    
    /**
     * Convierte estado de venta a formato display
     */
    public String convertirEstadoADisplay(String estadoDB) {
        switch (estadoDB) {
            case "PENDIENTE": return "Pendiente";
            case "COMPLETADA": return "Completada";
            case "CANCELADA": return "Cancelada";
            default: return estadoDB;
        }
    }
    
    /**
     * Formatea fecha para mostrar
     */
    public String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    /**
     * Formatea fecha corta
     */
    public String formatearFechaCorta(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    /**
     * Verifica si una venta se puede modificar
     */
    public boolean sePuedeModificar(Venta venta) {
        return venta != null && "PENDIENTE".equals(venta.getEstado());
    }
    
    /**
     * Verifica si una venta se puede completar
     */
    public boolean sePuedeCompletar(Venta venta) {
        return venta != null && 
               "PENDIENTE".equals(venta.getEstado()) && 
               venta.getDetalles() != null && 
               !venta.getDetalles().isEmpty();
    }
    
    /**
     * Verifica si una venta se puede cancelar
     */
    public boolean sePuedeCancelar(Venta venta) {
        return venta != null && !"CANCELADA".equals(venta.getEstado());
    }
    
    /**
     * Obtiene resumen de una venta para mostrar
     */
    public String obtenerResumenVenta(Venta venta) {
        if (venta == null) {
            return "Venta no válida";
        }
        
        return String.format("Factura: %s | Cliente: %s | Total: $%.2f | Estado: %s",
                           venta.getNumeroFactura(),
                           venta.getClienteNombre() != null ? venta.getClienteNombre() : "N/A",
                           venta.getTotal(),
                           convertirEstadoADisplay(venta.getEstado()));
    }
}