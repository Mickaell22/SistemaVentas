package views.main;

import services.AuthService;
import models.Usuario;
import utils.SessionManager;
import controllers.LoginController;
import views.clientes.ClientePanel;
import views.productos.ProductoPanel;

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
            panel.add(createQuickButton("Inventario", "📦", this::openInventario));
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
            itemNuevaVenta.addActionListener(e -> openNuevaVenta());

            JMenuItem itemGestionVentas = new JMenuItem("Gestión de Ventas");
            itemGestionVentas.addActionListener(e -> openVentas());

            JMenuItem itemHistorialVentas = new JMenuItem("Historial de Ventas");
            itemHistorialVentas.addActionListener(e -> openHistorialVentas());

            menuVentas.add(itemNuevaVenta);
            menuVentas.addSeparator();
            menuVentas.add(itemGestionVentas);
            menuVentas.add(itemHistorialVentas);
            menuBar.add(menuVentas);
        }

        // Menú Inventario
        if (authService.canManageInventory()) {
            JMenu menuInventario = new JMenu("Inventario");

            JMenuItem itemProductos = new JMenuItem("Productos");
            itemProductos.addActionListener(e -> openProductos());

            JMenuItem itemCategorias = new JMenuItem("Categorías");
            itemCategorias.addActionListener(e -> openCategorias());

            JMenuItem itemProveedores = new JMenuItem("Proveedores");
            itemProveedores.addActionListener(e -> openProveedores());

            JMenuItem itemInventario = new JMenuItem("Control de Stock");
            itemInventario.addActionListener(e -> openInventario());

            menuInventario.add(itemProductos);
            menuInventario.add(itemCategorias);
            menuInventario.add(itemProveedores);
            menuInventario.addSeparator();
            menuInventario.add(itemInventario);
            menuBar.add(menuInventario);
        }

        // Menú Clientes
        JMenu menuClientes = new JMenu("Clientes");
        
        JMenuItem itemGestionClientes = new JMenuItem("Gestión de Clientes");
        itemGestionClientes.addActionListener(e -> openClientes());
        
        if (authService.canViewReports()) {
            JMenuItem itemReportesClientes = new JMenuItem("Reportes de Clientes");
            itemReportesClientes.addActionListener(e -> openReportesClientes());
            menuClientes.add(itemGestionClientes);
            menuClientes.addSeparator();
            menuClientes.add(itemReportesClientes);
        } else {
            menuClientes.add(itemGestionClientes);
        }
        
        menuBar.add(menuClientes);

        // Menú Administración
        if (authService.canManageUsers()) {
            JMenu menuAdmin = new JMenu("Administración");

            JMenuItem itemUsuarios = new JMenuItem("Usuarios");
            itemUsuarios.addActionListener(e -> openUsuarios());

            JMenuItem itemRoles = new JMenuItem("Roles y Permisos");
            itemRoles.addActionListener(e -> openRoles());

            JMenuItem itemConfiguracion = new JMenuItem("Configuración");
            itemConfiguracion.addActionListener(e -> openConfiguracion());

            menuAdmin.add(itemUsuarios);
            menuAdmin.add(itemRoles);
            menuAdmin.addSeparator();
            menuAdmin.add(itemConfiguracion);
            menuBar.add(menuAdmin);
        }

        // Menú Reportes
        if (authService.canViewReports()) {
            JMenu menuReportes = new JMenu("Reportes");

            JMenuItem itemReportesVentas = new JMenuItem("Reportes de Ventas");
            itemReportesVentas.addActionListener(e -> openReportesVentas());

            JMenuItem itemReportesProductos = new JMenuItem("Reportes de Productos");
            itemReportesProductos.addActionListener(e -> openReportesProductos());

            JMenuItem itemEstadisticas = new JMenuItem("Estadísticas Generales");
            itemEstadisticas.addActionListener(e -> openEstadisticas());

            menuReportes.add(itemReportesVentas);
            menuReportes.add(itemReportesProductos);
            menuReportes.addSeparator();
            menuReportes.add(itemEstadisticas);
            menuBar.add(menuReportes);
        }

        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        
        JMenuItem itemManual = new JMenuItem("Manual de Usuario");
        itemManual.addActionListener(e -> showManual());
        
        JMenuItem itemAcerca = new JMenuItem("Acerca de...");
        itemAcerca.addActionListener(e -> showAbout());
        
        menuAyuda.add(itemManual);
        menuAyuda.addSeparator();
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
            addToolBarButton("Nueva Venta", "💰", this::openNuevaVenta);
            addToolBarButton("Ventas", "📊", this::openVentas);
            toolBar.addSeparator();
        }

        if (authService.canManageInventory()) {
            addToolBarButton("Productos", "🏷️", this::openProductos);
            addToolBarButton("Inventario", "📦", this::openInventario);
            toolBar.addSeparator();
        }

        addToolBarButton("Clientes", "👤", this::openClientes);

        if (authService.canViewReports()) {
            toolBar.addSeparator();
            addToolBarButton("Reportes", "📈", this::openReportes);
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

    // ===== MÉTODOS DE NAVEGACIÓN =====

    // Módulo de Ventas (Próximamente)
    private void openNuevaVenta() {
        JOptionPane.showMessageDialog(this, 
            "MÓDULO DE VENTAS\n\n" +
            "El módulo de ventas está siendo implementado.\n" +
            "Próximamente estará disponible con:\n\n" +
            "• Carrito de compras inteligente\n" +
            "• Cálculo automático de IVA y descuentos\n" +
            "• Gestión de stock en tiempo real\n" +
            "• Facturación automática\n" +
            "• Múltiples métodos de pago", 
            "Nueva Venta - Próximamente", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void openVentas() {
        JOptionPane.showMessageDialog(this, 
            "GESTIÓN DE VENTAS\n\n" +
            "Funcionalidades pendientes:\n\n" +
            "• Ver historial de ventas\n" +
            "• Editar ventas pendientes\n" +
            "• Completar y cancelar ventas\n" +
            "• Búsquedas y filtros avanzados\n" +
            "• Estadísticas de ventas", 
            "Gestión de Ventas - Próximamente", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void openHistorialVentas() {
        JOptionPane.showMessageDialog(this, 
            "HISTORIAL DE VENTAS\n\n" +
            "Próximamente podrás:\n\n" +
            "• Consultar todas las ventas realizadas\n" +
            "• Filtrar por fechas, clientes y estados\n" +
            "• Ver detalles completos de cada venta\n" +
            "• Generar reportes de ventas\n" +
            "• Imprimir facturas", 
            "Historial de Ventas - Próximamente", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    // Módulo de Inventario
    private void openInventario() {
        cambiarPanel(new ProductoPanel(), "Control de Inventario");
    }

    private void openProductos() {
        cambiarPanel(new ProductoPanel(), "Gestión de Productos");
    }

    private void openCategorias() {
        JOptionPane.showMessageDialog(this, "Módulo de Categorías - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openProveedores() {
        JOptionPane.showMessageDialog(this, "Módulo de Proveedores - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Módulo de Clientes
    private void openClientes() {
        cambiarPanel(new ClientePanel(), "Gestión de Clientes");
    }

    // Módulo de Usuarios
    private void openUsuarios() {
        JOptionPane.showMessageDialog(this, "Módulo de Usuarios - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openRoles() {
        JOptionPane.showMessageDialog(this, "Módulo de Roles - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openConfiguracion() {
        JOptionPane.showMessageDialog(this, "Módulo de Configuración - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Módulo de Reportes
    private void openReportes() {
        JOptionPane.showMessageDialog(this, "Módulo de Reportes Generales - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openReportesVentas() {
        JOptionPane.showMessageDialog(this, "Reportes de Ventas - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openReportesProductos() {
        JOptionPane.showMessageDialog(this, "Reportes de Productos - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openReportesClientes() {
        JOptionPane.showMessageDialog(this, "Reportes de Clientes - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openEstadisticas() {
        JOptionPane.showMessageDialog(this, "Estadísticas Generales - Próximamente", "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Ayuda
    private void showManual() {
        String mensaje = "MANUAL DE USUARIO - SISTEMA DE VENTAS\n\n" +
                "MÓDULOS DISPONIBLES:\n\n" +
                "• PRODUCTOS E INVENTARIO:\n" +
                "  - Gestión completa de productos\n" +
                "  - Control de stock con alertas\n" +
                "  - Categorías y proveedores\n" +
                "  - Cálculo automático de márgenes\n\n" +
                "• CLIENTES:\n" +
                "  - Gestión completa de clientes\n" +
                "  - Validaciones de documentos ecuatorianos\n" +
                "  - Búsquedas y filtros avanzados\n\n" +
                "• VENTAS (Próximamente):\n" +
                "  - Carrito de compras inteligente\n" +
                "  - Cálculo automático de IVA\n" +
                "  - Gestión de facturas\n\n" +
                "PERMISOS POR ROL:\n" +
                "• Administrador: Acceso completo\n" +
                "• Gerente: Gestión y reportes\n" +
                "• Vendedor: Ventas e inventario\n" +
                "• Cajero: Solo ventas\n\n" +
                "ESTADO ACTUAL:\n" +
                "✅ Autenticación y usuarios\n" +
                "✅ Productos e inventario\n" +
                "✅ Gestión de clientes\n" +
                "🔄 Módulo de ventas (en desarrollo)";

        JTextArea textArea = new JTextArea(mensaje);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Manual de Usuario", JOptionPane.INFORMATION_MESSAGE);
    }

    // Método para cambiar el panel central
    private void cambiarPanel(JPanel nuevoPanel, String titulo) {
        contentPanel.removeAll();
        contentPanel.add(nuevoPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();

        // Actualizar título de la ventana
        setTitle("Sistema de Ventas - " + titulo + " - " + authService.getCurrentUser().getRolNombre());
    }

    private void logout() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar Logout",
                JOptionPane.YES_NO_OPTION);

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
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            statusTimer.stop();
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
                "🔄 Módulo de Ventas (en desarrollo)\n\n" +
                "CARACTERÍSTICAS ACTUALES:\n" +
                "• Interfaz gráfica moderna\n" +
                "• Sistema de permisos dinámico\n" +
                "• Validaciones robustas\n" +
                "• Gestión de stock con alertas\n" +
                "• Búsquedas y filtros avanzados\n" +
                "• Auditoría y seguridad\n\n" +
                "PRÓXIMAS CARACTERÍSTICAS:\n" +
                "• Carrito de compras inteligente\n" +
                "• Cálculos automáticos (IVA, descuentos)\n" +
                "• Facturación automática\n" +
                "• Reportes avanzados\n\n" +
                "TECNOLOGÍAS:\n" +
                "• Java 11+ con Swing\n" +
                "• MySQL con XAMPP\n" +
                "• Patrón MVC\n" +
                "• BCrypt para seguridad\n\n" +
                "Estado: PARCIALMENTE FUNCIONAL\n" +
                "Versión estable lista para uso";

        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 600));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Acerca del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }
}