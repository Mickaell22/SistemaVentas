// Main.java - Ubicación: src/Main.java

import config.DatabaseConnection;
import controllers.LoginController;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;

public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE VENTAS ===");
        System.out.println("Iniciando aplicación...\n");
        
        // Configurar Look and Feel
        try {
            // Usar Nimbus si está disponible, sino el del sistema
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                System.out.println("Fallo");
            } catch (Exception ex) {
                System.out.println("No se pudo con el fallo");
            }
        }
        
        // Probar conexión a la base de datos
        try {
            DatabaseConnection.getInstance();
            System.out.println("Conexión a base de datos establecida");
            
            // Iniciar la aplicación en el hilo de eventos de Swing
            SwingUtilities.invokeLater(() -> {
                System.out.println("Iniciando interfaz gráfica...");
                new LoginController();
            });
            
        } catch (Exception e) {
            System.err.println("Error al iniciar el sistema: " + e.getMessage());
            
            // Mostrar error en ventana si es posible
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                    null,
                    "Error al conectar con la base de datos:\n" + e.getMessage() + 
                    "\n\nVerifique que XAMPP esté ejecutándose y la base de datos configurada correctamente.",
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE
                );
            });
            
            System.exit(1);
        }
    }
}