package controllers;

import services.AuthService;
import views.main.LoginFrame;
import views.main.MainFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class LoginController {
    
    private LoginFrame loginFrame;
    private AuthService authService;
    
    public LoginController() {
        this.authService = AuthService.getInstance();
        initializeView();
    }
    
    private void initializeView() {
        SwingUtilities.invokeLater(() -> {
            this.loginFrame = new LoginFrame(this);
            this.loginFrame.setVisible(true);
        });
    }
    
    /**
     * Procesa el intento de login
     */
    public void processLogin(String username, String password) {
        // Validar entrada
        if (username == null || username.trim().isEmpty()) {
            showError("Por favor ingrese su nombre de usuario");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            showError("Por favor ingrese su contraseña");
            return;
        }
        
        // Mostrar indicador de carga
        loginFrame.setLoading(true);
        
        // Procesar login en un hilo separado para no bloquear la UI
        SwingUtilities.invokeLater(() -> {
            try {
                boolean loginSuccess = authService.login(username.trim(), password);
                
                loginFrame.setLoading(false);
                
                if (loginSuccess) {
                    onLoginSuccess();
                } else {
                    showError("Usuario o contraseña incorrectos");
                    loginFrame.clearPassword();
                }
                
            } catch (Exception e) {
                loginFrame.setLoading(false);
                showError("Error durante el login: " + e.getMessage());
                System.err.println("Error en login: " + e.getMessage());
            }
        });
    }
    
    /**
     * Maneja login exitoso
     */
    private void onLoginSuccess() {
        String welcomeMessage = "¡Bienvenido " + authService.getCurrentUser().getNombreCompleto() + "!";
        showInfo(welcomeMessage);
        
        // Cerrar ventana de login
        loginFrame.dispose();
        
        // Abrir ventana principal
        SwingUtilities.invokeLater(() -> {
            new MainFrame();
        });
    }
    
    /**
     * Maneja la cancelación del login
     */
    public void cancelLogin() {
        int option = JOptionPane.showConfirmDialog(
            loginFrame,
            "¿Está seguro que desea salir del sistema?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    /**
     * Muestra mensaje de error
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            loginFrame,
            message,
            "Error de Autenticación",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Muestra mensaje informativo
     */
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            loginFrame,
            message,
            "Login Exitoso",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Obtiene la ventana de login
     */
    public LoginFrame getLoginFrame() {
        return loginFrame;
    }
}