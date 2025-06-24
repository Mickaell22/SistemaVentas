package views.clientes;

import models.Cliente;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClienteTableModel extends AbstractTableModel {
    
    private final String[] columnas = {
        "ID", "Tipo Doc", "Documento", "Nombre", "Apellido", 
        "Email", "Teléfono", "Estado", "Fecha Creación"
    };
    
    private List<Cliente> clientes;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public ClienteTableModel() {
        this.clientes = new ArrayList<>();
    }
    
    public ClienteTableModel(List<Cliente> clientes) {
        this.clientes = clientes != null ? clientes : new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return clientes.size();
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
            case 1: return String.class;   // Tipo Doc
            case 2: return String.class;   // Documento
            case 3: return String.class;   // Nombre
            case 4: return String.class;   // Apellido
            case 5: return String.class;   // Email
            case 6: return String.class;   // Teléfono
            case 7: return String.class;   // Estado
            case 8: return String.class;   // Fecha Creación
            default: return String.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Solo lectura
    }
    
    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= clientes.size()) {
            return null;
        }
        
        Cliente cliente = clientes.get(row);
        
        switch (column) {
            case 0: return cliente.getId();
            case 1: return cliente.getTipoDocumentoFormateado();
            case 2: return cliente.getNumeroDocumento();
            case 3: return cliente.getNombre();
            case 4: return cliente.getApellido() != null ? cliente.getApellido() : "";
            case 5: return cliente.getEmail() != null ? cliente.getEmail() : "";
            case 6: return cliente.getTelefono() != null ? cliente.getTelefono() : "";
            case 7: return cliente.getEstadoTexto();
            case 8: return cliente.getFechaCreacion() != null ? 
                          cliente.getFechaCreacion().format(dateFormatter) : "";
            default: return "";
        }
    }
    
    // ===== MÉTODOS PÚBLICOS PARA GESTIONAR DATOS =====
    
    /**
     * Establece la lista completa de clientes
     */
    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes != null ? clientes : new ArrayList<>();
        fireTableDataChanged();
    }
    
    /**
     * Agrega un cliente a la tabla
     */
    public void addCliente(Cliente cliente) {
        if (cliente != null) {
            clientes.add(cliente);
            fireTableRowsInserted(clientes.size() - 1, clientes.size() - 1);
        }
    }
    
    /**
     * Actualiza un cliente en una fila específica
     */
    public void updateCliente(int row, Cliente cliente) {
        if (row >= 0 && row < clientes.size() && cliente != null) {
            clientes.set(row, cliente);
            fireTableRowsUpdated(row, row);
        }
    }
    
    /**
     * Elimina un cliente de una fila específica
     */
    public void removeCliente(int row) {
        if (row >= 0 && row < clientes.size()) {
            clientes.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    /**
     * Obtiene el cliente en una fila específica
     */
    public Cliente getClienteAt(int row) {
        if (row >= 0 && row < clientes.size()) {
            return clientes.get(row);
        }
        return null;
    }
    
    /**
     * Obtiene todos los clientes
     */
    public List<Cliente> getAllClientes() {
        return new ArrayList<>(clientes);
    }
    
    /**
     * Limpia todos los datos de la tabla
     */
    public void clear() {
        int size = clientes.size();
        if (size > 0) {
            clientes.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    /**
     * Busca un cliente por ID y retorna su índice en la tabla
     */
    public int findClienteById(int id) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Busca un cliente por documento y retorna su índice en la tabla
     */
    public int findClienteByDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return -1;
        }
        
        for (int i = 0; i < clientes.size(); i++) {
            if (numeroDocumento.equals(clientes.get(i).getNumeroDocumento())) {
                return i;
            }
        }
        return -1;
    }
    
    // ===== MÉTODOS DE FILTRADO =====
    
    /**
     * Filtra clientes por estado
     */
    public List<Cliente> getClientesPorEstado(boolean activos) {
        List<Cliente> filtrados = new ArrayList<>();
        for (Cliente cliente : clientes) {
            if (cliente.isActivo() == activos) {
                filtrados.add(cliente);
            }
        }
        return filtrados;
    }
    
    /**
     * Filtra clientes por tipo de documento
     */
    public List<Cliente> getClientesPorTipoDocumento(String tipoDocumento) {
        List<Cliente> filtrados = new ArrayList<>();
        for (Cliente cliente : clientes) {
            if (tipoDocumento.equals(cliente.getTipoDocumento())) {
                filtrados.add(cliente);
            }
        }
        return filtrados;
    }
    
    /**
     * Busca clientes por término (nombre, apellido, documento, email)
     */
    public List<Cliente> buscarClientes(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return new ArrayList<>(clientes);
        }
        
        String terminoLower = termino.toLowerCase().trim();
        List<Cliente> encontrados = new ArrayList<>();
        
        for (Cliente cliente : clientes) {
            boolean coincide = false;
            
            // Buscar en nombre
            if (cliente.getNombre() != null && 
                cliente.getNombre().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            // Buscar en apellido
            if (!coincide && cliente.getApellido() != null && 
                cliente.getApellido().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            // Buscar en documento
            if (!coincide && cliente.getNumeroDocumento() != null && 
                cliente.getNumeroDocumento().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            // Buscar en email
            if (!coincide && cliente.getEmail() != null && 
                cliente.getEmail().toLowerCase().contains(terminoLower)) {
                coincide = true;
            }
            
            if (coincide) {
                encontrados.add(cliente);
            }
        }
        
        return encontrados;
    }
    
    // ===== MÉTODOS DE ESTADÍSTICAS =====
    
    /**
     * Obtiene el número total de clientes
     */
    public int getTotalClientes() {
        return clientes.size();
    }
    
    /**
     * Obtiene el número de clientes activos
     */
    public int getClientesActivos() {
        return (int) clientes.stream().filter(Cliente::isActivo).count();
    }
    
    /**
     * Obtiene el número de clientes inactivos
     */
    public int getClientesInactivos() {
        return (int) clientes.stream().filter(c -> !c.isActivo()).count();
    }
    
    /**
     * Obtiene conteo por tipo de documento
     */
    public int getClientesPorTipo(String tipoDocumento) {
        return (int) clientes.stream()
                .filter(c -> tipoDocumento.equals(c.getTipoDocumento()))
                .count();
    }
    
    /**
     * Obtiene estadísticas resumidas
     */
    public String getEstadisticasResumidas() {
        int total = getTotalClientes();
        int activos = getClientesActivos();
        int inactivos = getClientesInactivos();
        int cedulas = getClientesPorTipo("CEDULA");
        int rucs = getClientesPorTipo("RUC");
        int pasaportes = getClientesPorTipo("PASAPORTE");
        
        return String.format("Total: %d | Activos: %d | Inactivos: %d | Cédulas: %d | RUCs: %d | Pasaportes: %d",
                           total, activos, inactivos, cedulas, rucs, pasaportes);
    }
    
    // ===== MÉTODOS DE ORDENAMIENTO =====
    
    /**
     * Ordena clientes por nombre
     */
    public void ordenarPorNombre() {
        clientes.sort((c1, c2) -> {
            String nombre1 = c1.getNombre() != null ? c1.getNombre() : "";
            String nombre2 = c2.getNombre() != null ? c2.getNombre() : "";
            return nombre1.compareToIgnoreCase(nombre2);
        });
        fireTableDataChanged();
    }
    
    /**
     * Ordena clientes por documento
     */
    public void ordenarPorDocumento() {
        clientes.sort((c1, c2) -> {
            String doc1 = c1.getNumeroDocumento() != null ? c1.getNumeroDocumento() : "";
            String doc2 = c2.getNumeroDocumento() != null ? c2.getNumeroDocumento() : "";
            return doc1.compareToIgnoreCase(doc2);
        });
        fireTableDataChanged();
    }
    
    /**
     * Ordena clientes por fecha de creación (más recientes primero)
     */
    public void ordenarPorFechaCreacion() {
        clientes.sort((c1, c2) -> {
            if (c1.getFechaCreacion() == null && c2.getFechaCreacion() == null) return 0;
            if (c1.getFechaCreacion() == null) return 1;
            if (c2.getFechaCreacion() == null) return -1;
            return c2.getFechaCreacion().compareTo(c1.getFechaCreacion()); // Más recientes primero
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
        csv.append("ID,Tipo Documento,Número Documento,Nombre,Apellido,Email,Teléfono,Estado,Fecha Creación\n");
        
        // Datos
        for (Cliente cliente : clientes) {
            csv.append(cliente.getId()).append(",");
            csv.append(escaparCSV(cliente.getTipoDocumentoFormateado())).append(",");
            csv.append(escaparCSV(cliente.getNumeroDocumento())).append(",");
            csv.append(escaparCSV(cliente.getNombre())).append(",");
            csv.append(escaparCSV(cliente.getApellido() != null ? cliente.getApellido() : "")).append(",");
            csv.append(escaparCSV(cliente.getEmail() != null ? cliente.getEmail() : "")).append(",");
            csv.append(escaparCSV(cliente.getTelefono() != null ? cliente.getTelefono() : "")).append(",");
            csv.append(escaparCSV(cliente.getEstadoTexto())).append(",");
            csv.append(escaparCSV(cliente.getFechaCreacion() != null ? 
                                cliente.getFechaCreacion().format(dateFormatter) : "")).append("\n");
        }
        
        return csv.toString();
    }
    
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
    
    // ===== MÉTODOS DE VALIDACIÓN =====
    
    /**
     * Verifica si hay datos en la tabla
     */
    public boolean isEmpty() {
        return clientes.isEmpty();
    }
    
    /**
     * Verifica si un índice es válido
     */
    public boolean isValidIndex(int index) {
        return index >= 0 && index < clientes.size();
    }
    
    /**
     * Obtiene el último cliente agregado
     */
    public Cliente getUltimoCliente() {
        if (clientes.isEmpty()) {
            return null;
        }
        return clientes.get(clientes.size() - 1);
    }
    
    /**
     * Verifica si existe un cliente con el documento dado
     */
    public boolean existeDocumento(String numeroDocumento) {
        return findClienteByDocumento(numeroDocumento) != -1;
    }
    
    /**
     * Obtiene información resumida del modelo
     */
    public String getInfoModelo() {
        return String.format("ClienteTableModel: %d clientes, %d columnas", 
                           getRowCount(), getColumnCount());
    }
}