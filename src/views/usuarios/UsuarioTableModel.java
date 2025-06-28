package views.usuarios;

import models.Usuario;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UsuarioTableModel extends AbstractTableModel {
    
    private List<Usuario> usuarios;
    private final String[] columnas = {
        "ID", "Nombre Completo", "Username", "Email", "Rol", "Estado", "Último Login"
    };
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public UsuarioTableModel() {
        this.usuarios = new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return usuarios.size();
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= usuarios.size()) {
            return null;
        }
        
        Usuario usuario = usuarios.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return usuario.getId();
            case 1: return usuario.getNombreCompleto();
            case 2: return usuario.getUsername();
            case 3: return usuario.getEmail();
            case 4: return usuario.getRolNombre() != null ? usuario.getRolNombre() : "Sin rol";
            case 5: return usuario.isActivo() ? "✅ Activo" : "❌ Inactivo";
            case 6: return usuario.getUltimoLogin() != null ? 
                        usuario.getUltimoLogin().format(dateFormatter) : "Nunca";
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6: return String.class;
            default: return Object.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Solo lectura
    }
    
    // Métodos específicos del modelo
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios != null ? new ArrayList<>(usuarios) : new ArrayList<>();
        fireTableDataChanged();
    }
    
    public void addUsuario(Usuario usuario) {
        usuarios.add(usuario);
        int row = usuarios.size() - 1;
        fireTableRowsInserted(row, row);
    }
    
    public void updateUsuario(int index, Usuario usuario) {
        if (index >= 0 && index < usuarios.size()) {
            usuarios.set(index, usuario);
            fireTableRowsUpdated(index, index);
        }
    }
    
    public void removeUsuario(int index) {
        if (index >= 0 && index < usuarios.size()) {
            usuarios.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }
    
    public Usuario getUsuarioAt(int index) {
        if (index >= 0 && index < usuarios.size()) {
            return usuarios.get(index);
        }
        return null;
    }
    
    public int findUsuario(int id) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
    
    public List<Usuario> getUsuarios() {
        return new ArrayList<>(usuarios);
    }
    
    public boolean isEmpty() {
        return usuarios.isEmpty();
    }
    
    public void clear() {
        usuarios.clear();
        fireTableDataChanged();
    }
}