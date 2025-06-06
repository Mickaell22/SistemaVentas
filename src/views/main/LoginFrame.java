package views.main;

import controllers.LoginController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class LoginFrame extends JFrame {
    
    private LoginController controller;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    
    public LoginFrame(LoginController controller) {
        this.controller = controller;
        initializeComponents();
        setupLayout();
        setupEvents();
        setupWindow();
    }
    
    private void initializeComponents() {
        // Campos de texto
        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        
        // Botones
        btnLogin = new JButton("Iniciar Sesión");
        btnCancel = new JButton("Cancelar");
        
        // Etiquetas
        lblStatus = new JLabel(" ");
        lblStatus.setForeground(Color.RED);
        
        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        
        // Configurar estilos
        setupStyles();
    }
    
    private void setupStyles() {
        // Fuentes
        Font titleFont = new Font("Arial", Font.BOLD, 24);
        Font labelFont = new Font("Arial", Font.PLAIN, 14);
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);
        
        // Tamaños preferidos
        Dimension fieldSize = new Dimension(250, 35);
        Dimension buttonSize = new Dimension(120, 35);
        
        txtUsername.setPreferredSize(fieldSize);
        txtUsername.setFont(fieldFont);
        
        txtPassword.setPreferredSize(fieldSize);
        txtPassword.setFont(fieldFont);
        
        btnLogin.setPreferredSize(buttonSize);
        btnLogin.setFont(labelFont);
        btnLogin.setBackground(new Color(0, 123, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        
        btnCancel.setPreferredSize(buttonSize);
        btnCancel.setFont(labelFont);
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Título
        JLabel lblTitle = new JLabel("Sistema de Ventas");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 58, 64));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(lblTitle, gbc);
        
        // Subtítulo
        JLabel lblSubtitle = new JLabel("Ingrese sus credenciales");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(108, 117, 125));
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        mainPanel.add(lblSubtitle, gbc);
        
        // Usuario
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblUsername = new JLabel("Usuario:");
        lblUsername.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(lblUsername, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(txtUsername, gbc);
        
        // Contraseña
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(lblPassword, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(txtPassword, gbc);
        
        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnCancel);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);
        
        // Barra de progreso
        gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(progressBar, gbc);
        
        // Estado
        gbc.gridy = 6;
        mainPanel.add(lblStatus, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Panel inferior con información
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(248, 249, 250));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblFooter = new JLabel("Sistema de Ventas v1.0 - Proyecto Calidad de Software");
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(108, 117, 125));
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        footerPanel.add(lblFooter);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void setupEvents() {
        // Botón login
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        // Botón cancelar
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelLogin();
            }
        });
        
        // Enter en campos de texto
        KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyReleased(KeyEvent e) {}
        };
        
        txtUsername.addKeyListener(enterKeyListener);
        txtPassword.addKeyListener(enterKeyListener);
    }
    
    private void setupWindow() {
        setTitle("Login - Sistema de Ventas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Centrar en pantalla
        
        // Establecer icono (opcional)
        try {
            // setIconImage(Toolkit.getDefaultToolkit().getImage("path/to/icon.png"));
        } catch (Exception e) {
            // Ignorar si no hay icono
        }
        
        // Focus inicial en username
        SwingUtilities.invokeLater(() -> txtUsername.requestFocus());
    }
    
    private void performLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());
        controller.processLogin(username, password);
    }
    
    // Métodos públicos para el controller
    
    public void setLoading(boolean loading) {
        progressBar.setVisible(loading);
        btnLogin.setEnabled(!loading);
        btnCancel.setEnabled(!loading);
        txtUsername.setEnabled(!loading);
        txtPassword.setEnabled(!loading);
        
        if (loading) {
            lblStatus.setText("Verificando credenciales...");
            lblStatus.setForeground(new Color(0, 123, 255));
        } else {
            lblStatus.setText(" ");
        }
    }
    
    public void clearPassword() {
        txtPassword.setText("");
        txtPassword.requestFocus();
    }
    
    public void setStatus(String message, Color color) {
        lblStatus.setText(message);
        lblStatus.setForeground(color);
    }
}