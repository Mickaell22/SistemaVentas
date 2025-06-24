package views.ventas;

import controllers.VentaController;
import models.Venta;
import models.DetalleVenta;
import models.Cliente;
import models.Producto;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VentaFormDialog extends JDialog {
    
    private VentaController controller;
    private boolean esNueva;
    private Venta ventaActual;
    
    // Componentes del formulario
    private JTextField txtNumeroFactura;
    private JTextField txtBuscarCliente;
    private JButton btnBuscarCliente;
    private JLabel lblClienteSeleccionado;
    private JComboBox<String> cmbMetodoPago;
    private JTextArea txtObservaciones;
    
    // Componentes del carrito
    private JTextField txtCodigoProducto;
    private JTextField txtCantidad;
    private JButton btnAgregarProducto;
    private JButton btnBuscarProducto;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    
    // Totales
    private JLabel lblSubtotal;
    private JLabel lblDescuento;
    private JTextField txtDescuentoMonto;
    private JButton btnAplicarDescuento;
    private JLabel lblImpuestos;
    private JButton btnCalcularIVA;
    private JLabel lblTotal;
    
    // Botones principales
    private JButton btnGuardar;
    private JButton btnGuardarYCompletar;
    private JButton btnCancelar;
    private JButton btnLimpiarCarrito;
    
    // Variables de control
    private Cliente clienteSeleccionado;
    
    public VentaFormDialog(Frame parent, VentaController controller, boolean esNueva) {
        super(parent, esNueva ? "Nueva Venta" : "Editar Venta", true);
        this.controller = controller;
        this.esNueva = esNueva;
        
        initializeComponents();
        setupLayout();
        setupEvents();
        setupTable();
        
        if (esNueva) {
            controller.inicializarNuevaVenta();
            this.ventaActual = controller.getVentaActual();
            cargarDatosIniciales();
        }
        
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }
    
    private void initializeComponents() {
        // Información de la venta
        txtNumeroFactura = new JTextField(15);
        txtNumeroFactura.setEditable(false);
        txtNumeroFactura.setBackground(new Color(248, 249, 250));
        
        // Cliente
        txtBuscarCliente = new JTextField(20);
        txtBuscarCliente.setToolTipText("Buscar por nombre o documento del cliente");
        btnBuscarCliente = new JButton("🔍");
        lblClienteSeleccionado = new JLabel("Ningún cliente seleccionado");
        lblClienteSeleccionado.setForeground(Color.GRAY);
        
        // Método de pago
        cmbMetodoPago = new JComboBox<>(controller.getMetodosPago());
        
        // Observaciones
        txtObservaciones = new JTextArea(3, 30);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        
        // Carrito de compras
        txtCodigoProducto = new JTextField(15);
        txtCodigoProducto.setToolTipText("Código del producto");
        txtCantidad = new JTextField("1", 5);
        btnAgregarProducto = new JButton("➕ Agregar");
        btnBuscarProducto = new JButton("🔍 Buscar");
        
        // Tabla del carrito
        String[] columnasCarrito = {"Código", "Producto", "Cantidad", "Precio Unit.", "Descuento", "Subtotal", "Acciones"};
        modeloCarrito = new DefaultTableModel(columnasCarrito, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Solo cantidad es editable
            }
        };
        tablaCarrito = new JTable(modeloCarrito);
        
        // Totales
        lblSubtotal = new JLabel("$0.00");
        lblDescuento = new JLabel("$0.00");
        txtDescuentoMonto = new JTextField("0.00", 8);
        btnAplicarDescuento = new JButton("Aplicar");
        lblImpuestos = new JLabel("$0.00");
        btnCalcularIVA = new JButton("Calcular IVA");
        lblTotal = new JLabel("$0.00");
        
        // Botones principales
        btnGuardar = new JButton("💾 Guardar");
        btnGuardarYCompletar = new JButton("✅ Guardar y Completar");
        btnCancelar = new JButton("❌ Cancelar");
        btnLimpiarCarrito = new JButton("🧹 Limpiar Carrito");
        
        setupStyles();
    }
    
    private void setupStyles() {
        Font titleFont = new Font("Arial", Font.BOLD, 14);
        Font labelFont = new Font("Arial", Font.PLAIN, 12);
        
        // Estilos de totales
        lblSubtotal.setFont(titleFont);
        lblDescuento.setFont(titleFont);
        lblImpuestos.setFont(titleFont);
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(40, 167, 69));
        
        // Botones principales
        btnGuardar.setBackground(new Color(0, 123, 255));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        
        btnGuardarYCompletar.setBackground(new Color(40, 167, 69));
        btnGuardarYCompletar.setForeground(Color.WHITE);
        btnGuardarYCompletar.setFocusPainted(false);
        
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        
        btnLimpiarCarrito.setBackground(new Color(255, 193, 7));
        btnLimpiarCarrito.setForeground(Color.BLACK);
        btnLimpiarCarrito.setFocusPainted(false);
        
        // Tamaños de botones
        Dimension buttonSize = new Dimension(140, 35);
        btnGuardar.setPreferredSize(buttonSize);
        btnGuardarYCompletar.setPreferredSize(new Dimension(180, 35));
        btnCancelar.setPreferredSize(buttonSize);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Panel superior - Información de la venta
        JPanel panelInfo = createPanelInformacionVenta();
        panelPrincipal.add(panelInfo, BorderLayout.NORTH);
        
        // Panel central - Carrito de compras
        JPanel panelCarrito = createPanelCarrito();
        panelPrincipal.add(panelCarrito, BorderLayout.CENTER);
        
        // Panel inferior - Totales y botones
        JPanel panelInferior = createPanelInferior();
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);
        
        add(panelPrincipal, BorderLayout.CENTER);
    }
    
    private JPanel createPanelInformacionVenta() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Información de la Venta"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Número de factura
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nº Factura:"), gbc);
        gbc.gridx = 1;
        panel.add(txtNumeroFactura, gbc);
        
        // Método de pago
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Método de Pago:"), gbc);
        gbc.gridx = 3;
        panel.add(cmbMetodoPago, gbc);
        
        // Cliente
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1;
        panel.add(txtBuscarCliente, gbc);
        gbc.gridx = 2;
        panel.add(btnBuscarCliente, gbc);
        
        // Cliente seleccionado
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 4;
        panel.add(lblClienteSeleccionado, gbc);
        gbc.gridwidth = 1;
        
        // Observaciones
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JScrollPane(txtObservaciones), gbc);
        
        return panel;
    }
    
    private JPanel createPanelCarrito() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Carrito de Compras"));
        
        // Panel superior - Agregar productos
        JPanel panelAgregar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAgregar.add(new JLabel("Código:"));
        panelAgregar.add(txtCodigoProducto);
        panelAgregar.add(new JLabel("Cantidad:"));
        panelAgregar.add(txtCantidad);
        panelAgregar.add(btnAgregarProducto);
        panelAgregar.add(btnBuscarProducto);
        panelAgregar.add(Box.createHorizontalStrut(20));
        panelAgregar.add(btnLimpiarCarrito);
        
        panel.add(panelAgregar, BorderLayout.NORTH);
        
        // Tabla del carrito
        setupTableCarrito();
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.setPreferredSize(new Dimension(600, 200));
        panel.add(scrollCarrito, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Panel de totales
        JPanel panelTotales = createPanelTotales();
        panel.add(panelTotales, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnGuardar);
        panelBotones.add(btnGuardarYCompletar);
        panelBotones.add(btnCancelar);
        
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createPanelTotales() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Totales"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.EAST;
        
        // Subtotal
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1;
        panel.add(lblSubtotal, gbc);
        
        // Descuento
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Descuento:"), gbc);
        gbc.gridx = 1;
        panel.add(lblDescuento, gbc);
        gbc.gridx = 2;
        panel.add(new JLabel("$"), gbc);
        gbc.gridx = 3;
        panel.add(txtDescuentoMonto, gbc);
        gbc.gridx = 4;
        panel.add(btnAplicarDescuento, gbc);
        
        // Impuestos
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Impuestos (IVA):"), gbc);
        gbc.gridx = 1;
        panel.add(lblImpuestos, gbc);
        gbc.gridx = 2; gbc.gridwidth = 2;
        panel.add(btnCalcularIVA, gbc);
        gbc.gridwidth = 1;
        
        // Total
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("TOTAL:"), gbc);
        gbc.gridx = 1;
        panel.add(lblTotal, gbc);
        
        return panel;
    }
    
    private void setupTableCarrito() {
        tablaCarrito.setRowHeight(25);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Anchos de columnas
        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(80);  // Código
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(200); // Producto
        tablaCarrito.getColumnModel().getColumn(2).setPreferredWidth(80);  // Cantidad
        tablaCarrito.getColumnModel().getColumn(3).setPreferredWidth(100); // Precio Unit.
        tablaCarrito.getColumnModel().getColumn(4).setPreferredWidth(80);  // Descuento
        tablaCarrito.getColumnModel().getColumn(5).setPreferredWidth(100); // Subtotal
        tablaCarrito.getColumnModel().getColumn(6).setPreferredWidth(80);  // Acciones
        
        // Configurar columna de acciones
        tablaCarrito.getColumnModel().getColumn(6).setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
            JButton btnEliminar = new JButton("🗑️");
            btnEliminar.setToolTipText("Eliminar producto");
            return btnEliminar;
        });
        
        tablaCarrito.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            private JButton button;
            
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                button = new JButton("🗑️");
                button.addActionListener(e -> {
                    controller.removerProductoDelCarrito(row);
                    actualizarTablaCarrito();
                    fireEditingStopped();
                });
                return button;
            }
        });
    }
    
    private void setupEvents() {
        // Buscar cliente
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        txtBuscarCliente.addActionListener(e -> buscarClientePorDocumento());
        
        // Agregar producto
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        btnBuscarProducto.addActionListener(e -> buscarProducto());
        
        // Enter en código de producto
        txtCodigoProducto.addActionListener(e -> agregarProducto());
        
        // Aplicar descuento e impuestos
        btnAplicarDescuento.addActionListener(e -> aplicarDescuento());
        btnCalcularIVA.addActionListener(e -> calcularIVA());
        
        // Limpiar carrito
        btnLimpiarCarrito.addActionListener(e -> limpiarCarrito());
        
        // Botones principales
        btnGuardar.addActionListener(e -> guardarVenta(false));
        btnGuardarYCompletar.addActionListener(e -> guardarVenta(true));
        btnCancelar.addActionListener(e -> cancelar());
        
        // Validación en tiempo real de cantidad
        txtCantidad.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        });
        
        // Actualizar cantidad en tabla
        modeloCarrito.addTableModelListener(e -> {
            if (e.getColumn() == 2) { // Columna cantidad
                int row = e.getFirstRow();
                String nuevaCantidadStr = (String) modeloCarrito.getValueAt(row, 2);
                try {
                    int nuevaCantidad = Integer.parseInt(nuevaCantidadStr);
                    controller.actualizarCantidadEnCarrito(row, nuevaCantidad);
                    actualizarTablaCarrito();
                } catch (NumberFormatException ex) {
                    // Restaurar valor anterior
                    actualizarTablaCarrito();
                }
            }
        });
        
        // Manejar cierre de ventana
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmarCierre();
            }
        });
    }
    
    private void setupTable() {
        actualizarTablaCarrito();
    }
    
    // ===== MÉTODOS DE ACCIÓN =====
    
    private void buscarCliente() {
        String termino = txtBuscarCliente.getText().trim();
        if (termino.isEmpty()) {
            mostrarError("Ingrese un término de búsqueda");
            return;
        }
        
        // TODO: Implementar diálogo de búsqueda de clientes
        mostrarInfo("Funcionalidad de búsqueda de clientes pendiente");
    }
    
    private void buscarClientePorDocumento() {
        String documento = txtBuscarCliente.getText().trim();
        if (documento.isEmpty()) {
            return;
        }
        
        var clienteOpt = controller.buscarClientePorDocumento(documento);
        if (clienteOpt.isPresent()) {
            seleccionarCliente(clienteOpt.get());
        } else {
            lblClienteSeleccionado.setText("Cliente no encontrado: " + documento);
            lblClienteSeleccionado.setForeground(Color.RED);
            clienteSeleccionado = null;
        }
    }
    
    private void seleccionarCliente(Cliente cliente) {
        this.clienteSeleccionado = cliente;
        controller.seleccionarCliente(cliente.getId());
        
        lblClienteSeleccionado.setText(String.format("%s - %s (%s)", 
            cliente.getNumeroDocumento(), 
            cliente.getNombreCompleto(),
            cliente.getTipoDocumentoFormateado()));
        lblClienteSeleccionado.setForeground(new Color(40, 167, 69));
        
        txtBuscarCliente.setText(cliente.getNumeroDocumento());
    }
    
    private void buscarProducto() {
        // TODO: Implementar diálogo de búsqueda de productos
        mostrarInfo("Funcionalidad de búsqueda de productos pendiente");
    }
    
    private void agregarProducto() {
        String codigo = txtCodigoProducto.getText().trim();
        String cantidadStr = txtCantidad.getText().trim();
        
        if (codigo.isEmpty()) {
            mostrarError("Ingrese el código del producto");
            txtCodigoProducto.requestFocus();
            return;
        }
        
        if (!controller.validarCantidad(cantidadStr)) {
            mostrarError("Cantidad debe ser un número entero mayor a 0");
            txtCantidad.requestFocus();
            return;
        }
        
        int cantidad = Integer.parseInt(cantidadStr);
        
        if (controller.agregarProductoAlCarrito(codigo, cantidad)) {
            txtCodigoProducto.setText("");
            txtCantidad.setText("1");
            txtCodigoProducto.requestFocus();
            actualizarTablaCarrito();
        }
    }
    
    private void aplicarDescuento() {
        try {
            BigDecimal descuento = new BigDecimal(txtDescuentoMonto.getText().trim());
            if (controller.aplicarDescuento(descuento)) {
                actualizarTotales();
            }
        } catch (NumberFormatException e) {
            mostrarError("Monto de descuento inválido");
        }
    }
    
    private void calcularIVA() {
        BigDecimal iva = controller.calcularIVA();
        if (controller.aplicarImpuestos(iva)) {
            actualizarTotales();
        }
    }
    
    private void limpiarCarrito() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea limpiar todo el carrito?",
            "Confirmar Limpiar Carrito",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            controller.limpiarCarrito();
            actualizarTablaCarrito();
        }
    }
    
    private void guardarVenta(boolean completar) {
        if (!validarFormulario()) {
            return;
        }
        
        // Construir venta con datos del formulario
        Venta venta = construirVentaDeFormulario();
        
        // Guardar venta
        boolean resultado = controller.guardarVenta(venta, esNueva);
        
        if (resultado && completar) {
            // Completar venta si se solicitó
            controller.completarVenta(venta.getId());
        }
        
        if (resultado) {
            dispose();
        }
    }
    
    private void cancelar() {
        confirmarCierre();
    }
    
    private void confirmarCierre() {
        if (controller.hayProductosEnCarrito()) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Hay productos en el carrito. ¿Está seguro que desea cerrar sin guardar?",
                "Confirmar Cierre",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (option == JOptionPane.YES_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }
    
    // ===== MÉTODOS DE ACTUALIZACIÓN =====
    
    private void actualizarTablaCarrito() {
        modeloCarrito.setRowCount(0);
        
        List<DetalleVenta> carrito = controller.getCarritoTemporal();
        for (DetalleVenta detalle : carrito) {
            Object[] fila = {
                detalle.getProductoCodigo(),
                detalle.getProductoNombre(),
                detalle.getCantidad(),
                String.format("$%.2f", detalle.getPrecioUnitario()),
                String.format("$%.2f", detalle.getDescuento()),
                String.format("$%.2f", detalle.getSubtotal()),
                "Eliminar"
            };
            modeloCarrito.addRow(fila);
        }
        
        actualizarTotales();
    }
    
    private void actualizarTotales() {
        Venta venta = controller.getVentaActual();
        if (venta != null) {
            lblSubtotal.setText(String.format("$%.2f", venta.getSubtotal()));
            lblDescuento.setText(String.format("$%.2f", venta.getDescuento()));
            lblImpuestos.setText(String.format("$%.2f", venta.getImpuestos()));
            lblTotal.setText(String.format("$%.2f", venta.getTotal()));
        }
    }
    
    // ===== MÉTODOS PÚBLICOS PARA EL CONTROLLER =====
    
    public void cargarVenta(Venta venta) {
        this.ventaActual = venta;
        
        txtNumeroFactura.setText(venta.getNumeroFactura());
        cmbMetodoPago.setSelectedItem(controller.convertirMetodoPagoADisplay(venta.getMetodoPago()));
        txtObservaciones.setText(venta.getObservaciones() != null ? venta.getObservaciones() : "");
        
        // Cargar cliente si existe
        if (venta.getClienteNombre() != null) {
            lblClienteSeleccionado.setText(String.format("%s - %s", 
                venta.getClienteDocumento(), venta.getClienteNombre()));
            lblClienteSeleccionado.setForeground(new Color(40, 167, 69));
            txtBuscarCliente.setText(venta.getClienteDocumento());
        }
        
        actualizarTablaCarrito();
    }
    
    private void cargarDatosIniciales() {
        if (ventaActual != null) {
            txtNumeroFactura.setText(ventaActual.getNumeroFactura());
            cmbMetodoPago.setSelectedIndex(0); // Efectivo por defecto
        }
    }
    
    // ===== VALIDACIONES =====
    
    private boolean validarFormulario() {
        if (clienteSeleccionado == null) {
            mostrarError("Debe seleccionar un cliente");
            txtBuscarCliente.requestFocus();
            return false;
        }
        
        if (!controller.hayProductosEnCarrito()) {
            mostrarError("Debe agregar al menos un producto a la venta");
            txtCodigoProducto.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private Venta construirVentaDeFormulario() {
        Venta venta = controller.getVentaActual();
        
        if (venta == null) {
            venta = new Venta();
        }
        
        venta.setNumeroFactura(txtNumeroFactura.getText().trim());
        venta.setMetodoPago(controller.convertirMetodoPagoADB((String) cmbMetodoPago.getSelectedItem()));
        venta.setObservaciones(txtObservaciones.getText().trim());
        
        if (!esNueva && ventaActual != null) {
            venta.setId(ventaActual.getId());
        }
        
        return venta;
    }
    
    // ===== MENSAJES =====
    
    private void mostrarInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void mostrarError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}