package Test;

import services.AuthService;
import services.UsuarioService;
import models.Usuario;
import models.Rol;
import utils.SessionManager;
import utils.PasswordUtils;

import java.util.List;

public class TestBasicoUsuarios {
    
    public static void main(String[] args) {
        System.out.println("=== TEST BÁSICO DEL MÓDULO DE USUARIOS ===\n");
        
        try {
            // 1. Verificar conexión y servicios
            System.out.println("1. 🔌 Verificando servicios...");
            AuthService authService = AuthService.getInstance();
            UsuarioService usuarioService = UsuarioService.getInstance();
            SessionManager sessionManager = SessionManager.getInstance();
            System.out.println("   ✅ Servicios inicializados correctamente");
            
            // 2. Probar obtener roles (básico)
            System.out.println("\n2. 👥 Probando obtener roles...");
            try {
                List<Rol> roles = usuarioService.obtenerTodosLosRoles();
                System.out.println("   ✅ Roles obtenidos: " + roles.size());
                for (Rol rol : roles) {
                    System.out.println("     - " + rol.getNombre() + " (ID: " + rol.getId() + ")");
                }
            } catch (Exception e) {
                System.err.println("   ❌ Error al obtener roles: " + e.getMessage());
            }
            
            // 3. Probar obtener usuarios
            System.out.println("\n3. 👤 Probando obtener usuarios...");
            try {
                List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
                System.out.println("   ✅ Usuarios obtenidos: " + usuarios.size());
                for (Usuario usuario : usuarios) {
                    System.out.println("     - " + usuario.getUsername() + " (" + usuario.getNombreCompleto() + ") - Rol: " + usuario.getRolNombre());
                }
            } catch (Exception e) {
                System.err.println("   ❌ Error al obtener usuarios: " + e.getMessage());
            }
            
            // 4. Probar validación de contraseñas
            System.out.println("\n4. 🔑 Probando validación de contraseñas...");
            String[] passwords = {
                "123",              // Muy simple
                "password",         // Sin requisitos
                "Password123!",     // Válida
            };
            
            for (String pwd : passwords) {
                boolean valida = PasswordUtils.isValidPassword(pwd);
                System.out.println("   '" + pwd + "': " + (valida ? "✅ Válida" : "❌ Inválida"));
            }
            
            // 5. Probar hash de contraseña
            System.out.println("\n5. 🔐 Probando hash de contraseñas...");
            String passwordTest = "TestPassword123!";
            try {
                String hash = PasswordUtils.hashPassword(passwordTest);
                boolean verificacion = PasswordUtils.verifyPassword(passwordTest, hash);
                System.out.println("   ✅ Hash generado y verificado: " + verificacion);
            } catch (Exception e) {
                System.err.println("   ❌ Error en hash: " + e.getMessage());
            }
            
            // 6. Probar autenticación básica
            System.out.println("\n6. 🔐 Probando autenticación...");
            try {
                boolean login = authService.login("admin", "admin123");
                System.out.println("   Login admin/admin123: " + (login ? "✅ EXITOSO" : "❌ FALLÓ"));
                
                if (login) {
                    Usuario currentUser = authService.getCurrentUser();
                    if (currentUser != null) {
                        System.out.println("   Usuario logueado: " + currentUser.getNombreCompleto());
                        System.out.println("   Rol: " + currentUser.getRolNombre());
                        System.out.println("   Sesión: " + sessionManager.getSessionInfo());
                        
                        // Probar permisos básicos
                        System.out.println("   Es Admin: " + authService.isAdmin());
                        System.out.println("   Puede gestionar usuarios: " + authService.canManageUsers());
                    } else {
                        System.out.println("   ❌ Usuario logueado es null");
                    }
                }
            } catch (Exception e) {
                System.err.println("   ❌ Error en autenticación: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 7. Crear usuario de prueba simple
            if (authService.isAuthenticated()) {
                System.out.println("\n7. ➕ Probando crear usuario...");
                try {
                    Usuario usuarioPrueba = new Usuario();
                    usuarioPrueba.setNombre("Usuario");
                    usuarioPrueba.setApellido("Prueba");
                    usuarioPrueba.setEmail("prueba@test.com");
                    usuarioPrueba.setUsername("prueba");
                    usuarioPrueba.setPassword("Password123!");
                    usuarioPrueba.setTelefono("0999888777");
                    usuarioPrueba.setRolId(3); // Vendedor por defecto
                    
                    boolean creado = usuarioService.crearUsuario(usuarioPrueba);
                    System.out.println("   Usuario creado: " + (creado ? "✅ SÍ" : "❌ NO"));
                    
                } catch (Exception e) {
                    System.err.println("   ❌ Error al crear usuario: " + e.getMessage());
                }
            }
            
            // 8. Logout
            System.out.println("\n8. 🚪 Probando logout...");
            try {
                authService.logout();
                boolean autenticado = authService.isAuthenticated();
                System.out.println("   Logout exitoso: " + (!autenticado ? "✅ SÍ" : "❌ NO"));
            } catch (Exception e) {
                System.err.println("   ❌ Error en logout: " + e.getMessage());
            }
            
            System.out.println("\n🎉 ¡Test básico completado!");
            
        } catch (Exception e) {
            System.err.println("❌ Error general en el test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}