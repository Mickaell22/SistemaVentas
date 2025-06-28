package views.usuarios;

import models.Usuario;
import models.Rol;
import services.UsuarioService;
import services.AuthService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

public class UsuarioPanel extends JPanel {
    
    private UsuarioService usuarioService;
    private AuthService authService;
    private DefaultTableModel tableModel;
    private JTable tablaUsuarios;
    private JTextField txtBuscar;
    private JButton btnNuevo, btnEditar, btnEliminar, btnActivar, btnDesactivar, btnCambiarPassword, btnActualizar;
    private JLabel lblEstadisticas;
    private List<Rol> roles;
    
    public UsuarioPanel() {
        this.usuarioService = UsuarioService.getInstance();
        this.authService = AuthService.getInstance();
        initializeComponents();
        setupLayout();
        setupEvents();
        cargarDatos();
    }
    
    private void initializeComponents() {
        // Modelo de tabla simple
        String[] columnas = {"ID", "Nombre Completo", "Username", "Email", "Rol", "Estado"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Solo lectura
            }
        };
        
        tablaUsuarios = new JTable(tableModel);
        setupTable();
        
        // Componentes de búsqueda
        txtBuscar = new JTextField(20);
        
        // Botones
        btnNuevo = new JButton("➕ Nuevo Usuario");
        btnEditar = new JButton("✏️ Editar");
        btnEliminar = new JButton("🗑️ Eliminar");
        btnActivar = new JButton("✅ Activar");
        btnDesactivar = new JButton("❌ Desactivar");
        btnCambiarPassword = new JButton("🔑 Cambiar Contraseña");
        btnActualizar = new JButton("🔄 Actualizar");
        
        // Estadísticas
        lblEstadisticas = new JLabel("Cargando estadísticas...");
        lblEstadisticas.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Configurar botones
        btnNuevo.setPreferredSize(new Dimension(150, 35));
        btnEditar.setPreferredSize(new Dimension(120, 35));
        btnEliminar.setPreferredSize(new Dimension(120, 35));
        btnActivar.setPreferredSize(new Dimension(120, 35));
        btnDesactivar.setPreferredSize(new Dimension(120, 35));
        btnCambiarPassword.setPreferredSize(new Dimension(150, 35));
        btnActualizar.setPreferredSize(new Dimension(120, 35));
        
        // Estados iniciales
        btnEditar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnActivar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        btnCambiarPassword.setEnabled(false);
        
        // Configurar colores
        btnNuevo.setBackground(new Color(40, 167, 69));
        btnNuevo.setForeground(Color.WHITE);
        btnEditar.setBackground(new Color(255, 193, 7));
        btnEliminar.setBackground(new Color(220, 53, 69));
        btnEliminar.setForeground(Color.WHITE);
        btnActivar.setBackground(new Color(40, 167, 69));
        btnActivar.setForeground(Color.WHITE);
        btnDesactivar.setBackground(new Color(108, 117, 125));
        btnDesactivar.setForeground(Color.WHITE);
        btnCambiarPassword.setBackground(new Color(0, 123, 255));
        btnCambiarPassword.setForeground(Color.WHITE);
    }
    
    private void setupTable() {
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setRowHeight(25);
        tablaUsuarios.getTableHeader().setReorderingAllowed(false);
        
        // Configurar anchos de columnas
        if (tablaUsuarios.getColumnModel().getColumnCount() > 0) {
            tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            tablaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(150); // Nombre
            tablaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(120); // Username
            tablaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(180); // Email
            tablaUsuarios.getColumnModel().getColumn(4).setPreferredWidth(100); // Rol
            tablaUsuarios.getColumnModel().getColumn(5).setPreferredWidth(80);  // Estado
        }
        
        // Selección de fila
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarEstadoBotones();
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior - Búsqueda y estadísticas
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Gestión de Usuarios"));
        
        // Búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("🔍 Buscar:"));
        panelBusqueda.add(txtBuscar);
        panelBusqueda.add(btnActualizar);
        
        // Estadísticas
        JPanel panelEstadisticas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelEstadisticas.add(lblEstadisticas);
        
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelEstadisticas, BorderLayout.EAST);
        
        // Panel central - Tabla
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        panelCentral.add(scrollPane, BorderLayout.CENTER);
        
        // Panel inferior - Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelBotones.setBorder(BorderFactory.createTitledBorder("Acciones"));
        
        panelBotones.add(btnNuevo);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL));
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL));
        panelBotones.add(btnCambiarPassword);
        panelBotones.add(new JSeparator(SwingConstants.VERTICAL));
        panelBotones.add(btnActivar);
        panelBotones.add(btnDesactivar);
        
        // Agregar paneles al layout principal
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private void setupEvents() {
        // Búsqueda en tiempo real
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String termino = txtBuscar.getText().trim();
                buscarUsuarios(termino);
            }
        });
        
        // Doble clic en tabla para editar
        tablaUsuarios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tablaUsuarios.getSelectedRow() != -1) {
                    editarUsuario();
                }
            }
        });
        
        // Eventos de botones
        btnNuevo.addActionListener(e -> nuevoUsuario());
        btnEditar.addActionListener(e -> editarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnActivar.addActionListener(e -> activarUsuario());
        btnDesactivar.addActionListener(e -> desactivarUsuario());
        btnCambiarPassword.addActionListener(e -> cambiarPassword());
        btnActualizar.addActionListener(e -> cargarDatos());
    }
    
    private void cargarDatos() {
        try {
            // Cargar usuarios
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            cargarUsuarios(usuarios);
            
            // Cargar roles
            roles = usuarioService.obtenerTodosLosRoles();
            
            // Actualizar estadísticas
            actualizarEstadisticas();
            
            System.out.println("✅ Datos cargados - Usuarios: " + usuarios.size() + ", Roles: " + roles.size());
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar datos: " + e.getMessage());
            mostrarError("Error al cargar datos: " + e.getMessage());
        }
    }
    
    private void cargarUsuarios(List<Usuario> usuarios) {
        try {
            // Limpiar tabla
            tableModel.setRowCount(0);
            
            // Agregar usuarios
            for (Usuario usuario : usuarios) {
                Object[] fila = {
                    usuario.getId(),
                    usuario.getNombreCompleto(),
                    usuario.getUsername(),
                    usuario.getEmail(),
                    usuario.getRolNombre() != null ? usuario.getRolNombre() : "Sin rol",
                    usuario.isActivo() ? "✅ Activo" : "❌ Inactivo"
                };
                tableModel.addRow(fila);
            }
            
            actualizarEstadoBotones();
            
        } catch (Exception e) {
            System.err.println("❌ Error al cargar usuarios en tabla: " + e.getMessage());
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    private void buscarUsuarios(String termino) {
        try {
            List<Usuario> todosUsuarios = usuarioService.obtenerTodosLosUsuarios();
            
            if (termino.isEmpty()) {
                cargarUsuarios(todosUsuarios);
            } else {
                List<Usuario> usuariosFiltrados = todosUsuarios.stream()
                    .filter(u -> u.getNombreCompleto().toLowerCase().contains(termino.toLowerCase()) ||
                               u.getUsername().toLowerCase().contains(termino.toLowerCase()) ||
                               u.getEmail().toLowerCase().contains(termino.toLowerCase()))
                    .toList();
                
                cargarUsuarios(usuariosFiltrados);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al buscar usuarios: " + e.getMessage());
        }
    }
    
    private void actualizarEstadoBotones() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        boolean haySeleccion = filaSeleccionada != -1;
        
        btnEditar.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion);
        btnCambiarPassword.setEnabled(haySeleccion);
        
        if (haySeleccion) {
            // Obtener estado del usuario de la tabla
            String estado = (String) tableModel.getValueAt(filaSeleccionada, 5);
            boolean esActivo = estado.contains("Activo");
            
            btnActivar.setEnabled(!esActivo);
            btnDesactivar.setEnabled(esActivo);
            
            // No permitir eliminar el propio usuario
            int userId = (Integer) tableModel.getValueAt(filaSeleccionada, 0);
            Usuario currentUser = authService.getCurrentUser();
            boolean esPropioUsuario = currentUser != null && currentUser.getId() == userId;
            
            if (esPropioUsuario) {
                btnEliminar.setEnabled(false);
                btnDesactivar.setEnabled(false);
            }
        } else {
            btnActivar.setEnabled(false);
            btnDesactivar.setEnabled(false);
        }
    }
    
    private void actualizarEstadisticas() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            List<Usuario> activos = usuarioService.obtenerUsuariosActivos();
            
            long totalUsuarios = usuarios.size();
            long usuariosActivos = activos.size();
            long usuariosInactivos = totalUsuarios - usuariosActivos;
            
            lblEstadisticas.setText(String.format(
                "Total: %d | Activos: %d | Inactivos: %d",
                totalUsuarios, usuariosActivos, usuariosInactivos
            ));
            
        } catch (Exception e) {
            lblEstadisticas.setText("Error al obtener estadísticas");
        }
    }
    
    // ===== OPERACIONES CRUD =====
    
    private void nuevoUsuario() {
        try {
            if (!authService.canManageUsers()) {
                mostrarError("No tiene permisos para crear usuarios");
                return;
            }
            
            UsuarioFormDialog dialog = new UsuarioFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                "Nuevo Usuario", 
                null, 
                roles
            );
            
            dialog.setVisible(true);
            
            if (dialog.isConfirmado()) {
                Usuario nuevoUsuario = dialog.getUsuario();
                boolean resultado = usuarioService.crearUsuario(nuevoUsuario);
                
                if (resultado) {
                    mostrarInfo("Usuario creado exitosamente");
                    cargarDatos(); // Recargar datos
                } else {
                    mostrarError("Error al crear el usuario");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear usuario: " + e.getMessage());
            mostrarError("Error al crear usuario: " + e.getMessage());
        }
    }
    
    private void editarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) return;
        
        try {
            if (!authService.canManageUsers()) {
                mostrarError("No tiene permisos para editar usuarios");
                return;
            }
            
            int userId = (Integer) tableModel.getValueAt(filaSeleccionada, 0);
            
            // Obtener usuario completo
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorId(userId);
            if (!usuarioOpt.isPresent()) {
                mostrarError("Usuario no encontrado");
                return;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            UsuarioFormDialog dialog = new UsuarioFormDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                "Editar Usuario", 
                usuario, 
                roles
            );
            
            dialog.setVisible(true);
            
            if (dialog.isConfirmado()) {
                Usuario usuarioEditado = dialog.getUsuario();
                boolean resultado = usuarioService.actualizarUsuario(usuarioEditado);
                
                if (resultado) {
                    mostrarInfo("Usuario actualizado exitosamente");
                    cargarDatos(); // Recargar datos
                } else {
                    mostrarError("Error al actualizar el usuario");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al editar usuario: " + e.getMessage());
            mostrarError("Error al editar usuario: " + e.getMessage());
        }
    }
    
    private void eliminarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) return;
        
        try {
            if (!authService.canManageUsers()) {
                mostrarError("No tiene permisos para eliminar usuarios");
                return;
            }
            
            int userId = (Integer) tableModel.getValueAt(filaSeleccionada, 0);
            String username = (String) tableModel.getValueAt(filaSeleccionada, 2);
            
            // Verificar que no sea el propio usuario
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser != null && currentUser.getId() == userId) {
                mostrarError("No puede eliminar su propio usuario");
                return;
            }
            
            int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea ELIMINAR permanentemente al usuario '" + username + "'?\n\n" +
                "Esta acción NO se puede deshacer. Considere desactivar el usuario en su lugar.",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                // Confirmación adicional
                String respuesta = JOptionPane.showInputDialog(
                    this,
                    "Para confirmar la eliminación, escriba: ELIMINAR",
                    "Confirmación Final",
                    JOptionPane.WARNING_MESSAGE
                );
                
                if ("ELIMINAR".equals(respuesta)) {
                    boolean resultado = usuarioService.eliminarUsuario(userId);
                    
                    if (resultado) {
                        mostrarInfo("Usuario eliminado exitosamente");
                        cargarDatos(); // Recargar datos
                    } else {
                        mostrarError("Error al eliminar el usuario");
                    }
                } else {
                    mostrarInfo("Eliminación cancelada");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar usuario: " + e.getMessage());
            mostrarError("Error al eliminar usuario: " + e.getMessage());
        }
    }
    
    private void activarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) return;
        
        int userId = (Integer) tableModel.getValueAt(filaSeleccionada, 0);
        String username = (String) tableModel.getValueAt(filaSeleccionada, 2);
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea activar al usuario '" + username + "'?",
            "Confirmar Activación",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                boolean resultado = usuarioService.activarUsuario(userId);
                if (resultado) {
                    mostrarInfo("Usuario activado exitosamente");
                    cargarDatos(); // Recargar datos
                } else {
                    mostrarError("Error al activar el usuario");
                }
            } catch (Exception e) {
                mostrarError("Error al activar usuario: " + e.getMessage());
            }
        }
    }
    
    private void desactivarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) return;
        
        int userId = (Integer) tableModel.getValueAt(filaSeleccionada, 0);
        String username = (String) tableModel.getValueAt(filaSeleccionada, 2);
        
        // Verificar que no sea el propio usuario
        Usuario currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.getId() == userId) {
            mostrarError("No puede desactivar su propio usuario");
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de que desea desactivar al usuario '" + username + "'?",
            "Confirmar Desactivación",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                boolean resultado = usuarioService.desactivarUsuario(userId);
                if (resultado) {
                    mostrarInfo("Usuario desactivado exitosamente");
                    cargarDatos(); // Recargar datos
                } else {
                    mostrarError("Error al desactivar el usuario");
                }
            } catch (Exception e) {
                mostrarError("Error al desactivar usuario: " + e.getMessage());
            }
        }
    }
    
    private void cambiarPassword() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) return;
        
        try {
            int userId = (Integer) tableModel.getValueAt(filaSeleccionada, 0);
            
            // Obtener usuario completo
            Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorId(userId);
            if (!usuarioOpt.isPresent()) {
                mostrarError("Usuario no encontrado");
                return;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            CambiarPasswordDialog dialog = new CambiarPasswordDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), 
                usuario
            );
            
            dialog.setVisible(true);
            
            if (dialog.isConfirmado()) {
                String passwordActual = dialog.getPasswordActual();
                String passwordNueva = dialog.getPasswordNueva();
                
                boolean resultado = usuarioService.cambiarPassword(userId, passwordActual, passwordNueva);
                
                if (resultado) {
                    mostrarInfo("Contraseña cambiada exitosamente");
                } else {
                    mostrarError("Error al cambiar la contraseña");
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al cambiar contraseña: " + e.getMessage());
            mostrarError("Error al cambiar contraseña: " + e.getMessage());
        }
    }
    
    // Métodos de utilidad
    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}