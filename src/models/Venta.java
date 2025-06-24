package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    
    public String getClienteDocumento() { return clienteDocumento; }
    public void setClienteDocumento(String clienteDocumento) { this.clienteDocumento = clienteDocumento; }
    
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    
    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }
    
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    
    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
    
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
            nuevoSubtotal = nuevoSubtotal.add(detalle.getSubtotal());
        }
        
        this.subtotal = nuevoSubtotal;
        
        // Calcular total = subtotal - descuento + impuestos
        this.total = this.subtotal.subtract(this.descuento).add(this.impuestos);
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
        switch (estado) {
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
        switch (metodoPago) {
            case "EFECTIVO": return "Efectivo";
            case "TARJETA": return "Tarjeta";
            case "TRANSFERENCIA": return "Transferencia";
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
                           total);
    }
    
    /**
     * Calcula el porcentaje de descuento aplicado
     */
    public double getPorcentajeDescuento() {
        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            return descuento.divide(subtotal, 4, BigDecimal.ROUND_HALF_UP)
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
        return subtotal.subtract(descuento);
    }
    
    @Override
    public String toString() {
        return String.format("Venta{id=%d, numeroFactura='%s', cliente='%s', total=%.2f, estado='%s'}", 
                           id, numeroFactura, getClienteInfo(), total, estado);
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