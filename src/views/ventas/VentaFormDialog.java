package views.ventas;

import controllers.VentaController;
import models.Venta;
import models.DetalleVenta;
import models.Cliente;
import models.Producto;
import services.ClienteService;
import services.ProductoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class VentaFormDialog extends JDialog {

    private VentaController controller;
    private ClienteService clienteService;
    private ProductoService productoService;
    private boolean esNuevaVenta;

    // Componentes de la interfaz
    private JTextField txtNumeroFactura;
    private JTextField txtBuscarCliente;
    private JLabel lblClienteSeleccionado;
    private JTextField txtBuscarProducto;
    private JTable tablaCarrito;
    private DefaultTableModel modeloTablaCarrito;
    private JLabel lblSubtotal;
    private JTextField txtDescuento;
    private JTextField txtImpuestos;
    private JLabel lblTotal;
    private JComboBox<String> cmbMetodoPago;
    private JTextArea txtObservaciones;
    private JButton btnGuardar;
    private JButton btnCancelar;

    // Variables para el cliente seleccionado
    private Cliente clienteActual;

    public VentaFormDialog(Frame parent, VentaController controller, boolean esNuevaVenta) {
        super(parent, esNuevaVenta ? "Nueva Venta" : "Editar Venta", true);
        this.controller = controller;
        this.clienteService = ClienteService.getInstance();
        this.productoService = ProductoService.getInstance();
        this.esNuevaVenta = esNuevaVenta;

        initializeComponents();
        setupLayout();
        setupEvents();
        setupWindow();

        if (esNuevaVenta) {
            controller.inicializarNuevaVenta();
        }

        actualizarInterfaz();
    }

    private void initializeComponents() {
        // Información de la venta
        txtNumeroFactura = new JTextField(15);
        txtNumeroFactura.setEditable(false);

        // Búsqueda de cliente
        txtBuscarCliente = new JTextField(20);
        txtBuscarCliente.setToolTipText("Buscar por documento o nombre");
        lblClienteSeleccionado = new JLabel("Ningún cliente seleccionado");
        lblClienteSeleccionado.setForeground(Color.RED);

        // Búsqueda de productos
        txtBuscarProducto = new JTextField(20);
        txtBuscarProducto.setToolTipText("Buscar producto por código o nombre");

        // Tabla del carrito
        String[] columnasCarrito = { "Código", "Producto", "Precio", "Cantidad", "Subtotal" };
        modeloTablaCarrito = new DefaultTableModel(columnasCarrito, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Solo la columna cantidad es editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3)
                    return Integer.class;
                return String.class;
            }
        };

        tablaCarrito = new JTable(modeloTablaCarrito);
        tablaCarrito.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCarrito.setRowHeight(25);

        // Configurar anchos de columnas
        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(80); // Código
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(200); // Producto
        tablaCarrito.getColumnModel().getColumn(2).setPreferredWidth(80); // Precio
        tablaCarrito.getColumnModel().getColumn(3).setPreferredWidth(80); // Cantidad
        tablaCarrito.getColumnModel().getColumn(4).setPreferredWidth(100); // Subtotal

        // Totales
        lblSubtotal = new JLabel("$0.00");
        lblSubtotal.setFont(new Font("Arial", Font.BOLD, 14));

        txtDescuento = new JTextField("0.00", 10);
        txtImpuestos = new JTextField("0.00", 10);

        lblTotal = new JLabel("$0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotal.setForeground(new Color(0, 128, 0));

        // Método de pago y observaciones
        cmbMetodoPago = new JComboBox<>(controller.getMetodosPago());

        txtObservaciones = new JTextArea(3, 30);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);

        // Botones
        btnGuardar = new JButton(esNuevaVenta ? "Guardar Venta" : "Actualizar Venta");
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(130, 35));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setPreferredSize(new Dimension(100, 35));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Panel principal SIN scroll - cambio principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel superior - Información de la venta
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Panel central - Carrito
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Panel inferior - Totales y botones
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // CAMBIO: Agregar directamente sin JScrollPane
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Información de la Venta"));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Número de factura
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Número de Factura:"), gbc);
        gbc.gridx = 1;
        panel.add(txtNumeroFactura, gbc);

        // Fecha
        gbc.gridx = 2;
        gbc.gridy = 0;
        panel.add(new JLabel("Fecha:"), gbc);
        gbc.gridx = 3;
        String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        panel.add(new JLabel(fechaActual), gbc);

        // Cliente
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Buscar Cliente:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(txtBuscarCliente, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        JButton btnBuscarCliente = new JButton("Buscar");
        btnBuscarCliente.addActionListener(e -> buscarCliente());
        panel.add(btnBuscarCliente, gbc);

        // Cliente seleccionado
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(lblClienteSeleccionado, gbc);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Productos de la Venta"));

        // Panel de búsqueda de productos
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Buscar Producto:"));
        searchPanel.add(txtBuscarProducto);

        JButton btnAgregarProducto = new JButton("Agregar");
        btnAgregarProducto.setBackground(new Color(40, 167, 69));
        btnAgregarProducto.setForeground(Color.WHITE);
        btnAgregarProducto.addActionListener(e -> agregarProducto());
        searchPanel.add(btnAgregarProducto);

        JButton btnRemoverProducto = new JButton("Remover");
        btnRemoverProducto.setBackground(new Color(220, 53, 69));
        btnRemoverProducto.setForeground(Color.WHITE);
        btnRemoverProducto.addActionListener(e -> removerProductoSeleccionado());
        searchPanel.add(btnRemoverProducto);

        JButton btnLimpiarCarrito = new JButton("Limpiar Todo");
        btnLimpiarCarrito.setBackground(new Color(255, 193, 7));
        btnLimpiarCarrito.setForeground(Color.BLACK);
        btnLimpiarCarrito.addActionListener(e -> limpiarCarritoCompleto());
        searchPanel.add(btnLimpiarCarrito);

        panel.add(searchPanel, BorderLayout.NORTH);

        // CAMBIO: Tabla del carrito con altura específica
        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        scrollCarrito.setPreferredSize(new Dimension(0, 250)); // Reducir altura
        panel.add(scrollCarrito, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel de totales
        JPanel totalesPanel = new JPanel(new GridBagLayout());
        totalesPanel.setBorder(BorderFactory.createTitledBorder("Totales"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Subtotal
        gbc.gridx = 0;
        gbc.gridy = 0;
        totalesPanel.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1;
        totalesPanel.add(lblSubtotal, gbc);

        // Descuento
        gbc.gridx = 0;
        gbc.gridy = 1;
        totalesPanel.add(new JLabel("Descuento:"), gbc);
        gbc.gridx = 1;
        totalesPanel.add(txtDescuento, gbc);

        // Impuestos/IVA
        gbc.gridx = 0;
        gbc.gridy = 2;
        totalesPanel.add(new JLabel("IVA:"), gbc);
        gbc.gridx = 1;
        totalesPanel.add(txtImpuestos, gbc);

        gbc.gridx = 2;
        JButton btnCalcularIVA = new JButton("Calcular IVA");
        btnCalcularIVA.addActionListener(e -> calcularIVA());
        totalesPanel.add(btnCalcularIVA, gbc);

        // Total
        gbc.gridx = 0;
        gbc.gridy = 3;
        totalesPanel.add(new JLabel("TOTAL:"), gbc);
        gbc.gridx = 1;
        totalesPanel.add(lblTotal, gbc);

        // Panel de detalles adicionales
        JPanel detallesPanel = new JPanel(new GridBagLayout());
        detallesPanel.setBorder(BorderFactory.createTitledBorder("Detalles Adicionales"));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Método de pago
        gbc.gridx = 0;
        gbc.gridy = 0;
        detallesPanel.add(new JLabel("Método de Pago:"), gbc);
        gbc.gridx = 1;
        detallesPanel.add(cmbMetodoPago, gbc);

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        detallesPanel.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1;
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setPreferredSize(new Dimension(250, 60));
        detallesPanel.add(scrollObs, gbc);

        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);

        // Ensamblar panel inferior
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(totalesPanel, BorderLayout.WEST);
        leftPanel.add(detallesPanel, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupEvents() {
        // Enter en búsqueda de cliente
        txtBuscarCliente.addActionListener(e -> buscarCliente());

        // Enter en búsqueda de producto
        txtBuscarProducto.addActionListener(e -> agregarProducto());

        // Cambios en descuento e impuestos
        txtDescuento.addActionListener(e -> aplicarDescuento());
        txtImpuestos.addActionListener(e -> aplicarImpuestos());

        // Cambios en la tabla del carrito
        modeloTablaCarrito.addTableModelListener(e -> {
            if (e.getColumn() == 3) { // Columna cantidad
                actualizarCantidadProducto(e.getFirstRow());
            }
        });

        // Botones principales
        btnGuardar.addActionListener(e -> guardarVenta());
        btnCancelar.addActionListener(e -> cancelar());

        // Escape para cancelar
        getRootPane().registerKeyboardAction(e -> cancelar(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void setupWindow() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);

        // CAMBIO: Establecer tamaño específico más grande
        setSize(950, 750); // Tamaño más grande para evitar scroll
        setMinimumSize(new Dimension(900, 700));

        setLocationRelativeTo(getParent());
    }

    // ===== MÉTODOS DE FUNCIONALIDAD =====

    private void buscarCliente() {
        String termino = txtBuscarCliente.getText().trim();
        if (termino.isEmpty()) {
            mostrarError("Ingrese un documento o nombre para buscar");
            return;
        }

        try {
            // Buscar por documento primero usando el controller
            var clienteOpt = controller.buscarClientePorDocumento(termino);
            if (clienteOpt.isPresent()) {
                seleccionarCliente(clienteOpt.get());
                return;
            }

            // Si no se encuentra por documento, hacer búsqueda local de todos los clientes
            try {
                List<Cliente> todosLosClientes = clienteService.obtenerTodosLosClientes();
                List<Cliente> clientesEncontrados = buscarClientesLocalmente(todosLosClientes, termino);

                if (clientesEncontrados.isEmpty()) {
                    mostrarError("No se encontraron clientes con ese criterio");
                    return;
                }

                if (clientesEncontrados.size() == 1) {
                    seleccionarCliente(clientesEncontrados.get(0));
                } else {
                    // Mostrar lista para seleccionar
                    mostrarListaClientes(clientesEncontrados);
                }
            } catch (Exception serviceError) {
                // Si no existe el método obtenerTodosLosClientes, mostrar mensaje
                mostrarError("Búsqueda de clientes no disponible. Use el documento exacto.");
            }

        } catch (Exception e) {
            mostrarError("Error al buscar cliente: " + e.getMessage());
        }
    }

    /**
     * Busca clientes localmente en una lista
     */
    private List<Cliente> buscarClientesLocalmente(List<Cliente> clientes, String termino) {
        List<Cliente> clientesEncontrados = new ArrayList<>();
        String terminoBusqueda = termino.toLowerCase();

        for (Cliente cliente : clientes) {
            boolean coincideNombre = cliente.getNombreCompleto() != null &&
                    cliente.getNombreCompleto().toLowerCase().contains(terminoBusqueda);
            boolean coincideDocumento = cliente.getNumeroDocumento() != null &&
                    cliente.getNumeroDocumento().contains(termino);

            if (coincideNombre || coincideDocumento) {
                clientesEncontrados.add(cliente);
            }
        }

        return clientesEncontrados;
    }

    private void seleccionarCliente(Cliente cliente) {
        this.clienteActual = cliente;
        lblClienteSeleccionado.setText(cliente.getNombreCompleto() + " - " + cliente.getNumeroDocumento());
        lblClienteSeleccionado.setForeground(new Color(0, 128, 0));
        controller.seleccionarCliente(cliente.getId());
        txtBuscarCliente.setText("");
    }

    private void mostrarListaClientes(List<Cliente> clientes) {
        String[] nombres = clientes.stream()
                .map(c -> c.getNombreCompleto() + " - " + c.getNumeroDocumento())
                .toArray(String[]::new);

        String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un cliente:",
                "Múltiples clientes encontrados",
                JOptionPane.QUESTION_MESSAGE,
                null,
                nombres,
                nombres[0]);

        if (seleccion != null) {
            int index = java.util.Arrays.asList(nombres).indexOf(seleccion);
            seleccionarCliente(clientes.get(index));
        }
    }

    private void agregarProducto() {
        String termino = txtBuscarProducto.getText().trim();
        if (termino.isEmpty()) {
            mostrarError("Ingrese un código o nombre de producto");
            return;
        }

        try {
            // CAMBIO: Intentar agregar por código primero SIN mostrar error si falla
            if (controller.agregarProductoAlCarrito(termino, 1)) {
                txtBuscarProducto.setText("");
                actualizarTablaCarrito();
                actualizarTotales();
                return;
            }

            // Si no funciona como código, buscar productos
            List<Producto> productos = controller.buscarProductosParaVenta(termino);
            if (productos.isEmpty()) {
                mostrarError("No se encontraron productos con ese criterio: " + termino);
                return;
            }

            if (productos.size() == 1) {
                if (controller.agregarProductoAlCarrito(productos.get(0).getCodigo(), 1)) {
                    txtBuscarProducto.setText("");
                    actualizarTablaCarrito();
                    actualizarTotales();
                }
            } else {
                // Mostrar lista para seleccionar
                mostrarListaProductos(productos);
            }

        } catch (Exception e) {
            mostrarError("Error al agregar producto: " + e.getMessage());
        }
    }

    private void mostrarListaProductos(List<Producto> productos) {
        String[] nombres = productos.stream()
                .map(p -> p.getCodigo() + " - " + p.getNombre() + " ($" + p.getPrecioVenta() + ")")
                .toArray(String[]::new);

        String seleccion = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione un producto:",
                "Múltiples productos encontrados",
                JOptionPane.QUESTION_MESSAGE,
                null,
                nombres,
                nombres[0]);

        if (seleccion != null) {
            int index = java.util.Arrays.asList(nombres).indexOf(seleccion);
            if (controller.agregarProductoAlCarrito(productos.get(index).getId(), 1)) {
                txtBuscarProducto.setText("");
                actualizarTablaCarrito();
                actualizarTotales();
            }
        }
    }

    private void removerProductoSeleccionado() {
        int selectedRow = tablaCarrito.getSelectedRow();
        if (selectedRow >= 0) {
            if (controller.removerProductoDelCarrito(selectedRow)) {
                actualizarTablaCarrito();
                actualizarTotales();
            }
        } else {
            mostrarError("Seleccione un producto para remover");
        }
    }

    private void limpiarCarritoCompleto() {
        if (controller.hayProductosEnCarrito()) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "¿Está seguro que desea limpiar todo el carrito?",
                    "Confirmar Limpiar Carrito",
                    JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                controller.limpiarCarrito();
                actualizarTablaCarrito();
                actualizarTotales();
            }
        } else {
            mostrarInfo("El carrito ya está vacío");
        }
    }

    private void actualizarCantidadProducto(int row) {
        try {
            Object value = modeloTablaCarrito.getValueAt(row, 3);
            int nuevaCantidad = Integer.parseInt(value.toString());

            if (controller.actualizarCantidadEnCarrito(row, nuevaCantidad)) {
                actualizarTotales();
                // Actualizar solo el subtotal de esa fila
                List<DetalleVenta> carrito = controller.getCarritoTemporal();
                if (row < carrito.size()) {
                    DetalleVenta detalle = carrito.get(row);
                    modeloTablaCarrito.setValueAt(
                            controller.formatearMonto(detalle.getSubtotal()),
                            row, 4);
                }
            }
        } catch (NumberFormatException e) {
            mostrarError("Cantidad inválida");
            actualizarTablaCarrito(); // Revertir cambio
        }
    }

    private void calcularIVA() {
        BigDecimal iva = controller.calcularIVA();
        txtImpuestos.setText(iva.toString());
        aplicarImpuestos();
    }

    private void aplicarDescuento() {
        try {
            BigDecimal descuento = new BigDecimal(txtDescuento.getText());
            controller.aplicarDescuento(descuento);
            actualizarTotales();
        } catch (NumberFormatException e) {
            mostrarError("Descuento inválido");
            txtDescuento.setText("0.00");
        }
    }

    private void aplicarImpuestos() {
        try {
            BigDecimal impuestos = new BigDecimal(txtImpuestos.getText());
            controller.aplicarImpuestos(impuestos);
            actualizarTotales();
        } catch (NumberFormatException e) {
            mostrarError("Impuestos inválidos");
            txtImpuestos.setText("0.00");
        }
    }

    private void guardarVenta() {
        try {
            // Validaciones
            if (clienteActual == null) {
                mostrarError("Debe seleccionar un cliente");
                return;
            }

            if (!controller.hayProductosEnCarrito()) {
                mostrarError("Debe agregar al menos un producto");
                return;
            }

            // CAMBIO: Obtener venta actual y validar
            Venta venta = controller.getVentaActual();
            if (venta == null) {
                mostrarError("Error: No hay venta activa. Intente crear una nueva venta.");
                return;
            }

            // CAMBIO: Asegurar que los datos del cliente están en la venta
            venta.setClienteId(clienteActual.getId());
            venta.setClienteNombre(clienteActual.getNombreCompleto());
            venta.setClienteDocumento(clienteActual.getNumeroDocumento());

            // Actualizar otros campos
            venta.setMetodoPago(controller.convertirMetodoPagoADB((String) cmbMetodoPago.getSelectedItem()));
            venta.setObservaciones(txtObservaciones.getText().trim());

            // Aplicar descuento e impuestos actuales
            try {
                BigDecimal descuento = new BigDecimal(txtDescuento.getText().trim());
                venta.setDescuento(descuento);
            } catch (NumberFormatException e) {
                venta.setDescuento(BigDecimal.ZERO);
            }

            try {
                BigDecimal impuestos = new BigDecimal(txtImpuestos.getText().trim());
                venta.setImpuestos(impuestos);
            } catch (NumberFormatException e) {
                venta.setImpuestos(BigDecimal.ZERO);
            }

            // CAMBIO: Mostrar mensaje de guardando
            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            // Guardar
            boolean resultado = controller.guardarVenta(venta, esNuevaVenta);

            if (resultado) {
                dispose(); // Cerrar solo si se guardó exitosamente
            } else {
                // CAMBIO: Restaurar botón si falla
                btnGuardar.setEnabled(true);
                btnGuardar.setText(esNuevaVenta ? "Guardar Venta" : "Actualizar Venta");
            }

        } catch (Exception e) {
            mostrarError("Error al guardar venta: " + e.getMessage());
            System.err.println("Error en guardarVenta(): " + e.toString());
            e.printStackTrace();

            // Restaurar botón
            btnGuardar.setEnabled(true);
            btnGuardar.setText(esNuevaVenta ? "Guardar Venta" : "Actualizar Venta");
        }
    }

    private void cancelar() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cancelar? Se perderán los cambios.",
                "Confirmar Cancelación",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    // ===== MÉTODOS DE ACTUALIZACIÓN DE INTERFAZ =====

    private void actualizarInterfaz() {
        Venta ventaActual = controller.getVentaActual();
        if (ventaActual != null) {
            txtNumeroFactura.setText(ventaActual.getNumeroFactura());

            // Verificar cliente de manera segura
            try {
                if (ventaActual.getClienteId() > 0) {
                    String nombreCliente = ventaActual.getClienteNombre();
                    if (nombreCliente != null && !nombreCliente.trim().isEmpty()) {
                        lblClienteSeleccionado.setText(nombreCliente);
                        lblClienteSeleccionado.setForeground(new Color(0, 128, 0));
                    }
                }
            } catch (Exception e) {
                // Si hay error accediendo al cliente, mantener el estado inicial
                lblClienteSeleccionado.setText("Ningún cliente seleccionado");
                lblClienteSeleccionado.setForeground(Color.RED);
            }

            if (ventaActual.getDescuento() != null) {
                txtDescuento.setText(ventaActual.getDescuento().toString());
            }

            if (ventaActual.getImpuestos() != null) {
                txtImpuestos.setText(ventaActual.getImpuestos().toString());
            }

            if (ventaActual.getMetodoPago() != null) {
                String metodoDisplay = controller.convertirMetodoPagoADisplay(ventaActual.getMetodoPago());
                cmbMetodoPago.setSelectedItem(metodoDisplay);
            }

            if (ventaActual.getObservaciones() != null) {
                txtObservaciones.setText(ventaActual.getObservaciones());
            }
        }

        actualizarTablaCarrito();
        actualizarTotales();
    }

    private void actualizarTablaCarrito() {
        modeloTablaCarrito.setRowCount(0);

        List<DetalleVenta> carrito = controller.getCarritoTemporal();
        for (DetalleVenta detalle : carrito) {
            Object[] row = {
                    detalle.getProductoCodigo() != null ? detalle.getProductoCodigo() : "N/A",
                    detalle.getProductoNombre() != null ? detalle.getProductoNombre() : "Producto",
                    String.format("$%.2f", detalle.getPrecioUnitario()),
                    detalle.getCantidad(),
                    String.format("$%.2f", detalle.getSubtotal())
            };
            modeloTablaCarrito.addRow(row);
        }

        // Actualizar título del panel con cantidad de productos
        int totalProductos = controller.getCantidadTotalCarrito();
        if (totalProductos > 0) {
            // Buscar el panel padre y actualizar su título
            setBorderTitle("Productos de la Venta (" + totalProductos + " productos)");
        } else {
            setBorderTitle("Productos de la Venta");
        }
    }

    private void setBorderTitle(String title) {
        // Este método ayuda a actualizar el título del borde del panel del carrito
        // Se implementaría si es necesario mostrar el conteo de productos
    }

    private void actualizarTotales() {
        Venta ventaActual = controller.getVentaActual();
        if (ventaActual != null) {
            lblSubtotal.setText(controller.formatearMonto(ventaActual.getSubtotal()));
            lblTotal.setText(controller.formatearMonto(ventaActual.getTotal()));
        }
    }

    // ===== MÉTODOS PÚBLICOS =====

    public void cargarVenta(Venta venta) {
        // Método para cargar una venta existente en modo edición
        actualizarInterfaz();
    }

    // ===== UTILIDADES =====

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}