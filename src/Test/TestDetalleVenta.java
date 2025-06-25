package Test;

import config.DatabaseConnection;
import java.sql.*;

public class TestDetalleVenta {
    
    public static void main(String[] args) {
        System.out.println("=== DIAGNÓSTICO DE ERROR DETALLE_VENTA ===\n");
        
        try {
            // 1. Verificar conexión
            System.out.println("1. Verificando conexión a base de datos...");
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            Connection conn = dbConnection.getConnection();
            
            if (conn != null) {
                System.out.println("   ✅ Conexión establecida");
                System.out.println("   📊 Base de datos: " + conn.getCatalog());
                System.out.println("   🔗 URL: " + conn.getMetaData().getURL());
            } else {
                System.out.println("   ❌ Error: Conexión nula");
                return;
            }
            
            // 2. Verificar que la tabla existe
            System.out.println("\n2. Verificando existencia de tabla detalle_venta...");
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "detalle_venta", null);
            
            if (tables.next()) {
                System.out.println("   ✅ Tabla detalle_venta existe");
            } else {
                System.out.println("   ❌ Tabla detalle_venta NO existe");
                return;
            }
            tables.close();
            
            // 3. Verificar estructura de la tabla
            System.out.println("\n3. Verificando estructura de la tabla...");
            ResultSet columns = metaData.getColumns(null, null, "detalle_venta", null);
            System.out.println("   Columnas encontradas:");
            
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");
                
                System.out.printf("   - %s: %s(%d) - Nullable: %s%n", 
                    columnName, dataType, columnSize, isNullable);
            }
            columns.close();
            
            // 4. Probar INSERT simple
            System.out.println("\n4. Probando INSERT básico...");
            
            // Verificar si hay datos en ventas primero
            String checkVentasSQL = "SELECT COUNT(*) as total FROM ventas LIMIT 1";
            PreparedStatement checkStmt = conn.prepareStatement(checkVentasSQL);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int ventasCount = rs.getInt("total");
            rs.close();
            checkStmt.close();
            
            System.out.println("   📈 Ventas en BD: " + ventasCount);
            
            if (ventasCount == 0) {
                System.out.println("   ⚠️  No hay ventas, creando venta de prueba...");
                crearVentaPrueba(conn);
            }
            
            // Ahora probar el INSERT de detalle
            String sql = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, 1);  // venta_id
            stmt.setInt(2, 1);  // producto_id  
            stmt.setInt(3, 2);  // cantidad
            stmt.setBigDecimal(4, new java.math.BigDecimal("10.50")); // precio_unitario
            stmt.setBigDecimal(5, new java.math.BigDecimal("0.00"));  // descuento
            stmt.setBigDecimal(6, new java.math.BigDecimal("21.00")); // subtotal
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("   ✅ INSERT exitoso - Filas afectadas: " + filasAfectadas);
                
                // Obtener ID generado
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    System.out.println("   🆔 ID generado: " + keys.getInt(1));
                }
                keys.close();
                
                // Hacer commit
                conn.commit();
                System.out.println("   ✅ Commit exitoso");
                
            } else {
                System.out.println("   ❌ INSERT falló - Sin filas afectadas");
            }
            
            stmt.close();
            
            // 5. Verificar configuración de conexión
            System.out.println("\n5. Verificando configuración de conexión...");
            System.out.println("   AutoCommit: " + conn.getAutoCommit());
            System.out.println("   Nivel aislamiento: " + conn.getTransactionIsolation());
            System.out.println("   Solo lectura: " + conn.isReadOnly());
            
            // 6. Verificar permisos
            System.out.println("\n6. Verificando permisos...");
            try {
                Statement testStmt = conn.createStatement();
                ResultSet testRs = testStmt.executeQuery("SELECT USER(), DATABASE()");
                if (testRs.next()) {
                    System.out.println("   Usuario actual: " + testRs.getString(1));
                    System.out.println("   Base datos actual: " + testRs.getString(2));
                }
                testRs.close();
                testStmt.close();
            } catch (Exception e) {
                System.out.println("   ⚠️  Error verificando permisos: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error SQL: " + e.getMessage());
            System.err.println("   Código error: " + e.getErrorCode());
            System.err.println("   Estado SQL: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Error general: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void crearVentaPrueba(Connection conn) throws SQLException {
        // Crear venta básica para testing
        String ventaSQL = "INSERT INTO ventas (numero_factura, cliente_id, usuario_id, " +
                         "subtotal, descuento, impuestos, total, estado, metodo_pago) " +
                         "VALUES ('TEST-001', 1, 1, 0.00, 0.00, 0.00, 0.00, 'PENDIENTE', 'EFECTIVO')";
        
        PreparedStatement ventaStmt = conn.prepareStatement(ventaSQL);
        int filasVenta = ventaStmt.executeUpdate();
        ventaStmt.close();
        
        if (filasVenta > 0) {
            System.out.println("   ✅ Venta de prueba creada");
            conn.commit();
        }
    }
}