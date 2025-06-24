package views.ventas;

import controllers.VentaController;
import models.Venta;
import services.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaPanel extends JPanel {
    
    private VentaController controller;
    private AuthService authService;
    
    // Componentes principales
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnNuevaVenta;
    private JButton btnEditarVenta;
    private JButton btnCompletarVenta;
    private JButton btnCancelarVenta;
    private JButton btnImprimirFactura;
    private JButton btnRefresh;
    
    // Filtros
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbMetodoPago;
    private JButton btnFiltrar;
    private JButton btnLimpiarFiltros;
    private JButton btnVentasHoy;
    private JButton btnUltimasVentas;
    
    // Tabla
    private JTable tablaVentas;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Información y estadísticas
    private JLabel lblTotalVentas;
    private JLabel lblEstadisticas;
    private JLabel lblResumenSeleccion;
    
    public VentaPanel() {
        this.controller = new VentaController();
        this.authService = AuthService.getInstance();
        
        initializeComponents();
        setupLayout();
        setupEvents();
        setupTable();
        
        // Conectar el controller con este panel
        controller.setVentaPanel(this);
    }
    
    private void initializeComponents() {
        // Campo de búsqueda
        txtBuscar = new JTextField(20);
        txtBuscar.setToolTipText("Buscar por número de factura, cliente o documento");
        
        // Botones principales
        btnBuscar = new JButton("🔍 Buscar");
        btnNuevaVenta = new JButton("➕ Nueva Venta");
        btnEditarVenta = new JButton("✏️ Editar");
        btnCompletarVenta = new JButton("✅ Completar");
        btnCancelarVenta = new JButton("❌ Cancelar");
        btnImprimirFactura = new JButton("🖨️ Imprimir");
        btnRefresh = new JButton("🔄 Actualizar");
        
        // Filtros rápidos
        btnVentasHoy = new JButton("📅 Hoy");
        btnUltimasVentas = new JButton("⏰ Últimas");
        
        // Filtros avanzados
        String[] estados = {"Todos", "Pendiente", "Completada", "Cancelada"};
        cmbEstado = new JComboBox<>(estados);
        
        String[] metodosPago = {"Todos", "Efectivo", "Tarjeta", "Transferencia"};
        cmbMetodoPago = new JComboBox<>(metodosPago);
        
        btnFiltrar = new JButton("🔽 Filtrar");
        btnLimpiarFiltros = new JButton("🧹 Limpiar");
        
        // Configurar botones según permisos
        configurarPermisos();
        
        // Tabla de ventas
        String[] columnas = {
            "ID", "Nº Factura", "Cliente", "Documento", "Usuario", 
            "Fecha", "Subtotal", "Descuento", "Impuestos", "Total", "Estado", "Método Pago"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };
        
        tablaVentas = new JTable(modeloTabla);
        sorter = new TableRowSorter<>(modeloTabla);
        tablaVentas.setRowSorter(sorter);
        
        // Labels informativos
        lblTotalVentas = new JLabel("Total: 0 ventas");
        lblEstadisticas = new JLabel("Cargando...");
        lblResumenSeleccion = new JLabel(" ");
        
        setupTableStyles();
    }
    
    private void configurarPermisos() {
        boolean canMakeSales = authService.canMakeSales();
        boolean canViewReports = authService.canViewReports();
        
        btnNuevaVenta.setEnabled(canMakeSales);
        btnEditarVenta.setEnabled(canMakeSales);
        btnCompletarVenta.setEnabled(canMakeSales);
        btnCancelarVenta.setEnabled(canMakeSales);
        btnImprimirFactura.setEnabled(canViewReports);
        
        if (!canMakeSales) {
            btnNuevaVenta.setToolTipText("Sin permisos para realizar ventas");
            btnEditarVenta.setToolTipText("Sin permisos para editar ventas");
            btnCompletarVenta.setToolTipText("Sin permisos para completar ventas");
            btnCancelarVenta.setToolTipText("Sin permisos para cancelar ventas");
        }
        
        if (!canViewReports) {
            btnImprimirFactura.setToolTipText("Sin permisos para imprimir facturas");
        }
    }
    
    private void setupTableStyles() {
        // Configurar tabla
        tablaVentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVentas.setRowHeight(28);
        tablaVentas.setShowGrid(true);
        tablaVentas.setGridColor(new Color(230, 230, 230));
        
        // Colores básicos
        tablaVentas.setBackground(Color.WHITE);
        tablaVentas.setSelectionBackground(new Color(0, 123, 255));
        tablaVentas.setSelectionForeground(Color.WHITE);
        
        // Header
        tablaVentas.getTableHeader().setBackground(new Color(52, 58, 64));
        tablaVentas.getTableHeader().setForeground(Color.WHITE);
        tablaVentas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Anchos de columnas
        tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tablaVentas.getColumnModel().getColumn(1).setPreferredWidth(120);  // Nº Factura
        tablaVentas.getColumnModel().getColumn(2).setPreferredWidth(180);  // Cliente
        tablaVentas.getColumnModel().getColumn(3).setPreferredWidth(120);  // Documento
        tablaVentas.getColumnModel().getColumn(4).setPreferredWidth(150);  // Usuario
        tablaVentas.getColumnModel().getColumn(5).setPreferredWidth(140);  // Fecha
        tablaVentas.getColumnModel().getColumn(6).setPreferredWidth(100);  // Subtotal
        tablaVentas.getColumnModel().getColumn(7).setPreferredWidth(100);  // Descuento
        tablaVentas.getColumnModel().getColumn(8).setPreferredWidth(100);  // Impuestos
        tablaVentas.getColumnModel().getColumn(9).setPreferredWidth(100);  // Total
        tablaVentas.getColumnModel().getColumn(10).setPreferredWidth(100); // Estado
        tablaVentas.getColumnModel().getColumn(11).setPreferredWidth(120); // Método Pago
        
        // Ocultar columna ID
        tablaVentas.getColumnModel().getColumn(0).setMinWidth(0);
        tablaVentas.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(0);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Título y controles
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Título
        JLabel lblTitulo = new JLabel("Gestión de Ventas");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(52, 58, 64));
        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        
        // Panel de búsqueda y filtros
        JPanel panelBusquedaFiltros = createPanelBusquedaFiltros();
        panelSuperior.add(panelBusquedaFiltros, BorderLayout.EAST);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        // Panel central - Tabla
        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Ventas"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior - Botones e información
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Panel de botones principales
        JPanel panelBotones = createPanelBotones();
        panelInferior.add(panelBotones, BorderLayout.WEST);
        
        // Panel de información
        JPanel panelInfo = createPanelInfo();
        panelInferior.add(panelInfo, BorderLayout.EAST);
        
        // Panel de resumen de selección
        JPanel panelResumen = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelResumen.add(lblResumenSeleccion);
        panelInferior.add(panelResumen, BorderLayout.CENTER);
        
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private JPanel createPanelBusquedaFiltros() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Primera línea: Búsqueda
        JPanel lineaBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lineaBusqueda.add(new JLabel("Buscar:"));
        lineaBusqueda.add(txtBuscar);
        lineaBusqueda.add(btnBuscar);
        lineaBusqueda.add(new JSeparator(SwingConstants.VERTICAL));
        lineaBusqueda.add(btnVentasHoy);
        lineaBusqueda.add(btnUltimasVentas);
        
        // Segunda línea: Filtros
        JPanel lineaFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lineaFiltros.add(new JLabel("Estado:"));
        lineaFiltros.add(cmbEstado);
        lineaFiltros.add(new JLabel("Método:"));
        lineaFiltros.add(cmbMetodoPago);
        lineaFiltros.add(btnFiltrar);
        lineaFiltros.add(btnLimpiarFiltros);
        
        panel.add(lineaBusqueda);
        panel.add(lineaFiltros);
        
        return panel;
    }
    
    private JPanel createPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Botones principales
        panel.add(btnNuevaVenta);
        panel.add(btnEditarVenta);
        panel.add(btnCompletarVenta);
        panel.add(btnCancelarVenta);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Botones secundarios
        panel.add(btnImprimirFactura);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Botón actualizar
        panel.add(btnRefresh);
        
        return panel;
    }
    
    private JPanel createPanelInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        // Primera línea: Total de ventas
        JPanel lineaTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lineaTotal.add(lblTotalVentas);
        
        // Segunda línea: Estadísticas
        JPanel lineaEstadisticas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lineaEstadisticas.add(lblEstadisticas);
        
        panel.add(lineaTotal);
        panel.add(lineaEstadisticas);
        
        return panel;
    }
    
    private void setupEvents() {
        // Botón búsqueda
        btnBuscar.addActionListener(e -> buscarVentas());
        
        // Enter en campo de búsqueda
        txtBuscar.addActionListener(e -> buscarVentas());
        
        // Botones principales
        btnNuevaVenta.addActionListener(e -> controller.mostrarFormularioNuevaVenta());
        btnEditarVenta.addActionListener(e -> editarVentaSeleccionada());
        btnCompletarVenta.addActionListener(e -> completarVentaSeleccionada());
        btnCancelarVenta.addActionListener(e -> cancelarVentaSeleccionada());
        btnImprimirFactura.addActionListener(e -> imprimirFacturaSeleccionada());
        btnRefresh.addActionListener(e -> controller.cargarDatos());
        
        // Filtros rápidos
        btnVentasHoy.addActionListener(e -> controller.mostrarVentasDelDia());
        btnUltimasVentas.addActionListener(e -> controller.mostrarUltimasVentas());
        
        // Filtros avanzados
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());
        
        // Doble clic en tabla para ver/editar
        tablaVentas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (authService.canMakeSales()) {
                        editarVentaSeleccionada();
                    } else {
                        verDetalleVenta();
                    }
                }
            }
        });
        
        // Cambio de selección en tabla
        tablaVentas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesSegunSeleccion();
                actualizarResumenSeleccion();
            }
        });
    }
    
    private void setupTable() {
        // Configurar renderer personalizado para el estado
        tablaVentas.getColumnModel().getColumn(10).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
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
                switch (estado) {
                    case "Pendiente":
                        label.setBackground(new Color(255, 243, 205));
                        label.setForeground(new Color(102, 77, 3));
                        break;
                    case "Completada":
                        label.setBackground(new Color(212, 237, 218));
                        label.setForeground(new Color(21, 87, 36));
                        break;
                    case "Cancelada":
                        label.setBackground(new Color(248, 215, 218));
                        label.setForeground(new Color(114, 28, 36));
                        break;
                }
            }
            
            return label;
        });
        
        // Renderer para columnas de montos (alineación derecha)
        for (int i = 6; i <= 9; i++) { // Subtotal, Descuento, Impuestos, Total
            tablaVentas.getColumnModel().getColumn(i).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.RIGHT);
                
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
                
                return label;
            });
        }
        
        // Renderer para todas las demás columnas (filas alternadas)
        for (int i = 0; i < tablaVentas.getColumnCount(); i++) {
            if (i != 10 && !(i >= 6 && i <= 9)) { // Excepto estado y montos
                final int columnIndex = i;
                tablaVentas.getColumnModel().getColumn(i).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
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
                    
                    // Alineación según columna
                    if (columnIndex == 1 || columnIndex == 3) { // Nº Factura, Documento
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
    
    public void actualizarTablaVentas(List<Venta> ventas) {
        modeloTabla.setRowCount(0);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Venta venta : ventas) {
            Object[] fila = {
                venta.getId(),
                venta.getNumeroFactura(),
                venta.getClienteNombre() != null ? venta.getClienteNombre() : "N/A",
                venta.getClienteDocumento() != null ? venta.getClienteDocumento() : "N/A",
                venta.getUsuarioNombre() != null ? venta.getUsuarioNombre() : "N/A",
                venta.getFechaVenta() != null ? venta.getFechaVenta().format(formatter) : "",
                String.format("$%.2f", venta.getSubtotal()),
                String.format("$%.2f", venta.getDescuento()),
                String.format("$%.2f", venta.getImpuestos()),
                String.format("$%.2f", venta.getTotal()),
                controller.convertirEstadoADisplay(venta.getEstado()),
                controller.convertirMetodoPagoADisplay(venta.getMetodoPago())
            };
            modeloTabla.addRow(fila);
        }
        
        // Actualizar información
        lblTotalVentas.setText("Total: " + ventas.size() + " ventas");
        actualizarBotonesSegunSeleccion();
    }
    
    public void actualizarEstadisticas(String estadisticas) {
        lblEstadisticas.setText(estadisticas);
    }
    
    // ===== MÉTODOS PRIVADOS =====
    
    private void buscarVentas() {
        String termino = txtBuscar.getText().trim();
        controller.buscarVentas(termino);
    }
    
    private void aplicarFiltros() {
        String estado = (String) cmbEstado.getSelectedItem();
        String metodoPago = (String) cmbMetodoPago.getSelectedItem();
        
        // Convertir a formato DB si no es "Todos"
        String estadoDB = "Todos".equals(estado) ? null : controller.convertirMetodoPagoADB(estado);
        String metodoDB = "Todos".equals(metodoPago) ? null : controller.convertirMetodoPagoADB(metodoPago);
        
        // Por ahora filtro simple por estado
        if (estadoDB != null) {
            controller.filtrarPorEstado(estadoDB);
        } else {
            controller.cargarDatos();
        }
    }
    
    private void limpiarFiltros() {
        txtBuscar.setText("");
        cmbEstado.setSelectedIndex(0);
        cmbMetodoPago.setSelectedIndex(0);
        controller.cargarDatos();
    }
    
    private void editarVentaSeleccionada() {
        int filaSeleccionada = tablaVentas.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaVentas.convertRowIndexToModel(filaSeleccionada);
            int ventaId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.mostrarFormularioEditarVenta(ventaId);
        } else {
            mostrarMensajeSeleccion("editar");
        }
    }
    
    private void completarVentaSeleccionada() {
        int filaSeleccionada = tablaVentas.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaVentas.convertRowIndexToModel(filaSeleccionada);
            int ventaId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.completarVenta(ventaId);
        } else {
            mostrarMensajeSeleccion("completar");
        }
    }
    
    private void cancelarVentaSeleccionada() {
        int filaSeleccionada = tablaVentas.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaVentas.convertRowIndexToModel(filaSeleccionada);
            int ventaId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.cancelarVenta(ventaId);
        } else {
            mostrarMensajeSeleccion("cancelar");
        }
    }
    
    private void imprimirFacturaSeleccionada() {
        int filaSeleccionada = tablaVentas.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaVentas.convertRowIndexToModel(filaSeleccionada);
            int ventaId = (Integer) modeloTabla.getValueAt(modelRow, 0);
            controller.imprimirFactura(ventaId);
        } else {
            mostrarMensajeSeleccion("imprimir");
        }
    }
    
    private void verDetalleVenta() {
        int filaSeleccionada = tablaVentas.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaVentas.convertRowIndexToModel(filaSeleccionada);
            String numeroFactura = (String) modeloTabla.getValueAt(modelRow, 1);
            String cliente = (String) modeloTabla.getValueAt(modelRow, 2);
            String total = (String) modeloTabla.getValueAt(modelRow, 9);
            String estado = (String) modeloTabla.getValueAt(modelRow, 10);
            
            String mensaje = String.format(
                "Factura: %s\nCliente: %s\nTotal: %s\nEstado: %s",
                numeroFactura, cliente, total, estado
            );
            
            JOptionPane.showMessageDialog(this, mensaje, "Detalle de Venta", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void actualizarBotonesSegunSeleccion() {
        boolean haySeleccion = tablaVentas.getSelectedRow() != -1;
        boolean canMakeSales = authService.canMakeSales();
        boolean canViewReports = authService.canViewReports();
        
        if (haySeleccion) {
            int filaSeleccionada = tablaVentas.getSelectedRow();
            int modelRow = tablaVentas.convertRowIndexToModel(filaSeleccionada);
            String estado = (String) modeloTabla.getValueAt(modelRow, 10);
            
            // Habilitar botones según estado y permisos
            btnEditarVenta.setEnabled(canMakeSales && "Pendiente".equals(estado));
            btnCompletarVenta.setEnabled(canMakeSales && "Pendiente".equals(estado));
            btnCancelarVenta.setEnabled(canMakeSales && !"Cancelada".equals(estado));
            btnImprimirFactura.setEnabled(canViewReports);
        } else {
            // Sin selección, deshabilitar botones de acción
            btnEditarVenta.setEnabled(false);
            btnCompletarVenta.setEnabled(false);
            btnCancelarVenta.setEnabled(false);
            btnImprimirFactura.setEnabled(false);
        }
    }
    
    private void actualizarResumenSeleccion() {
        int filaSeleccionada = tablaVentas.getSelectedRow();
        if (filaSeleccionada != -1) {
            int modelRow = tablaVentas.convertRowIndexToModel(filaSeleccionada);
            String numeroFactura = (String) modeloTabla.getValueAt(modelRow, 1);
            String cliente = (String) modeloTabla.getValueAt(modelRow, 2);
            String total = (String) modeloTabla.getValueAt(modelRow, 9);
            String estado = (String) modeloTabla.getValueAt(modelRow, 10);
            
            lblResumenSeleccion.setText(String.format(
                "Seleccionado: %s | %s | %s | %s", 
                numeroFactura, cliente, total, estado
            ));
            lblResumenSeleccion.setFont(new Font("Arial", Font.BOLD, 12));
            lblResumenSeleccion.setForeground(new Color(0, 123, 255));
        } else {
            lblResumenSeleccion.setText(" ");
        }
    }
    
    private void mostrarMensajeSeleccion(String accion) {
        JOptionPane.showMessageDialog(this, 
            "Por favor seleccione una venta para " + accion, 
            "Venta no seleccionada", 
            JOptionPane.WARNING_MESSAGE);
    }
}

