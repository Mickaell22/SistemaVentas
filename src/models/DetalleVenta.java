package models;

import java.math.BigDecimal;
import java.math.RoundingMode;



public class DetalleVenta {
    
    private int id;
    private int ventaId;
    private int productoId;
    private String productoNombre; // Para joins
    private String productoCodigo; // Para joins
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal descuento;
    private BigDecimal subtotal;
    
    // Campos adicionales para información del producto (histórico)
    private String productoUnidadMedida; // Para mostrar en factura
    private int productoStockActual; // Para validaciones
    
    // Constructores
    
    /**
     * Constructor vacío
     */
    public DetalleVenta() {
        this.cantidad = 1;
        this.precioUnitario = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }
    
    /**
     * Constructor para nuevo detalle
     */
    public DetalleVenta(int ventaId, int productoId, int cantidad, BigDecimal precioUnitario) {
        this();
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
    
    /**
     * Constructor completo
     */
    public DetalleVenta(int id, int ventaId, int productoId, String productoNombre,
                       String productoCodigo, int cantidad, BigDecimal precioUnitario,
                       BigDecimal descuento, BigDecimal subtotal) {
        this.id = id;
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.productoCodigo = productoCodigo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento;
        this.subtotal = subtotal;
    }
    
    /**
     * Constructor desde producto (para agregar al carrito)
     */
    public DetalleVenta(Producto producto, int cantidad) {
        this();
        this.productoId = producto.getId();
        this.productoNombre = producto.getNombre();
        this.productoCodigo = producto.getCodigo();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta();
        this.productoUnidadMedida = producto.getUnidadMedida();
        this.productoStockActual = producto.getStockActual();
        calcularSubtotal();
    }
    
    // Getters y Setters
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }
    
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    
    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad;
        calcularSubtotal();
    }
    
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { 
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
    
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { 
        this.descuento = descuento;
        calcularSubtotal();
    }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public String getProductoUnidadMedida() { return productoUnidadMedida; }
    public void setProductoUnidadMedida(String productoUnidadMedida) { this.productoUnidadMedida = productoUnidadMedida; }
    
    public int getProductoStockActual() { return productoStockActual; }
    public void setProductoStockActual(int productoStockActual) { this.productoStockActual = productoStockActual; }
    
    // Métodos de utilidad
    
    /**
     * Calcula el subtotal basado en cantidad, precio unitario y descuento
     */
    public void calcularSubtotal() {
        if (precioUnitario != null && cantidad > 0) {
            BigDecimal total = precioUnitario.multiply(new BigDecimal(cantidad));
            
            if (descuento != null) {
                total = total.subtract(descuento);
            }
            
            this.subtotal = total.max(BigDecimal.ZERO); // No permitir negativos
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
    
    /**
     * Obtiene el precio total sin descuento
     */
    public BigDecimal getPrecioTotal() {
        return precioUnitario.multiply(new BigDecimal(cantidad));
    }
    
    /**
     * Verifica si hay suficiente stock para la cantidad solicitada
     */
    public boolean haySuficienteStock() {
        return productoStockActual >= cantidad;
    }
    
    /**
     * Obtiene el stock faltante si no hay suficiente
     */
    public int getStockFaltante() {
        return Math.max(0, cantidad - productoStockActual);
    }
    
    /**
     * Verifica si el detalle tiene descuento aplicado
     */
    public boolean tieneDescuento() {
        return descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Calcula el porcentaje de descuento sobre el precio total
     */
    public double getPorcentajeDescuento() {
        BigDecimal precioTotal = getPrecioTotal();
        if (precioTotal.compareTo(BigDecimal.ZERO) > 0 && tieneDescuento()) {
            return descuento.divide(precioTotal, 4, RoundingMode.HALF_UP)
                           .multiply(new BigDecimal(100)).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * Obtiene el descuento unitario (descuento / cantidad)
     */
    public BigDecimal getDescuentoUnitario() {
        if (cantidad > 0 && tieneDescuento()) {
            return descuento.divide(new BigDecimal(cantidad), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Obtiene el precio unitario con descuento aplicado
     */
    public BigDecimal getPrecioUnitarioConDescuento() {
        return precioUnitario.subtract(getDescuentoUnitario());
    }
    
    /**
     * Obtiene información del producto para mostrar
     */
    public String getProductoInfo() {
        if (productoCodigo != null && productoNombre != null) {
            return productoCodigo + " - " + productoNombre;
        } else if (productoNombre != null) {
            return productoNombre;
        } else if (productoCodigo != null) {
            return productoCodigo;
        } else {
            return "Producto ID: " + productoId;
        }
    }
    
    /**
     * Obtiene información de la cantidad con unidad de medida
     */
    public String getCantidadInfo() {
        if (productoUnidadMedida != null && !productoUnidadMedida.isEmpty()) {
            return cantidad + " " + productoUnidadMedida.toLowerCase();
        } else {
            return String.valueOf(cantidad);
        }
    }
    
    /**
     * Verifica si los datos del detalle son válidos
     */
    public boolean isValido() {
        return productoId > 0 && 
               cantidad > 0 && 
               precioUnitario != null && 
               precioUnitario.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Obtiene un resumen del detalle para mostrar
     */
    public String getResumenDetalle() {
        return String.format("%s x%d = $%.2f", 
                           getProductoInfo(),
                           cantidad,
                           subtotal);
    }
    
    /**
     * Obtiene información detallada para factura
     */
    public String getDetalleFactura() {
        StringBuilder detalle = new StringBuilder();
        detalle.append(getProductoInfo());
        
        if (productoUnidadMedida != null) {
            detalle.append(" (").append(productoUnidadMedida).append(")");
        }
        
        detalle.append(" - ").append(getCantidadInfo());
        detalle.append(" x $").append(precioUnitario);
        
        if (tieneDescuento()) {
            detalle.append(" (Desc: $").append(descuento).append(")");
        }
        
        detalle.append(" = $").append(subtotal);
        
        return detalle.toString();
    }
    
    /**
     * Clona el detalle de venta
     */
    public DetalleVenta clonar() {
        DetalleVenta clon = new DetalleVenta();
        clon.setVentaId(this.ventaId);
        clon.setProductoId(this.productoId);
        clon.setProductoNombre(this.productoNombre);
        clon.setProductoCodigo(this.productoCodigo);
        clon.setCantidad(this.cantidad);
        clon.setPrecioUnitario(this.precioUnitario);
        clon.setDescuento(this.descuento);
        clon.setProductoUnidadMedida(this.productoUnidadMedida);
        clon.setProductoStockActual(this.productoStockActual);
        return clon;
    }
    
    /**
     * Aumenta la cantidad en 1
     */
    public void aumentarCantidad() {
        setCantidad(this.cantidad + 1);
    }
    
    /**
     * Disminuye la cantidad en 1 (mínimo 1)
     */
    public void disminuirCantidad() {
        if (this.cantidad > 1) {
            setCantidad(this.cantidad - 1);
        }
    }
    
    /**
     * Aplica un descuento por porcentaje
     */
    public void aplicarDescuentoPorcentaje(double porcentaje) {
        if (porcentaje >= 0 && porcentaje <= 100) {
            BigDecimal descuentoCalculado = getPrecioTotal()
                .multiply(new BigDecimal(porcentaje))
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            setDescuento(descuentoCalculado);
        }
    }
    
    @Override
    public String toString() {
        return String.format("DetalleVenta{id=%d, producto='%s', cantidad=%d, precio=%.2f, subtotal=%.2f}", 
                           id, getProductoInfo(), cantidad, precioUnitario, subtotal);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DetalleVenta detalle = (DetalleVenta) obj;
        return id == detalle.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}