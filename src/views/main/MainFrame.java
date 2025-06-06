package views.main;

import services.AuthService;
import models.Usuario;
import utils.SessionManager;
import controllers.LoginController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    
    private AuthService authService;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel contentPanel;
    private JLabel lblStatusUser;
    private JLabel lblStatusTime;
    private Timer statusTimer;
    
    public MainFrame() {
        this.authService = AuthService.getInstance();
        
        // Verificar autenticación
        if (!authService.isAuthenticated()) {
            JOptionPane.showMessageDialog(null, "Debe iniciar sesión primero");
            new LoginController();
            dispose();
            return;
        }
        
        initializeComponents();
        setupLayout();
        setupMenus();
        setupToolBar();
        setupStatusBar();
        setupWindow();
        setupTimer();
    }
    
    private void initializeComponents() {
        menuBar = new JMenuBar();
        toolBar = new JToolBar();
        contentPanel = new JPanel(new BorderLayout());
        
        // Etiquetas de estado
        lblStatusUser = new JLabel();
        lblStatusTime = new JLabel();
        
        updateUserInfo();
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de bienvenida inicial
        JPanel welcomePanel = createWelcomePanel();
        contentPanel.add(welcomePanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // Título de bienvenida
        Usuario currentUser = authService.getCurrentUser();
        JLabel lblWelcome = new JLabel("¡Bienvenido, " + currentUser.getNombreCompleto() + "!");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 28));
        lblWelcome.setForeground(new Color(52, 58, 64));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(lblWelcome, gbc);
        
        // Información del rol
        JLabel lblRole = new JLabel("Rol: " + currentUser.getRolNombre());
        lblRole.setFont(new Font("Arial", Font.PLAIN, 16));
        lblRole.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        panel.add(lblRole, gbc);
        
        // Panel de accesos rápidos
        JPanel quickAccessPanel = createQuickAccessPanel();
        gbc.gridy = 2; gbc.insets = new Insets(40, 20, 20, 20);
        panel.add(quickAccessPanel, gbc);
        
        return panel;
    }
    
    private JPanel createQuickAccessPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Accesos Rápidos"));
        
        // Crear botones según permisos
        if (authService.canMakeSales()) {
            panel.add(createQuickButton("Nueva Venta", "💰", this::openVentas));
        }
        
        if (authService.canManageInventory()) {
            panel.add(createQuickButton("Inventario", "📦", this::openInventario));
            panel.add(createQuickButton("Productos", "🏷️", this::openProductos));
        }
        
        if (authService.canManageUsers()) {
            panel.add(createQuickButton("Usuarios", "👥", this::openUsuarios));
        }
        
        if (authService.canViewReports()) {
            panel.add(createQuickButton("Reportes", "📊", this::openReportes));
        }
        
        panel.add(createQuickButton("Clientes", "👤", this::openClientes));
        
        return panel;
    }
    
    private JButton createQuickButton(String text, String emoji, Runnable action) {
        JButton button = new JButton("<html><center>" + emoji + "<br>" + text + "</center></html>");
        button.setPreferredSize(new Dimension(140, 80));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setBackground(new Color(248, 249, 250));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        
        button.addActionListener(e -> action.run());
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(233, 236, 239));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(248, 249, 250));
            }
        });
        
        return button;
    }
    
    private void setupMenus() {
        // Menú Archivo
        JMenu menuArchivo = new JMenu("Archivo");
        
        JMenuItem itemLogout = new JMenuItem("Cerrar Sesión");
        itemLogout.addActionListener(e -> logout());
        
        JMenuItem itemExit = new JMenuItem("Salir");
        itemExit.addActionListener(e -> exitApplication());
        
        menuArchivo.add(itemLogout);
        menuArchivo.addSeparator();
        menuArchivo.add(itemExit);
        
        // Menú Ventas
        if (authService.canMakeSales()) {
            JMenu menuVentas = new JMenu("Ventas");
            
            JMenuItem itemNuevaVenta = new JMenuItem("Nueva Venta");
            itemNuevaVenta.addActionListener(e -> openVentas());
            
            JMenuItem itemHistorialVentas = new JMenuItem("Historial de Ventas");
            itemHistorialVentas.addActionListener(e -> openHistorialVentas());
            
            menuVentas.add(itemNuevaVenta);
            menuVentas.add(itemHistorialVentas);
            menuBar.add(menuVentas);
        }
        
        // Menú Inventario
        if (authService.canManageInventory()) {
            JMenu menuInventario = new JMenu("Inventario");
            
            JMenuItem itemProductos = new JMenuItem("Productos");
            itemProductos.addActionListener(e -> openProductos());
            
            JMenuItem itemInventario = new JMenuItem("Control de Stock");
            itemInventario.addActionListener(e -> openInventario());
            
            menuInventario.add(itemProductos);
            menuInventario.add(itemInventario);
            menuBar.add(menuInventario);
        }
        
        // Menú Administración
        if (authService.canManageUsers()) {
            JMenu menuAdmin = new JMenu("Administración");
            
            JMenuItem itemUsuarios = new JMenuItem("Usuarios");
            itemUsuarios.addActionListener(e -> openUsuarios());
            
            menuAdmin.add(itemUsuarios);
            menuBar.add(menuAdmin);
        }
        
        // Menú Reportes
        if (authService.canViewReports()) {
            JMenu menuReportes = new JMenu("Reportes");
            
            JMenuItem itemReportesVentas = new JMenuItem("Reportes de Ventas");
            itemReportesVentas.addActionListener(e -> openReportes());
            
            menuReportes.add(itemReportesVentas);
            menuBar.add(menuReportes);
        }
        
        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcerca = new JMenuItem("Acerca de...");
        itemAcerca.addActionListener(e -> showAbout());
        menuAyuda.add(itemAcerca);
        
        menuBar.add(menuArchivo);
        menuBar.add(Box.createHorizontalGlue()); // Espaciador
        menuBar.add(menuAyuda);
        
        setJMenuBar(menuBar);
    }
    
    private void setupToolBar() {
        toolBar.setFloatable(false);
        
        // Botones de herramientas según permisos
        if (authService.canMakeSales()) {
            addToolBarButton("Nueva Venta", "💰", this::openVentas);
        }
        
        if (authService.canManageInventory()) {
            addToolBarButton("Productos", "🏷️", this::openProductos);
        }
        
        addToolBarButton("Clientes", "👤", this::openClientes);
        
        if (authService.canViewReports()) {
            toolBar.addSeparator();
            addToolBarButton("Reportes", "📊", this::openReportes);
        }
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private void addToolBarButton(String text, String emoji, Runnable action) {
        JButton button = new JButton(emoji + " " + text);
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());
        toolBar.add(button);
    }
    
    private void setupStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusPanel.setBackground(new Color(248, 249, 250));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(new Color(248, 249, 250));
        leftPanel.add(lblStatusUser);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.add(lblStatusTime);
        
        statusPanel.add(leftPanel, BorderLayout.WEST);
        statusPanel.add(rightPanel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupWindow() {
        setTitle("Sistema de Ventas - " + authService.getCurrentUser().getRolNombre());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Manejar cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }
    
    private void setupTimer() {
        statusTimer = new Timer(1000, e -> updateStatusTime());
        statusTimer.start();
    }
    
    private void updateUserInfo() {
        Usuario user = authService.getCurrentUser();
        if (user != null) {
            lblStatusUser.setText("Usuario: " + user.getNombreCompleto() + " (" + user.getRolNombre() + ")");
        }
    }
    
    private void updateStatusTime() {
        lblStatusTime.setText("Sesión iniciada: " + 
            SessionManager.getInstance().getLoginTime().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
    
    // Métodos de acción (placeholders por ahora)
    private void openVentas() {
        JOptionPane.showMessageDialog(this, "Módulo de Ventas - Próximamente", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openInventario() {
        JOptionPane.showMessageDialog(this, "Módulo de Inventario - Próximamente", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openProductos() {
        JOptionPane.showMessageDialog(this, "Módulo de Productos - Próximamente", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openUsuarios() {
        JOptionPane.showMessageDialog(this, "Módulo de Usuarios - Próximamente", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openClientes() {
        JOptionPane.showMessageDialog(this, "Módulo de Clientes - Próximamente", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openReportes() {
        JOptionPane.showMessageDialog(this, "Módulo de Reportes - Próximamente", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void openHistorialVentas() {
        JOptionPane.showMessageDialog(this, "Historial de Ventas - Próximamente", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void logout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Confirmar Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            statusTimer.stop();
            authService.logout();
            dispose();
            new LoginController();
        }
    }
    
    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea salir del sistema?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION
        );
        
        if (option == JOptionPane.YES_OPTION) {
            statusTimer.stop();
            authService.logout();
            System.exit(0);
        }
    }
    
    private void showAbout() {
        String message = "Sistema de Ventas v1.0\n\n" +
                        "Proyecto de Calidad de Software\n" +
                        "Desarrollado con Java Swing\n\n" +
                        "Características:\n" +
                        "• Gestión de Usuarios y Roles\n" +
                        "• Control de Inventario\n" +
                        "• Sistema de Ventas\n" +
                        "• Reportes en PDF\n" +
                        "• Auditoría y Seguridad";
        
        JOptionPane.showMessageDialog(this, message, "Acerca del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
}