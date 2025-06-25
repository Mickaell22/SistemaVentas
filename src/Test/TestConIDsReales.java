package Test;

import config.DatabaseConnection;
import java.sql.*;
import java.math.BigDecimal;

public class TestConIDsReales {
    
    public static void main(String[] args) {
        System.out.println("=== TEST CON IDs REALES ===\n");
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // 1. Obtener IDs reales de ventas
            System.out.println("1. Verificando ventas existentes:");
            String ventasSQL = "SELECT id, numero_factura, total FROM ventas LIMIT 5";
            PreparedStatement ventasStmt = conn.prepareStatement(ventasSQL);
            ResultSet ventasRs = ventasStmt.executeQuery();
            
            Integer ventaIdReal = null;
            while (ventasRs.next()) {
                int id = ventasRs.getInt("id");
                String numero = ventasRs.getString("numero_factura");
                BigDecimal total = ventasRs.getBigDecimal("total");
                System.out.printf("   Venta ID: %d, Factura: %s, Total: $%.2f%n", id, numero, total);
                
                if (ventaIdReal == null) {
                    ventaIdReal = id; // Usar el primer ID encontrado
                }
            }
            ventasRs.close();
            ventasStmt.close();
            
            // 2. Obtener IDs reales de productos
            System.out.println("\n2. Verificando productos existentes:");
            String productosSQL = "SELECT id, codigo, nombre FROM productos LIMIT 5";
            PreparedStatement productosStmt = conn.prepareStatement(productosSQL);
            ResultSet productosRs = productosStmt.executeQuery();
            
            Integer productoIdReal = null;
            while (productosRs.next()) {
                int id = productosRs.getInt("id");
                String codigo = productosRs.getString("codigo");
                String nombre = productosRs.getString("nombre");
                System.out.printf("   Producto ID: %d, Código: %s, Nombre: %s%n", id, codigo, nombre);
                
                if (productoIdReal == null) {
                    productoIdReal = id; // Usar el primer ID encontrado
                }
            }
            productosRs.close();
            productosStmt.close();
            
            // 3. Si no hay datos, crearlos
            if (ventaIdReal == null) {
                System.out.println("\n⚠️  No hay ventas, creando venta de prueba...");
                ventaIdReal = crearVentaPrueba(conn);
            }
            
            if (productoIdReal == null) {
                System.out.println("\n⚠️  No hay productos, creando producto de prueba...");
                productoIdReal = crearProductoPrueba(conn);
            }
            
            // 4. Ahora probar INSERT con IDs reales
            if (ventaIdReal != null && productoIdReal != null) {
                System.out.printf("\n3. Probando INSERT con IDs reales (Venta: %d, Producto: %d)...%n", 
                                ventaIdReal, productoIdReal);
                
                String sql = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, descuento, subtotal) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
                
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, ventaIdReal);
                stmt.setInt(2, productoIdReal);
                stmt.setInt(3, 2);
                stmt.setBigDecimal(4, new BigDecimal("15.50"));
                stmt.setBigDecimal(5, new BigDecimal("0.00"));
                stmt.setBigDecimal(6, new BigDecimal("31.00"));
                
                int filasAfectadas = stmt.executeUpdate();
                
                if (filasAfectadas > 0) {
                    System.out.println("   ✅ INSERT EXITOSO!");
                    
                    ResultSet keys = stmt.getGeneratedKeys();
                    if (keys.next()) {
                        System.out.println("   🆔 Detalle ID generado: " + keys.getInt(1));
                    }
                    keys.close();
                    
                    conn.commit();
                    System.out.println("   ✅ Commit realizado");
                    
                    // Verificar que se insertó
                    String verificarSQL = "SELECT COUNT(*) FROM detalle_venta WHERE venta_id = ?";
                    PreparedStatement verificarStmt = conn.prepareStatement(verificarSQL);
                    verificarStmt.setInt(1, ventaIdReal);
                    ResultSet verificarRs = verificarStmt.executeQuery();
                    verificarRs.next();
                    int count = verificarRs.getInt(1);
                    System.out.println("   📊 Detalles en esta venta: " + count);
                    verificarRs.close();
                    verificarStmt.close();
                    
                } else {
                    System.out.println("   ❌ INSERT falló");
                }
                stmt.close();
                
            } else {
                System.out.println("❌ No se pudieron obtener IDs válidos");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Integer crearVentaPrueba(Connection conn) throws SQLException {
        String sql = "INSERT INTO ventas (numero_factura, cliente_id, usuario_id, " +
                    "subtotal, descuento, impuestos, total, estado, metodo_pago) " +
                    "VALUES ('PRUEBA-001', 1, 1, 0.00, 0.00, 0.00, 0.00, 'PENDIENTE', 'EFECTIVO')";
        
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int filas = stmt.executeUpdate();
        
        Integer ventaId = null;
        if (filas > 0) {
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                ventaId = keys.getInt(1);
                System.out.println("   ✅ Venta creada con ID: " + ventaId);
            }
            keys.close();
            conn.commit();
        }
        stmt.close();
        return ventaId;
    }
    
    private static Integer crearProductoPrueba(Connection conn) throws SQLException {
        String sql = "INSERT INTO productos (codigo, nombre, descripcion, categoria_id, proveedor_id, " +
                    "precio_compra, precio_venta, stock_actual, stock_minimo) " +
                    "VALUES ('PROD-001', 'Producto Prueba', 'Producto para testing', 1, 1, " +
                    "10.00, 15.00, 100, 5)";
        
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int filas = stmt.executeUpdate();
        
        Integer productoId = null;
        if (filas > 0) {
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                productoId = keys.getInt(1);
                System.out.println("   ✅ Producto creado con ID: " + productoId);
            }
            keys.close();
            conn.commit();
        }
        stmt.close();
        return productoId;
    }
}
