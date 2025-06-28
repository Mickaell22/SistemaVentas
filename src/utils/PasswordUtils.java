// =====================================================
// PASSWORDUTILS SÚPER SIMPLE - Reemplaza utils/PasswordUtils.java
// =====================================================
package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public class PasswordUtils {
    
    // Patrón para contraseña segura
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );
    
    /**
     * Genera hash MD5 (compatible con la mayoría de sistemas)
     */
    public static String hashPassword(String password) {
        return hashPasswordMD5(password);
    }
    
    /**
     * Hash MD5 simple
     */
    public static String hashPasswordMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            
            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash MD5", e);
        }
    }
    
    /**
     * Hash SHA1
     */
    public static String hashPasswordSHA1(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(password.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA1", e);
        }
    }
    
    /**
     * Hash SHA256
     */
    public static String hashPasswordSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA256", e);
        }
    }
    
    /**
     * Verifica contraseña con MÚLTIPLES FORMATOS (SÚPER COMPATIBLE)
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        
        try {
            String cleanHash = hashedPassword.trim();
            
            // 1. Texto plano (para desarrollo/testing)
            if (cleanHash.equals(password)) {
                System.out.println("⚠️ Contraseña en texto plano detectada");
                return true;
            }
            
            // 2. MD5 (32 caracteres hex)
            if (cleanHash.matches("^[a-fA-F0-9]{32}$")) {
                String md5Password = hashPasswordMD5(password);
                return md5Password.equalsIgnoreCase(cleanHash);
            }
            
            // 3. SHA1 (40 caracteres hex)
            if (cleanHash.matches("^[a-fA-F0-9]{40}$")) {
                String sha1Password = hashPasswordSHA1(password);
                return sha1Password.equalsIgnoreCase(cleanHash);
            }
            
            // 4. SHA256 (64 caracteres hex)
            if (cleanHash.matches("^[a-fA-F0-9]{64}$")) {
                String sha256Password = hashPasswordSHA256(password);
                return sha256Password.equalsIgnoreCase(cleanHash);
            }
            
            // 5. Intentar todos los formatos como último recurso
            String md5Test = hashPasswordMD5(password);
            String sha1Test = hashPasswordSHA1(password);
            String sha256Test = hashPasswordSHA256(password);
            
            if (md5Test.equalsIgnoreCase(cleanHash) || 
                sha1Test.equalsIgnoreCase(cleanHash) || 
                sha256Test.equalsIgnoreCase(cleanHash)) {
                return true;
            }
            
            System.err.println("❌ Formato de contraseña no soportado. Hash: " + 
                             cleanHash.substring(0, Math.min(20, cleanHash.length())) + "...");
            System.err.println("   Longitud: " + cleanHash.length());
            System.err.println("   Es hex: " + cleanHash.matches("^[a-fA-F0-9]+$"));
            
            return false;
            
        } catch (Exception e) {
            System.err.println("❌ Error al verificar contraseña: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Detecta el formato de hash
     */
    public static String detectHashFormat(String hashedPassword) {
        if (hashedPassword == null) return "NULL";
        
        String clean = hashedPassword.trim();
        
        if (clean.matches("^[a-fA-F0-9]{32}$")) return "MD5";
        if (clean.matches("^[a-fA-F0-9]{40}$")) return "SHA1";
        if (clean.matches("^[a-fA-F0-9]{64}$")) return "SHA256";
        if (clean.length() < 20) return "POSIBLE_TEXTO_PLANO";
        
        return "FORMATO_DESCONOCIDO";
    }
    
    /**
     * Valida si una contraseña cumple con los requisitos de seguridad
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    
    /**
     * Obtiene los requisitos de contraseña como texto
     */
    public static String getPasswordRequirements() {
        return "La contraseña debe tener:\n" +
               "• Mínimo 8 caracteres\n" +
               "• Al menos una letra minúscula\n" +
               "• Al menos una letra mayúscula\n" +
               "• Al menos un número\n" +
               "• Al menos un símbolo (@$!%*?&)";
    }
    
    /**
     * Genera una contraseña temporal segura
     */
    public static String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@$!%*?&";
        StringBuilder password = new StringBuilder();
        
        // Asegurar que tiene al menos uno de cada tipo requerido
        password.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(random.nextInt(26))); // Mayúscula
        password.append("abcdefghijkmnopqrstuvwxyz".charAt(random.nextInt(26))); // Minúscula
        password.append("0123456789".charAt(random.nextInt(10))); // Número
        password.append("@$!%*?&".charAt(random.nextInt(7))); // Símbolo
        
        // Completar hasta 12 caracteres
        for (int i = 4; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}