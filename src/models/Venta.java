package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;



public class Venta {
    
    private int id;
    private String numeroFactura;
    private int clienteId;
    private String clienteNombre; // Para joins
    private String clienteDocumento; // Para joins
    private int usuarioId;
    private String usuarioNombre; // Para joins
    private LocalDateTime fechaVenta;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String estado; // PENDIENTE, COMPLETADA, CANCELADA
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA
    private String observaciones;
    
    // Lista de detalles de la venta
    private List<DetalleVenta> detalles;
    
    // Constructores
    
    /**
     * Constructor vacío
     */
    public Venta() {
        this.subtotal = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
        this.impuestos = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.estado = "PENDIENTE";
        this.metodoPago = "EFECTIVO";
        this.detalles = new ArrayList<>();
    }
    
    /**
     * Constructor para nueva venta
     */
    public Venta(int clienteId, int usuarioId, String metodoPago) {
        this();
        this.clienteId = clienteId;
        this.usuarioId = usuarioId;
        this.metodoPago = metodoPago;
        this.fechaVenta = LocalDateTime.now();
    }
    
    /**
     * Constructor completo
     */
    public Venta(int id, String numeroFactura, int clienteId, String clienteNombre, 
                 String clienteDocumento, int usuarioId, String usuarioNombre,
                 LocalDateTime fechaVenta, BigDecimal subtotal, BigDecimal descuento,
                 BigDecimal impuestos, BigDecimal total, String estado, 
                 String metodoPago, String observaciones) {
        this.id = id;
        this.numeroFactura = numeroFactura;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.clienteDocumento = clienteDocumento;
        this.usuarioId = usuarioId;
        this.usuarioNombre = usuarioNombre;
        this.fechaVenta = fechaVenta;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.impuestos = impuestos;
        this.total = total;
        this.estado = estado;
        this.metodoPago = metodoPago;
        this.observaciones = observaciones;
        this.detalles = new ArrayList<>();
    }
    
    // Getters y Setters
    
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public String getNumeroFactura() { 
        return numeroFactura; 
    }
    
    public void setNumeroFactura(String numeroFactura) { 
        this.numeroFactura = numeroFactura; 
    }
    
    public int getClienteId() { 
        return clienteId; 
    }
    
    public void setClienteId(int clienteId) { 
        this.clienteId = clienteId; 
    }
    
    public String getClienteNombre() { 
        return clienteNombre; 
    }
    
    public void setClienteNombre(String clienteNombre) { 
        this.clienteNombre = clienteNombre; 
    }
    
    public String getClienteDocumento() { 
        return clienteDocumento; 
    }
    
    public void setClienteDocumento(String clienteDocumento) { 
        this.clienteDocumento = clienteDocumento; 
    }
    
    public int getUsuarioId() { 
        return usuarioId; 
    }
    
    public void setUsuarioId(int usuarioId) { 
        this.usuarioId = usuarioId; 
    }
    
    public String getUsuarioNombre() { 
        return usuarioNombre; 
    }
    
    public void setUsuarioNombre(String usuarioNombre) { 
        this.usuarioNombre = usuarioNombre; 
    }
    
    public LocalDateTime getFechaVenta() { 
        return fechaVenta; 
    }
    
    public void setFechaVenta(LocalDateTime fechaVenta) { 
        this.fechaVenta = fechaVenta; 
    }
    
    public BigDecimal getSubtotal() { 
        return subtotal; 
    }
    
    public void setSubtotal(BigDecimal subtotal) { 
        this.subtotal = subtotal; 
    }
    
    public BigDecimal getDescuento() { 
        return descuento; 
    }
    
    public void setDescuento(BigDecimal descuento) { 
        this.descuento = descuento; 
    }
    
    public BigDecimal getImpuestos() { 
        return impuestos; 
    }
    
    public void setImpuestos(BigDecimal impuestos) { 
        this.impuestos = impuestos; 
    }
    
    public BigDecimal getTotal() { 
        return total; 
    }
    
    public void setTotal(BigDecimal total) { 
        this.total = total; 
    }
    
    public String getEstado() { 
        return estado; 
    }
    
    public void setEstado(String estado) { 
        this.estado = estado; 
    }
    
    public String getMetodoPago() { 
        return metodoPago; 
    }
    
    public void setMetodoPago(String metodoPago) { 
        this.metodoPago = metodoPago; 
    }
    
    public String getObservaciones() { 
        return observaciones; 
    }
    
    public void setObservaciones(String observaciones) { 
        this.observaciones = observaciones; 
    }
    
    public List<DetalleVenta> getDetalles() { 
        return detalles; 
    }
    
    public void setDetalles(List<DetalleVenta> detalles) { 
        this.detalles = detalles; 
    }
    
    // Métodos de utilidad
    
    /**
     * Agrega un detalle a la venta
     */
    public void agregarDetalle(DetalleVenta detalle) {
        if (detalle != null) {
            this.detalles.add(detalle);
            recalcularTotales();
        }
    }
    
    /**
     * Remueve un detalle de la venta
     */
    public void removerDetalle(DetalleVenta detalle) {
        if (detalle != null) {
            this.detalles.remove(detalle);
            recalcularTotales();
        }
    }
    
    /**
     * Recalcula los totales de la venta basado en los detalles
     */
    public void recalcularTotales() {
        BigDecimal nuevoSubtotal = BigDecimal.ZERO;
        
        for (DetalleVenta detalle : detalles) {
            if (detalle.getSubtotal() != null) {
                nuevoSubtotal = nuevoSubtotal.add(detalle.getSubtotal());
            }
        }
        
        this.subtotal = nuevoSubtotal;
        
        // Calcular total = subtotal - descuento + impuestos
        BigDecimal descuentoActual = this.descuento != null ? this.descuento : BigDecimal.ZERO;
        BigDecimal impuestosActuales = this.impuestos != null ? this.impuestos : BigDecimal.ZERO;
        
        this.total = this.subtotal.subtract(descuentoActual).add(impuestosActuales);
    }
    
    /**
     * Obtiene la cantidad total de productos en la venta
     */
    public int getCantidadTotalProductos() {
        return detalles.stream().mapToInt(DetalleVenta::getCantidad).sum();
    }
    
    /**
     * Obtiene la cantidad de líneas de productos diferentes
     */
    public int getCantidadLineas() {
        return detalles.size();
    }
    
    /**
     * Verifica si la venta está completada
     */
    public boolean isCompletada() {
        return "COMPLETADA".equals(estado);
    }
    
    /**
     * Verifica si la venta está pendiente
     */
    public boolean isPendiente() {
        return "PENDIENTE".equals(estado);
    }
    
    /**
     * Verifica si la venta está cancelada
     */
    public boolean isCancelada() {
        return "CANCELADA".equals(estado);
    }
    
    /**
     * Obtiene el estado formateado para mostrar
     */
    public String getEstadoFormateado() {
        if (estado == null) return "Desconocido";
        
        switch (estado.toUpperCase()) {
            case "COMPLETADA": return "Completada";
            case "PENDIENTE": return "Pendiente";
            case "CANCELADA": return "Cancelada";
            default: return estado;
        }
    }
    
    /**
     * Obtiene el método de pago formateado
     */
    public String getMetodoPagoFormateado() {
        if (metodoPago == null) return "Efectivo";
        
        switch (metodoPago.toUpperCase()) {
            case "EFECTIVO": return "Efectivo";
            case "TARJETA_CREDITO": return "Tarjeta de Crédito";
            case "TARJETA_DEBITO": return "Tarjeta de Débito";
            case "TARJETA": return "Tarjeta";
            case "TRANSFERENCIA": return "Transferencia";
            case "CHEQUE": return "Cheque";
            default: return metodoPago;
        }
    }
    
    /**
     * Obtiene información del cliente para mostrar
     */
    public String getClienteInfo() {
        if (clienteNombre != null && clienteDocumento != null) {
            return clienteDocumento + " - " + clienteNombre;
        } else if (clienteNombre != null) {
            return clienteNombre;
        } else if (clienteDocumento != null) {
            return clienteDocumento;
        } else {
            return "Cliente ID: " + clienteId;
        }
    }
    
    /**
     * Obtiene un resumen de la venta para mostrar
     */
    public String getResumenVenta() {
        return String.format("Factura %s - %s - Total: $%.2f", 
                           numeroFactura != null ? numeroFactura : "N/A",
                           getClienteInfo(),
                           total != null ? total : BigDecimal.ZERO);
    }
    
    /**
     * Calcula el porcentaje de descuento aplicado
     */
    public double getPorcentajeDescuento() {
        if (subtotal != null && subtotal.compareTo(BigDecimal.ZERO) > 0 && descuento != null) {
            return descuento.divide(subtotal, 4, RoundingMode.HALF_UP)
                           .multiply(new BigDecimal(100)).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Verifica si la venta tiene descuento
     */
    public boolean tieneDescuento() {
        return descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Verifica si la venta tiene impuestos
     */
    public boolean tieneImpuestos() {
        return impuestos != null && impuestos.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Obtiene el total neto (subtotal - descuento, sin impuestos)
     */
    public BigDecimal getTotalNeto() {
        BigDecimal subtotalActual = subtotal != null ? subtotal : BigDecimal.ZERO;
        BigDecimal descuentoActual = descuento != null ? descuento : BigDecimal.ZERO;
        return subtotalActual.subtract(descuentoActual);
    }
    
    /**
     * Verifica si la venta tiene productos
     */
    public boolean tieneProductos() {
        return detalles != null && !detalles.isEmpty();
    }
    
    /**
     * Verifica si la venta es válida para guardar
     */
    public boolean isValidaParaGuardar() {
        return clienteId > 0 && 
               numeroFactura != null && !numeroFactura.trim().isEmpty() &&
               tieneProductos();
    }
    
    /**
     * Obtiene el valor promedio por producto
     */
    public BigDecimal getValorPromedioPorProducto() {
        if (detalles.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalProductos = new BigDecimal(getCantidadTotalProductos());
        BigDecimal totalVenta = total != null ? total : BigDecimal.ZERO;
        
        return totalVenta.divide(totalProductos, 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String toString() {
        return String.format("Venta{id=%d, numeroFactura='%s', cliente='%s', total=%.2f, estado='%s'}", 
                           id, numeroFactura, getClienteInfo(), 
                           total != null ? total : BigDecimal.ZERO, estado);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Venta venta = (Venta) obj;
        return id == venta.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}