package views.ventas;

import controllers.VentaController;
import models.Venta;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
        String[] columns = { "ID", "Factura", "Fecha", "Cliente", "Total", "Estado" };
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
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Panel de botones de acción principales
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton btnNuevaVenta = new JButton("🛒 Crear Nueva Venta");
        btnNuevaVenta.setFont(new Font("Arial", Font.BOLD, 16));
        btnNuevaVenta.setBackground(new Color(40, 167, 69));
        btnNuevaVenta.setForeground(Color.WHITE);
        btnNuevaVenta.setPreferredSize(new Dimension(250, 60));
        btnNuevaVenta.setBorder(BorderFactory.createRaisedBevelBorder());
        btnNuevaVenta.addActionListener(e -> controller.mostrarFormularioNuevaVenta());

        JButton btnVentasPendientes = new JButton("⏳ Ver Pendientes");
        btnVentasPendientes.setFont(new Font("Arial", Font.PLAIN, 14));
        btnVentasPendientes.setBackground(new Color(255, 193, 7));
        btnVentasPendientes.setForeground(Color.BLACK);
        btnVentasPendientes.setPreferredSize(new Dimension(180, 45));
        btnVentasPendientes.setBorder(BorderFactory.createRaisedBevelBorder());
        btnVentasPendientes.addActionListener(e -> controller.mostrarVentasPendientes());

        JButton btnVentasDelDia = new JButton("📅 Ventas del Día");
        btnVentasDelDia.setFont(new Font("Arial", Font.PLAIN, 14));
        btnVentasDelDia.setBackground(new Color(23, 162, 184));
        btnVentasDelDia.setForeground(Color.WHITE);
        btnVentasDelDia.setPreferredSize(new Dimension(180, 45));
        btnVentasDelDia.setBorder(BorderFactory.createRaisedBevelBorder());
        btnVentasDelDia.addActionListener(e -> controller.mostrarVentasDelDia());

        buttonPanel.add(btnNuevaVenta);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnVentasPendientes);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(btnVentasDelDia);

        // Panel central para centrar los botones
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.add(buttonPanel);

        panel.add(title, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

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
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Búsqueda General:"), gbc);
        gbc.gridx = 1;
        JTextField txtBusqueda = new JTextField(20);
        txtBusqueda.setToolTipText("Buscar por factura, cliente o término general");
        filterPanel.add(txtBusqueda, gbc);

        gbc.gridx = 2;
        JButton btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.setBackground(new Color(0, 123, 255));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.addActionListener(e -> controller.buscarVentas(txtBusqueda.getText()));
        filterPanel.add(btnBuscar, gbc);

        // Estado
        gbc.gridx = 0;
        gbc.gridy = 1;
        filterPanel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        String[] estados = { "Todos", "PENDIENTE", "COMPLETADA", "CANCELADA" };
        JComboBox<String> cmbEstado = new JComboBox<>(estados);
        cmbEstado.addActionListener(e -> {
            String estado = (String) cmbEstado.getSelectedItem();
            filtrarPorEstado(estado);
        });
        filterPanel.add(cmbEstado, gbc);

        // Botón limpiar
        gbc.gridx = 2;
        JButton btnLimpiar = new JButton("🧹 Limpiar");
        btnLimpiar.setBackground(new Color(108, 117, 125));
        btnLimpiar.setForeground(Color.WHITE);
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
        actionPanel.setBorder(BorderFactory.createTitledBorder("Acciones"));

        JButton btnEditar = new JButton("✏️ Editar");
        btnEditar.setBackground(new Color(255, 193, 7));
        btnEditar.setForeground(Color.BLACK);
        btnEditar.setToolTipText("Editar venta seleccionada");
        btnEditar.addActionListener(e -> editarVentaSeleccionada());

        JButton btnCompletar = new JButton("✅ Completar");
        btnCompletar.setBackground(new Color(40, 167, 69));
        btnCompletar.setForeground(Color.WHITE);
        btnCompletar.setToolTipText("Completar venta y actualizar inventario");
        btnCompletar.addActionListener(e -> completarVentaSeleccionada());

        JButton btnCancelar = new JButton("❌ Cancelar");
        btnCancelar.setBackground(new Color(220, 53, 69));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setToolTipText("Cancelar venta y restaurar stock");
        btnCancelar.addActionListener(e -> cancelarVentaSeleccionada());

        JButton btnImprimir = new JButton("🖨️ Imprimir");
        btnImprimir.setBackground(new Color(108, 117, 125));
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.setToolTipText("Imprimir factura (próximamente)");
        btnImprimir.addActionListener(e -> imprimirVentaSeleccionada());

        JButton btnRefrescar = new JButton("🔄 Refrescar");
        btnRefrescar.setBackground(new Color(23, 162, 184));
        btnRefrescar.setForeground(Color.WHITE);
        btnRefrescar.setToolTipText("Actualizar lista de ventas");
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
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Estadísticas Generales"));

        statsPanel.add(createStatCard("Ventas Hoy", "0", new Color(40, 167, 69)));
        statsPanel.add(createStatCard("Total Vendido", "$0.00", new Color(0, 123, 255)));
        statsPanel.add(createStatCard("Ventas Pendientes", "0", new Color(255, 193, 7)));
        statsPanel.add(createStatCard("Ventas Completadas", "0", new Color(40, 167, 69)));

        // Panel de acciones rápidas
        JPanel quickActionsPanel = new JPanel(new FlowLayout());
        quickActionsPanel.setBorder(BorderFactory.createTitledBorder("Acciones Rápidas"));

        JButton btnVentasHoy = new JButton("📅 Ventas de Hoy");
        btnVentasHoy.setBackground(new Color(23, 162, 184));
        btnVentasHoy.setForeground(Color.WHITE);
        btnVentasHoy.addActionListener(e -> controller.mostrarVentasDelDia());

        JButton btnUltimasVentas = new JButton("🕒 Últimas Ventas");
        btnUltimasVentas.setBackground(new Color(108, 117, 125));
        btnUltimasVentas.setForeground(Color.WHITE);
        btnUltimasVentas.addActionListener(e -> controller.cargarDatos());

        JButton btnVentasPendientes = new JButton("⏳ Pendientes");
        btnVentasPendientes.setBackground(new Color(255, 193, 7));
        btnVentasPendientes.setForeground(Color.BLACK);
        btnVentasPendientes.addActionListener(e -> controller.mostrarVentasPendientes());

        JButton btnReportePDF = new JButton("📄 Generar Reporte PDF");
        btnReportePDF.setBackground(new Color(220, 53, 69));
        btnReportePDF.setForeground(Color.WHITE);
        btnReportePDF.setToolTipText("Generar reporte de ventas en formato PDF");
        btnReportePDF.addActionListener(e -> generarReportePDF());

        JButton btnExportar = new JButton("💾 Exportar CSV");
        btnExportar.setBackground(new Color(40, 167, 69));
        btnExportar.setForeground(Color.WHITE);
        btnExportar.setToolTipText("Exportar datos a archivo CSV");
        btnExportar.addActionListener(e -> exportarVentasCSV());

        quickActionsPanel.add(btnVentasHoy);
        quickActionsPanel.add(btnUltimasVentas);
        quickActionsPanel.add(btnVentasPendientes);
        quickActionsPanel.add(btnReportePDF);
        quickActionsPanel.add(btnExportar);

        panel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(quickActionsPanel, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 20));
        lblValue.setForeground(color);
        lblValue.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    // ===== MÉTODOS PARA EL CONTROLLER =====

    public void actualizarTablaVentas(List<Venta> ventas) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Guardar selección actual
                int selectedRow = tablaVentas.getSelectedRow();
                Integer selectedId = null;
                if (selectedRow >= 0) {
                    try {
                        selectedId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
                    } catch (Exception e) {
                        // Ignorar errores de selección
                    }
                }

                String[] columns = { "ID", "Factura", "Fecha", "Cliente", "Total", "Estado" };
                Object[][] data = new Object[ventas.size()][6];

                int newSelectedRow = -1;
                for (int i = 0; i < ventas.size(); i++) {
                    Venta venta = ventas.get(i);
                    data[i][0] = venta.getId();
                    data[i][1] = venta.getNumeroFactura();
                    data[i][2] = controller.formatearFecha(venta.getFechaVenta());
                    data[i][3] = venta.getClienteNombre() != null ? venta.getClienteNombre() : "Sin nombre";
                    data[i][4] = controller.formatearMonto(venta.getTotal());
                    data[i][5] = controller.convertirEstadoADisplay(venta.getEstado());

                    // Restaurar selección si es posible
                    if (selectedId != null && selectedId.equals(venta.getId())) {
                        newSelectedRow = i;
                    }
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
                tablaVentas.getColumnModel().getColumn(2).setPreferredWidth(150);
                tablaVentas.getColumnModel().getColumn(3).setPreferredWidth(200);
                tablaVentas.getColumnModel().getColumn(4).setPreferredWidth(100);
                tablaVentas.getColumnModel().getColumn(5).setPreferredWidth(100);

                // Restaurar selección
                if (newSelectedRow >= 0) {
                    tablaVentas.setRowSelectionInterval(newSelectedRow, newSelectedRow);
                }

                // Configurar renderer para colores por estado
                configurarRendererEstados();

            } catch (Exception e) {
                mostrarError("Error al actualizar tabla: " + e.getMessage());
            }
        });
    }

    public void actualizarEstadisticas(String estadisticas) {
        SwingUtilities.invokeLater(() -> {
            if (lblEstadisticas != null) {
                lblEstadisticas.setText(estadisticas);
            }
        });
    }

    // ===== MÉTODOS DE ACCIONES =====

    private void editarVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
                controller.mostrarFormularioNuevaVenta(); // Por ahora usar este
                mostrarInfo("Funcionalidad de editar venta en desarrollo.\nID de venta: " + ventaId);
            } catch (Exception e) {
                mostrarError("Error al editar venta: " + e.getMessage());
            }
        } else {
            mostrarError("Por favor seleccione una venta para editar");
        }
    }

    private void completarVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
                String numeroFactura = (String) tablaVentas.getValueAt(selectedRow, 1);
                String cliente = (String) tablaVentas.getValueAt(selectedRow, 3);
                String estadoActual = (String) tablaVentas.getValueAt(selectedRow, 5);

                // Verificar si se puede completar
                if (!controller.sePuedeCompletar(ventaId)) {
                    if ("Completada".equals(estadoActual)) {
                        mostrarInfo("La venta ya está completada.\nFactura: " + numeroFactura);
                    } else if ("Cancelada".equals(estadoActual)) {
                        mostrarError("No se puede completar una venta cancelada.\nFactura: " + numeroFactura);
                    } else {
                        mostrarError("La venta no se puede completar en su estado actual.\nFactura: " + numeroFactura);
                    }
                    return;
                }

                // Mostrar información y confirmar
                String mensaje = String.format(
                        "¿Confirma que desea completar esta venta?\n\n" +
                                "📋 Factura: %s\n" +
                                "👤 Cliente: %s\n" +
                                "📊 Estado actual: %s\n\n" +
                                "⚠️ Esta acción actualizará el inventario y no se podrá deshacer.",
                        numeroFactura, cliente, estadoActual);

                int confirmacion = JOptionPane.showConfirmDialog(this,
                        mensaje,
                        "Confirmar Completar Venta",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    // Mostrar progreso
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    // Ejecutar completar venta
                    boolean resultado = controller.completarVenta(ventaId);

                    // Restaurar cursor
                    setCursor(Cursor.getDefaultCursor());

                    if (resultado) {
                        mostrarInfo("✅ Venta completada exitosamente\nFactura: " + numeroFactura);
                    }
                }

            } catch (Exception e) {
                setCursor(Cursor.getDefaultCursor());
                mostrarError("Error inesperado al completar venta: " + e.getMessage());
                System.err.println("Error en completarVentaSeleccionada(): " + e.toString());
                e.printStackTrace();
            }
        } else {
            mostrarError("Por favor seleccione una venta para completar");
        }
    }

    private void cancelarVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
                String numeroFactura = (String) tablaVentas.getValueAt(selectedRow, 1);
                String cliente = (String) tablaVentas.getValueAt(selectedRow, 3);
                String estadoActual = (String) tablaVentas.getValueAt(selectedRow, 5);

                // Verificar si se puede cancelar
                if (!controller.sePuedeCancelar(ventaId)) {
                    if ("Cancelada".equals(estadoActual)) {
                        mostrarInfo("La venta ya está cancelada.\nFactura: " + numeroFactura);
                    } else {
                        mostrarError("La venta no se puede cancelar.\nFactura: " + numeroFactura);
                    }
                    return;
                }

                // Mensaje diferente según el estado actual
                String mensajeAdicional = "";
                int tipoIcon = JOptionPane.WARNING_MESSAGE;

                if ("Completada".equals(estadoActual)) {
                    mensajeAdicional = "\n\n⚠️ NOTA: Esta venta está COMPLETADA.\n" +
                            "Al cancelarla se restaurará el stock de los productos.";
                    tipoIcon = JOptionPane.WARNING_MESSAGE;
                }

                String mensaje = String.format(
                        "¿Confirma que desea cancelar esta venta?\n\n" +
                                "📋 Factura: %s\n" +
                                "👤 Cliente: %s\n" +
                                "📊 Estado actual: %s%s\n\n" +
                                "❌ Esta acción no se puede deshacer.",
                        numeroFactura, cliente, estadoActual, mensajeAdicional);

                int confirmacion = JOptionPane.showConfirmDialog(this,
                        mensaje,
                        "Confirmar Cancelar Venta",
                        JOptionPane.YES_NO_OPTION,
                        tipoIcon);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    // Mostrar progreso
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    // Ejecutar cancelar venta
                    boolean resultado = controller.cancelarVenta(ventaId);

                    // Restaurar cursor
                    setCursor(Cursor.getDefaultCursor());

                    if (resultado) {
                        mostrarInfo("❌ Venta cancelada exitosamente\nFactura: " + numeroFactura);
                    }
                }

            } catch (Exception e) {
                setCursor(Cursor.getDefaultCursor());
                mostrarError("Error inesperado al cancelar venta: " + e.getMessage());
                System.err.println("Error en cancelarVentaSeleccionada(): " + e.toString());
                e.printStackTrace();
            }
        } else {
            mostrarError("Por favor seleccione una venta para cancelar");
        }
    }

    private void imprimirVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
                mostrarInfo("Funcionalidad de impresión en desarrollo.\nVenta ID: " + ventaId);
            } catch (Exception e) {
                mostrarError("Error al imprimir venta: " + e.getMessage());
            }
        } else {
            mostrarError("Por favor seleccione una venta para imprimir");
        }
    }

    // ===== MÉTODOS DE FILTROS =====

    private void filtrarPorEstado(String estado) {
        if ("Todos".equals(estado)) {
            controller.cargarDatos();
        } else {
            controller.filtrarPorEstado(estado);
        }
    }

    private void generarReportePDF() {
        mostrarInfo("📄 Generación de reportes PDF\n\n" +
                   "Esta funcionalidad será implementada próximamente.\n" +
                   "Permitirá generar reportes detallados de ventas en formato PDF\n" +
                   "con gráficos y estadísticas completas.");
    }

    private void exportarVentasCSV() {
        mostrarInfo("💾 Exportación a CSV\n\n" +
                   "Esta funcionalidad será implementada próximamente.\n" +
                   "Permitirá exportar los datos de ventas a archivo CSV\n" +
                   "para análisis en Excel u otras herramientas.");
    }

    // ===== MÉTODOS DE CONFIGURACIÓN DE TABLA =====

    private void configurarRendererEstados() {
        // Renderer para la columna de estado (columna 5)
        tablaVentas.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);

                if (!isSelected) {
                    String estado = value.toString();
                    Color colorFondo = getColorFondoEstado(estado);
                    Color colorTexto = getColorTextoEstado(estado);

                    component.setBackground(colorFondo);
                    component.setForeground(colorTexto);
                }

                return component;
            }
        });
    }

    private Color getColorFondoEstado(String estado) {
        switch (estado) {
            case "Completada":
                return new Color(212, 237, 218); // Verde claro
            case "Pendiente":
                return new Color(255, 243, 205); // Amarillo claro
            case "Cancelada":
                return new Color(248, 215, 218); // Rojo claro
            default:
                return Color.WHITE;
        }
    }

    private Color getColorTextoEstado(String estado) {
        switch (estado) {
            case "Completada":
                return new Color(21, 87, 36); // Verde oscuro
            case "Pendiente":
                return new Color(133, 100, 4); // Amarillo oscuro
            case "Cancelada":
                return new Color(114, 28, 36); // Rojo oscuro
            default:
                return Color.BLACK;
        }
    }

    // ===== MÉTODOS DE UTILIDAD =====

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Venta getVentaSeleccionada() {
        int selectedRow = tablaVentas.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                int ventaId = (Integer) tablaVentas.getValueAt(selectedRow, 0);
                Optional<Venta> ventaOpt = controller.obtenerVentaPorId(ventaId);
                return ventaOpt.orElse(null);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public VentaController getController() {
        return controller;
    }
}