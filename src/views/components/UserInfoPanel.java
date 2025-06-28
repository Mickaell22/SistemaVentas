package views.components;

import services.AuthService;
import models.Usuario;
import utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserInfoPanel extends JPanel {
    
    private JLabel lblUsuario;
    private JLabel lblRol;
    private JLabel lblSesion;
    private JButton btnPerfil;
    private JButton btnLogout;
    private Timer sessionTimer;
    
    public UserInfoPanel() {
        initializeComponents();
        setupLayout();
        setupEvents();
        startSessionMonitoring();
    }
    
    private void initializeComponents() {
        lblUsuario = new JLabel("No autenticado");
        lblRol = new JLabel("");
        lblSesion = new JLabel("");
        
        btnPerfil = new JButton("👤 Mi Perfil");
        btnLogout = new JButton("🚪 Salir");
        
        // Configurar estilos
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 12));
        lblRol.setFont(new Font("Arial", Font.ITALIC, 10));
        lblSesion.setFont(new Font("Arial", Font.PLAIN, 9));
        lblSesion.setForeground(Color.GRAY);
        
        btnPerfil.setPreferredSize(new Dimension(100, 25));
        btnLogout.setPreferredSize(new Dimension(70, 25));
        
        // Estados iniciales
        btnPerfil.setEnabled(false);
        btnLogout.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLoweredBevelBorder());
        setBackground(new Color(240, 248, 255)); // Azul muy claro
        
        // Panel izquierdo - Información del usuario
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        infoPanel.setBackground(new Color(240, 248, 255));
        infoPanel.add(lblUsuario);
        infoPanel.add(new JLabel("|"));
        infoPanel.add(lblRol);
        infoPanel.add(new JLabel("|"));
        infoPanel.add(lblSesion);
        
        // Panel derecho - Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.add(btnPerfil);
        buttonPanel.add(btnLogout);
        
        add(infoPanel, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.EAST);
    }
    
    private void setupEvents() {
        btnPerfil.addActionListener(e -> mostrarPerfil());
        btnLogout.addActionListener(e -> realizarLogout());
    }
    
    private void startSessionMonitoring() {
        // Timer para actualizar información de sesión cada 30 segundos
        sessionTimer = new Timer(30000, e -> actualizarInformacion());
        sessionTimer.start();
        
        // Actualizar inmediatamente
        actualizarInformacion();
    }
    
    /**
     * Actualiza la información mostrada
     */
    public void actualizarInformacion() {
        SwingUtilities.invokeLater(() -> {
            if (AuthService.getInstance().isAuthenticated()) {
                Usuario currentUser = AuthService.getInstance().getCurrentUser();
                SessionManager session = SessionManager.getInstance();
                
                if (currentUser != null) {
                    // Información del usuario
                    lblUsuario.setText("👤 " + currentUser.getNombreCompleto());
                    lblRol.setText("🎭 " + currentUser.getRolNombre());
                    
                    // Información de sesión
                    String timeRemaining = session.getTimeRemaining();
                    lblSesion.setText("⏰ " + timeRemaining);
                    
                    // Verificar si la sesión está cerca de expirar
                    if (session.isSessionNearExpiry()) {
                        lblSesion.setForeground(Color.RED);
                        lblSesion.setText("⚠️ Sesión expira pronto");
                    } else {
                        lblSesion.setForeground(Color.GRAY);
                    }
                    
                    // Habilitar botones
                    btnPerfil.setEnabled(true);
                    btnLogout.setEnabled(true);
                } else {
                    mostrarNoAutenticado();
                }
            } else {
                mostrarNoAutenticado();
            }
        });
    }
    
    private void mostrarNoAutenticado() {
        lblUsuario.setText("❌ No autenticado");
        lblRol.setText("");
        lblSesion.setText("");
        btnPerfil.setEnabled(false);
        btnLogout.setEnabled(false);
    }
    
    private void mostrarPerfil() {
        Usuario currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        // Crear diálogo simple de perfil
        JDialog perfilDialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                          "Mi Perfil", true);
        perfilDialog.setSize(400, 350);
        perfilDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Título
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titulo = new JLabel("Información del Usuario");
        titulo.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titulo, gbc);
        
        gbc.gridwidth = 1;
        
        // Información del usuario
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        JLabel lblNombre = new JLabel(currentUser.getNombreCompleto());
        lblNombre.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(lblNombre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(currentUser.getUsername()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(currentUser.getEmail()), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1;
        JLabel lblRol = new JLabel(currentUser.getRolNombre());
        lblRol.setFont(new Font("Arial", Font.BOLD, 12));
        lblRol.setForeground(new Color(0, 123, 255));
        panel.add(lblRol, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        JLabel lblEstado = new JLabel(currentUser.isActivo() ? "✅ Activo" : "❌ Inactivo");
        lblEstado.setForeground(currentUser.isActivo() ? Color.GREEN : Color.RED);
        panel.add(lblEstado, gbc);
        
        // Información de sesión
        SessionManager session = SessionManager.getInstance();
        
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Sesión iniciada:"), gbc);
        gbc.gridx = 1;
        if (session.getSessionStartTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            panel.add(new JLabel(session.getSessionStartTime().format(formatter)), gbc);
        } else {
            panel.add(new JLabel("Desconocido"), gbc);
        }
        
        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("Tiempo restante:"), gbc);
        gbc.gridx = 1;
        JLabel lblTiempo = new JLabel(session.getTimeRemaining());
        lblTiempo.setForeground(session.isSessionNearExpiry() ? Color.RED : Color.BLUE);
        panel.add(lblTiempo, gbc);
        
        // Botones
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton btnCambiarPassword = new JButton("🔑 Cambiar Contraseña");
        btnCambiarPassword.addActionListener(e -> {
            // Aquí puedes agregar funcionalidad para cambiar contraseña
            JOptionPane.showMessageDialog(perfilDialog, 
                "Funcionalidad de cambio de contraseña será implementada próximamente", 
                "Información", JOptionPane.INFORMATION_MESSAGE);
        });
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> perfilDialog.dispose());
        
        buttonPanel.add(btnCambiarPassword);
        buttonPanel.add(btnCerrar);
        panel.add(buttonPanel, gbc);
        
        perfilDialog.add(panel);
        perfilDialog.setVisible(true);
    }
    
    private void realizarLogout() {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea cerrar sesión?",
            "Confirmar Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            // Notificar al MainFrame para que haga el logout
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                JFrame mainFrame = (JFrame) window;
                // Intentar llamar método realizarLogout del MainFrame
                try {
                    mainFrame.getClass().getMethod("realizarLogout").invoke(mainFrame);
                } catch (Exception e) {
                    // Si no tiene el método, hacer logout básico
                    AuthService.getInstance().logout();
                    actualizarInformacion();
                    
                    // Mostrar ventana de login
                    try {
                        Class<?> loginControllerClass = Class.forName("controllers.LoginController");
                        loginControllerClass.getDeclaredConstructor().newInstance();
                        mainFrame.dispose();
                    } catch (Exception ex) {
                        System.err.println("Error al mostrar login: " + ex.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Detiene el monitoreo de sesión
     */
    public void detenerMonitoreo() {
        if (sessionTimer != null) {
            sessionTimer.stop();
        }
    }
    
    /**
     * Método para llamar desde MainFrame después del login
     */
    public void onLoginExitoso() {
        actualizarInformacion();
    }
    
    /**
     * Método para llamar desde MainFrame después del logout
     */
    public void onLogout() {
        mostrarNoAutenticado();
    }
}