public class TestBCryptLibrary {
    
    public static void main(String[] args) {
        System.out.println("=== VERIFICANDO LIBRERÍA BCRYPT ===\n");
        
        try {
            // Intentar importar BCrypt
            Class.forName("org.mindrot.jbcrypt.BCrypt");
            System.out.println("Librería BCrypt encontrada");
            
            // Probar funcionalidad básica
            String password = "test123";
            System.out.println("Probando encriptación con: " + password);
            
            String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12));
            System.out.println("Hash generado: " + hash);
            System.out.println("Longitud del hash: " + hash.length());
            
            boolean matches = org.mindrot.jbcrypt.BCrypt.checkpw(password, hash);
            System.out.println("Verificación: " + (matches ? "EXITOSA" : "FALLÓ"));
            
            boolean wrongPassword = org.mindrot.jbcrypt.BCrypt.checkpw("wrong", hash);
            System.out.println("Verificación con contraseña incorrecta: " + (wrongPassword ? "FALLÓ (MALO)" : "CORRECTA"));
            
            System.out.println("\nBCrypt funciona correctamente");
            
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Librería BCrypt no encontrada");
            System.err.println("Descarga bcrypt desde:");
            System.err.println("https://mvnrepository.com/artifact/org.mindrot/jbcrypt");
            System.err.println("Busca: jbcrypt-0.4.jar");
            System.err.println("Colócalo en la carpeta lib/");
            
        } catch (Exception e) {
            System.err.println("Error probando BCrypt: " + e.getMessage());
            e.printStackTrace();
        }
    }
}