package views.productos;

import controllers.ProductoController;
import models.Producto;
import models.Categoria;
import models.Proveedor;
import services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProductoPanel extends JPanel {
    
    private ProductoController controller;
    private AuthService authService;
    
    // Componentes principales
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnStockBajo;
    private JButton btnActualizarStock;
    private JButton btnRefresh;
    
    // Tabla
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Información
    private JLabel lblTotalProductos;
    private JLabel lblEstadisticas;
    
    public ProductoPanel() {
        this.controller = new ProductoController();
        this.authService = AuthService.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEvents();
        setupTable();
        
        // Conectar el controller con este panel
        controller.setProductoPanel(this);
    }
    
    private void initializeComponents() {
        // Campo de búsqueda
        txtBuscar = new JTextField(20);
        txtBuscar.setToolTipText("Buscar por nombre o código del producto");
        
        // Botones principales
        btnBuscar = new JButton("🔍 Buscar");
        btnNuevo = new JButton("➕ Nuevo");
        btnEditar = new JButton("✏️ Editar");
        btnEliminar = new JButton("🗑️ Eliminar");
        btnStockBajo = new JButton("⚠️ Stock Bajo");
        btnActualizarStock = new JButton("📦 Actualizar Stock");
        btnRefresh = new JButton("🔄 Actualizar");
        
        // Configurar botones según permisos
        configurarPermisos();
        
        // Tabla
        String[] columnas = {
            "ID", "Código", "Nombre", "Categoría", "Proveedor", 
            "Precio Compra", "Precio Venta", "Stock", "Stock Mín", "Estado Stock"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaProductos.setRowSorter(sorter);
        
        // Labels informativos
        lblTotalProductos = new JLabel("Total: 0 productos");
        lblEstadisticas = new JLabel("Cargando...");
        
        setupTableStyles();
    }
    
    private void configurarPermisos() {
        boolean canManage = authService.canManageInventory();
        
        btnNuevo.setEnabled(canManage);
        btnEditar.setEnabled(canManage);
        btnEliminar.setEnabled(canManage);
        btnActualizarStock.setEnabled(canManage);
        
        if (!canManage) {
            btnNuevo.setToolTipText("Sin permisos para gestionar inventario");
            btnEditar.setToolTipText("Sin permisos para gestionar inventario");
            btnEliminar.setToolTipText("Sin permisos para gestionar inventario");
            btnActualizarStock.setToolTipText("Sin permisos para gestionar inventario");
        }
    }
    
    private void setupTableStyles() {
        // Configurar tabla
        tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.setRowHeight(25);
        tablaProductos.setShowGrid(true);
        tablaProductos.setGridColor(new Color(230, 230, 230));
        
        // Colores básicos
        tablaProductos.setBackground(Color.WHITE);
        tablaProductos.setSelectionBackground(new Color(0, 123, 255));
        tablaProductos.setSelectionForeground(Color.WHITE);
        
        // Header
        tablaProductos.getTableHeader().setBackground(new Color(52, 58, 64));
        tablaProductos.getTableHeader().setForeground(Color.WHITE);
        tablaProductos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Anchos de columnas
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(100); // Código
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(200); // Nombre
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(120); // Categoría
        tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(120); // Proveedor
        tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(100); // Precio Compra
        tablaProductos.getColumnModel().getColumn(6).setPreferredWidth(100); // Precio Venta
        tablaProductos.getColumnModel().getColumn(7).setPreferredWidth(80);  // Stock
        tablaProductos.getColumnModel().getColumn(8).setPreferredWidth(80);  // Stock Mín
        tablaProductos.getColumnModel().getColumn(9).setPreferredWidth(100); // Estado
        
        // Ocultar columna ID
        tablaProductos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(0);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Título y búsqueda
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Título
        JLabel lblTitulo = new JLabel("Gestión de Productos");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(52, 58, 64));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnBuscar);
        panelSuperior.add(panelBusqueda, BorderLayout.EAST);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central - Tabla
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Productos"));
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
        panelBotones.add(btnActualizarStock);
        panelBotones.add(btnStockBajo);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL));
        panelBotones.add(btnRefresh);
        
        panelInferior.add(panelBotones, BorderLayout.WEST);
        
        // Información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInfo.add(lblTotalProductos);
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(lblEstadisticas);
        
        panelInferior.add(panelInfo, BorderLayout.EAST);
        
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void setupEvents() {
        // Botón búsqueda
        btnBuscar.addActionListener(e -> buscarProductos());
        
        // Enter en campo de búsqueda
        txtBuscar.addActionListener(e -> buscarProductos());
        
        // Botones principales
        btnNuevo.addActionListener(e -> controller.mostrarFormularioNuevoProducto());
        btnEditar.addActionListener(e -> editarProductoSeleccionado());
        btnEliminar.addActionListener(e -> eliminarProductoSeleccionado());
        btnStockBajo.addActionListener(e -> controller.mostrarProductosStockBajo());
        btnActualizarStock.addActionListener(e -> actualizarStockSeleccionado());
        btnRefresh.addActionListener(e -> controller.cargarDatos());
        
        // Doble clic en tabla para editar
        tablaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && authService.canManageInventory()) {
                    editarProductoSeleccionado();
                }
            }
        });
        
        // Cambio de selección en tabla
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean haySeleccion = tablaProductos.getSelectedRow() != -1;
                btnEditar.setEnabled(haySeleccion && authService.canManageInventory());
                btnEliminar.setEnabled(haySeleccion && authService.canManageInventory());
                btnActualizarStock.setEnabled(haySeleccion && authService.canManageInventory());
            }
        });
    }
    
    private void setupTable() {
        // Configurar renderer personalizado para el estado de stock
        tablaProductos.getColumnModel().getColumn(9).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JLabel label = new JLabel(value.toString());
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
                
                // Colorear según estado de stock
                String estado = value.toString();
                if ("CRÍTICO".equals(estado)) {
                    label.setBackground(new Color(248, 215, 218));
                    label.setForeground(new Color(114, 28, 36));
                } else if ("BAJO".equals(estado)) {
                    label.setBackground(new Color(255, 243, 205));
                    label.setForeground(new Color(102, 77, 3));
                }
            }
            
            label.setHorizontalAlignment(SwingConstants.CENTER);
            return label;
        });
        
        // Renderer para todas las columnas para filas alternadas
        for (int i = 0; i < tablaProductos.getColumnCount(); i++) {
            if (i != 9) { // Excepto la columna de estado que ya tiene su renderer
                final int columnIndex = i;
                tablaProductos.getColumnModel().getColumn(i).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
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
                    
                    // Alineación según tipo de columna
                    if (columnIndex >= 5 && columnIndex <= 8) { // Columnas numéricas
                        label.setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                    
                    return label;
                });
            }
        }
    }
    
    // ===== MÉTODOS PÚBLICOS PARA EL CONTROLLER =====
    
    public void actualizarTablaProductos(List<Producto> productos) {
        modeloTabla.setRowCount(0);
        
        for (Producto producto : productos) {
            Object[] fila = {
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getCategoriaNombre(),
                producto.getProveedorNombre(),
                String.format("$%.2f", producto.getPrecioCompra()),
                String.format("$%.2f", producto.getPrecioVenta()),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getEstadoStock()
            };
            modeloTabla.addRow(fila);
        }
        
        // Actualizar información
        lblTotalProductos.setText("Total: " + productos.size() + " productos");
        lblEstadisticas.setText(controller.obtenerEstadisticas());
    }
    
    public void actualizarCategorias(List<Categoria> categorias) {
        // Método para actualizar combos de categorías en formularios
        // Se implementará cuando creemos los formularios
    }
    
    public void actualizarProveedores(List<Proveedor> proveedores) {
        // Método para actualizar combos de proveedores en formularios
        // Se implementará cuando creemos los formularios
    }
    
    // ===== MÉTODOS PRIVADOS =====
    
    private void buscarProductos() {
        String termino = txtBuscar.getText().trim();
        controller.buscarProductos(termino);
    }
    
    private void editarProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaProductos.convertRowIndexToModel(filaSeleccionada);
            int productoId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.mostrarFormularioEditarProducto(productoId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un producto para editar", 
                "Producto no seleccionado", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void eliminarProductoSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaProductos.convertRowIndexToModel(filaSeleccionada);
            int productoId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            String nombreProducto = (String) modeloTabla.getValueAt(modelRow, 2);
            
            int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea eliminar el producto:\n" + nombreProducto + "?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (opcion == JOptionPane.YES_OPTION) {
                controller.eliminarProducto(productoId);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un producto para eliminar", 
                "Producto no seleccionado", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void actualizarStockSeleccionado() {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaProductos.convertRowIndexToModel(filaSeleccionada);
            int productoId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.actualizarStock(productoId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione un producto para actualizar stock", 
                "Producto no seleccionado", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
}