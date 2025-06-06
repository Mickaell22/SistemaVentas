package Test;
import utils.PasswordUtils;
import models.Usuario;

public class TestBCrypt {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE BCRYPT Y MODELO USUARIO ===\n");
        
        // Probar encriptación de contraseñas
        String password = "admin123";
        System.out.println("Contraseña original: " + password);
        
        String hashedPassword = PasswordUtils.hashPassword(password);
        System.out.println("Contraseña encriptada: " + hashedPassword);
        
        // Probar verificación
        boolean isValid = PasswordUtils.verifyPassword(password, hashedPassword);
        System.out.println("Verificación correcta: " + isValid);
        
        boolean isInvalid = PasswordUtils.verifyPassword("wrongpassword", hashedPassword);
        System.out.println("Verificación incorrecta: " + isInvalid);
        
        // Probar validación de contraseña
        System.out.println("\nValidación de contraseñas:");
        testPasswordValidation("123", false);
        testPasswordValidation("password", false);
        testPasswordValidation("PASSWORD", false);
        testPasswordValidation("Password123", true);
        testPasswordValidation("Admin123", true);
        
        // Probar modelo Usuario
        System.out.println("\nPrueba del modelo Usuario:");
        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");
        usuario.setUsername("jperez");
        usuario.setEmail("juan@email.com");
        usuario.setRolNombre("ADMINISTRADOR");
        usuario.setActivo(true);
        
        System.out.println("Usuario creado: " + usuario.toString());
        System.out.println("Nombre completo: " + usuario.getNombreCompleto());
        System.out.println("Es administrador: " + usuario.esAdministrador());
        System.out.println("Es vendedor: " + usuario.esVendedor());
        
        // Generar contraseña temporal
        System.out.println("\nContraseña temporal generada: " + PasswordUtils.generateTempPassword());
        
        System.out.println("\n" + PasswordUtils.getPasswordRequirements());
    }
    
    private static void testPasswordValidation(String password, boolean expected) {
        boolean result = PasswordUtils.isValidPassword(password);
        String status = result == expected ? "Ok" : "ERROR";
        System.out.println(String.format("%s '%s' -> %s (esperado: %s)", 
                          status, password, result, expected));
    }
}