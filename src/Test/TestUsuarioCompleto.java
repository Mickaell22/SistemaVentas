package Test;

import services.AuthService;
import services.UsuarioService;
import services.RolService;
import models.Usuario;
import models.Rol;
import utils.SessionManager;
import utils.PasswordUtils;

import java.util.List;

public class TestUsuarioCompleto {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA COMPLETA DEL MÓDULO DE USUARIOS ===\n");
        
        // Servicios
        AuthService authService = AuthService.getInstance();
        UsuarioService usuarioService = UsuarioService.getInstance();
        RolService rolService = RolService.getInstance();
        SessionManager sessionManager = SessionManager.getInstance();
        
        try {
            // 1. Probar autenticación
            System.out.println("1. 🔐 Probando autenticación...");
            boolean loginResult = authService.login("admin", "admin123");
            System.out.println("   Login admin: " + (loginResult ? "✅ EXITOSO" : "❌ FALLÓ"));
            
            if (loginResult) {
                Usuario currentUser = authService.getCurrentUser();
                System.out.println("   Usuario logueado: " + currentUser.getNombreCompleto());
                System.out.println("   Rol: " + currentUser.getRolNombre());
                System.out.println("   Sesión: " + sessionManager.getSessionInfo());
            }
            
            // 2. Probar permisos
            System.out.println("\n2. 🛡️ Verificando permisos...");
            System.out.println("   Es Admin: " + authService.isAdmin());
            System.out.println("   Puede gestionar usuarios: " + authService.canManageUsers());
            System.out.println("   Puede ver reportes: " + authService.canViewReports());
            System.out.println("   Puede hacer ventas: " + authService.canMakeSales());
            
            // 3. Probar gestión de roles
            System.out.println("\n3. 👥 Probando gestión de roles...");
            List<Rol> roles = rolService.obtenerTodosLosRoles();
            System.out.println("   Roles disponibles: " + roles.size());
            for (Rol rol : roles) {
                System.out.println("     - " + rol.toString());
            }
            
            // 4. Crear nuevo rol de prueba
            System.out.println("\n4. ➕ Creando rol de prueba...");
            Rol nuevoRol = new Rol("SUPERVISOR", "Supervisor de área", 3);
            nuevoRol.setPermissionsList(List.of("sales.view", "clients.view", "products.view"));
            
            boolean rolCreado = rolService.crearRol(nuevoRol);
            System.out.println("   Rol creado: " + (rolCreado ? "✅ SÍ" : "❌ NO"));
            
            // 5. Crear usuario de prueba
            System.out.println("\n5. 👤 Creando usuario de prueba...");
            Usuario usuarioPrueba = new Usuario(
                "Carlos", "Supervisor", "carlos@empresa.com", "csupervisor",
                "Password123!", "0999887766", "Av. Principal 123", 
                roles.size() > 2 ? roles.get(2).getId() : 3
            );
            
            boolean usuarioCreado = usuarioService.crearUsuario(usuarioPrueba);
            System.out.println("   Usuario creado: " + (usuarioCreado ? "✅ SÍ" : "❌ NO"));
            
            // 6. Probar validaciones de contraseña
            System.out.println("\n6. 🔑 Probando validaciones de contraseña...");
            String[] passwordsTest = {
                "123",           // Muy simple
                "password",      // Sin números ni mayúsculas
                "Password123",   // Sin símbolos
                "Password123!",  // Válida
                "SuperSecure2024@" // Válida
            };
            
            for (String pwd : passwordsTest) {
                boolean valida = PasswordUtils.isValidPassword(pwd);
                System.out.println("   '" + pwd + "': " + (valida ? "✅ Válida" : "❌ Inválida"));
            }
            
            // 7. Probar hash y verificación de contraseñas
            System.out.println("\n7. 🔐 Probando hash de contraseñas...");
            String passwordOriginal = "MiPasswordSegura123!";
            String hash = PasswordUtils.hashPassword(passwordOriginal);
            System.out.println("   Password original: " + passwordOriginal);
            System.out.println("   Hash generado: " + hash.substring(0, 20) + "...");
            
            boolean verificacionCorrecta = PasswordUtils.verifyPassword(passwordOriginal, hash);
            boolean verificacionIncorrecta = PasswordUtils.verifyPassword("PasswordIncorrecta", hash);
            
            System.out.println("   Verificación correcta: " + (verificacionCorrecta ? "✅ SÍ" : "❌ NO"));
            System.out.println("   Verificación incorrecta: " + (verificacionIncorrecta ? "❌ NO" : "✅ SÍ"));
            
            // 8. Probar gestión de sesión
            System.out.println("\n8. ⏰ Probando gestión de sesión...");
            System.out.println("   Sesión autenticada: " + authService.isAuthenticated());
            System.out.println("   Sesión expirada: " + sessionManager.isSessionExpired());
            System.out.println("   Tiempo restante: " + sessionManager.getTimeRemaining());
            System.out.println("   Cerca de expirar: " + sessionManager.isSessionNearExpiry());
            
            // 9. Probar cambio de contraseña
            if (usuarioCreado) {
                System.out.println("\n9. 🔄 Probando cambio de contraseña...");
                boolean passwordCambiada = usuarioService.cambiarPassword(
                    usuarioPrueba.getId(), 
                    "Password123!", 
                    "NuevaPassword456@"
                );
                System.out.println("   Contraseña cambiada: " + (passwordCambiada ? "✅ SÍ" : "❌ NO"));
            }
            
            // 10. Probar login con usuario creado
            if (usuarioCreado) {
                System.out.println("\n10. 🚪 Probando login con usuario creado...");
                authService.logout();
                
                boolean loginNuevo = authService.login("csupervisor", "NuevaPassword456@");
                System.out.println("    Login usuario nuevo: " + (loginNuevo ? "✅ EXITOSO" : "❌ FALLÓ"));
                
                if (loginNuevo) {
                    Usuario userNuevo = authService.getCurrentUser();
                    System.out.println("    Usuario: " + userNuevo.getNombreCompleto());
                    System.out.println("    Rol: " + userNuevo.getRolNombre());
                    System.out.println("    Puede hacer ventas: " + authService.canMakeSales());
                }
            }
            
            // 11. Estadísticas finales
            System.out.println("\n11. 📊 Estadísticas del sistema...");
            List<Usuario> todosUsuarios = usuarioService.obtenerTodosLosUsuarios();
            List<Usuario> usuariosActivos = usuarioService.obtenerUsuariosActivos();
            
            System.out.println("   Total usuarios: " + todosUsuarios.size());
            System.out.println("   Usuarios activos: " + usuariosActivos.size());
            System.out.println("   Total roles: " + rolService.obtenerTodosLosRoles().size());
            
            // 12. Logout final
            System.out.println("\n12. 🚪 Logout final...");
            authService.logout();
            System.out.println("   Sesión cerrada: " + (!authService.isAuthenticated() ? "✅ SÍ" : "❌ NO"));
            
            System.out.println("\n🎉 ¡Pruebas del módulo de usuarios completadas exitosamente!");
            
        } catch (Exception e) {
            System.err.println("❌ Error durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}