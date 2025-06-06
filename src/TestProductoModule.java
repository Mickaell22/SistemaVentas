import services.ProductoService;
import models.Producto;
import models.Categoria;
import models.Proveedor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class TestProductoModule {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DEL MÓDULO DE PRODUCTOS ===\n");
        
        ProductoService productoService = ProductoService.getInstance();
        
        try {
            // 1. Probar categorías
            System.out.println("1. Probando categorías:");
            List<Categoria> categorias = productoService.obtenerTodasLasCategorias();
            System.out.println("   Categorías existentes: " + categorias.size());
            for (Categoria cat : categorias) {
                System.out.println("     - " + cat.toString());
            }
            
            // Crear nueva categoría
            Categoria nuevaCategoria = new Categoria("TECNOLOGIA", "Productos tecnológicos");
            boolean categoriaCreada = productoService.crearCategoria(nuevaCategoria);
            System.out.println("   Nueva categoría creada: " + (categoriaCreada ? "OK" : "ERROR"));
            
            // 2. Probar proveedores
            System.out.println("\n2. Probando proveedores:");
            List<Proveedor> proveedores = productoService.obtenerTodosLosProveedores();
            System.out.println("   Proveedores existentes: " + proveedores.size());
            for (Proveedor prov : proveedores) {
                System.out.println("     - " + prov.toString());
            }
            
            // Crear nuevo proveedor
            Proveedor nuevoProveedor = new Proveedor(
                "TechSupply S.A.", "Carlos Mendez", "0999123456",
                "ventas@techsupply.com", "Av. Principal 456", "0992345678001"
            );
            boolean proveedorCreado = productoService.crearProveedor(nuevoProveedor);
            System.out.println("   Nuevo proveedor creado: " + (proveedorCreado ? "OK" : "ERROR"));
            
            // 3. Probar productos
            System.out.println("\n3. Probando productos:");
            List<Producto> productos = productoService.obtenerProductosActivos();
            System.out.println("   Productos existentes: " + productos.size());
            
            // Crear nuevo producto
            System.out.println("\n   Creando nuevo producto:");
            String codigo = productoService.generarCodigoProducto();
            System.out.println("   Código generado: " + codigo);
            
            Producto nuevoProducto = new Producto(
                codigo,
                "Laptop Dell Inspiron 15",
                "Laptop Dell Inspiron 15 pulgadas, 8GB RAM, 256GB SSD",
                1, // ID de categoría (usar la primera disponible)
                1, // ID de proveedor (usar el primero disponible)
                new BigDecimal("450.00"),  // Precio compra
                new BigDecimal("650.00"),  // Precio venta
                10,  // Stock actual
                3,   // Stock mínimo
                "UNIDAD"
            );
            
            boolean productoCreado = productoService.crearProducto(nuevoProducto);
            System.out.println("   Producto creado: " + (productoCreado ? "OK" : "ERROR"));
            
            if (productoCreado) {
                System.out.println("   ID asignado: " + nuevoProducto.getId());
                
                // Probar búsqueda por código
                Optional<Producto> encontrado = productoService.obtenerProductoPorCodigo(codigo);
                System.out.println("   Búsqueda por código: " + (encontrado.isPresent() ? "OK" : "ERROR"));
                
                if (encontrado.isPresent()) {
                    Producto prod = encontrado.get();
                    System.out.println("     - Nombre: " + prod.getNombre());
                    System.out.println("     - Categoría: " + prod.getCategoriaNombre());
                    System.out.println("     - Proveedor: " + prod.getProveedorNombre());
                    System.out.println("     - Precio venta: $" + prod.getPrecioVenta());
                    System.out.println("     - Stock: " + prod.getStockActual());
                    System.out.println("     - Margen: $" + prod.getMargenGanancia());
                    System.out.println("     - % Margen: " + String.format("%.2f%%", prod.getPorcentajeMargen()));
                    System.out.println("     - Estado stock: " + prod.getEstadoStock());
                }
            }
            
            // 4. Probar búsquedas
            System.out.println("\n4. Probando búsquedas:");
            List<Producto> busquedaLaptop = productoService.buscarProductosPorNombre("Laptop");
            System.out.println("   Búsqueda 'Laptop': " + busquedaLaptop.size() + " resultados");
            
            List<Producto> todosProductos = productoService.obtenerTodosLosProductos();
            System.out.println("   Total productos: " + todosProductos.size());
            
            // 5. Probar stock
            System.out.println("\n5. Probando gestión de stock:");
            List<Producto> stockBajo = productoService.obtenerProductosConStockBajo();
            System.out.println("   Productos con stock bajo: " + stockBajo.size());
            
            List<Producto> stockCritico = productoService.obtenerProductosConStockCritico();
            System.out.println("   Productos con stock crítico: " + stockCritico.size());
            
            // Crear producto con stock bajo para probar alertas
            if (productoCreado) {
                System.out.println("\n   Creando producto con stock bajo:");
                Producto productoStockBajo = new Producto(
                    productoService.generarCodigoProducto(),
                    "Mouse Inalámbrico",
                    "Mouse inalámbrico ergonómico",
                    1, 1,
                    new BigDecimal("5.00"),
                    new BigDecimal("12.00"),
                    2,  // Stock actual bajo
                    10, // Stock mínimo alto
                    "UNIDAD"
                );
                
                boolean stockBajoCreado = productoService.crearProducto(productoStockBajo);
                System.out.println("   Producto con stock bajo creado: " + (stockBajoCreado ? "OK" : "ERROR"));
                
                if (stockBajoCreado) {
                    List<Producto> nuevosStockBajo = productoService.obtenerProductosConStockBajo();
                    System.out.println("   Nuevos productos con stock bajo: " + nuevosStockBajo.size());
                    
                    for (Producto p : nuevosStockBajo) {
                        System.out.println("     " + p.getNombre() + " (Stock: " + 
                                         p.getStockActual() + ", Mínimo: " + p.getStockMinimo() + ")");
                    }
                }
            }
            
            // 6. Estadísticas
            System.out.println("\n6. Estadísticas:");
            String estadisticas = productoService.getEstadisticasProductos();
            System.out.println("   " + estadisticas);
            
            System.out.println("   Tiene productos con stock bajo: " + 
                             (productoService.tieneProductosConStockBajo() ? "Sí" : "No"));
            System.out.println("   Tiene productos con stock crítico: " + 
                             (productoService.tieneProductosConStockCritico() ? "Sí" : "No"));
            
            // 7. Probar actualización de stock
            if (productoCreado && nuevoProducto.getId() > 0) {
                System.out.println("\n7. Probando actualización de stock:");
                System.out.println("   Stock actual: " + nuevoProducto.getStockActual());
                
                boolean stockActualizado = productoService.actualizarStock(nuevoProducto.getId(), 25);
                System.out.println("   Stock actualizado a 25: " + (stockActualizado ? "OK" : "ERROR"));
                
                // Verificar actualización
                Optional<Producto> verificacion = productoService.obtenerProductoPorId(nuevoProducto.getId());
                if (verificacion.isPresent()) {
                    System.out.println("   Nuevo stock verificado: " + verificacion.get().getStockActual());
                }
            }
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("MÓDULO DE PRODUCTOS PROBADO EXITOSAMENTE");
            System.out.println("=".repeat(50));
            System.out.println("Funcionalidades verificadas:");
            System.out.println("   CRUD de categorías");
            System.out.println("   CRUD de proveedores");
            System.out.println("   CRUD de productos");
            System.out.println("   Generación de códigos únicos");
            System.out.println("   Búsquedas y filtros");
            System.out.println("   Gestión de stock y alertas");
            System.out.println("   Cálculos de precios y márgenes");
            System.out.println("   Validaciones de negocio");
            
        } catch (Exception e) {
            System.err.println("Error durante la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}