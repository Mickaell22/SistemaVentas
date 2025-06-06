package Test;
import config.DatabaseConnection;
import services.AuthService;
import services.UsuarioService;
import models.Usuario;
import dao.impl.UsuarioDAOImpl;
import utils.PasswordUtils;

public class TestCompleteSystem {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA COMPLETA DEL SISTEMA ===\n");
        
        try {
            // 1. Verificar conexión a BD
            System.out.println("1. Verificando conexión a base de datos...");
            DatabaseConnection.getInstance();
            System.out.println("Conexión establecida\n");
            
            // 2. Verificar que existe usuario admin
            System.out.println("2. Verificando usuario administrador...");
            UsuarioDAOImpl usuarioDAO = new UsuarioDAOImpl();
            
            if (!usuarioDAO.existeUsername("admin")) {
                System.out.println("   Usuario admin no encontrado, creando...");
                crearUsuarioAdmin();
            } else {
                System.out.println("   Usuario admin existe");
            }
            
            // 3. Actualizar contraseña admin para testing
            System.out.println("\n3. Actualizando contraseña de admin para testing...");
            String hashedPassword = PasswordUtils.hashPassword("admin123");
            
            // Actualizar directamente en BD
            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(
                "UPDATE usuarios SET password = ? WHERE username = 'admin'"
            );
            stmt.setString(1, hashedPassword);
            int updated = stmt.executeUpdate();
            conn.commit();
            stmt.close();
            
            System.out.println("   Contraseña actualizada (filas afectadas: " + updated + ")");
            
            // 4. Probar autenticación
            System.out.println("\n4. Probando autenticación...");
            AuthService authService = AuthService.getInstance();
            
            boolean loginResult = authService.login("admin", "admin123");
            System.out.println("   Login admin/admin123: " + (loginResult ? "EXITOSO" : "FALLÓ"));
            
            if (loginResult) {
                Usuario currentUser = authService.getCurrentUser();
                System.out.println("   Usuario logueado: " + currentUser.getNombreCompleto());
                System.out.println("   Rol: " + currentUser.getRolNombre());
                System.out.println("   Permisos:");
                System.out.println("     - Es Admin: " + authService.isAdmin());
                System.out.println("     - Puede gestionar usuarios: " + authService.canManageUsers());
                System.out.println("     - Puede ver reportes: " + authService.canViewReports());
                System.out.println("     - Puede hacer ventas: " + authService.canMakeSales());
            }
            
            // 5. Crear usuario de prueba
            System.out.println("\n5. Creando usuario de prueba...");
            UsuarioService usuarioService = UsuarioService.getInstance();
            
            Usuario usuarioPrueba = new Usuario(
                "Usuario", "Prueba", "prueba@test.com", "prueba",
                "Prueba123", "0999888777", "Dirección de prueba", 3
            );
            
            boolean usuarioCreado = usuarioService.crearUsuario(usuarioPrueba);
            System.out.println("   Usuario de prueba creado: " + (usuarioCreado ? "SÍ" : "NO"));
            
            // 6. Probar login con usuario de prueba
            if (usuarioCreado) {
                System.out.println("\n6. Probando login con usuario de prueba...");
                authService.logout();
                
                boolean loginPrueba = authService.login("prueba", "Prueba123");
                System.out.println("   Login prueba/Prueba123: " + (loginPrueba ? "EXITOSO" : "FALLÓ"));
                
                if (loginPrueba) {
                    Usuario userPrueba = authService.getCurrentUser();
                    System.out.println("   Usuario: " + userPrueba.getNombreCompleto());
                    System.out.println("   Rol: " + userPrueba.getRolNombre());
                    System.out.println("   Es Vendedor: " + authService.isVendedor());
                }
            }
            
            // 7. Resumen final
            System.out.println("\n" + "=".repeat(50));
            System.out.println("SISTEMA LISTO PARA USAR");
            System.out.println("=".repeat(50));
            System.out.println("Credenciales disponibles:");
            System.out.println("   Admin: admin / admin123");
            if (usuarioCreado) {
                System.out.println("   Prueba: prueba / Prueba123");
            }
            System.out.println("\nEjecute Main.java para iniciar la aplicación");
            
        } catch (Exception e) {
            System.err.println("Error durante la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void crearUsuarioAdmin() {
        try {
            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Insertar usuario admin
            String sql = "INSERT INTO usuarios (nombre, apellido, email, username, password, rol_id) VALUES (?, ?, ?, ?, ?, ?)";
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, "Administrador");
            stmt.setString(2, "Sistema");
            stmt.setString(3, "admin@sistema.com");
            stmt.setString(4, "admin");
            stmt.setString(5, PasswordUtils.hashPassword("admin123"));
            stmt.setInt(6, 1); // Rol administrador
            
            int inserted = stmt.executeUpdate();
            conn.commit();
            stmt.close();
            
            System.out.println("   Usuario admin creado (filas insertadas: " + inserted + ")");
            
        } catch (Exception e) {
            System.err.println("   Error creando usuario admin: " + e.getMessage());
        }
    }
}