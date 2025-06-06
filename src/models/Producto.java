package models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Producto {
    
    private int id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private int categoriaId;
    private String categoriaNombre; // Para joins
    private int proveedorId;
    private String proveedorNombre; // Para joins
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private int stockActual;
    private int stockMinimo;
    private String unidadMedida;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor vacío
    public Producto() {
        this.precioCompra = BigDecimal.ZERO;
        this.precioVenta = BigDecimal.ZERO;
        this.stockActual = 0;
        this.stockMinimo = 0;
        this.unidadMedida = "UNIDAD";
        this.activo = true;
    }
    
    // Constructor para crear nuevo producto
    public Producto(String codigo, String nombre, String descripcion, int categoriaId, 
                   int proveedorId, BigDecimal precioCompra, BigDecimal precioVenta, 
                   int stockActual, int stockMinimo, String unidadMedida) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.proveedorId = proveedorId;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = unidadMedida;
    }
    
    // Constructor completo
    public Producto(int id, String codigo, String nombre, String descripcion, 
                   int categoriaId, String categoriaNombre, int proveedorId, String proveedorNombre,
                   BigDecimal precioCompra, BigDecimal precioVenta, int stockActual, int stockMinimo,
                   String unidadMedida, boolean activo, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = unidadMedida;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }
    
    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }
    
    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }
    
    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal precioCompra) { this.precioCompra = precioCompra; }
    
    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }
    
    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
    
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    // Métodos de utilidad
    public BigDecimal getMargenGanancia() {
        if (precioCompra != null && precioVenta != null && precioCompra.compareTo(BigDecimal.ZERO) > 0) {
            return precioVenta.subtract(precioCompra);
        }
        return BigDecimal.ZERO;
    }
    
    public double getPorcentajeMargen() {
        if (precioCompra != null && precioVenta != null && precioCompra.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margen = getMargenGanancia();
            return margen.divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).doubleValue();
        }
        return 0.0;
    }
    
    public boolean isStockBajo() {
        return stockActual <= stockMinimo;
    }
    
    public boolean isStockCritico() {
        return stockActual <= (stockMinimo * 0.5);
    }
    
    public String getEstadoStock() {
        if (isStockCritico()) return "CRÍTICO";
        if (isStockBajo()) return "BAJO";
        return "NORMAL";
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s (Stock: %d)", codigo, nombre, stockActual);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Producto producto = (Producto) obj;
        return id == producto.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}