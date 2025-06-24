package views.clientes;

import controllers.ClienteController;
import models.Cliente;
import services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ClientePanel extends JPanel {
    
    private ClienteController controller;
    private AuthService authService;
    
    // Componentes principales
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnActivar;
    private JButton btnDesactivar;
    private JButton btnRefresh;
    
    // Filtros
    private JComboBox<String> cmbTipoDocumento;
    private JComboBox<String> cmbEstado;
    private JButton btnFiltrar;
    private JButton btnLimpiarFiltros;
    
    // Tabla
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Información
    private JLabel lblTotalClientes;
    private JLabel lblEstadisticas;
    
    public ClientePanel() {
        this.controller = new ClienteController();
        this.authService = AuthService.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEvents();
        setupTable();
        
        // Conectar el controller con este panel
        controller.setClientePanel(this);
    }
    
    private void initializeComponents() {
        // Campo de búsqueda
        txtBuscar = new JTextField(20);
        txtBuscar.setToolTipText("Buscar por nombre, apellido, documento o email");
        
        // Botones principales
        btnBuscar = new JButton("🔍 Buscar");
        btnNuevo = new JButton("➕ Nuevo");
        btnEditar = new JButton("✏️ Editar");
        btnEliminar = new JButton("🗑️ Eliminar");
        btnActivar = new JButton("✅ Activar");
        btnDesactivar = new JButton("❌ Desactivar");
        btnRefresh = new JButton("🔄 Actualizar");
        
        // Filtros
        String[] tiposDoc = {"Todos", "Cédula", "RUC", "Pasaporte"};
        cmbTipoDocumento = new JComboBox<>(tiposDoc);
        
        String[] estados = {"Solo Activos", "Solo Inactivos", "Todos"};
        cmbEstado = new JComboBox<>(estados);
        
        btnFiltrar = new JButton("🔽 Filtrar");
        btnLimpiarFiltros = new JButton("🧹 Limpiar");
        
        // Configurar botones según permisos
        configurarPermisos();
        
        // Tabla
        String[] columnas = {
            "ID", "Tipo Doc", "Documento", "Nombre", "Apellido", 
            "Email", "Teléfono", "Estado", "Fecha Creación"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };
        
        tablaClientes = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaClientes.setRowSorter(sorter);
        
        // Labels informativos
        lblTotalClientes = new JLabel("Total: 0 clientes");
        lblEstadisticas = new JLabel("Cargando...");
        
        setupTableStyles();
    }
    
    private void configurarPermisos() {
        boolean canManage = authService.canManageUsers();
        
        btnNuevo.setEnabled(canManage);
        btnEditar.setEnabled(canManage);
        btnEliminar.setEnabled(canManage);
        btnActivar.setEnabled(canManage);
        btnDesactivar.setEnabled(canManage);
        
        if (!canManage) {
            btnNuevo.setToolTipText("Sin permisos para gestionar clientes");
            btnEditar.setToolTipText("Sin permisos para gestionar clientes");
            btnEliminar.setToolTipText("Sin permisos para gestionar clientes");
            btnActivar.setToolTipText("Sin permisos para gestionar clientes");
            btnDesactivar.setToolTipText("Sin permisos para gestionar clientes");
        }
    }
    
    private void setupTableStyles() {
        // Configurar tabla
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.setRowHeight(25);
        tablaClientes.setShowGrid(true);
        tablaClientes.setGridColor(new Color(230, 230, 230));
        
        // Colores básicos
        tablaClientes.setBackground(Color.WHITE);
        tablaClientes.setSelectionBackground(new Color(0, 123, 255));
        tablaClientes.setSelectionForeground(Color.WHITE);
        
        // Header
        tablaClientes.getTableHeader().setBackground(new Color(52, 58, 64));
        tablaClientes.getTableHeader().setForeground(Color.WHITE);
        tablaClientes.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Anchos de columnas
        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(80);  // Tipo Doc
        tablaClientes.getColumnModel().getColumn(2).setPreferredWidth(120); // Documento
        tablaClientes.getColumnModel().getColumn(3).setPreferredWidth(150); // Nombre
        tablaClientes.getColumnModel().getColumn(4).setPreferredWidth(150); // Apellido
        tablaClientes.getColumnModel().getColumn(5).setPreferredWidth(200); // Email
        tablaClientes.getColumnModel().getColumn(6).setPreferredWidth(100); // Teléfono
        tablaClientes.getColumnModel().getColumn(7).setPreferredWidth(80);  // Estado
        tablaClientes.getColumnModel().getColumn(8).setPreferredWidth(120); // Fecha
        
        // Ocultar columna ID
        tablaClientes.getColumnModel().getColumn(0).setMinWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(0);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Título y búsqueda
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Título
        JLabel lblTitulo = new JLabel("Gestión de Clientes");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(52, 58, 64));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        
        // Panel de búsqueda y filtros
        JPanel panelBusquedaFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBusquedaFiltros.add(new JLabel("Buscar:"));
        panelBusquedaFiltros.add(txtBuscar);
        panelBusquedaFiltros.add(btnBuscar);
        panelBusquedaFiltros.add(new JSeparator(SwingConstants.VERTICAL));
        panelBusquedaFiltros.add(new JLabel("Tipo:"));
        panelBusquedaFiltros.add(cmbTipoDocumento);
        panelBusquedaFiltros.add(new JLabel("Estado:"));
        panelBusquedaFiltros.add(cmbEstado);
        panelBusquedaFiltros.add(btnFiltrar);
        panelBusquedaFiltros.add(btnLimpiarFiltros);
        
        panelSuperior.add(panelBusquedaFiltros, BorderLayout.EAST);
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central - Tabla
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Clientes"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior - Botones e información
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.add(btnNuevo);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL));
        panelBotones.add(btnActivar);
        panelBotones.add(btnDesactivar);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL));
        panelBotones.add(btnRefresh);
        
        panelInferior.add(panelBotones, BorderLayout.WEST);
        
        // Información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInfo.add(lblTotalClientes);
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(lblEstadisticas);
        
        panelInferior.add(panelInfo, BorderLayout.EAST);
        
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void setupEvents() {
        // Botón búsqueda
        btnBuscar.addActionListener(e -> buscarClientes());
        
        // Enter en campo de búsqueda
        txtBuscar.addActionListener(e -> buscarClientes());
        
        // Botones principales
        btnNuevo.addActionListener(e -> controller.mostrarFormularioNuevoCliente());
        btnEditar.addActionListener(e -> editarClienteSeleccionado());
        btnEliminar.addActionListener(e -> eliminarClienteSeleccionado());
        btnActivar.addActionListener(e -> activarClienteSeleccionado());
        btnDesactivar.addActionListener(e -> desactivarClienteSeleccionado());
        btnRefresh.addActionListener(e -> controller.cargarDatos());
        
        // Filtros
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());
        
        // Doble clic en tabla para editar
        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && authService.canManageUsers()) {
                    editarClienteSeleccionado();
                }
            }
        });
        
        // Cambio de selección en tabla
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean haySeleccion = tablaClientes.getSelectedRow() != -1;
                boolean canManage = authService.canManageUsers();
                
                btnEditar.setEnabled(haySeleccion && canManage);
                btnEliminar.setEnabled(haySeleccion && canManage);
                
                // Activar/Desactivar según estado del cliente
                if (haySeleccion && canManage) {
                    String estado = getEstadoClienteSeleccionado();
                    btnActivar.setEnabled("Inactivo".equals(estado));
                    btnDesactivar.setEnabled("Activo".equals(estado));
                } else {
                    btnActivar.setEnabled(false);
                    btnDesactivar.setEnabled(false);
                }
            }
        });
    }
    
    private void setupTable() {
        // Configurar renderer personalizado para el estado
        tablaClientes.getColumnModel().getColumn(7).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString());
            label.setOpaque(true);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else {
                // Filas alternadas
                if (row % 2 == 0) {
                    label.setBackground(Color.WHITE);
                } else {
                    label.setBackground(new Color(248, 249, 250));
                }
                
                // Colorear según estado
                String estado = value.toString();
                if ("Activo".equals(estado)) {
                    label.setBackground(new Color(212, 237, 218));
                    label.setForeground(new Color(21, 87, 36));
                } else if ("Inactivo".equals(estado)) {
                    label.setBackground(new Color(248, 215, 218));
                    label.setForeground(new Color(114, 28, 36));
                }
            }
            
            return label;
        });
        
        // Renderer para todas las demás columnas (filas alternadas)
        for (int i = 0; i < tablaClientes.getColumnCount(); i++) {
            if (i != 7) { // Excepto la columna de estado
                final int columnIndex = i;
                tablaClientes.getColumnModel().getColumn(i).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    JLabel label = new JLabel(value != null ? value.toString() : "");
                    label.setOpaque(true);
                    
                    if (isSelected) {
                        label.setBackground(table.getSelectionBackground());
                        label.setForeground(table.getSelectionForeground());
                    } else {
                        // Filas alternadas
                        if (row % 2 == 0) {
                            label.setBackground(Color.WHITE);
                        } else {
                            label.setBackground(new Color(248, 249, 250));
                        }
                        label.setForeground(table.getForeground());
                    }
                    
                    // Alineación
                    if (columnIndex == 2) { // Columna documento
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                    
                    return label;
                });
            }
        }
    }
    
    // ===== MÉTODOS PÚBLICOS PARA EL CONTROLLER =====
    
    public void actualizarTablaClientes(List<Cliente> clientes) {
        modeloTabla.setRowCount(0);
        
        for (Cliente cliente : clientes) {
            Object[] fila = {
                cliente.getId(),
                cliente.getTipoDocumentoFormateado(),
                cliente.getNumeroDocumento(),
                cliente.getNombre(),
                cliente.getApellido() != null ? cliente.getApellido() : "",
                cliente.getEmail() != null ? cliente.getEmail() : "",
                cliente.getTelefono() != null ? cliente.getTelefono() : "",
                cliente.getEstadoTexto(),
                cliente.getFechaCreacion() != null ? 
                    cliente.getFechaCreacion().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
            };
            modeloTabla.addRow(fila);
        }
        
        // Actualizar información
        lblTotalClientes.setText("Total: " + clientes.size() + " clientes");
    }
    
    public void actualizarEstadisticas(String estadisticas) {
        lblEstadisticas.setText(estadisticas);
    }
    
    // ===== MÉTODOS PRIVADOS =====
    
    private void buscarClientes() {
        String termino = txtBuscar.getText().trim();
        controller.buscarClientes(termino);
    }
    
    private void aplicarFiltros() {
        String termino = txtBuscar.getText().trim();
        String tipoDoc = (String) cmbTipoDocumento.getSelectedItem();
        String estado = (String) cmbEstado.getSelectedItem();
        
        // Convertir tipo de documento
        String tipoDocDB = null;
        if (!"Todos".equals(tipoDoc)) {
            tipoDocDB = controller.convertirTipoDocumentoADB(tipoDoc);
        }
        
        // Determinar filtro de estado
        boolean soloActivos = "Solo Activos".equals(estado);
        
        if ("Solo Inactivos".equals(estado)) {
            // Para solo inactivos, usar búsqueda específica
            // TODO: Implementar método en controller para solo inactivos
            controller.buscarClientesConFiltros(termino, tipoDocDB, false);
        } else {
            controller.buscarClientesConFiltros(termino, tipoDocDB, soloActivos);
        }
    }
    
    private void limpiarFiltros() {
        txtBuscar.setText("");
        cmbTipoDocumento.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        controller.mostrarClientesActivos();
    }
    
    private void editarClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaClientes.convertRowIndexToModel(filaSeleccionada);
            int clienteId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.mostrarFormularioEditarCliente(clienteId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un cliente para editar", 
                "Cliente no seleccionado", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void eliminarClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaClientes.convertRowIndexToModel(filaSeleccionada);
            int clienteId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.eliminarCliente(clienteId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un cliente para eliminar", 
                "Cliente no seleccionado", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void activarClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaClientes.convertRowIndexToModel(filaSeleccionada);
            int clienteId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.activarCliente(clienteId);
        }
    }
    
    private void desactivarClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaClientes.convertRowIndexToModel(filaSeleccionada);
            int clienteId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.desactivarCliente(clienteId);
        }
    }
    
    private String getEstadoClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaClientes.convertRowIndexToModel(filaSeleccionada);
            return (String) modeloTabla.getValueAt(modelRow, 7);
        }
        return "";
    }
}