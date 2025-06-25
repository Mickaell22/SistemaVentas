package views.reportes;

import javax.swing.*;
import java.awt.*;

public class ReportePanel extends JPanel {
    
    public ReportePanel() {
        initializeComponents();
        setupLayout();
    }
    
    private void initializeComponents() {
        setBackground(Color.WHITE);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel de título
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // Panel principal con tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab Dashboard
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        
        // Tab Reportes de Ventas
        tabbedPane.addTab("Reportes de Ventas", createReportesVentasPanel());
        
        // Tab Reportes de Inventario
        tabbedPane.addTab("Reportes de Inventario", createReportesInventarioPanel());
        
        // Tab Reportes de Clientes
        tabbedPane.addTab("Reportes de Clientes", createReportesClientesPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(52, 58, 64));
        
        JLabel title = new JLabel("📊 Sistema de Reportes y Estadísticas");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        panel.add(title);
        return panel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de KPIs principales
        JPanel kpiPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        kpiPanel.setBorder(BorderFactory.createTitledBorder("KPIs Principales - Enero 2025"));
        
        // Primera fila de KPIs
        kpiPanel.add(createKPICard("Ventas del Mes", "$25,480.00", "+15.2%", Color.GREEN));
        kpiPanel.add(createKPICard("Ventas del Día", "$1,250.00", "+8.5%", Color.BLUE));
        kpiPanel.add(createKPICard("Productos Vendidos", "324", "+12.1%", Color.ORANGE));
        kpiPanel.add(createKPICard("Clientes Atendidos", "89", "+5.8%", Color.MAGENTA));
        
        // Segunda fila de KPIs
        kpiPanel.add(createKPICard("Ticket Promedio", "$35.67", "+3.2%", Color.CYAN));
        kpiPanel.add(createKPICard("Productos en Stock", "1,547", "-2.1%", Color.RED));
        kpiPanel.add(createKPICard("Nuevos Clientes", "23", "+18.9%", Color.GREEN));
        kpiPanel.add(createKPICard("Margen Promedio", "28.5%", "+1.4%", Color.BLUE));
        
        // Panel de gráficos
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsPanel.setBorder(BorderFactory.createTitledBorder("Análisis Visual"));
        
        // Gráfico de ventas
        JPanel salesChart = createChartPlaceholder("Ventas por Día - Últimos 30 días", "📈");
        chartsPanel.add(salesChart);
        
        // Gráfico de productos
        JPanel productsChart = createChartPlaceholder("Productos Más Vendidos", "📦");
        chartsPanel.add(productsChart);
        
        panel.add(kpiPanel, BorderLayout.NORTH);
        panel.add(chartsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createKPICard(String title, String value, String change, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 80));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 16));
        lblValue.setForeground(color);
        
        JLabel lblChange = new JLabel(change, SwingConstants.CENTER);
        lblChange.setFont(new Font("Arial", Font.PLAIN, 10));
        lblChange.setForeground(change.startsWith("+") ? Color.GREEN : Color.RED);
        lblChange.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(lblValue, BorderLayout.CENTER);
        centerPanel.add(lblChange, BorderLayout.SOUTH);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createChartPlaceholder(String title, String icon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(Color.WHITE);
        
        JLabel placeholder = new JLabel(icon + " " + title, SwingConstants.CENTER);
        placeholder.setFont(new Font("Arial", Font.ITALIC, 14));
        placeholder.setForeground(Color.GRAY);
        
        panel.add(placeholder, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReportesVentasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de filtros
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtros de Reporte"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Periodo
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Período:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cmbPeriodo = new JComboBox<>(new String[]{"Hoy", "Esta Semana", "Este Mes", "Trimestre", "Año", "Personalizado"});
        filterPanel.add(cmbPeriodo, gbc);
        
        // Fecha desde
        gbc.gridx = 2; gbc.gridy = 0;
        filterPanel.add(new JLabel("Desde:"), gbc);
        gbc.gridx = 3;
        JTextField txtFechaDesde = new JTextField(10);
        filterPanel.add(txtFechaDesde, gbc);
        
        // Fecha hasta
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("Hasta:"), gbc);
        gbc.gridx = 1;
        JTextField txtFechaHasta = new JTextField(10);
        filterPanel.add(txtFechaHasta, gbc);
        
        // Usuario
        gbc.gridx = 2; gbc.gridy = 1;
        filterPanel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 3;
        JComboBox<String> cmbUsuario = new JComboBox<>(new String[]{"Todos", "Vendedor 1", "Vendedor 2", "Cajero 1"});
        filterPanel.add(cmbUsuario, gbc);
        
        // Botones
        gbc.gridx = 0; gbc.gridy = 2;
        JButton btnGenerar = new JButton("Generar Reporte");
        btnGenerar.setBackground(new Color(40, 167, 69));
        btnGenerar.setForeground(Color.WHITE);
        filterPanel.add(btnGenerar, gbc);
        
        gbc.gridx = 1;
        JButton btnExportar = new JButton("Exportar Excel");
        btnExportar.setBackground(new Color(23, 162, 184));
        btnExportar.setForeground(Color.WHITE);
        filterPanel.add(btnExportar, gbc);
        
        gbc.gridx = 2;
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBackground(new Color(108, 117, 125));
        btnImprimir.setForeground(Color.WHITE);
        filterPanel.add(btnImprimir, gbc);
        
        // Área de resultados
        JTextArea resultArea = new JTextArea();
        resultArea.setText("REPORTE DE VENTAS - ENERO 2025\n" +
                          "================================\n\n" +
                          "RESUMEN EJECUTIVO:\n" +
                          "• Total de Ventas: $25,480.00\n" +
                          "• Número de Transacciones: 324\n" +
                          "• Ticket Promedio: $78.64\n" +
                          "• Productos Vendidos: 1,247 unidades\n\n" +
                          "TOP 5 PRODUCTOS MÁS VENDIDOS:\n" +
                          "1. Producto A - 45 unidades - $2,250.00\n" +
                          "2. Producto B - 38 unidades - $1,900.00\n" +
                          "3. Producto C - 32 unidades - $1,600.00\n" +
                          "4. Producto D - 28 unidades - $1,400.00\n" +
                          "5. Producto E - 25 unidades - $1,250.00\n\n" +
                          "VENTAS POR VENDEDOR:\n" +
                          "• Juan Pérez: $8,450.00 (33.2%)\n" +
                          "• María García: $7,230.00 (28.4%)\n" +
                          "• Carlos López: $5,890.00 (23.1%)\n" +
                          "• Ana Rodríguez: $3,910.00 (15.3%)\n\n" +
                          "ANÁLISIS POR MÉTODO DE PAGO:\n" +
                          "• Efectivo: $12,740.00 (50%)\n" +
                          "• Tarjeta: $8,926.00 (35%)\n" +
                          "• Transferencia: $3,814.00 (15%)\n\n" +
                          "TENDENCIAS:\n" +
                          "• Crecimiento vs. mes anterior: +15.2%\n" +
                          "• Día de mayor venta: Viernes\n" +
                          "• Hora pico: 14:00 - 16:00\n\n" +
                          "Este reporte se generó automáticamente el " + 
                          java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resultado del Reporte"));
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReportesInventarioPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de opciones
        JPanel optionsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Tipos de Reporte de Inventario"));
        
        JButton btnStockActual = new JButton("Stock Actual");
        btnStockActual.setPreferredSize(new Dimension(150, 60));
        optionsPanel.add(btnStockActual);
        
        JButton btnBajoStock = new JButton("Productos con Bajo Stock");
        optionsPanel.add(btnBajoStock);
        
        JButton btnSinStock = new JButton("Productos sin Stock");
        optionsPanel.add(btnSinStock);
        
        JButton btnValorizacion = new JButton("Valorización de Inventario");
        optionsPanel.add(btnValorizacion);
        
        JButton btnRotacion = new JButton("Rotación de Productos");
        optionsPanel.add(btnRotacion);
        
        JButton btnMovimientos = new JButton("Movimientos de Stock");
        optionsPanel.add(btnMovimientos);
        
        // Tabla de ejemplo
        String[] columns = {"Código", "Producto", "Stock Actual", "Stock Mínimo", "Precio", "Valor Total", "Estado"};
        Object[][] data = {
            {"P001", "Producto A", "15", "10", "$50.00", "$750.00", "Normal"},
            {"P002", "Producto B", "5", "10", "$30.00", "$150.00", "Bajo Stock"},
            {"P003", "Producto C", "0", "5", "$25.00", "$0.00", "Sin Stock"},
            {"P004", "Producto D", "25", "15", "$40.00", "$1,000.00", "Normal"},
            {"P005", "Producto E", "8", "10", "$35.00", "$280.00", "Bajo Stock"}
        };
        
        JTable table = new JTable(data, columns);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(new Color(52, 58, 64));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Estado Actual del Inventario"));
        
        panel.add(optionsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReportesClientesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel de métricas de clientes
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Métricas de Clientes"));
        
        metricsPanel.add(createMetricCard("Total Clientes", "156", Color.BLUE));
        metricsPanel.add(createMetricCard("Nuevos Este Mes", "23", Color.GREEN));
        metricsPanel.add(createMetricCard("Clientes Activos", "134", Color.ORANGE));
        metricsPanel.add(createMetricCard("Frecuencia Promedio", "2.3 compras/mes", Color.MAGENTA));
        metricsPanel.add(createMetricCard("Gasto Promedio", "$145.67", Color.CYAN));
        metricsPanel.add(createMetricCard("Tiempo Cliente", "8.5 meses", Color.RED));
        
        // Top clientes
        String[] columns = {"Cliente", "Total Compras", "Frecuencia", "Última Compra", "Categoría"};
        Object[][] data = {
            {"Juan Pérez", "$2,450.00", "15 compras", "15/01/2025", "VIP"},
            {"María García", "$1,890.00", "12 compras", "14/01/2025", "Premium"},
            {"Carlos López", "$1,560.00", "18 compras", "13/01/2025", "VIP"},
            {"Ana Rodríguez", "$1,230.00", "8 compras", "12/01/2025", "Regular"},
            {"Luis Martínez", "$980.00", "6 compras", "11/01/2025", "Regular"}
        };
        
        JTable table = new JTable(data, columns);
        table.setRowHeight(25);
        table.getTableHeader().setBackground(new Color(52, 58, 64));
        table.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Top 10 Clientes"));
        
        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(color, 2));
        card.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Arial", Font.BOLD, 16));
        lblValue.setForeground(color);
        lblValue.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }
}