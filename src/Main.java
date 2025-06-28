import config.DatabaseConnection;
import controllers.LoginController;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE VENTAS ===");
        System.out.println("Iniciando aplicación...\n");

        // Configurar Look and Feel - VERSIÓN CORREGIDA
        configurarLookAndFeel();

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
                        JOptionPane.ERROR_MESSAGE);
            });

            System.exit(1);
        }
    }

    /**
     * Configura el Look and Feel de la aplicación
     * Método separado para mejor manejo de errores
     */
    private static void configurarLookAndFeel() {
        try {
            // Intentar usar Nimbus primero (más moderno)
            boolean nimbusEncontrado = false;

            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusEncontrado = true;
                    System.out.println("Look and Feel configurado: Nimbus");
                    break;
                }
            }

            // Si no encontró Nimbus, usar el del sistema
            if (!nimbusEncontrado) {
                try {
                    // Usar solo Nimbus (más seguro)
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Si falla, usar el por defecto (no hacer nada)
                    System.out.println("Usando Look and Feel por defecto");
                }
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            // Si todo falla, continuar con el Look and Feel por defecto
            System.out.println("No se pudo configurar Look and Feel personalizado, usando por defecto");
        }
    }
}