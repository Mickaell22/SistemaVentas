package Test;

import config.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class TestChangePasswords {
    
    public static void main(String[] args) {
        System.out.println("=== CAMBIAR CONTRASEÑAS EN BASE DE DATOS ===\n");
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // 1. Mostrar usuarios actuales
            System.out.println("1. 👥 Usuarios actuales en la base de datos:");
            mostrarUsuarios(conn);
            
            // 2. Cambiar contraseñas
            System.out.println("\n2. 🔐 Cambiando contraseñas...");
            cambiarContrasenias(conn);
            
            // 3. Verificar cambios
            System.out.println("\n3. ✅ Verificando contraseñas cambiadas:");
            verificarContrasenias(conn);
            
            conn.close();
            
            System.out.println("\n🎉 ¡CONTRASEÑAS CAMBIADAS EXITOSAMENTE!");
            System.out.println("\n📋 Credenciales actualizadas:");
            System.out.println("   micka/micka123 (si existe)");
            System.out.println("   admin/admin123 (si existe)");
            System.out.println("   test/test123 (si existe)");
            System.out.println("\n🚀 Prueba ahora el login en tu aplicación!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void mostrarUsuarios(Connection conn) throws SQLException {
        String sql = "SELECT id, username, nombre, apellido, password FROM usuarios";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            int id = rs.getInt("id");
            String username = rs.getString("username");
            String nombre = rs.getString("nombre");
            String apellido = rs.getString("apellido");
            String password = rs.getString("password");
            
            System.out.printf("   ID: %d | Usuario: %s | Nombre: %s %s%n", 
                            id, username, nombre, apellido);
            System.out.printf("   Hash actual: %s%n", password);
            System.out.printf("   Longitud: %d | BCrypt: %s%n", 
                            password != null ? password.length() : 0,
                            password != null && password.startsWith("$2a$") ? "SÍ" : "NO");
            System.out.println("   ---");
        }
        
        rs.close();
        stmt.close();
    }
    
    private static void cambiarContrasenias(Connection conn) throws SQLException {
        // Definir usuarios y sus nuevas contraseñas
        String[][] usuarios = {
            {"micka", "micka123"},
            {"admin", "admin123"}, 
            {"test", "test123"},
            {"juan", "juan123"},
            {"maria", "maria123"}
        };
        
        String updateSql = "UPDATE usuarios SET password = ? WHERE username = ?";
        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        
        for (String[] usuario : usuarios) {
            String username = usuario[0];
            String plainPassword = usuario[1];
            
            // Verificar si el usuario existe
            String checkSql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                // Usuario existe, cambiar contraseña
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
                
                updateStmt.setString(1, hashedPassword);
                updateStmt.setString(2, username);
                int rowsUpdated = updateStmt.executeUpdate();
                
                System.out.printf("   ✅ %s -> Contraseña actualizada (%d filas)%n", 
                                username, rowsUpdated);
                System.out.printf("   🔐 Nuevo hash: %s%n", hashedPassword);
            } else {
                System.out.printf("   ⚠️  %s -> Usuario no encontrado%n", username);
            }
            
            checkRs.close();
            checkStmt.close();
        }
        
        // Confirmar cambios
        conn.commit();
        updateStmt.close();
        
        System.out.println("\n   💾 Cambios guardados en la base de datos");
    }
    
    private static void verificarContrasenias(Connection conn) throws SQLException {
        String[][] credenciales = {
            {"micka", "micka123"},
            {"admin", "admin123"}, 
            {"test", "test123"},
            {"juan", "juan123"},
            {"maria", "maria123"}
        };
        
        String selectSql = "SELECT username, password FROM usuarios WHERE username = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        
        for (String[] credencial : credenciales) {
            String username = credencial[0];
            String testPassword = credencial[1];
            
            selectStmt.setString(1, username);
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                
                try {
                    boolean matches = BCrypt.checkpw(testPassword, storedHash);
                    System.out.printf("   👤 %s con '%s': %s%n", 
                                    username, testPassword, 
                                    matches ? "✅ CORRECTO" : "❌ INCORRECTO");
                } catch (Exception e) {
                    System.out.printf("   👤 %s con '%s': ❌ ERROR - %s%n", 
                                    username, testPassword, e.getMessage());
                }
            } else {
                System.out.printf("   👤 %s: ⚠️  Usuario no encontrado%n", username);
            }
            
            rs.close();
        }
        
        selectStmt.close();
    }
    
    // Método adicional para crear usuario micka si no existe
    public static void crearUsuarioMicka() {
        System.out.println("\n🔧 CREANDO USUARIO MICKA...");
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Verificar si micka ya existe
            String checkSql = "SELECT COUNT(*) FROM usuarios WHERE username = 'micka'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            ResultSet checkRs = checkStmt.executeQuery();
            
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                System.out.println("   ⚠️  Usuario micka ya existe, actualizando contraseña...");
                checkRs.close();
                checkStmt.close();
                return;
            }
            
            checkRs.close();
            checkStmt.close();
            
            // Crear usuario micka
            String hashedPassword = BCrypt.hashpw("micka123", BCrypt.gensalt(12));
            
            String insertSql = "INSERT INTO usuarios (nombre, apellido, email, username, password, telefono, direccion, rol_id, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            
            insertStmt.setString(1, "Micka");
            insertStmt.setString(2, "Admin");
            insertStmt.setString(3, "micka@sistema.com");
            insertStmt.setString(4, "micka");
            insertStmt.setString(5, hashedPassword);
            insertStmt.setString(6, "0999999999");
            insertStmt.setString(7, "Guayaquil, Ecuador");
            insertStmt.setInt(8, 1); // ROL ADMINISTRADOR
            insertStmt.setBoolean(9, true);
            
            int rowsInserted = insertStmt.executeUpdate();
            conn.commit();
            insertStmt.close();
            conn.close();
            
            System.out.printf("   ✅ Usuario micka creado (%d filas)%n", rowsInserted);
            System.out.println("   🔐 Contraseña: micka123");
            System.out.println("   👑 Rol: ADMINISTRADOR");
            
        } catch (Exception e) {
            System.err.println("   ❌ Error creando usuario micka: " + e.getMessage());
        }
    }
}