package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/sistema_ventas";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection() {
        try {
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            this.connection.setAutoCommit(false);
            System.out.println("Conexión a la base de datos establecida");
            
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL no encontrado: " + e.getMessage());
            throw new RuntimeException("Error al cargar el driver MySQL", e);
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            throw new RuntimeException("Error de conexión a la base de datos", e);
        }
    }
    
    public static DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                instance = new DatabaseConnection();
                return instance.connection;
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Error al obtener la conexión: " + e.getMessage());
            return null;
        }
    }
    
    private static boolean isConnectionClosed() {
        try {
            return instance == null || instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }
    
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    
    public void commit() {
        try {
            if (connection != null) {
                connection.commit();
            }
        } catch (SQLException e) {
            System.err.println("Error al hacer commit: " + e.getMessage());
        }
    }
    
    public void rollback() {
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Error al hacer rollback: " + e.getMessage());
        }
    }
}