package views.ventas;

import controllers.VentaController;
import models.Venta;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class VentaPanel extends JPanel {
    
    private VentaController controller;
    private JTable tablaVentas;
    private JLabel lblEstadisticas;
    
    public VentaPanel() {
        this.controller = new VentaController();
        initializeComponents();
        setupLayout();
        controller.setVentaPanel(this);
    }
    
    private void initializeComponents() {
        setBackground(Color.WHITE);
        
        // Inicializar tabla de ventas
        String[] columns = {"ID", "Factura", "Fecha", "Cliente", "Total", "Estado"};
        Object[][] data = {};
        tablaVentas = new JTable(data, columns);
        tablaVentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVentas.getTableHeader().setBackground(new Color(52, 58, 64));
        tablaVentas.getTableHeader().setForeground(Color.WHITE);
        tablaVentas.setRowHeight(25);
        
        // Inicializar etiqueta de estadísticas
        lblEstadisticas = new JLabel("Cargando estadísticas...");
        lblEstadisticas.setFont(new Font("Arial", Font.ITALIC, 12));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel de título
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Panel principal con tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab Nueva Venta
        tabbedPane.addTab("Nueva Venta", createNuevaVentaPanel());
        
        // Tab Buscar Ventas
        tabbedPane.addTab("Buscar Ventas", createBuscarVentasPanel());
        
        // Tab Historial
        tabbedPane.addTab("Historial", createHistorialPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de estadísticas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.add(lblEstadisticas);
        add(statsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(52, 58, 64));
        
        JLabel title = new JLabel("💰 Sistema de Ventas");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        panel.add(title);
        return panel;
    }
    
    private JPanel createNuevaVentaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel title = new JLabel("Nueva Venta", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Panel de botones de acción
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton btnNuevaVenta = new JButton("🛒 Crear Nueva Venta");
        btnNuevaVenta.setFont(new Font("Arial", Font.BOLD, 14));
        btnNuevaVenta.setBackground(new Color(40, 167, 69));
        btnNuevaVenta.setForeground(Color.WHITE);
        btnNuevaVenta.setPreferredSize(new Dimension(200, 50));
        btnNuevaVenta.addActionListener(e -> controller.mostrarFormularioNuevaVenta());
        
        JButton btnVentasPendientes = new JButton("⏳ Ver Pendientes");
        btnVentasPendientes.setFont(new Font("Arial", Font.PLAIN, 12));
        btnVentasPendientes.setBackground(new Color(255, 193, 7));
        btnVentasPendientes.setForeground(Color.BLACK);
        btnVentasPendientes.setPreferredSize(new Dimension(150, 40));
        btnVentasPendientes.addActionListener(e -> controller.mostrarVentasPendientes());
        
        JButton btnVentasDelDia = new JButton("📅 Ventas del Día");
        btnVentasDelDia.setFont(new Font("Arial", Font.PLAIN, 12));
        btnVentasDelDia.setBackground(new Color(23, 162, 184));
        btnVentasDelDia.setForeground(Color.WHITE);
        btnVentasDelDia.setPreferredSize(new Dimension(150, 40));
        btnVentasDelDia.addActionListener(e -> controller.mostrarVentasDelDia());
        
        buttonPanel.add(btnNuevaVenta);
        buttonPanel.add(btnVentasPendientes);
        buttonPanel.add(btnVentasDelDia);
        
        // Mensaje de desarrollo
        JTextArea message = new JTextArea();
        message.setText("MÓDULO DE VENTAS - SISTEMA INTEGRADO\n\n" +
                       "✅ FUNCIONALIDADES IMPLEMENTADAS:\n\n" +
                       "1. GESTIÓN DE VENTAS\n" +
                       "   • Crear nuevas ventas\n" +
                       "   • Editar ventas pendientes\n" +
                       "   • Completar ventas\n" +
                       "   • Cancelar ventas\n\n" +
                       "2. CARRITO DE COMPRAS\n" +
                       "   • Agregar productos por código o ID\n" +
                       "   • Modificar cantidades\n" +
                       "   • Eliminar productos del carrito\n" +
                       "   • Cálculos automáticos de totales\n\n" +
                       "3. GESTIÓN DE CLIENTES\n" +
                       "   • Selección de cliente existente\n" +
                       "   • Búsqueda por documento\n" +
                       "   • Validación de clientes activos\n\n" +
                       "4. CÁLCULOS Y TOTALES\n" +
                       "   • Subtotales automáticos\n" +
                       "   • Aplicación de descuentos\n" +
                       "   • Cálculo de IVA\n" +
                       "   • Total final con impuestos\n\n" +
                       "5. VALIDACIONES\n" +
                       "   • Control de stock en tiempo real\n" +
                       "   • Validación de cantidades\n" +
                       "   • Verificación de permisos\n" +
                       "   • Números de factura únicos\n\n" +
                       "6. FILTROS Y BÚSQUEDAS\n" +
                       "   • Filtrar por estado (pendiente, completada, cancelada)\n" +
                       "   • Búsqueda por término general\n" +
                       "   • Filtros avanzados con fechas\n" +
                       "   • Ventas del día y últimas ventas\n\n" +
                       "🔄 PRÓXIMAS CARACTERÍSTICAS:\n" +
                       "• Interfaz gráfica del carrito de compras\n" +
                       "• Formulario completo de nueva venta\n" +
                       "• Impresión de facturas\n" +
                       "• Reportes avanzados\n\n" +
                       "ESTADO: LÓGICA DE NEGOCIO COMPLETA\n" +
                       "Backend funcional - UI en desarrollo");
        
        message.setEditable(false);
        message.setBackground(panel.getBackground());
        message.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(message);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Estado del Módulo"));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createBuscarVentasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de filtros
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtros de Búsqueda"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Búsqueda general
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Búsqueda General:"), gbc);
        gbc.gridx = 1;
        JTextField txtBusqueda = new JTextField(20);
        filterPanel.add(txtBusqueda, gbc);
        
        gbc.gridx = 2;
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> controller.buscarVentas(txtBusqueda.getText()));
        filterPanel.add(btnBuscar, gbc);
        
        // Estado
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cmbEstado = new JComboBox<>(controller.getEstadosVenta());
        cmbEstado.addActionListener(e -> {
            String estado = (String) cmbEstado.getSelectedItem();
            controller.filtrarPorEstado(estado);
        });
        filterPanel.add(cmbEstado, gbc);
        
        // Botón limpiar
        gbc.gridx = 2;
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> {
            txtBusqueda.setText("");
            cmbEstado.setSelectedIndex(0);
            controller.cargarDatos();
        });
        filterPanel.add(btnLimpiar, gbc);
        
        // Tabla de resultados
        JScrollPane scrollPane = new JScrollPane(tablaVentas);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultados de Búsqueda"));
        
        // Panel de botones de acción
        JPanel actionPanel = new JPanel(new FlowLayout());
        
        JButton btnEditar = new JButton("✏️ Editar");
        btnEditar.setBackground(new Color(255, 193, 7));
        btnEditar.setForeground(Color.BLACK);
        btnEditar.addActionListener(e -> editarVentaSeleccionada());
        
        JButton btnCompletar = new JButton("✅ Completar");
        btnCompletar.setBackground(new Color(40, 167, 69));
        btnCompletar.setForeground(Color.WHITE);
        btnCompletar.addActionListener(e -> completarVentaSeleccionada());
        
        JButton btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.setBackground(new Color(220, 53, 69));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.addActionListener(e -> cancelarVentaSeleccionada());
        
        JButton btnImprimir = new JButton("🖨️ Imprimir");
        btnImprimir.setBackground(new Color(108, 117, 125));
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.addActionListener(e -> imprimirVentaSeleccionada());
        
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.setBackground(new Color(23, 162, 184));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.addActionListener(e -> controller.cargarDatos());
        
        actionPanel.add(btnEditar);
        actionPanel.add(btnCompletar);
        actionPanel.add(btnCancelar);
        actionPanel.add(btnImprimir);
        actionPanel.add(btnRefrescar);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createHistorialPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel title = new JLabel("Historial de Ventas", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Panel de estadísticas rápidas
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Estadísticas del Día"));
        
        statsPanel.add(createStatCard("Ventas Hoy", "15", Color.GREEN));
        statsPanel.add(createStatCard("Total Vendido", "$2,350.00", Color.BLUE));
        statsPanel.add(createStatCard("Productos Vendidos", "87", Color.ORANGE));
        statsPanel.add(createStatCard("Clientes Atendidos", "12", Color.MAGENTA));
        
        // Panel de acciones rápidas
        JPanel quickActionsPanel = new JPanel(new FlowLayout());
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Acciones Rápidas"));
        
        JButton btnVentasHoy = new JButton("📅 Ventas de Hoy");
        btnVentasHoy.addActionListener(e -> controller.mostrarVentasDelDia());
        
        JButton btnUltimasVentas = new JButton("🕒 Últimas 20 Ventas");
        btnUltimasVentas.addActionListener(e -> controller.mostrarUltimasVentas());
        
        JButton btnVentasPendientes = new JButton("⏳ Pendientes");
        btnVentasPendientes.addActionListener(e -> controller.mostrarVentasPendientes());
        
        JButton btnReporte = new JButton("📊 Generar Reporte");
        btnReporte.addActionListener(e -> controller.generarReporteVentas());
        
        JButton btnExportar = new JButton("💾 Exportar CSV");
        btnExportar.addActionListener(e -> controller.exportarVentasCSV());
        
        quickActionsPanel.add(btnVentasHoy);
        quickActionsPanel.add(btnUltimasVentas);
        quickActionsPanel.add(btnVentasPendientes);
        quickActionsPanel.add(btnReporte);
        quickActionsPanel.add(btnExportar);
        
        // Gráfico de ventas (placeholder)
        JPanel chartPanel = new JPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de Ventas - Últimos 7 días"));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel chartPlaceholder = new JLabel("📊 Gráfico de Ventas (En desarrollo)", SwingConstants.CENTER);
        chartPlaceholder.setFont(new Font("Arial", Font.ITALIC, 16));
        chartPlaceholder.setForeground(Color.GRAY);
        chartPanel.add(chartPlaceholder);
        
        panel.add(title, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(quickActionsPanel, BorderLayout.CENTER);
        centerPanel.add(chartPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 18));
        lblValue.setForeground(color);
        lblValue.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }
    
    // ===== MÉTODOS PARA EL CONTROLLER =====
    
    public void actualizarTablaVentas(List<Venta> ventas) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] columns = {"ID", "Factura", "Fecha", "Cliente", "Total", "Estado"};
                Object[][] data = new Object[ventas.size()][6];
                
                for (int i = 0; i < ventas.size(); i++) {
                    Venta venta = ventas.get(i);
                    data[i][0] = venta.getId();
                    data[i][1] = venta.getNumeroFactura();
                    data[i][2] = venta.getFechaVenta() != null ? 
                        venta.getFechaVenta().toLocalDate().toString() : "";
                    data[i][3] = venta.getClienteNombre() != null ? 
                        venta.getClienteNombre() : "Cliente no especificado";
                    data[i][4] = controller.formatearMonto(venta.getTotal());
                    data[i][5] = formatearEstado(venta.getEstado());
                }
                
                DefaultTableModel model = new DefaultTableModel(data, columns) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                
                tablaVentas.setModel(model);
                
                // Configurar anchos de columnas
                tablaVentas.getColumnModel().getColumn(0).setPreferredWidth(50);
                tablaVentas.getColumnModel().getColumn(1).setPreferredWidth(100);
                tablaVentas.getColumnModel().getColumn(2).setPreferredWidth(100);
                tablaVentas.getColumnModel().getColumn(3).setPreferredWidth(200);
                tablaVentas.getColumnModel().getColumn(4).setPreferredWidth(100);
                tablaVentas.getColumnModel().getColumn(5).setPreferredWidth(100);
                
                // Colorear filas según estado
                tablaVentas.setDefaultRenderer(Object.class, new VentaTableCellRenderer());
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al actualizar tabla: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    public void actualizarEstadisticas(String estadisticas) {
        SwingUtilities.invokeLater(() -> {
            lblEstadisticas.setText(estadisticas);
        });
    }
    
    // ===== MÉTODOS DE ACCIONES =====
    
    private void editarVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
            controller.mostrarFormularioEditarVenta(ventaId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione una venta para editar", 
                "Selección requerida", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void completarVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
            controller.completarVenta(ventaId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione una venta para completar", 
                "Selección requerida", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void cancelarVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
            controller.cancelarVenta(ventaId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione una venta para cancelar", 
                "Selección requerida", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void imprimirVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
            controller.imprimirFactura(ventaId);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Por favor seleccione una venta para imprimir", 
                "Selección requerida", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // ===== MÉTODOS AUXILIARES =====
    
    private String formatearEstado(String estado) {
        if (estado == null) return "DESCONOCIDO";
        
        switch (estado.toUpperCase()) {
            case "PENDIENTE": return "⏳ PENDIENTE";
            case "COMPLETADA": return "✅ COMPLETADA";
            case "CANCELADA": return "❌ CANCELADA";
            default: return estado;
        }
    }
    
    private class VentaTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                String estado = (String) table.getValueAt(row, 5);
                if (estado != null) {
                    if (estado.contains("PENDIENTE")) {
                        setBackground(new Color(255, 248, 220));
                    } else if (estado.contains("COMPLETADA")) {
                        setBackground(new Color(240, 255, 240));
                    } else if (estado.contains("CANCELADA")) {
                        setBackground(new Color(255, 240, 240));
                    } else {
                        setBackground(Color.WHITE);
                    }
                }
            }
            
            return this;
        }
    }
    
    public VentaController getController() {
        return controller;
    }
}