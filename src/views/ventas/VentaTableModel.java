package views.ventas;

import models.Venta;
import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentaTableModel extends AbstractTableModel {
    
    private final String[] columnas = {
        "ID", "Nº Factura", "Cliente", "Documento", "Usuario", 
        "Fecha", "Subtotal", "Descuento", "Impuestos", "Total", "Estado", "Método Pago"
    };
    
    private List<Venta> ventas;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public VentaTableModel() {
        this.ventas = new ArrayList<>();
    }
    
    public VentaTableModel(List<Venta> ventas) {
        this.ventas = ventas != null ? ventas : new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return ventas.size();
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
            case 0: return Integer.class;    // ID
            case 1: return String.class;     // Nº Factura
            case 2: return String.class;     // Cliente
            case 3: return String.class;     // Documento
            case 4: return String.class;     // Usuario
            case 5: return String.class;     // Fecha
            case 6: return BigDecimal.class; // Subtotal
            case 7: return BigDecimal.class; // Descuento
            case 8: return BigDecimal.class; // Impuestos
            case 9: return BigDecimal.class; // Total
            case 10: return String.class;    // Estado
            case 11: return String.class;    // Método Pago
            default: return String.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Solo lectura
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= ventas.size()) {
            return null;
        }
        
        Venta venta = ventas.get(row);
        
        switch (column) {
            case 0: return venta.getId();
            case 1: return venta.getNumeroFactura() != null ? venta.getNumeroFactura() : "";
            case 2: return venta.getClienteNombre() != null ? venta.getClienteNombre() : "N/A";
            case 3: return venta.getClienteDocumento() != null ? venta.getClienteDocumento() : "N/A";
            case 4: return venta.getUsuarioNombre() != null ? venta.getUsuarioNombre() : "N/A";
            case 5: return venta.getFechaVenta() != null ? venta.getFechaVenta().format(dateFormatter) : "";
            case 6: return venta.getSubtotal() != null ? venta.getSubtotal() : BigDecimal.ZERO;
            case 7: return venta.getDescuento() != null ? venta.getDescuento() : BigDecimal.ZERO;
            case 8: return venta.getImpuestos() != null ? venta.getImpuestos() : BigDecimal.ZERO;
            case 9: return venta.getTotal() != null ? venta.getTotal() : BigDecimal.ZERO;
            case 10: return convertirEstadoADisplay(venta.getEstado());
            case 11: return convertirMetodoPagoADisplay(venta.getMetodoPago());
            default: return "";
        }
    }
    
    // ===== MÉTODOS PÚBLICOS PARA GESTIONAR DATOS =====
    
    /**
     * Establece la lista completa de ventas
     */
    public void setVentas(List<Venta> ventas) {
        this.ventas = ventas != null ? ventas : new ArrayList<>();
        fireTableDataChanged();
    }
    
    /**
     * Agrega una venta a la tabla
     */
    public void addVenta(Venta venta) {
        if (venta != null) {
            ventas.add(venta);
            fireTableRowsInserted(ventas.size() - 1, ventas.size() - 1);
        }
    }
    
    /**
     * Actualiza una venta en una fila específica
     */
    public void updateVenta(int row, Venta venta) {
        if (row >= 0 && row < ventas.size() && venta != null) {
            ventas.set(row, venta);
            fireTableRowsUpdated(row, row);
        }
    }
    
    /**
     * Elimina una venta de una fila específica
     */
    public void removeVenta(int row) {
        if (row >= 0 && row < ventas.size()) {
            ventas.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    /**
     * Obtiene la venta en una fila específica
     */
    public Venta getVentaAt(int row) {
        if (row >= 0 && row < ventas.size()) {
            return ventas.get(row);
        }
        return null;
    }
    
    /**
     * Obtiene todas las ventas
     */
    public List<Venta> getAllVentas() {
        return new ArrayList<>(ventas);
    }
    
    /**
     * Limpia todos los datos de la tabla
     */
    public void clear() {
        int size = ventas.size();
        if (size > 0) {
            ventas.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    /**
     * Busca una venta por ID y retorna su índice en la tabla
     */
    public int findVentaById(int id) {
        for (int i = 0; i < ventas.size(); i++) {
            if (ventas.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Busca una venta por número de factura y retorna su índice
     */
    public int findVentaByNumeroFactura(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.trim().isEmpty()) {
            return -1;
        }
        
        for (int i = 0; i < ventas.size(); i++) {
            if (numeroFactura.equals(ventas.get(i).getNumeroFactura())) {
                return i;
            }
        }
        return -1;
    }
    
    // ===== MÉTODOS DE FILTRADO =====
    
    /**
     * Filtra ventas por estado
     */
    public List<Venta> getVentasPorEstado(String estado) {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta venta : ventas) {
            if (estado.equals(venta.getEstado())) {
                filtradas.add(venta);
            }
        }
        return filtradas;
    }
    
    /**
     * Filtra ventas por método de pago
     */
    public List<Venta> getVentasPorMetodoPago(String metodoPago) {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta venta : ventas) {
            if (metodoPago.equals(venta.getMetodoPago())) {
                filtradas.add(venta);
            }
        }
        return filtradas;
    }
    
    /**
     * Filtra ventas por cliente
     */
    public List<Venta> getVentasPorCliente(int clienteId) {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta venta : ventas) {
            if (venta.getClienteId() == clienteId) {
                filtradas.add(venta);
            }
        }
        return filtradas;
    }
    
    /**
     * Busca ventas por término (número de factura, cliente, documento)
     */
    public List<Venta> buscarVentas(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return new ArrayList<>(ventas);
        }
        
        String terminoLower = termino.toLowerCase().trim();
        List<Venta> encontradas = new ArrayList<>();
        
        for (Venta venta : ventas) {
            boolean coincide = false;
            
            // Buscar en número de factura
            if (venta.getNumeroFactura() != null && 
                venta.getNumeroFactura().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            // Buscar en nombre del cliente
            if (!coincide && venta.getClienteNombre() != null && 
                venta.getClienteNombre().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            // Buscar en documento del cliente
            if (!coincide && venta.getClienteDocumento() != null && 
                venta.getClienteDocumento().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            // Buscar en usuario
            if (!coincide && venta.getUsuarioNombre() != null && 
                venta.getUsuarioNombre().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            if (coincide) {
                encontradas.add(venta);
            }
        }
        
        return encontradas;
    }
    
    /**
     * Filtra ventas por rango de totales
     */
    public List<Venta> getVentasPorRangoTotal(BigDecimal minimo, BigDecimal maximo) {
        List<Venta> filtradas = new ArrayList<>();
        for (Venta venta : ventas) {
            BigDecimal total = venta.getTotal();
            if (total != null && 
                total.compareTo(minimo) >= 0 && 
                total.compareTo(maximo) <= 0) {
                filtradas.add(venta);
            }
        }
        return filtradas;
    }
    
    // ===== MÉTODOS DE ESTADÍSTICAS =====
    
    /**
     * Obtiene el número total de ventas
     */
    public int getTotalVentas() {
        return ventas.size();
    }
    
    /**
     * Obtiene el número de ventas por estado
     */
    public int getVentasPorEstado(String estado) {
        return (int) ventas.stream()
                .filter(v -> estado.equals(v.getEstado()))
                .count();
    }
    
    /**
     * Obtiene el número de ventas pendientes
     */
    public int getVentasPendientes() {
        return getVentasPorEstado("PENDIENTE");
    }
    
    /**
     * Obtiene el número de ventas completadas
     */
    public int getVentasCompletadas() {
        return getVentasPorEstado("COMPLETADA");
    }
    
    /**
     * Obtiene el número de ventas canceladas
     */
    public int getVentasCanceladas() {
        return getVentasPorEstado("CANCELADA");
    }
    
    /**
     * Calcula el total de todas las ventas completadas
     */
    public BigDecimal getTotalVentasCompletadas() {
        return ventas.stream()
                .filter(v -> "COMPLETADA".equals(v.getEstado()))
                .map(Venta::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calcula el promedio de ventas completadas
     */
    public BigDecimal getPromedioVentasCompletadas() {
        List<Venta> completadas = ventas.stream()
                .filter(v -> "COMPLETADA".equals(v.getEstado()))
                .filter(v -> v.getTotal() != null)
                .toList();
        
        if (completadas.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = completadas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.divide(new BigDecimal(completadas.size()), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Obtiene la venta con mayor total
     */
    public Venta getVentaConMayorTotal() {
        return ventas.stream()
                .filter(v -> v.getTotal() != null)
                .max((v1, v2) -> v1.getTotal().compareTo(v2.getTotal()))
                .orElse(null);
    }
    
    /**
     * Obtiene estadísticas resumidas
     */
    public String getEstadisticasResumidas() {
        int total = getTotalVentas();
        int pendientes = getVentasPendientes();
        int completadas = getVentasCompletadas();
        int canceladas = getVentasCanceladas();
        BigDecimal totalMonto = getTotalVentasCompletadas();
        
        return String.format("Total: %d | Pendientes: %d | Completadas: %d | Canceladas: %d | Monto Total: $%.2f",
                           total, pendientes, completadas, canceladas, totalMonto);
    }
    
    // ===== MÉTODOS DE ORDENAMIENTO =====
    
    /**
     * Ordena ventas por fecha (más recientes primero)
     */
    public void ordenarPorFecha() {
        ventas.sort((v1, v2) -> {
            if (v1.getFechaVenta() == null && v2.getFechaVenta() == null) return 0;
            if (v1.getFechaVenta() == null) return 1;
            if (v2.getFechaVenta() == null) return -1;
            return v2.getFechaVenta().compareTo(v1.getFechaVenta()); // Más recientes primero
        });
        fireTableDataChanged();
    }
    
    /**
     * Ordena ventas por total (mayor a menor)
     */
    public void ordenarPorTotal() {
        ventas.sort((v1, v2) -> {
            BigDecimal total1 = v1.getTotal() != null ? v1.getTotal() : BigDecimal.ZERO;
            BigDecimal total2 = v2.getTotal() != null ? v2.getTotal() : BigDecimal.ZERO;
            return total2.compareTo(total1); // Mayor a menor
        });
        fireTableDataChanged();
    }
    
    /**
     * Ordena ventas por número de factura
     */
    public void ordenarPorNumeroFactura() {
        ventas.sort((v1, v2) -> {
            String num1 = v1.getNumeroFactura() != null ? v1.getNumeroFactura() : "";
            String num2 = v2.getNumeroFactura() != null ? v2.getNumeroFactura() : "";
            return num1.compareToIgnoreCase(num2);
        });
        fireTableDataChanged();
    }
    
    /**
     * Ordena ventas por cliente
     */
    public void ordenarPorCliente() {
        ventas.sort((v1, v2) -> {
            String cliente1 = v1.getClienteNombre() != null ? v1.getClienteNombre() : "";
            String cliente2 = v2.getClienteNombre() != null ? v2.getClienteNombre() : "";
            return cliente1.compareToIgnoreCase(cliente2);
        });
        fireTableDataChanged();
    }
    
    /**
     * Ordena ventas por estado
     */
    public void ordenarPorEstado() {
        ventas.sort((v1, v2) -> {
            String estado1 = v1.getEstado() != null ? v1.getEstado() : "";
            String estado2 = v2.getEstado() != null ? v2.getEstado() : "";
            return estado1.compareToIgnoreCase(estado2);
        });
        fireTableDataChanged();
    }
    
    // ===== MÉTODOS DE EXPORTACIÓN =====
    
    /**
     * Convierte los datos a formato CSV
     */
    public String toCSV() {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("ID,Número Factura,Cliente,Documento Cliente,Usuario,Fecha,Subtotal,Descuento,Impuestos,Total,Estado,Método Pago\n");
        
        // Datos
        for (Venta venta : ventas) {
            csv.append(venta.getId()).append(",");
            csv.append(escaparCSV(venta.getNumeroFactura())).append(",");
            csv.append(escaparCSV(venta.getClienteNombre())).append(",");
            csv.append(escaparCSV(venta.getClienteDocumento())).append(",");
            csv.append(escaparCSV(venta.getUsuarioNombre())).append(",");
            csv.append(escaparCSV(venta.getFechaVenta() != null ? 
                                venta.getFechaVenta().format(dateFormatter) : "")).append(",");
            csv.append(venta.getSubtotal() != null ? venta.getSubtotal() : "0").append(",");
            csv.append(venta.getDescuento() != null ? venta.getDescuento() : "0").append(",");
            csv.append(venta.getImpuestos() != null ? venta.getImpuestos() : "0").append(",");
            csv.append(venta.getTotal() != null ? venta.getTotal() : "0").append(",");
            csv.append(escaparCSV(convertirEstadoADisplay(venta.getEstado()))).append(",");
            csv.append(escaparCSV(convertirMetodoPagoADisplay(venta.getMetodoPago()))).append("\n");
        }
        
        return csv.toString();
    }
    
    /**
     * Convierte a formato de reporte resumido
     */
    public String toReporteResumido() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("REPORTE DE VENTAS\n");
        reporte.append("=================\n\n");
        
        reporte.append("ESTADÍSTICAS GENERALES:\n");
        reporte.append(getEstadisticasResumidas()).append("\n\n");
        
        reporte.append("VENTAS POR ESTADO:\n");
        reporte.append("- Pendientes: ").append(getVentasPendientes()).append("\n");
        reporte.append("- Completadas: ").append(getVentasCompletadas()).append("\n");
        reporte.append("- Canceladas: ").append(getVentasCanceladas()).append("\n\n");
        
        BigDecimal totalMonto = getTotalVentasCompletadas();
        BigDecimal promedio = getPromedioVentasCompletadas();
        
        reporte.append("MONTOS:\n");
        reporte.append("- Total Ventas Completadas: $").append(totalMonto).append("\n");
        reporte.append("- Promedio por Venta: $").append(promedio).append("\n");
        
        Venta ventaMayor = getVentaConMayorTotal();
        if (ventaMayor != null) {
            reporte.append("- Venta Mayor: ").append(ventaMayor.getNumeroFactura())
                   .append(" ($").append(ventaMayor.getTotal()).append(")\n");
        }
        
        return reporte.toString();
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    /**
     * Escapa caracteres especiales para CSV
     */
    private String escaparCSV(String valor) {
        if (valor == null || valor.isEmpty()) {
            return "";
        }
        
        // Si contiene coma, comillas o salto de línea, envolver en comillas
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        
        return valor;
    }
    
    /**
     * Convierte estado de venta a formato display
     */
    private String convertirEstadoADisplay(String estadoDB) {
        if (estadoDB == null) return "";
        switch (estadoDB) {
            case "PENDIENTE": return "Pendiente";
            case "COMPLETADA": return "Completada";
            case "CANCELADA": return "Cancelada";
            default: return estadoDB;
        }
    }
    
    /**
     * Convierte método de pago a formato display
     */
    private String convertirMetodoPagoADisplay(String metodoDB) {
        if (metodoDB == null) return "";
        switch (metodoDB) {
            case "EFECTIVO": return "Efectivo";
            case "TARJETA": return "Tarjeta";
            case "TRANSFERENCIA": return "Transferencia";
            default: return metodoDB;
        }
    }
    
    // ===== MÉTODOS DE VALIDACIÓN =====
    
    /**
     * Verifica si hay datos en la tabla
     */
    public boolean isEmpty() {
        return ventas.isEmpty();
    }
    
    /**
     * Verifica si un índice es válido
     */
    public boolean isValidIndex(int index) {
        return index >= 0 && index < ventas.size();
    }
    
    /**
     * Obtiene la última venta agregada
     */
    public Venta getUltimaVenta() {
        if (ventas.isEmpty()) {
            return null;
        }
        return ventas.get(ventas.size() - 1);
    }
    
    /**
     * Verifica si existe una venta con el número de factura dado
     */
    public boolean existeNumeroFactura(String numeroFactura) {
        return findVentaByNumeroFactura(numeroFactura) != -1;
    }
    
    /**
     * Obtiene información resumida del modelo
     */
    public String getInfoModelo() {
        return String.format("VentaTableModel: %d ventas, %d columnas", 
                           getRowCount(), getColumnCount());
    }
    
    /**
     * Obtiene las ventas del día actual
     */
    public List<Venta> getVentasDeHoy() {
        return ventas.stream()
                .filter(v -> v.getFechaVenta() != null && 
                           v.getFechaVenta().toLocalDate().equals(java.time.LocalDate.now()))
                .toList();
    }
    
    /**
     * Obtiene las ventas de los últimos N días
     */
    public List<Venta> getVentasUltimosDias(int dias) {
        java.time.LocalDate fechaLimite = java.time.LocalDate.now().minusDays(dias);
        return ventas.stream()
                .filter(v -> v.getFechaVenta() != null && 
                           v.getFechaVenta().toLocalDate().isAfter(fechaLimite))
                .toList();
    }
}