import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CONEXIÓN A LA BASE DE DATOS ===\n");
        
        try {
            // Obtener la conexión
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            Connection connection = dbConnection.getConnection();
            
            if (connection != null) {
                System.out.println("Conexión exitosa!");
                
                // Probar una consulta simple
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) as total FROM usuarios");
                
                if (resultSet.next()) {
                    int totalUsuarios = resultSet.getInt("total");
                    System.out.println("Consulta exitosa!");
                    System.out.println("Total de usuarios en la BD: " + totalUsuarios);
                }
                
                // Probar listar roles
                System.out.println("\nRoles disponibles:");
                ResultSet rolesResult = statement.executeQuery("SELECT id, nombre FROM roles");
                while (rolesResult.next()) {
                    System.out.println("   - ID: " + rolesResult.getInt("id") + 
                                     ", Nombre: " + rolesResult.getString("nombre"));
                }
                
                // Cerrar recursos
                resultSet.close();
                rolesResult.close();
                statement.close();
                
                System.out.println("\nPrueba completada exitosamente!");
                
            } else {
                System.out.println("Error: No se pudo establecer la conexión");
            }
            
        } catch (Exception e) {
            System.out.println("Error durante la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}