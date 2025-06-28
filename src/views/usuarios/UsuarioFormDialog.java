package views.usuarios;

import models.Usuario;
import models.Rol;
import utils.PasswordUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UsuarioFormDialog extends JDialog {
    
    private Usuario usuario;
    private List<Rol> roles;
    private boolean confirmado = false;
    private boolean esEdicion;
    
    // Componentes del formulario
    private JTextField txtNombre, txtApellido, txtEmail, txtUsername, txtTelefono, txtDireccion;
    private JPasswordField txtPassword, txtConfirmarPassword;
    private JComboBox<Rol> cmbRol;
    private JCheckBox chkActivo;
    private JButton btnGuardar, btnCancelar;
    private JLabel lblPasswordRequirements;
    
    public UsuarioFormDialog(JFrame parent, String title, Usuario usuario, List<Rol> roles) {
        super(parent, title, true);
        this.usuario = usuario;
        this.roles = roles;
        this.esEdicion = (usuario != null);
        
        initializeComponents();
        setupLayout();
        setupEvents();
        cargarDatos();
        
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initializeComponents() {
        // Campos de texto
        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtEmail = new JTextField(25);
        txtUsername = new JTextField(20);
        txtTelefono = new JTextField(15);
        txtDireccion = new JTextField(30);
        
        // Campos de contraseña
        txtPassword = new JPasswordField(20);
        txtConfirmarPassword = new JPasswordField(20);
        
        // ComboBox de roles
        cmbRol = new JComboBox<>();
        if (roles != null) {
            for (Rol rol : roles) {
                cmbRol.addItem(rol);
            }
        }
        
        // Checkbox
        chkActivo = new JCheckBox("Usuario activo", true);
        
        // Botones
        btnGuardar = new JButton(esEdicion ? "💾 Actualizar" : "💾 Crear");
        btnCancelar = new JButton("❌ Cancelar");
        
        // Label para requisitos de contraseña
        lblPasswordRequirements = new JLabel();
        lblPasswordRequirements.setFont(new Font("Arial", Font.ITALIC, 10));
        lblPasswordRequirements.setForeground(Color.GRAY);
        
        // Configurar tamaños
        Dimension fieldSize = new Dimension(200, 25);
        txtNombre.setPreferredSize(fieldSize);
        txtApellido.setPreferredSize(fieldSize);
        txtEmail.setPreferredSize(new Dimension(250, 25));
        txtUsername.setPreferredSize(fieldSize);
        txtTelefono.setPreferredSize(new Dimension(150, 25));
        txtDireccion.setPreferredSize(new Dimension(300, 25));
        
        // Si es edición, deshabilitar username y hacer contraseña opcional
        if (esEdicion) {
            txtUsername.setEnabled(false);
            lblPasswordRequirements.setText("Dejar en blanco para mantener contraseña actual");
        } else {
            lblPasswordRequirements.setText("Mínimo 8 caracteres, mayúsculas, minúsculas, números y símbolos");
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Fila 0: Nombre y Apellido
        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("*Nombre:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtNombre, gbc);
        gbc.gridx = 2;
        panelFormulario.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 3;
        panelFormulario.add(txtApellido, gbc);
        
        // Fila 1: Email
        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("*Email:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelFormulario.add(txtEmail, gbc);
        gbc.gridwidth = 1;
        
        // Fila 2: Username y Rol
        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("*Username:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtUsername, gbc);
        gbc.gridx = 2;
        panelFormulario.add(new JLabel("*Rol:"), gbc);
        gbc.gridx = 3;
        panelFormulario.add(cmbRol, gbc);
        
        // Fila 3: Contraseña
        if (!esEdicion) {
            gbc.gridx = 0; gbc.gridy = 3;
            panelFormulario.add(new JLabel("*Contraseña:"), gbc);
            gbc.gridx = 1;
            panelFormulario.add(txtPassword, gbc);
            gbc.gridx = 2;
            panelFormulario.add(new JLabel("*Confirmar:"), gbc);
            gbc.gridx = 3;
            panelFormulario.add(txtConfirmarPassword, gbc);
        } else {
            gbc.gridx = 0; gbc.gridy = 3;
            panelFormulario.add(new JLabel("Nueva Contraseña:"), gbc);
            gbc.gridx = 1;
            panelFormulario.add(txtPassword, gbc);
            gbc.gridx = 2;
            panelFormulario.add(new JLabel("Confirmar:"), gbc);
            gbc.gridx = 3;
            panelFormulario.add(txtConfirmarPassword, gbc);
        }
        
        // Fila 4: Teléfono y Estado
        gbc.gridx = 0; gbc.gridy = 4;
        panelFormulario.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1;
        panelFormulario.add(txtTelefono, gbc);
        gbc.gridx = 2;
        panelFormulario.add(chkActivo, gbc);
        
        // Fila 5: Dirección
        gbc.gridx = 0; gbc.gridy = 5;
        panelFormulario.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        panelFormulario.add(txtDireccion, gbc);
        gbc.gridwidth = 1;
        
        // Fila 6: Requisitos de contraseña
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 4;
        panelFormulario.add(lblPasswordRequirements, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        
        // Agregar al layout principal
        add(panelFormulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        // Nota de campos obligatorios
        JLabel lblNota = new JLabel("* Campos obligatorios");
        lblNota.setFont(new Font("Arial", Font.ITALIC, 10));
        lblNota.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        add(lblNota, BorderLayout.NORTH);
    }
    
    private void setupEvents() {
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> cancelar());
        
        // Validación en tiempo real de contraseña
        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validarPassword();
            }
        });
        
        // Enter para guardar, Escape para cancelar
        getRootPane().setDefaultButton(btnGuardar);
        
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelar();
            }
        });
    }
    
    private void cargarDatos() {
        if (esEdicion && usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtApellido.setText(usuario.getApellido());
            txtEmail.setText(usuario.getEmail());
            txtUsername.setText(usuario.getUsername());
            txtTelefono.setText(usuario.getTelefono());
            txtDireccion.setText(usuario.getDireccion());
            chkActivo.setSelected(usuario.isActivo());
            
            // Seleccionar rol
            for (int i = 0; i < cmbRol.getItemCount(); i++) {
                Rol rol = cmbRol.getItemAt(i);
                if (rol.getId() == usuario.getRolId()) {
                    cmbRol.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    private void validarPassword() {
        String password = new String(txtPassword.getPassword());
        if (!password.isEmpty()) {
            if (PasswordUtils.isValidPassword(password)) {
                lblPasswordRequirements.setText("✅ Contraseña válida");
                lblPasswordRequirements.setForeground(Color.GREEN);
            } else {
                lblPasswordRequirements.setText("❌ Contraseña no cumple requisitos");
                lblPasswordRequirements.setForeground(Color.RED);
            }
        } else if (esEdicion) {
            lblPasswordRequirements.setText("Dejar en blanco para mantener contraseña actual");
            lblPasswordRequirements.setForeground(Color.GRAY);
        }
    }
    
    private void guardar() {
        if (validarFormulario()) {
            crearUsuarioDesdeFormulario();
            confirmado = true;
            dispose();
        }
    }
    
    private void cancelar() {
        confirmado = false;
        dispose();
    }
    
    private boolean validarFormulario() {
        // Validar campos obligatorios
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            txtNombre.requestFocus();
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            mostrarError("El email es obligatorio");
            txtEmail.requestFocus();
            return false;
        }
        
        if (txtUsername.getText().trim().isEmpty()) {
            mostrarError("El username es obligatorio");
            txtUsername.requestFocus();
            return false;
        }
        
        if (cmbRol.getSelectedItem() == null) {
            mostrarError("Debe seleccionar un rol");
            cmbRol.requestFocus();
            return false;
        }
        
        // Validar formato de email
        String email = txtEmail.getText().trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            mostrarError("El formato del email es inválido");
            txtEmail.requestFocus();
            return false;
        }
        
        // Validar contraseña (obligatoria solo en creación)
        String password = new String(txtPassword.getPassword());
        String confirmarPassword = new String(txtConfirmarPassword.getPassword());
        
        if (!esEdicion || !password.isEmpty()) {
            if (password.isEmpty()) {
                mostrarError("La contraseña es obligatoria");
                txtPassword.requestFocus();
                return false;
            }
            
            if (!PasswordUtils.isValidPassword(password)) {
                mostrarError("La contraseña no cumple con los requisitos de seguridad");
                txtPassword.requestFocus();
                return false;
            }
            
            if (!password.equals(confirmarPassword)) {
                mostrarError("Las contraseñas no coinciden");
                txtConfirmarPassword.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void crearUsuarioDesdeFormulario() {
        if (usuario == null) {
            usuario = new Usuario();
        }
        
        usuario.setNombre(txtNombre.getText().trim());
        usuario.setApellido(txtApellido.getText().trim().isEmpty() ? null : txtApellido.getText().trim());
        usuario.setEmail(txtEmail.getText().trim());
        usuario.setUsername(txtUsername.getText().trim());
        usuario.setTelefono(txtTelefono.getText().trim().isEmpty() ? null : txtTelefono.getText().trim());
        usuario.setDireccion(txtDireccion.getText().trim().isEmpty() ? null : txtDireccion.getText().trim());
        usuario.setActivo(chkActivo.isSelected());
        
        // Rol seleccionado
        Rol rolSeleccionado = (Rol) cmbRol.getSelectedItem();
        if (rolSeleccionado != null) {
            usuario.setRolId(rolSeleccionado.getId());
            usuario.setRolNombre(rolSeleccionado.getNombre());
        }
        
        // Contraseña (solo si se proporcionó)
        String password = new String(txtPassword.getPassword());
        if (!password.isEmpty()) {
            usuario.setPassword(password);
        }
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Validación", JOptionPane.ERROR_MESSAGE);
    }
    
    // Getters
    public Usuario getUsuario() {
        return usuario;
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
}