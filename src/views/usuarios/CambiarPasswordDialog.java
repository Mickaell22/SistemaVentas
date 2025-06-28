package views.usuarios;

import models.Usuario;
import utils.PasswordUtils;
import services.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CambiarPasswordDialog extends JDialog {
    
    private Usuario usuario;
    private boolean confirmado = false;
    
    // Componentes del formulario
    private JPasswordField txtPasswordActual, txtPasswordNueva, txtConfirmarPassword;
    private JButton btnCambiar, btnCancelar;
    private JLabel lblUsuario, lblRequisitos;
    
    public CambiarPasswordDialog(JFrame parent, Usuario usuario) {
        super(parent, "Cambiar Contraseña", true);
        this.usuario = usuario;
        
        initializeComponents();
        setupLayout();
        setupEvents();
        
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initializeComponents() {
        // Campos de contraseña
        txtPasswordActual = new JPasswordField(20);
        txtPasswordNueva = new JPasswordField(20);
        txtConfirmarPassword = new JPasswordField(20);
        
        // Botones
        btnCambiar = new JButton("🔑 Cambiar Contraseña");
        btnCancelar = new JButton("❌ Cancelar");
        
        // Labels
        lblUsuario = new JLabel("Usuario: " + usuario.getNombreCompleto());
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblRequisitos = new JLabel();
        lblRequisitos.setFont(new Font("Arial", Font.ITALIC, 10));
        lblRequisitos.setForeground(Color.GRAY);
        lblRequisitos.setText("Mínimo 8 caracteres, mayúsculas, minúsculas, números y símbolos");
        
        // Verificar si es el propio usuario o un admin
        Usuario currentUser = AuthService.getInstance().getCurrentUser();
        boolean esPropio = currentUser != null && currentUser.getId() == usuario.getId();
        boolean esAdmin = AuthService.getInstance().isAdmin();
        
        // Si es admin cambiando contraseña de otro usuario, no pedir contraseña actual
        if (esAdmin && !esPropio) {
            txtPasswordActual.setEnabled(false);
            txtPasswordActual.setText("ADMIN_OVERRIDE");
        }
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título con usuario
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(lblUsuario, gbc);
        
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        
        // Contraseña actual
        gbc.gridx = 0; gbc.gridy = 1;
        panelPrincipal.add(new JLabel("Contraseña actual:"), gbc);
        gbc.gridx = 1;
        panelPrincipal.add(txtPasswordActual, gbc);
        
        // Contraseña nueva
        gbc.gridx = 0; gbc.gridy = 2;
        panelPrincipal.add(new JLabel("Contraseña nueva:"), gbc);
        gbc.gridx = 1;
        panelPrincipal.add(txtPasswordNueva, gbc);
        
        // Confirmar contraseña
        gbc.gridx = 0; gbc.gridy = 3;
        panelPrincipal.add(new JLabel("Confirmar contraseña:"), gbc);
        gbc.gridx = 1;
        panelPrincipal.add(txtConfirmarPassword, gbc);
        
        // Requisitos
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(lblRequisitos, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        panelBotones.add(btnCancelar);
        panelBotones.add(btnCambiar);
        
        // Agregar al layout principal
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        // Verificar si es admin
        Usuario currentUser = AuthService.getInstance().getCurrentUser();
        boolean esPropio = currentUser != null && currentUser.getId() == usuario.getId();
        boolean esAdmin = AuthService.getInstance().isAdmin();
        
        if (esAdmin && !esPropio) {
            JLabel lblAdminInfo = new JLabel("Como administrador, no necesita la contraseña actual");
            lblAdminInfo.setFont(new Font("Arial", Font.ITALIC, 10));
            lblAdminInfo.setForeground(Color.BLUE);
            lblAdminInfo.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
            add(lblAdminInfo, BorderLayout.NORTH);
        }
    }
    
    private void setupEvents() {
        btnCambiar.addActionListener(e -> cambiarPassword());
        btnCancelar.addActionListener(e -> cancelar());
        
        // Validación en tiempo real de contraseña nueva
        txtPasswordNueva.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validarPasswordNueva();
            }
        });
        
        // Enter para cambiar, Escape para cancelar
        getRootPane().setDefaultButton(btnCambiar);
        
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelar();
            }
        });
    }
    
    private void validarPasswordNueva() {
        String password = new String(txtPasswordNueva.getPassword());
        if (!password.isEmpty()) {
            if (PasswordUtils.isValidPassword(password)) {
                lblRequisitos.setText("✅ Contraseña válida");
                lblRequisitos.setForeground(Color.GREEN);
            } else {
                lblRequisitos.setText("❌ Contraseña no cumple requisitos");
                lblRequisitos.setForeground(Color.RED);
            }
        } else {
            lblRequisitos.setText("Mínimo 8 caracteres, mayúsculas, minúsculas, números y símbolos");
            lblRequisitos.setForeground(Color.GRAY);
        }
    }
    
    private void cambiarPassword() {
        if (validarFormulario()) {
            confirmado = true;
            dispose();
        }
    }
    
    private void cancelar() {
        confirmado = false;
        dispose();
    }
    
    private boolean validarFormulario() {
        String passwordActual = new String(txtPasswordActual.getPassword());
        String passwordNueva = new String(txtPasswordNueva.getPassword());
        String confirmarPassword = new String(txtConfirmarPassword.getPassword());
        
        // Verificar si es admin
        Usuario currentUser = AuthService.getInstance().getCurrentUser();
        boolean esPropio = currentUser != null && currentUser.getId() == usuario.getId();
        boolean esAdmin = AuthService.getInstance().isAdmin();
        
        // Validar contraseña actual (solo si no es admin cambiando otra contraseña)
        if (esPropio || !esAdmin) {
            if (passwordActual.isEmpty()) {
                mostrarError("Debe ingresar la contraseña actual");
                txtPasswordActual.requestFocus();
                return false;
            }
        }
        
        // Validar contraseña nueva
        if (passwordNueva.isEmpty()) {
            mostrarError("Debe ingresar la nueva contraseña");
            txtPasswordNueva.requestFocus();
            return false;
        }
        
        if (!PasswordUtils.isValidPassword(passwordNueva)) {
            mostrarError("La nueva contraseña no cumple con los requisitos de seguridad");
            txtPasswordNueva.requestFocus();
            return false;
        }
        
        // Validar confirmación
        if (!passwordNueva.equals(confirmarPassword)) {
            mostrarError("Las contraseñas no coinciden");
            txtConfirmarPassword.requestFocus();
            return false;
        }
        
        // Verificar que la nueva contraseña sea diferente
        if (passwordActual.equals(passwordNueva)) {
            mostrarError("La nueva contraseña debe ser diferente a la actual");
            txtPasswordNueva.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Validación", JOptionPane.ERROR_MESSAGE);
    }
    
    // Getters para obtener las contraseñas
    public String getPasswordActual() {
        return new String(txtPasswordActual.getPassword());
    }
    
    public String getPasswordNueva() {
        return new String(txtPasswordNueva.getPassword());
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
}