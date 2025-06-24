package views.clientes;

import controllers.ClienteController;
import models.Cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ClienteFormDialog extends JDialog {
    
    private ClienteController controller;
    private boolean esNuevo;
    private Cliente clienteActual;
    
    // Componentes del formulario
    private JComboBox<String> cmbTipoDocumento;
    private JTextField txtNumeroDocumento;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtEmail;
    private JTextField txtTelefono;
    private JTextArea txtDireccion;
    private JCheckBox chkActivo;
    
    // Botones
    private JButton btnGuardar;
    private JButton btnCancelar;
    
    // Labels informativos
    private JLabel lblTituloDocumento;
    private JLabel lblEjemploDocumento;
    private JLabel lblValidacionDocumento;
    private JLabel lblValidacionEmail;
    
    public ClienteFormDialog(Frame parent, ClienteController controller, boolean esNuevo) {
        super(parent, esNuevo ? "Nuevo Cliente" : "Editar Cliente", true);
        this.controller = controller;
        this.esNuevo = esNuevo;
        
        initializeComponents();
        setupLayout();
        setupEvents();
        setupValidations();
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        // ComboBox tipo documento
        cmbTipoDocumento = new JComboBox<>(controller.getTiposDocumento());
        
        // Campos de texto
        txtNumeroDocumento = new JTextField(20);
        txtNombre = new JTextField(25);
        txtApellido = new JTextField(25);
        txtEmail = new JTextField(25);
        txtTelefono = new JTextField(15);
        txtDireccion = new JTextArea(3, 25);
        txtDireccion.setLineWrap(true);
        txtDireccion.setWrapStyleWord(true);
        
        // Checkbox
        chkActivo = new JCheckBox("Cliente Activo");
        chkActivo.setSelected(true);
        
        // Botones principales
        btnGuardar = new JButton("💾 Guardar");
        btnCancelar = new JButton("❌ Cancelar");
        
        // Labels informativos
        lblTituloDocumento = new JLabel("Número de Documento:*");
        lblEjemploDocumento = new JLabel("Ej: 1234567890");
        lblValidacionDocumento = new JLabel(" ");
        lblValidacionEmail = new JLabel(" ");
        
        setupStyles();
    }
    
    private void setupStyles() {
        Font labelFont = new Font("Arial", Font.PLAIN, 12);
        Font fieldFont = new Font("Arial", Font.PLAIN, 12);
        Font smallFont = new Font("Arial", Font.PLAIN, 10);
        
        // Tamaño de componentes
        Dimension fieldSize = new Dimension(200, 25);
        Dimension largeFieldSize = new Dimension(300, 25);
        
        cmbTipoDocumento.setPreferredSize(fieldSize);
        txtNumeroDocumento.setPreferredSize(fieldSize);
        txtNombre.setPreferredSize(largeFieldSize);
        txtApellido.setPreferredSize(largeFieldSize);
        txtEmail.setPreferredSize(largeFieldSize);
        txtTelefono.setPreferredSize(fieldSize);
        
        // Botones de acción
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setPreferredSize(new Dimension(120, 35));
        
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setPreferredSize(new Dimension(120, 35));
        
        // Labels informativos
        lblEjemploDocumento.setFont(smallFont);
        lblEjemploDocumento.setForeground(new Color(108, 117, 125));
        
        lblValidacionDocumento.setFont(smallFont);
        lblValidacionEmail.setFont(smallFont);
        
        // Tooltips
        cmbTipoDocumento.setToolTipText("Seleccione el tipo de documento de identificación");
        txtNumeroDocumento.setToolTipText("Ingrese el número de documento sin espacios ni guiones");
        txtNombre.setToolTipText("Nombre completo del cliente");
        txtApellido.setToolTipText("Apellidos del cliente (opcional para RUC)");
        txtEmail.setToolTipText("Correo electrónico (opcional)");
        txtTelefono.setToolTipText("Número de teléfono (opcional)");
        txtDireccion.setToolTipText("Dirección completa del cliente (opcional)");
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Tipo de documento
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Tipo de Documento:*"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(cmbTipoDocumento, gbc);
        
        // Número de documento
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(lblTituloDocumento, gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNumeroDocumento, gbc);
        
        // Ejemplo de documento
        row++;
        gbc.gridx = 1; gbc.gridy = row;
        panelFormulario.add(lblEjemploDocumento, gbc);
        
        // Validación documento
        row++;
        gbc.gridx = 1; gbc.gridy = row;
        panelFormulario.add(lblValidacionDocumento, gbc);
        
        // Nombre
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Nombre:*"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNombre, gbc);
        
        // Apellido
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtApellido, gbc);
        
        // Email
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtEmail, gbc);
        
        // Validación email
        row++;
        gbc.gridx = 1; gbc.gridy = row;
        panelFormulario.add(lblValidacionEmail, gbc);
        
        // Teléfono
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtTelefono, gbc);
        
        // Dirección
        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelFormulario.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panelFormulario.add(new JScrollPane(txtDireccion), gbc);
        
        // Estado (solo para edición)
        if (!esNuevo) {
            row++;
            gbc.gridx = 0; gbc.gridy = row;
            gbc.gridwidth = 2;
            panelFormulario.add(chkActivo, gbc);
            gbc.gridwidth = 1;
        }
        
        add(panelFormulario, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        add(panelBotones, BorderLayout.SOUTH);
        
        // Panel superior con instrucciones
        JPanel panelInstrucciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInstrucciones.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));
        JLabel lblInstrucciones = new JLabel("Los campos marcados con (*) son obligatorios");
        lblInstrucciones.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInstrucciones.setForeground(new Color(108, 117, 125));
        panelInstrucciones.add(lblInstrucciones);
        
        add(panelInstrucciones, BorderLayout.NORTH);
    }
    
    private void setupEvents() {
        // Botón guardar
        btnGuardar.addActionListener(e -> guardarCliente());
        
        // Botón cancelar
        btnCancelar.addActionListener(e -> dispose());
        
        // Cambio de tipo de documento
        cmbTipoDocumento.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    actualizarEjemploDocumento();
                    validarDocumentoEnTiempoReal();
                }
            }
        });
        
        // Escape para cerrar
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void setupValidations() {
        // Validación en tiempo real para documento
        txtNumeroDocumento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validarDocumentoEnTiempoReal();
            }
        });
        
        // Validación en tiempo real para email
        txtEmail.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validarEmailEnTiempoReal();
            }
        });
        
        // Solo números para documento cuando es cédula o RUC
        txtNumeroDocumento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                String tipoDoc = (String) cmbTipoDocumento.getSelectedItem();
                if ("Cédula".equals(tipoDoc) || "RUC".equals(tipoDoc)) {
                    char c = evt.getKeyChar();
                    if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE) {
                        evt.consume();
                    }
                }
            }
        });
        
        // Solo letras para nombre
        txtNombre.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isLetter(c) && !Character.isSpaceChar(c) && 
                    c != '.' && c != '-' && c != java.awt.event.KeyEvent.VK_BACK_SPACE &&
                    "áéíóúÁÉÍÓÚñÑ".indexOf(c) == -1) {
                    evt.consume();
                }
            }
        });
        
        // Solo letras para apellido
        txtApellido.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isLetter(c) && !Character.isSpaceChar(c) && 
                    c != '.' && c != '-' && c != java.awt.event.KeyEvent.VK_BACK_SPACE &&
                    "áéíóúÁÉÍÓÚñÑ".indexOf(c) == -1) {
                    evt.consume();
                }
            }
        });
        
        // Configurar ejemplo inicial
        actualizarEjemploDocumento();
    }
    
    private void actualizarEjemploDocumento() {
        String tipoDoc = (String) cmbTipoDocumento.getSelectedItem();
        switch (tipoDoc) {
            case "Cédula":
                lblTituloDocumento.setText("Número de Cédula:*");
                lblEjemploDocumento.setText("Ej: 1234567890 (10 dígitos)");
                break;
            case "RUC":
                lblTituloDocumento.setText("Número de RUC:*");
                lblEjemploDocumento.setText("Ej: 1234567890001 (13 dígitos)");
                break;
            case "Pasaporte":
                lblTituloDocumento.setText("Número de Pasaporte:*");
                lblEjemploDocumento.setText("Ej: AB1234567 (6-15 caracteres)");
                break;
        }
    }
    
    private void validarDocumentoEnTiempoReal() {
        String tipoDoc = (String) cmbTipoDocumento.getSelectedItem();
        String documento = txtNumeroDocumento.getText().trim();
        
        if (documento.isEmpty()) {
            lblValidacionDocumento.setText(" ");
            lblValidacionDocumento.setForeground(Color.BLACK);
            return;
        }
        
        String tipoDocDB = controller.convertirTipoDocumentoADB(tipoDoc);
        boolean formatoValido = controller.validarFormatoDocumento(tipoDocDB, documento);
        
        if (!formatoValido) {
            lblValidacionDocumento.setText("❌ Formato inválido");
            lblValidacionDocumento.setForeground(new Color(220, 53, 69));
        } else {
            // Verificar si ya existe
            int clienteId = clienteActual != null ? clienteActual.getId() : 0;
            boolean esUnico = controller.validarDocumentoUnico(documento, clienteId);
            
            if (!esUnico) {
                lblValidacionDocumento.setText("❌ Documento ya existe");
                lblValidacionDocumento.setForeground(new Color(220, 53, 69));
            } else {
                lblValidacionDocumento.setText("✅ Válido");
                lblValidacionDocumento.setForeground(new Color(40, 167, 69));
            }
        }
    }
    
    private void validarEmailEnTiempoReal() {
        String email = txtEmail.getText().trim();
        
        if (email.isEmpty()) {
            lblValidacionEmail.setText(" ");
            lblValidacionEmail.setForeground(Color.BLACK);
            return;
        }
        
        boolean formatoValido = controller.validarFormatoEmail(email);
        
        if (!formatoValido) {
            lblValidacionEmail.setText("❌ Email inválido");
            lblValidacionEmail.setForeground(new Color(220, 53, 69));
        } else {
            // Verificar si ya existe
            int clienteId = clienteActual != null ? clienteActual.getId() : 0;
            boolean esUnico = controller.validarEmailUnico(email, clienteId);
            
            if (!esUnico) {
                lblValidacionEmail.setText("❌ Email ya existe");
                lblValidacionEmail.setForeground(new Color(220, 53, 69));
            } else {
                lblValidacionEmail.setText("✅ Válido");
                lblValidacionEmail.setForeground(new Color(40, 167, 69));
            }
        }
    }
    
    public void cargarCliente(Cliente cliente) {
        this.clienteActual = cliente;
        
        // Cargar datos en el formulario
        String tipoDocDisplay = controller.convertirTipoDocumentoADisplay(cliente.getTipoDocumento());
        cmbTipoDocumento.setSelectedItem(tipoDocDisplay);
        
        txtNumeroDocumento.setText(cliente.getNumeroDocumento());
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido() != null ? cliente.getApellido() : "");
        txtEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");
        txtTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");
        txtDireccion.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "");
        chkActivo.setSelected(cliente.isActivo());
        
        // Actualizar validaciones
        actualizarEjemploDocumento();
        validarDocumentoEnTiempoReal();
        validarEmailEnTiempoReal();
    }
    
    private void guardarCliente() {
        try {
            // Validar campos requeridos
            if (!validarCamposRequeridos()) {
                return;
            }
            
            // Crear cliente con los datos del formulario
            Cliente cliente = construirClienteDeFormulario();
            
            // Guardar cliente
            boolean resultado = controller.guardarCliente(cliente, esNuevo);
            
            if (resultado) {
                dispose(); // Cerrar diálogo si se guardó exitosamente
            }
            
        } catch (Exception e) {
            mostrarError("Error al guardar cliente: " + e.getMessage());
        }
    }
    
    private boolean validarCamposRequeridos() {
        // Tipo de documento
        if (cmbTipoDocumento.getSelectedItem() == null) {
            mostrarError("Debe seleccionar un tipo de documento", cmbTipoDocumento);
            return false;
        }
        
        // Número de documento
        String documento = txtNumeroDocumento.getText().trim();
        if (documento.isEmpty()) {
            mostrarError("El número de documento es requerido", txtNumeroDocumento);
            return false;
        }
        
        // Validar formato de documento
        String tipoDoc = controller.convertirTipoDocumentoADB((String) cmbTipoDocumento.getSelectedItem());
        if (!controller.validarFormatoDocumento(tipoDoc, documento)) {
            mostrarError("El formato del documento no es válido", txtNumeroDocumento);
            return false;
        }
        
        // Validar unicidad de documento
        int clienteId = clienteActual != null ? clienteActual.getId() : 0;
        if (!controller.validarDocumentoUnico(documento, clienteId)) {
            mostrarError("Ya existe un cliente con este documento", txtNumeroDocumento);
            return false;
        }
        
        // Nombre
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("El nombre es requerido", txtNombre);
            return false;
        }
        
        if (!controller.validarFormatoNombre(nombre)) {
            mostrarError("El nombre contiene caracteres no válidos", txtNombre);
            return false;
        }
        
        // Apellido (validar formato si se proporciona)
        String apellido = txtApellido.getText().trim();
        if (!apellido.isEmpty() && !controller.validarFormatoNombre(apellido)) {
            mostrarError("El apellido contiene caracteres no válidos", txtApellido);
            return false;
        }
        
        // Email (validar formato y unicidad si se proporciona)
        String email = txtEmail.getText().trim();
        if (!email.isEmpty()) {
            if (!controller.validarFormatoEmail(email)) {
                mostrarError("El formato del email no es válido", txtEmail);
                return false;
            }
            
            if (!controller.validarEmailUnico(email, clienteId)) {
                mostrarError("Ya existe un cliente con este email", txtEmail);
                return false;
            }
        }
        
        return true;
    }
    
    private Cliente construirClienteDeFormulario() {
        Cliente cliente = new Cliente();
        
        // Si estamos editando, mantener el ID
        if (!esNuevo && clienteActual != null) {
            cliente.setId(clienteActual.getId());
        }
        
        String tipoDocDisplay = (String) cmbTipoDocumento.getSelectedItem();
        cliente.setTipoDocumento(controller.convertirTipoDocumentoADB(tipoDocDisplay));
        cliente.setNumeroDocumento(txtNumeroDocumento.getText().trim());
        cliente.setNombre(txtNombre.getText().trim());
        
        String apellido = txtApellido.getText().trim();
        cliente.setApellido(apellido.isEmpty() ? null : apellido);
        
        String email = txtEmail.getText().trim();
        cliente.setEmail(email.isEmpty() ? null : email);
        
        String telefono = txtTelefono.getText().trim();
        cliente.setTelefono(telefono.isEmpty() ? null : telefono);
        
        String direccion = txtDireccion.getText().trim();
        cliente.setDireccion(direccion.isEmpty() ? null : direccion);
        
        cliente.setActivo(esNuevo ? true : chkActivo.isSelected());
        
        return cliente;
    }
    
    private void mostrarError(String mensaje, Component componente) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Validación", JOptionPane.ERROR_MESSAGE);
        if (componente != null) {
            componente.requestFocus();
        }
    }
    
    private void mostrarError(String mensaje) {
        mostrarError(mensaje, null);
    }
}