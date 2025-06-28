package views.main;

import services.AuthService;
import models.Usuario;
import utils.SessionManager;
import controllers.LoginController;
import controllers.VentaController;
import views.clientes.ClientePanel;
import views.productos.ProductoPanel;
import views.ventas.VentaPanel;
import views.usuarios.UsuarioPanel;
import views.reportes.ReportePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    private AuthService authService;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel contentPanel;
    private JLabel lblStatusUser;
    private JLabel lblStatusTime;
    private Timer statusTimer;
    private JPanel currentPanel;
    private UsuarioPanel usuarioPanel;
    private JMenuItem menuUsuarios;
    private JButton btnUsuarios;
    private JPanel panelUsuarioInfo;

    public MainFrame() {
        this.authService = AuthService.getInstance();

        // Verificar autenticación
        if (!authService.isAuthenticated()) {
            JOptionPane.showMessageDialog(null, "Debe iniciar sesión primero");
            new LoginController();
            dispose();
            return;
        }

        initializeComponents(); // 1. Primero los componentes básicos
        createUserInfoPanel(); // 2. Crear UserInfoPanel
        setupLayout(); // 3. Layout del contenido principal
        setupMenus(); // 4. Menús
        setupToolBar(); // 5. Toolbar
        setupStatusBar(); // 6. Barra de estado (usa UserInfoPanel)
        setupWindow(); // 7. Configuración de ventana
        setupTimer(); // 8. Timer al final
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

    private void createUserInfoPanel() {
        panelUsuarioInfo = new JPanel(new BorderLayout());
        panelUsuarioInfo.setBackground(new Color(240, 248, 255));
        panelUsuarioInfo.setBorder(BorderFactory.createLoweredBevelBorder());
        panelUsuarioInfo.setPreferredSize(new Dimension(0, 30));

        try {
            // Información del usuario
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                String infoText = "👤 " + currentUser.getNombreCompleto() +
                        " | 🎭 " + currentUser.getRolNombre() +
                        " | ⏰ Sesión activa";

                JLabel lblUsuario = new JLabel("  " + infoText);
                lblUsuario.setFont(new Font("Arial", Font.BOLD, 11));

                // Botones
                JButton btnPerfil = new JButton("👤 Perfil");
                btnPerfil.setPreferredSize(new Dimension(80, 25));
                btnPerfil.addActionListener(e -> mostrarPerfilSimple());

                JButton btnLogout = new JButton("🚪 Salir");
                btnLogout.setPreferredSize(new Dimension(70, 25));
                btnLogout.addActionListener(e -> logout());

                // Layout
                JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
                panelDerecho.setBackground(new Color(240, 248, 255));
                panelDerecho.add(btnPerfil);
                panelDerecho.add(btnLogout);

                panelUsuarioInfo.add(lblUsuario, BorderLayout.WEST);
                panelUsuarioInfo.add(panelDerecho, BorderLayout.EAST);
            } else {
                JLabel lblError = new JLabel("  ❌ Usuario no disponible");
                panelUsuarioInfo.add(lblError, BorderLayout.CENTER);
            }

            System.out.println("✅ Panel de usuario creado exitosamente");

        } catch (Exception e) {
            System.err.println("❌ Error al crear panel de usuario: " + e.getMessage());
            JLabel lblError = new JLabel("  ⚠️ Error en panel de usuario");
            panelUsuarioInfo.add(lblError, BorderLayout.CENTER);
        }
    }

    private void mostrarPerfilSimple() {
        try {
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(this, "No hay usuario logueado", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String mensaje = "INFORMACIÓN DEL USUARIO\n\n" +
                    "Nombre: " + currentUser.getNombreCompleto() + "\n" +
                    "Username: " + currentUser.getUsername() + "\n" +
                    "Email: " + currentUser.getEmail() + "\n" +
                    "Rol: " + currentUser.getRolNombre() + "\n" +
                    "Estado: " + (currentUser.isActivo() ? "✅ Activo" : "❌ Inactivo") + "\n" +
                    "ID: " + currentUser.getId();

            JOptionPane.showMessageDialog(this, mensaje, "Mi Perfil", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al mostrar perfil: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblWelcome, gbc);

        // Información del rol
        JLabel lblRole = new JLabel("Rol: " + currentUser.getRolNombre());
        lblRole.setFont(new Font("Arial", Font.PLAIN, 16));
        lblRole.setForeground(new Color(108, 117, 125));
        gbc.gridy = 1;
        panel.add(lblRole, gbc);

        // Panel de accesos rápidos
        JPanel quickAccessPanel = createQuickAccessPanel();
        gbc.gridy = 2;
        gbc.insets = new Insets(40, 20, 20, 20);
        panel.add(quickAccessPanel, gbc);

        return panel;
    }

    private JPanel createQuickAccessPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Accesos Rápidos"));

        // Crear botones según permisos
        if (authService.canMakeSales()) {
            panel.add(createQuickButton("Nueva Venta", "💰", this::openNuevaVenta));
            panel.add(createQuickButton("Ventas", "📊", this::openVentas));
        }

        if (authService.canManageInventory()) {
            panel.add(createQuickButton("Productos", "🏷️", this::openProductos));
        }

        if (authService.canManageUsers()) {
            panel.add(createQuickButton("Usuarios", "👥", this::openUsuarios));
        }

        panel.add(createQuickButton("Clientes", "👤", this::openClientes));

        if (authService.canViewReports()) {
            panel.add(createQuickButton("Reportes", "📈", this::openReportes));
        }

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
        menuArchivo.setMnemonic('A');

        JMenuItem itemNuevaVenta = new JMenuItem("Nueva Venta", 'N');
        itemNuevaVenta.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        itemNuevaVenta.addActionListener(e -> openNuevaVenta());
        if (authService.canMakeSales()) {
            menuArchivo.add(itemNuevaVenta);
        }

        menuArchivo.addSeparator();

        JMenuItem itemSalir = new JMenuItem("Salir", 'S');
        itemSalir.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        itemSalir.addActionListener(e -> exitApplication());
        menuArchivo.add(itemSalir);

        // Menú Ventas
        if (authService.canMakeSales()) {
            JMenu menuVentas = new JMenu("Ventas");
            menuVentas.setMnemonic('V');

            JMenuItem itemVentas = new JMenuItem("Gestión de Ventas", 'G');
            itemVentas.addActionListener(e -> openVentas());
            menuVentas.add(itemVentas);

            JMenuItem itemNuevaVenta2 = new JMenuItem("Nueva Venta", 'N');
            itemNuevaVenta2.addActionListener(e -> openNuevaVenta());
            menuVentas.add(itemNuevaVenta2);

            menuBar.add(menuVentas);
        }

        // Menú Inventario (solo productos)
        if (authService.canManageInventory()) {
            JMenu menuInventario = new JMenu("Inventario");
            menuInventario.setMnemonic('I');

            JMenuItem itemProductos = new JMenuItem("Gestión de Productos", 'P');
            itemProductos.addActionListener(e -> openProductos());
            menuInventario.add(itemProductos);

            menuBar.add(menuInventario);
        }

        // Menú Clientes
        JMenu menuClientes = new JMenu("Clientes");
        menuClientes.setMnemonic('C');

        JMenuItem itemClientes = new JMenuItem("Gestión de Clientes", 'G');
        itemClientes.addActionListener(e -> openClientes());
        menuClientes.add(itemClientes);

        menuBar.add(menuClientes);

        // Menú Usuarios (solo para administradores)
        if (authService.canManageUsers()) {
            JMenu menuUsuarios = new JMenu("Usuarios");
            menuUsuarios.setMnemonic('U');

            JMenuItem itemUsuarios = new JMenuItem("Gestión de Usuarios", 'G');
            itemUsuarios.addActionListener(e -> openUsuarios());
            menuUsuarios.add(itemUsuarios);

            JMenuItem itemRoles = new JMenuItem("Roles y Permisos", 'R');
            itemRoles.addActionListener(e -> openRoles());
            menuUsuarios.add(itemRoles);

            menuBar.add(menuUsuarios);
        }

        // Menú Reportes (separado de ventas)
        if (authService.canViewReports()) {
            JMenu menuReportes = new JMenu("Reportes");
            menuReportes.setMnemonic('R');

            JMenuItem itemDashboard = new JMenuItem("Dashboard", 'D');
            itemDashboard.addActionListener(e -> openReportes());
            menuReportes.add(itemDashboard);

            JMenuItem itemReportesVentas = new JMenuItem("Reportes de Ventas", 'V');
            itemReportesVentas.addActionListener(e -> openReportesVentas());
            menuReportes.add(itemReportesVentas);

            JMenuItem itemReportesProductos = new JMenuItem("Reportes de Inventario", 'I');
            itemReportesProductos.addActionListener(e -> openReportesProductos());
            menuReportes.add(itemReportesProductos);

            JMenuItem itemReportesClientes = new JMenuItem("Reportes de Clientes", 'C');
            itemReportesClientes.addActionListener(e -> openReportesClientes());
            menuReportes.add(itemReportesClientes);

            menuBar.add(menuReportes);
        }

        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        menuAyuda.setMnemonic('Y');

        JMenuItem itemAcerca = new JMenuItem("Acerca de...", 'A');
        itemAcerca.addActionListener(e -> showAbout());
        menuAyuda.add(itemAcerca);

        menuBar.add(menuArchivo);
        menuBar.add(menuAyuda);

        setJMenuBar(menuBar);
    }

    private void setupToolBar() {
        toolBar.setFloatable(false);
        toolBar.setBackground(new Color(248, 249, 250));

        // Botón Nueva Venta
        if (authService.canMakeSales()) {
            JButton btnNuevaVenta = new JButton("Nueva Venta");
            btnNuevaVenta.setIcon(createIcon("💰"));
            btnNuevaVenta.addActionListener(e -> openNuevaVenta());
            toolBar.add(btnNuevaVenta);
            toolBar.addSeparator();
        }

        // Botón Productos
        if (authService.canManageInventory()) {
            JButton btnProductos = new JButton("Productos");
            btnProductos.setIcon(createIcon("🏷️"));
            btnProductos.addActionListener(e -> openProductos());
            toolBar.add(btnProductos);
        }

        // Botón Clientes
        JButton btnClientes = new JButton("Clientes");
        btnClientes.setIcon(createIcon("👤"));
        btnClientes.addActionListener(e -> openClientes());
        toolBar.add(btnClientes);

        // ✅ AGREGAR BOTÓN USUARIOS SI TIENE PERMISOS
        if (authService.canManageUsers()) {
            JButton btnUsuarios = new JButton("Usuarios");
            btnUsuarios.setIcon(createIcon("👥"));
            btnUsuarios.addActionListener(e -> openUsuarios());
            toolBar.add(btnUsuarios);
        }

        if (authService.canViewReports()) {
            toolBar.addSeparator();
            JButton btnReportes = new JButton("Reportes");
            btnReportes.setIcon(createIcon("📈"));
            btnReportes.addActionListener(e -> openReportes());
            toolBar.add(btnReportes);
        }

        // Espacio flexible
        toolBar.add(Box.createHorizontalGlue());

        // Botón Logout
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setIcon(createIcon("🔐"));
        btnLogout.addActionListener(e -> logout());
        toolBar.add(btnLogout);

        add(toolBar, BorderLayout.NORTH);
    }

    private Icon createIcon(String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g.drawString(emoji, x, y + 16);
            }

            @Override
            public int getIconWidth() {
                return 20;
            }

            @Override
            public int getIconHeight() {
                return 20;
            }
        };
    }

    private void setupStatusBar() {
        // Crear panel contenedor para ambas barras de estado
        JPanel statusContainer = new JPanel(new BorderLayout());

        // Barra de estado original (fecha/hora)
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.setBackground(new Color(248, 249, 250));

        // Panel izquierdo con información del usuario
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setBackground(new Color(248, 249, 250));
        leftPanel.add(lblStatusUser);

        // Panel derecho con fecha y hora
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.add(lblStatusTime);

        statusPanel.add(leftPanel, BorderLayout.WEST);
        statusPanel.add(rightPanel, BorderLayout.EAST);

        // ✅ VERIFICAR QUE EL PANEL NO SEA NULL Y USAR EL NUEVO NOMBRE
        if (panelUsuarioInfo != null) {
            statusContainer.add(panelUsuarioInfo, BorderLayout.NORTH); // Panel de usuario arriba
            System.out.println("✅ Panel de usuario agregado a la interfaz");
        } else {
            System.err.println("⚠️ panelUsuarioInfo es null, saltando...");
        }

        statusContainer.add(statusPanel, BorderLayout.SOUTH); // Panel de fecha/hora abajo

        add(statusContainer, BorderLayout.SOUTH);
    }

    private void setupWindow() {
        setTitle("Sistema de Ventas v1.5");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // Icono de la aplicación
        try {
            setIconImage(Toolkit.getDefaultToolkit().createImage("resources/icon.png"));
        } catch (Exception e) {
            // Si no hay icono, continuar sin él
        }

        // Manejo del cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });

        setVisible(true);
    }

    private void setupTimer() {
        statusTimer = new Timer(1000, e -> updateTime());
        statusTimer.start();
    }

    private void updateUserInfo() {
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            lblStatusUser.setText("Usuario: " + currentUser.getNombreCompleto() +
                    " | Rol: " + currentUser.getRolNombre());
        }
    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        lblStatusTime.setText("Fecha y Hora: " + sdf.format(new Date()));
    }

    // Método para cambiar panel
    private void cambiarPanel(JPanel newPanel, String title) {
        contentPanel.removeAll();
        currentPanel = newPanel;
        contentPanel.add(newPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        setTitle("Sistema de Ventas v1.5 - " + title);
    }

    // ===== MÉTODOS DE NAVEGACIÓN =====

    // Módulo de Ventas
    private void openNuevaVenta() {
        try {
            // Crear controller temporal para nueva venta
            VentaController ventaController = new VentaController();
            ventaController.mostrarFormularioNuevaVenta();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir nueva venta: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openVentas() {
        VentaPanel ventaPanel = new VentaPanel();
        cambiarPanel(ventaPanel, "Gestión de Ventas");
        // Enfocar en la pestaña de búsqueda de ventas
        SwingUtilities.invokeLater(() -> {
            if (ventaPanel.getComponentCount() > 0 && ventaPanel.getComponent(1) instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) ventaPanel.getComponent(1);
                tabbedPane.setSelectedIndex(1); // Pestaña "Buscar Ventas"
            }
        });
    }

    // Módulo de Inventario (solo productos)
    private void openProductos() {
        cambiarPanel(new ProductoPanel(), "Gestión de Productos");
    }

    // Módulo de Clientes
    private void openClientes() {
        cambiarPanel(new ClientePanel(), "Gestión de Clientes");
    }

    // ✅ COMPLETAR EL MÉTODO DE USUARIOS
    private void openUsuarios() {
        try {
            // Verificar permisos
            if (!authService.canManageUsers()) {
                JOptionPane.showMessageDialog(this,
                        "No tiene permisos para gestionar usuarios",
                        "Acceso Denegado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Crear el panel si no existe
            if (usuarioPanel == null) {
                usuarioPanel = new UsuarioPanel();
            }

            // Cambiar al panel de usuarios
            cambiarPanel(usuarioPanel, "Gestión de Usuarios");

            System.out.println("✅ Panel de usuarios mostrado");

        } catch (Exception e) {
            System.err.println("Error al mostrar panel de usuarios: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al cargar módulo de usuarios: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRoles() {
        try {
            // Verificar permisos
            if (!authService.canManageUsers()) {
                JOptionPane.showMessageDialog(this,
                        "No tiene permisos para gestionar roles",
                        "Acceso Denegado",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Por ahora usar el mismo panel de usuarios
            // En el futuro se puede crear un panel específico para roles
            if (usuarioPanel == null) {
                usuarioPanel = new UsuarioPanel();
            }

            cambiarPanel(usuarioPanel, "Roles y Permisos");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar módulo de roles: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Módulo de Reportes
    private void openReportes() {
        ReportePanel reportePanel = new ReportePanel();
        cambiarPanel(reportePanel, "Dashboard");
        // Enfocar en la pestaña de dashboard
        SwingUtilities.invokeLater(() -> {
            if (reportePanel.getComponentCount() > 0 && reportePanel.getComponent(1) instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) reportePanel.getComponent(1);
                tabbedPane.setSelectedIndex(0); // Pestaña "Dashboard"
            }
        });
    }

    private void openReportesVentas() {
        ReportePanel reportePanel = new ReportePanel();
        cambiarPanel(reportePanel, "Reportes de Ventas");
        SwingUtilities.invokeLater(() -> {
            if (reportePanel.getComponentCount() > 0 && reportePanel.getComponent(1) instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) reportePanel.getComponent(1);
                tabbedPane.setSelectedIndex(1); // Pestaña "Reportes de Ventas"
            }
        });
    }

    private void openReportesProductos() {
        ReportePanel reportePanel = new ReportePanel();
        cambiarPanel(reportePanel, "Reportes de Inventario");
        SwingUtilities.invokeLater(() -> {
            if (reportePanel.getComponentCount() > 0 && reportePanel.getComponent(1) instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) reportePanel.getComponent(1);
                tabbedPane.setSelectedIndex(2); // Pestaña "Reportes de Inventario"
            }
        });
    }

    private void openReportesClientes() {
        ReportePanel reportePanel = new ReportePanel();
        cambiarPanel(reportePanel, "Reportes de Clientes");
        SwingUtilities.invokeLater(() -> {
            if (reportePanel.getComponentCount() > 0 && reportePanel.getComponent(1) instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) reportePanel.getComponent(1);
                tabbedPane.setSelectedIndex(3); // Pestaña "Reportes de Clientes"
            }
        });
    }

    // ===== MÉTODOS DE CONTROL =====

    private void logout() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar Logout",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // ✅ HACER LOGOUT COMPLETO
            realizarLogout();
        }
    }

    public void realizarLogout() {
        try {
            // Detener timer de estado
            if (statusTimer != null) {
                statusTimer.stop();
            }

            // ✅ CAMBIAR ESTA PARTE - YA NO HAY MÉTODO detenerMonitoreo()
            // Solo limpiamos el panel
            if (panelUsuarioInfo != null) {
                panelUsuarioInfo.removeAll();
                panelUsuarioInfo = null;
            }

            // Cerrar sesión
            authService.logout();

            // Cerrar ventana actual
            dispose();

            // Mostrar ventana de login
            new LoginController();

        } catch (Exception e) {
            System.err.println("Error durante logout: " + e.getMessage());
        }
    }

    private void exitApplication() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea salir del sistema?",
                "Confirmar Salida",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            // Detener timer
            if (statusTimer != null) {
                statusTimer.stop();
            }
            
            if (panelUsuarioInfo != null) {
                panelUsuarioInfo.removeAll();
                panelUsuarioInfo = null;
            }

            // Cerrar sesión y salir
            authService.logout();
            System.exit(0);
        }
    }

    private void showAbout() {
        String message = "SISTEMA DE VENTAS v1.5\n\n" +
                "Proyecto de Calidad de Software\n" +
                "Desarrollado con Java Swing\n\n" +
                "MÓDULOS IMPLEMENTADOS:\n" +
                "✅ Sistema de Autenticación\n" +
                "✅ Gestión de Usuarios y Roles\n" +
                "✅ Módulo de Productos e Inventario\n" +
                "✅ Módulo de Clientes\n" +
                "✅ Módulo de Ventas\n" +
                "✅ Sistema de Reportes\n\n" +
                "CARACTERÍSTICAS ACTUALES:\n" +
                "• Interfaz gráfica moderna\n" +
                "• Sistema de permisos dinámico\n" +
                "• Validaciones robustas\n" +
                "• Gestión de stock con alertas\n" +
                "• Búsquedas y filtros avanzados\n" +
                "• Auditoría y seguridad\n" +
                "• Carrito de compras inteligente\n" +
                "• Cálculos automáticos (IVA, descuentos)\n" +
                "• Facturación automática\n" +
                "• Reportes avanzados\n\n" +
                "TECNOLOGÍAS:\n" +
                "• Java 11+ con Swing\n" +
                "• MySQL con XAMPP\n" +
                "• Patrón MVC\n" +
                "• BCrypt para seguridad\n\n" +
                "Estado: FUNCIONAL Y COMPLETO\n" +
                "Versión estable para producción";

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 600));

        JOptionPane.showMessageDialog(this, scrollPane, "Acerca del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
}