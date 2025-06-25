import config.DatabaseConnection;
import controllers.LoginController;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE VENTAS ===");
        System.out.println("Iniciando aplicación...\n");

        // Configurar Look and Feel - VERSIÓN CORREGIDA
        configurarLookAndFeel();

        // Probar conexión a la base de datos
        try {
            DatabaseConnection.getInstance();
            System.out.println("Conexión a base de datos establecida");

            System.out.println("\n=== PROBANDO CREACIÓN DE VENTA ===");
            try {
                testCrearVentaRapido();
            } catch (Exception e) {
                System.err.println("Error en test de venta: " + e.getMessage());
            }

            // Iniciar la aplicación en el hilo de eventos de Swing
            SwingUtilities.invokeLater(() -> {
                System.out.println("Iniciando interfaz gráfica...");
                new LoginController();
            });

        } catch (Exception e) {
            System.err.println("Error al iniciar el sistema: " + e.getMessage());

            // Mostrar error en ventana si es posible
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Error al conectar con la base de datos:\n" + e.getMessage() +
                                "\n\nVerifique que XAMPP esté ejecutándose y la base de datos configurada correctamente.",
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
            });

            System.exit(1);
        }
    }

    /**
     * Configura el Look and Feel de la aplicación
     * Método separado para mejor manejo de errores
     */
    private static void configurarLookAndFeel() {
        try {
            // Intentar usar Nimbus primero (más moderno)
            boolean nimbusEncontrado = false;

            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    nimbusEncontrado = true;
                    System.out.println("Look and Feel configurado: Nimbus");
                    break;
                }
            }

            // Si no encontró Nimbus, usar el del sistema
            if (!nimbusEncontrado) {
                try {
                    // Usar solo Nimbus (más seguro)
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Si falla, usar el por defecto (no hacer nada)
                    System.out.println("Usando Look and Feel por defecto");
                }
            }

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            // Si todo falla, continuar con el Look and Feel por defecto
            System.out.println("No se pudo configurar Look and Feel personalizado, usando por defecto");
        }
    }

    /**
     * Test para verificar que la creación de ventas funciona correctamente
     */
    private static void testCrearVentaRapido() {
        try {
            // Importar las clases necesarias
            config.DatabaseConnection dbConn = config.DatabaseConnection.getInstance();
            java.sql.Connection conn = dbConn.getConnection();

            // 1. Verificar que hay datos
            System.out.println("1. Verificando datos existentes...");

            // Verificar ventas
            java.sql.PreparedStatement ventasStmt = conn.prepareStatement("SELECT COUNT(*) FROM ventas");
            java.sql.ResultSet ventasRs = ventasStmt.executeQuery();
            ventasRs.next();
            int countVentas = ventasRs.getInt(1);
            ventasRs.close();
            ventasStmt.close();

            // Verificar productos
            java.sql.PreparedStatement prodStmt = conn.prepareStatement("SELECT COUNT(*) FROM productos");
            java.sql.ResultSet prodRs = prodStmt.executeQuery();
            prodRs.next();
            int countProductos = prodRs.getInt(1);
            prodRs.close();
            prodStmt.close();

            // Verificar clientes
            java.sql.PreparedStatement clientStmt = conn.prepareStatement("SELECT COUNT(*) FROM clientes");
            java.sql.ResultSet clientRs = clientStmt.executeQuery();
            clientRs.next();
            int countClientes = clientRs.getInt(1);
            clientRs.close();
            clientStmt.close();

            System.out.printf("   Ventas: %d, Productos: %d, Clientes: %d%n",
                    countVentas, countProductos, countClientes);

            // 2. Crear datos básicos si no existen
            if (countClientes == 0) {
                System.out.println("2. Creando cliente de prueba...");
                String sqlCliente = "INSERT INTO clientes (nombre, tipo_documento, numero_documento) " +
                        "VALUES ('Cliente Prueba', 'CEDULA', '1234567890')";
                java.sql.PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente);
                stmtCliente.executeUpdate();
                stmtCliente.close();
                conn.commit();
                System.out.println("   Cliente creado");
            }

            if (countProductos == 0) {
                System.out.println("3. Creando producto de prueba...");
                String sqlProducto = "INSERT INTO productos (codigo, nombre, descripcion, precio_compra, precio_venta, stock_actual, stock_minimo) "
                        +
                        "VALUES ('PROD-TEST', 'Producto Test', 'Producto para pruebas', 10.00, 15.00, 100, 5)";
                java.sql.PreparedStatement stmtProducto = conn.prepareStatement(sqlProducto);
                stmtProducto.executeUpdate();
                stmtProducto.close();
                conn.commit();
                System.out.println("   Producto creado");
            }

            // 3. Probar creación de venta usando VentaService
            System.out.println("4. Probando VentaService...");
            services.VentaService ventaService = services.VentaService.getInstance();

            // Crear venta básica
            models.Venta venta = new models.Venta();
            venta.setNumeroFactura("TEST-" + System.currentTimeMillis());
            venta.setClienteId(1);
            venta.setUsuarioId(1);
            venta.setFechaVenta(java.time.LocalDateTime.now());
            venta.setEstado("PENDIENTE");
            venta.setMetodoPago("EFECTIVO");
            venta.setSubtotal(new java.math.BigDecimal("15.00"));
            venta.setDescuento(java.math.BigDecimal.ZERO);
            venta.setImpuestos(java.math.BigDecimal.ZERO);
            venta.setTotal(new java.math.BigDecimal("15.00"));

            // Crear detalle
            models.DetalleVenta detalle = new models.DetalleVenta();
            detalle.setProductoId(1);
            detalle.setCantidad(1);
            detalle.setPrecioUnitario(new java.math.BigDecimal("15.00"));
            detalle.setDescuento(java.math.BigDecimal.ZERO);
            detalle.setSubtotal(new java.math.BigDecimal("15.00"));

            // Agregar detalle a la venta
            java.util.List<models.DetalleVenta> detalles = new java.util.ArrayList<>();
            detalles.add(detalle);
            venta.setDetalles(detalles);

            // Intentar crear la venta
            boolean resultado = ventaService.crearVenta(venta);

            if (resultado) {
                System.out.println("   ✅ VENTA CREADA EXITOSAMENTE!");
                System.out.println("   🆔 ID Venta: " + venta.getId());
                System.out.println("   📄 Número: " + venta.getNumeroFactura());
            } else {
                System.out.println("   ❌ Error al crear venta");
            }

            System.out.println("=== FIN DEL TEST ===\n");

        } catch (Exception e) {
            System.err.println("❌ Error en test: " + e.getMessage());
            e.printStackTrace();
        }
    }

}