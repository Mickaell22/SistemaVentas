package Test;
import config.DatabaseConnection;
import utils.PasswordUtils;
import java.sql.*;

public class FixPasswordIssue {
    
    public static void main(String[] args) {
        System.out.println("=== SOLUCIONANDO PROBLEMA DE CONTRASEÑAS ===\n");
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // 1. Verificar contraseñas actuales
            System.out.println("1. 🔍 Verificando contraseñas actuales en BD:");
            String selectSql = "SELECT id, username, password FROM usuarios";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String currentPassword = rs.getString("password");
                
                System.out.println("   Usuario: " + username);
                System.out.println("   Hash actual: " + currentPassword);
                System.out.println("   Longitud del hash: " + (currentPassword != null ? currentPassword.length() : "NULL"));
                
                // Verificar si es un hash BCrypt válido
                boolean isBCryptFormat = currentPassword != null && currentPassword.startsWith("$2a$");
                System.out.println("   ¿Es formato BCrypt válido?: " + isBCryptFormat);
                System.out.println("   ---");
            }
            rs.close();
            selectStmt.close();
            
            // 2. Generar nuevas contraseñas encriptadas
            System.out.println("\n2. 🔐 Generando nuevas contraseñas:");
            
            String adminPassword = "admin123";
            String pruebaPassword = "Prueba123";
            
            String adminHash = PasswordUtils.hashPassword(adminPassword);
            String pruebaHash = PasswordUtils.hashPassword(pruebaPassword);
            
            System.out.println("   admin123 -> " + adminHash);
            System.out.println("   Longitud: " + adminHash.length());
            System.out.println("   Prueba123 -> " + pruebaHash);
            System.out.println("   Longitud: " + pruebaHash.length());
            
            // 3. Actualizar contraseñas en la BD
            System.out.println("\n3. 🔄 Actualizando contraseñas en BD:");
            
            // Actualizar admin
            String updateSql = "UPDATE usuarios SET password = ? WHERE username = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            
            updateStmt.setString(1, adminHash);
            updateStmt.setString(2, "admin");
            int adminUpdated = updateStmt.executeUpdate();
            System.out.println("   Admin actualizado: " + (adminUpdated > 0 ? "✅" : "❌"));
            
            // Actualizar prueba (si existe)
            updateStmt.setString(1, pruebaHash);
            updateStmt.setString(2, "prueba");
            int pruebaUpdated = updateStmt.executeUpdate();
            System.out.println("   Prueba actualizado: " + (pruebaUpdated > 0 ? "✅" : "❌"));
            
            conn.commit();
            updateStmt.close();
            
            // 4. Verificar que las contraseñas funcionan
            System.out.println("\n4. ✅ Verificando contraseñas:");
            
            // Obtener contraseñas actualizadas
            PreparedStatement verifyStmt = conn.prepareStatement(
                "SELECT username, password FROM usuarios WHERE username IN ('admin', 'prueba')"
            );
            ResultSet verifyRs = verifyStmt.executeQuery();
            
            while (verifyRs.next()) {
                String username = verifyRs.getString("username");
                String storedHash = verifyRs.getString("password");
                
                String testPassword = username.equals("admin") ? "admin123" : "Prueba123";
                
                System.out.println("   Usuario: " + username);
                System.out.println("   Hash almacenado: " + storedHash);
                
                try {
                    boolean passwordMatch = PasswordUtils.verifyPassword(testPassword, storedHash);
                    System.out.println("   Verificación con '" + testPassword + "': " + (passwordMatch ? "✅ EXITOSA" : "❌ FALLÓ"));
                } catch (Exception e) {
                    System.out.println("   Verificación con '" + testPassword + "': ❌ ERROR - " + e.getMessage());
                }
                System.out.println("   ---");
            }
            
            verifyRs.close();
            verifyStmt.close();
            conn.close();
            
            // 5. Crear usuario prueba si no existe
            System.out.println("\n5. 👤 Creando usuario prueba si no existe:");
            createTestUserIfNotExists();
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("✅ PROBLEMA DE CONTRASEÑAS SOLUCIONADO");
            System.out.println("=".repeat(50));
            System.out.println("📋 Credenciales actualizadas:");
            System.out.println("   👑 Admin: admin / admin123");
            System.out.println("   👤 Prueba: prueba / Prueba123");
            System.out.println("\n🚀 Ahora puedes ejecutar Main.java y hacer login");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createTestUserIfNotExists() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Verificar si existe usuario prueba
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM usuarios WHERE username = 'prueba'"
            );
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            checkStmt.close();
            
            if (count == 0) {
                System.out.println("   Usuario prueba no existe, creando...");
                
                String insertSql = "INSERT INTO usuarios (nombre, apellido, email, username, password, telefono, direccion, rol_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                
                insertStmt.setString(1, "Usuario");
                insertStmt.setString(2, "Prueba");
                insertStmt.setString(3, "prueba@test.com");
                insertStmt.setString(4, "prueba");
                insertStmt.setString(5, PasswordUtils.hashPassword("Prueba123"));
                insertStmt.setString(6, "0999888777");
                insertStmt.setString(7, "Dirección de prueba");
                insertStmt.setInt(8, 3); // Rol vendedor
                
                int inserted = insertStmt.executeUpdate();
                conn.commit();
                insertStmt.close();
                
                System.out.println("   Usuario prueba creado: " + (inserted > 0 ? "✅" : "❌"));
            } else {
                System.out.println("   Usuario prueba ya existe: ✅");
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("   Error creando usuario prueba: " + e.getMessage());
        }
    }
}