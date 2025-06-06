package utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    
    private static final int SALT_ROUNDS = 12;
    
    /**
     * Encripta una contraseña usando BCrypt
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(SALT_ROUNDS));
    }
    
    /**
     * Verifica si una contraseña coincide con su hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Error al verificar contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida que una contraseña cumpla con los requisitos mínimos
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Genera una contraseña temporal aleatoria
     */
    public static String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
    
    /**
     * Obtiene los requisitos de contraseña como mensaje
     */
    public static String getPasswordRequirements() {
        return "La contraseña debe tener:\n" +
               "- Mínimo 6 caracteres\n" +
               "- Al menos una letra mayúscula\n" +
               "- Al menos una letra minúscula\n" +
               "- Al menos un número";
    }
}