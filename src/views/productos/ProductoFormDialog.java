package views.productos;

import controllers.ProductoController;
import models.Producto;
import models.Categoria;
import models.Proveedor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

public class ProductoFormDialog extends JDialog {
    
    private ProductoController controller;
    private boolean esNuevo;
    private Producto productoActual;
    
    // Componentes del formulario
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JComboBox<Categoria> cmbCategoria;
    private JComboBox<Proveedor> cmbProveedor;
    private JTextField txtPrecioCompra;
    private JTextField txtPrecioVenta;
    private JTextField txtStockActual;
    private JTextField txtStockMinimo;
    private JComboBox<String> cmbUnidadMedida;
    private JCheckBox chkActivo;
    
    // Botones
    private JButton btnGuardar;
    private JButton btnCancelar;
    private JButton btnGenerarCodigo;
    private JButton btnNuevaCategoria;
    private JButton btnNuevoProveedor;
    
    // Labels informativos
    private JLabel lblMargen;
    private JLabel lblPorcentajeMargen;
    
    public ProductoFormDialog(Frame parent, ProductoController controller, boolean esNuevo) {
        super(parent, esNuevo ? "Nuevo Producto" : "Editar Producto", true);
        this.controller = controller;
        this.esNuevo = esNuevo;
        
        initializeComponents();
        setupLayout();
        setupEvents();
        cargarDatosIniciales();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // Campos de texto
        txtCodigo = new JTextField(15);
        txtNombre = new JTextField(25);
        txtDescripcion = new JTextArea(3, 25);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        
        txtPrecioCompra = new JTextField(10);
        txtPrecioVenta = new JTextField(10);
        txtStockActual = new JTextField(10);
        txtStockMinimo = new JTextField(10);
        
        // ComboBoxes
        cmbCategoria = new JComboBox<>();
        cmbProveedor = new JComboBox<>();
        
        String[] unidades = {"UNIDAD", "KILOGRAMO", "GRAMO", "LITRO", "METRO", "CAJA", "PAQUETE"};
        cmbUnidadMedida = new JComboBox<>(unidades);
        
        // Checkbox
        chkActivo = new JCheckBox("Activo");
        chkActivo.setSelected(true);
        
        // Botones principales
        btnGuardar = new JButton("💾 Guardar");
        btnCancelar = new JButton("❌ Cancelar");
        btnGenerarCodigo = new JButton("🎲 Generar");
        btnNuevaCategoria = new JButton("➕");
        btnNuevoProveedor = new JButton("➕");
        
        // Labels informativos
        lblMargen = new JLabel("Margen: $0.00");
        lblPorcentajeMargen = new JLabel("0.00%");
        
        // Configurar estilos
        setupStyles();
        
        // Configurar según modo
        if (!esNuevo) {
            txtCodigo.setEditable(false);
            txtCodigo.setBackground(new Color(248, 249, 250));
        }
    }
    
    private void setupStyles() {
        Font labelFont = new Font("Arial", Font.PLAIN, 12);
        Font fieldFont = new Font("Arial", Font.PLAIN, 12);
        
        // Tamaño de componentes
        Dimension fieldSize = new Dimension(200, 25);
        Dimension comboSize = new Dimension(200, 25);
        
        txtCodigo.setPreferredSize(fieldSize);
        txtNombre.setPreferredSize(new Dimension(300, 25));
        cmbCategoria.setPreferredSize(comboSize);
        cmbProveedor.setPreferredSize(comboSize);
        
        // Botones de acción
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        
        btnGenerarCodigo.setToolTipText("Generar código automático");
        btnNuevaCategoria.setToolTipText("Crear nueva categoría");
        btnNuevoProveedor.setToolTipText("Crear nuevo proveedor");
        
        // Labels informativos
        lblMargen.setFont(new Font("Arial", Font.BOLD, 12));
        lblPorcentajeMargen.setFont(new Font("Arial", Font.BOLD, 12));
        lblMargen.setForeground(new Color(40, 167, 69));
        lblPorcentajeMargen.setForeground(new Color(40, 167, 69));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Código
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Código:*"), gbc);
        gbc.gridx = 1;
        JPanel panelCodigo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelCodigo.add(txtCodigo);
        if (esNuevo) {
            panelCodigo.add(Box.createHorizontalStrut(5));
            panelCodigo.add(btnGenerarCodigo);
        }
        panelFormulario.add(panelCodigo, gbc);
        
        // Nombre
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Nombre:*"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNombre, gbc);
        
        // Descripción
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelFormulario.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panelFormulario.add(new JScrollPane(txtDescripcion), gbc);
        
        // Categoría
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Categoría:*"), gbc);
        gbc.gridx = 1;
        JPanel panelCategoria = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelCategoria.add(cmbCategoria);
        panelCategoria.add(Box.createHorizontalStrut(5));
        panelCategoria.add(btnNuevaCategoria);
        panelFormulario.add(panelCategoria, gbc);
        
        // Proveedor
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Proveedor:*"), gbc);
        gbc.gridx = 1;
        JPanel panelProveedor = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelProveedor.add(cmbProveedor);
        panelProveedor.add(Box.createHorizontalStrut(5));
        panelProveedor.add(btnNuevoProveedor);
        panelFormulario.add(panelProveedor, gbc);
        
        // Precios en panel separado
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panelFormulario.add(crearPanelPrecios(), gbc);
        
        // Stock en panel separado
        row++;
        gbc.gridy = row;
        panelFormulario.add(crearPanelStock(), gbc);
        
        // Unidad de medida
        row++;
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Unidad:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(cmbUnidadMedida, gbc);
        
        // Estado
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        panelFormulario.add(chkActivo, gbc);
        
        add(panelFormulario, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelPrecios() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Precios"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Precio compra
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Precio Compra:*"), gbc);
        gbc.gridx = 1;
        panel.add(txtPrecioCompra, gbc);
        
        // Precio venta
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Precio Venta:*"), gbc);
        gbc.gridx = 3;
        panel.add(txtPrecioVenta, gbc);
        
        // Margen
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Margen:"), gbc);
        gbc.gridx = 1;
        panel.add(lblMargen, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("% Margen:"), gbc);
        gbc.gridx = 3;
        panel.add(lblPorcentajeMargen, gbc);
        
        return panel;
    }
    
    private JPanel crearPanelStock() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Inventario"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Stock actual
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Stock Actual:*"), gbc);
        gbc.gridx = 1;
        panel.add(txtStockActual, gbc);
        
        // Stock mínimo
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(new JLabel("Stock Mínimo:*"), gbc);
        gbc.gridx = 3;
        panel.add(txtStockMinimo, gbc);
        
        return panel;
    }
    
    private void setupEvents() {
        // Botón guardar
        btnGuardar.addActionListener(e -> guardarProducto());
        
        // Botón cancelar
        btnCancelar.addActionListener(e -> dispose());
        
        // Botón generar código
        btnGenerarCodigo.addActionListener(e -> {
            String codigo = controller.generarCodigoProducto();
            txtCodigo.setText(codigo);
        });
        
        // Botón nueva categoría
        btnNuevaCategoria.addActionListener(e -> {
            controller.mostrarFormularioNuevaCategoria();
            cargarCategorias(); // Recargar después de crear
        });
        
        // Botón nuevo proveedor
        btnNuevoProveedor.addActionListener(e -> {
            controller.mostrarFormularioNuevoProveedor();
            cargarProveedores(); // Recargar después de crear
        });
        
        // Calcular margen cuando cambien los precios
        txtPrecioCompra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularMargen();
            }
        });
        
        txtPrecioVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularMargen();
            }
        });
        
        // Validación en tiempo real
        txtStockActual.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    evt.consume();
                }
            }
        });
        
        txtStockMinimo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                    evt.consume();
                }
            }
        });
    }
    
    private void cargarDatosIniciales() {
        cargarCategorias();
        cargarProveedores();
        
        if (esNuevo) {
            // Generar código automáticamente para productos nuevos
            txtCodigo.setText(controller.generarCodigoProducto());
            txtStockActual.setText("0");
            txtStockMinimo.setText("5");
        }
    }
    
    private void cargarCategorias() {
        List<Categoria> categorias = controller.obtenerCategorias();
        cmbCategoria.removeAllItems();
        
        if (categorias.isEmpty()) {
            cmbCategoria.addItem(new Categoria(0, "Sin categorías", ""));
        } else {
            for (Categoria categoria : categorias) {
                cmbCategoria.addItem(categoria);
            }
        }
    }
    
    private void cargarProveedores() {
        List<Proveedor> proveedores = controller.obtenerProveedores();
        cmbProveedor.removeAllItems();
        
        if (proveedores.isEmpty()) {
            cmbProveedor.addItem(new Proveedor(0, "Sin proveedores", "", "", "", "", ""));
        } else {
            for (Proveedor proveedor : proveedores) {
                cmbProveedor.addItem(proveedor);
            }
        }
    }
    
    public void cargarProducto(Producto producto) {
        this.productoActual = producto;
        
        txtCodigo.setText(producto.getCodigo());
        txtNombre.setText(producto.getNombre());
        txtDescripcion.setText(producto.getDescripcion());
        txtPrecioCompra.setText(producto.getPrecioCompra().toString());
        txtPrecioVenta.setText(producto.getPrecioVenta().toString());
        txtStockActual.setText(String.valueOf(producto.getStockActual()));
        txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
        cmbUnidadMedida.setSelectedItem(producto.getUnidadMedida());
        chkActivo.setSelected(producto.isActivo());
        
        // Seleccionar categoría
        for (int i = 0; i < cmbCategoria.getItemCount(); i++) {
            Categoria cat = cmbCategoria.getItemAt(i);
            if (cat.getId() == producto.getCategoriaId()) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
        
        // Seleccionar proveedor
        for (int i = 0; i < cmbProveedor.getItemCount(); i++) {
            Proveedor prov = cmbProveedor.getItemAt(i);
            if (prov.getId() == producto.getProveedorId()) {
                cmbProveedor.setSelectedIndex(i);
                break;
            }
        }
        
        calcularMargen();
    }
    
    private void calcularMargen() {
        try {
            String precioCompraStr = txtPrecioCompra.getText().trim();
            String precioVentaStr = txtPrecioVenta.getText().trim();
            
            if (!precioCompraStr.isEmpty() && !precioVentaStr.isEmpty()) {
                BigDecimal precioCompra = new BigDecimal(precioCompraStr);
                BigDecimal precioVenta = new BigDecimal(precioVentaStr);
                
                BigDecimal margen = precioVenta.subtract(precioCompra);
                lblMargen.setText("Margen: $" + margen.toString());
                
                if (precioCompra.compareTo(BigDecimal.ZERO) > 0) {
                    double porcentaje = margen.divide(precioCompra, 4, BigDecimal.ROUND_HALF_UP)
                                           .multiply(new BigDecimal(100)).doubleValue();
                    lblPorcentajeMargen.setText(String.format("%.2f%%", porcentaje));
                    
                    // Colorear según el margen
                    if (porcentaje < 10) {
                        lblPorcentajeMargen.setForeground(new Color(220, 53, 69));
                    } else if (porcentaje < 25) {
                        lblPorcentajeMargen.setForeground(new Color(255, 193, 7));
                    } else {
                        lblPorcentajeMargen.setForeground(new Color(40, 167, 69));
                    }
                }
            } else {
                lblMargen.setText("Margen: $0.00");
                lblPorcentajeMargen.setText("0.00%");
            }
        } catch (Exception e) {
            lblMargen.setText("Margen: Error");
            lblPorcentajeMargen.setText("Error");
        }
    }
    
    private void guardarProducto() {
        try {
            // Validar campos requeridos
            if (!validarCampos()) {
                return;
            }
            
            // Crear producto con los datos del formulario
            Producto producto = construirProductoDeFormulario();
            
            // Guardar producto
            boolean resultado = controller.guardarProducto(producto, esNuevo);
            
            if (resultado) {
                dispose(); // Cerrar diálogo si se guardó exitosamente
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al guardar producto: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validarCampos() {
        // Código
        if (txtCodigo.getText().trim().isEmpty()) {
            mostrarError("El código es requerido", txtCodigo);
            return false;
        }
        
        // Nombre
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es requerido", txtNombre);
            return false;
        }
        
        // Categoría
        if (cmbCategoria.getSelectedItem() == null || 
            ((Categoria)cmbCategoria.getSelectedItem()).getId() == 0) {
            mostrarError("Debe seleccionar una categoría", cmbCategoria);
            return false;
        }
        
        // Proveedor
        if (cmbProveedor.getSelectedItem() == null || 
            ((Proveedor)cmbProveedor.getSelectedItem()).getId() == 0) {
            mostrarError("Debe seleccionar un proveedor", cmbProveedor);
            return false;
        }
        
        // Precio compra
        if (!controller.validarPrecio(txtPrecioCompra.getText().trim())) {
            mostrarError("El precio de compra debe ser un número válido mayor o igual a 0", txtPrecioCompra);
            return false;
        }
        
        // Precio venta
        if (!controller.validarPrecio(txtPrecioVenta.getText().trim())) {
            mostrarError("El precio de venta debe ser un número válido mayor a 0", txtPrecioVenta);
            return false;
        }
        
        // Validar que precio venta >= precio compra
        try {
            BigDecimal precioCompra = new BigDecimal(txtPrecioCompra.getText().trim());
            BigDecimal precioVenta = new BigDecimal(txtPrecioVenta.getText().trim());
            
            if (precioVenta.compareTo(precioCompra) < 0) {
                mostrarError("El precio de venta debe ser mayor o igual al precio de compra", txtPrecioVenta);
                return false;
            }
        } catch (Exception e) {
            mostrarError("Error en los precios", txtPrecioVenta);
            return false;
        }
        
        // Stock actual
        if (!controller.validarStock(txtStockActual.getText().trim())) {
            mostrarError("El stock actual debe ser un número válido mayor o igual a 0", txtStockActual);
            return false;
        }
        
        // Stock mínimo
        if (!controller.validarStock(txtStockMinimo.getText().trim())) {
            mostrarError("El stock mínimo debe ser un número válido mayor o igual a 0", txtStockMinimo);
            return false;
        }
        
        return true;
    }
    
    private Producto construirProductoDeFormulario() {
        Producto producto = new Producto();
        
        // Si estamos editando, mantener el ID
        if (!esNuevo && productoActual != null) {
            producto.setId(productoActual.getId());
        }
        
        producto.setCodigo(txtCodigo.getText().trim());
        producto.setNombre(txtNombre.getText().trim());
        producto.setDescripcion(txtDescripcion.getText().trim());
        
        Categoria categoria = (Categoria) cmbCategoria.getSelectedItem();
        producto.setCategoriaId(categoria.getId());
        
        Proveedor proveedor = (Proveedor) cmbProveedor.getSelectedItem();
        producto.setProveedorId(proveedor.getId());
        
        producto.setPrecioCompra(new BigDecimal(txtPrecioCompra.getText().trim()));
        producto.setPrecioVenta(new BigDecimal(txtPrecioVenta.getText().trim()));
        producto.setStockActual(Integer.parseInt(txtStockActual.getText().trim()));
        producto.setStockMinimo(Integer.parseInt(txtStockMinimo.getText().trim()));
        producto.setUnidadMedida((String) cmbUnidadMedida.getSelectedItem());
        producto.setActivo(chkActivo.isSelected());
        
        return producto;
    }
    
    private void mostrarError(String mensaje, Component componente) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Validación", JOptionPane.ERROR_MESSAGE);
        componente.requestFocus();
    }
}