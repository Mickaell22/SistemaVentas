import services.AuthService;
import services.UsuarioService;
import models.Usuario;
import models.Rol;
import utils.SessionManager;
import java.util.List;

public class TestAuthService {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DEL SISTEMA DE AUTENTICACIÓN ===\n");
        
        AuthService authService = AuthService.getInstance();
        UsuarioService usuarioService = UsuarioService.getInstance();
        SessionManager sessionManager = SessionManager.getInstance();
        
        // 1. Probar estado inicial
        System.out.println("1. Estado inicial:");
        System.out.println("   Autenticado: " + authService.isAuthenticated());
        System.out.println("   Sesión: " + sessionManager.getSessionInfo());
        
        // 2. Probar login con credenciales incorrectas
        System.out.println("\n2. Login con credenciales incorrectas:");
        boolean loginFail = authService.login("admin", "wrongpassword");
        System.out.println("   Resultado: " + loginFail);
        System.out.println("   Autenticado: " + authService.isAuthenticated());
        
        // 3. Probar login con credenciales correctas
        System.out.println("\n3. Login con credenciales correctas:");
        boolean loginSuccess = authService.login("admin", "admin123");
        System.out.println("   Resultado: " + loginSuccess);
        System.out.println("   Autenticado: " + authService.isAuthenticated());
        
        if (loginSuccess) {
            Usuario currentUser = authService.getCurrentUser();
            System.out.println("   Usuario: " + currentUser.getNombreCompleto());
            System.out.println("   Rol: " + currentUser.getRolNombre());
            System.out.println("   Sesión: " + sessionManager.getSessionInfo());
            
            // 4. Probar permisos
            System.out.println("\n4. Verificación de permisos:");
            System.out.println("   Es Admin: " + authService.isAdmin());
            System.out.println("   Es Gerente: " + authService.isGerente());
            System.out.println("   Es Vendedor: " + authService.isVendedor());
            System.out.println("   Es Cajero: " + authService.isCajero());
            System.out.println("   Puede gestionar usuarios: " + authService.canManageUsers());
            System.out.println("   Puede ver reportes: " + authService.canViewReports());
            System.out.println("   Puede gestionar inventario: " + authService.canManageInventory());
            System.out.println("   Puede realizar ventas: " + authService.canMakeSales());
        }
        
        // 5. Probar UsuarioService - obtener roles
        System.out.println("\n5. Roles disponibles:");
        List<Rol> roles = usuarioService.obtenerTodosLosRoles();
        for (Rol rol : roles) {
            System.out.println("   " + rol.toString());
        }
        
        // 6. Probar UsuarioService - crear usuario
        System.out.println("\n6. 👤 Creando nuevo usuario:");
        Usuario nuevoUsuario = new Usuario(
            "Carlos", "López", "carlos@email.com", "clopez",
            "Password123", "0999888777", "Calle 1 y 2", 3
        );
        
        boolean usuarioCreado = usuarioService.crearUsuario(nuevoUsuario);
        System.out.println("   Usuario creado: " + usuarioCreado);
        
        if (usuarioCreado) {
            // 7. Probar login con el nuevo usuario
            System.out.println("\n7. Login con nuevo usuario:");
            authService.logout();
            System.out.println("   Logout admin completado");
            
            boolean loginNuevo = authService.login("clopez", "Password123");
            System.out.println("   Login nuevo usuario: " + loginNuevo);
            
            if (loginNuevo) {
                Usuario nuevoCurrentUser = authService.getCurrentUser();
                System.out.println("   Usuario: " + nuevoCurrentUser.getNombreCompleto());
                System.out.println("   Rol: " + nuevoCurrentUser.getRolNombre());
                System.out.println("   Es Vendedor: " + authService.isVendedor());
                System.out.println("   Puede hacer ventas: " + authService.canMakeSales());
            }
        }
        
        // 8. Probar validaciones
        System.out.println("\n8. Prueba de validaciones:");
        
        // Usuario con datos inválidos
        Usuario usuarioInvalido = new Usuario();
        usuarioInvalido.setNombre("");
        usuarioInvalido.setUsername("ab"); // Muy corto
        usuarioInvalido.setEmail("email_sin_arroba");
        usuarioInvalido.setPassword("123"); // Muy simple
        
        boolean usuarioInvalidoCreado = usuarioService.crearUsuario(usuarioInvalido);
        System.out.println("   Usuario inválido creado: " + usuarioInvalidoCreado);
        
        // Usuario con username duplicado
        Usuario usuarioDuplicado = new Usuario(
            "Test", "Test", "test@email.com", "admin", // Username ya existe
            "Password123", "123456789", "Test Address", 2
        );
        
        boolean usuarioDuplicadoCreado = usuarioService.crearUsuario(usuarioDuplicado);
        System.out.println("   Usuario duplicado creado: " + usuarioDuplicadoCreado);
        
        // 9. Logout final
        System.out.println("\n9. Logout final:");
        authService.logout();
        System.out.println("   Autenticado: " + authService.isAuthenticated());
        System.out.println("   Sesión: " + sessionManager.getSessionInfo());
        
        System.out.println("\nPruebas del sistema de autenticación completadas!");
    }
}