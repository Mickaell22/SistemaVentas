package views.productos;

import models.Producto;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoTableModel extends AbstractTableModel {
    
    private final String[] columnas = {
        "ID", "Código", "Nombre", "Categoría", "Proveedor", 
        "Precio Compra", "Precio Venta", "Stock", "Stock Mín", "Estado", "Margen %"
    };
    
    private List<Producto> productos;
    
    public ProductoTableModel() {
        this.productos = new ArrayList<>();
    }
    
    public ProductoTableModel(List<Producto> productos) {
        this.productos = productos != null ? productos : new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return productos.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnas.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }
    
    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0: return Integer.class;  // ID
            case 1: return String.class;   // Código
            case 2: return String.class;   // Nombre
            case 3: return String.class;   // Categoría
            case 4: return String.class;   // Proveedor
            case 5: return BigDecimal.class; // Precio Compra
            case 6: return BigDecimal.class; // Precio Venta
            case 7: return Integer.class;  // Stock
            case 8: return Integer.class;  // Stock Mín
            case 9: return String.class;   // Estado
            case 10: return String.class;  // Margen %
            default: return String.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Solo lectura
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= productos.size()) {
            return null;
        }
        
        Producto producto = productos.get(row);
        
        switch (column) {
            case 0: return producto.getId();
            case 1: return producto.getCodigo();
            case 2: return producto.getNombre();
            case 3: return producto.getCategoriaNombre();
            case 4: return producto.getProveedorNombre();
            case 5: return producto.getPrecioCompra();
            case 6: return producto.getPrecioVenta();
            case 7: return producto.getStockActual();
            case 8: return producto.getStockMinimo();
            case 9: return producto.getEstadoStock();
            case 10: return String.format("%.1f%%", producto.getPorcentajeMargen());
            default: return "";
        }
    }
    
    // Métodos públicos para gestionar datos
    
    public void setProductos(List<Producto> productos) {
        this.productos = productos != null ? productos : new ArrayList<>();
        fireTableDataChanged();
    }
    
    public void addProducto(Producto producto) {
        if (producto != null) {
            productos.add(producto);
            fireTableRowsInserted(productos.size() - 1, productos.size() - 1);
        }
    }
    
    public void updateProducto(int row, Producto producto) {
        if (row >= 0 && row < productos.size() && producto != null) {
            productos.set(row, producto);
            fireTableRowsUpdated(row, row);
        }
    }
    
    public void removeProducto(int row) {
        if (row >= 0 && row < productos.size()) {
            productos.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    public Producto getProductoAt(int row) {
        if (row >= 0 && row < productos.size()) {
            return productos.get(row);
        }
        return null;
    }
    
    public List<Producto> getAllProductos() {
        return new ArrayList<>(productos);
    }
    
    public void clear() {
        int size = productos.size();
        if (size > 0) {
            productos.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    public int findProductoById(int id) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
    
    public List<Producto> getProductosConStockBajo() {
        List<Producto> stockBajo = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto.isStockBajo()) {
                stockBajo.add(producto);
            }
        }
        return stockBajo;
    }
    
    public int getTotalProductos() {
        return productos.size();
    }
    
    public int getProductosStockBajo() {
        return (int) productos.stream().filter(Producto::isStockBajo).count();
    }
    
    public int getProductosStockCritico() {
        return (int) productos.stream().filter(Producto::isStockCritico).count();
    }
    
    public BigDecimal getValorTotalInventario() {
        return productos.stream()
                .map(p -> p.getPrecioVenta().multiply(new BigDecimal(p.getStockActual())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}