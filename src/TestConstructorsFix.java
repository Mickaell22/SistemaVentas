import models.Categoria;
import models.Proveedor;

public class TestConstructorsFix {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE CONSTRUCTORES CORREGIDOS ===\n");
        
        try {
            // Probar constructores de Categoria
            System.out.println("1. 📁 Probando constructores de Categoría:");
            
            // Constructor normal
            Categoria cat1 = new Categoria("ELECTRONICA", "Productos electrónicos");
            System.out.println("   Constructor normal: " + cat1.toString());
            
            // Constructor con ID (para ComboBox)
            Categoria cat2 = new Categoria(0, "Sin categorías", "");
            System.out.println("   Constructor con ID: " + cat2.toString());
            
            // Constructor completo
            Categoria cat3 = new Categoria(1, "HOGAR", "Artículos del hogar", true, null);
            System.out.println("   Constructor completo: " + cat3.toString());
            
            // Probar constructores de Proveedor
            System.out.println("\n2. 🏢 Probando constructores de Proveedor:");
            
            // Constructor normal
            Proveedor prov1 = new Proveedor("TechSupply", "Juan Pérez", "099999999", 
                                          "ventas@tech.com", "Av. Principal 123", "1234567890001");
            System.out.println("   Constructor normal: " + prov1.toString());
            
            // Constructor con ID (para ComboBox)
            Proveedor prov2 = new Proveedor(0, "Sin proveedores", "", "", "", "", "");
            System.out.println("   Constructor con ID: " + prov2.toString());
            
            // Constructor completo
            Proveedor prov3 = new Proveedor(1, "Proveedor Test", "María García", "098888888",
                                          "maria@test.com", "Calle Test 456", "9876543210001",
                                          true, null, null);
            System.out.println("   Constructor completo: " + prov3.toString());
            
            System.out.println("\n✅ TODOS LOS CONSTRUCTORES FUNCIONAN CORRECTAMENTE");
            System.out.println("🎯 Ahora ProductoFormDialog debería funcionar sin errores");
            
        } catch (Exception e) {
            System.err.println("❌ Error durante la prueba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}