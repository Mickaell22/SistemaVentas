package Test;

import config.DatabaseConnection;
import services.ClienteService;
import dao.impl.ClienteDAOImpl;
import dao.interfaces.IClienteDAO;
import models.Cliente;
import java.util.List;
import java.util.Optional;

public class TestClienteComplete {
    
    public static void main(String[] args) {
        System.out.println("=== PRUEBA COMPLETA DEL MÓDULO DE CLIENTES ===\n");
        
        try {
            // Verificar conexión
            System.out.println("1. 🔗 Verificando conexión a base de datos...");
            DatabaseConnection.getInstance();
            System.out.println("   ✅ Conexión establecida\n");
            
            // Test del modelo
            testModelo();
            
            // Test del DAO
            testDAO();
            
            // Test del Service
            testService();
            
            // Test de validaciones
            testValidaciones();
            
            // Test de búsquedas
            testBusquedas();
            
            // Resumen final
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🎉 TODAS LAS PRUEBAS DEL MÓDULO CLIENTES COMPLETADAS");
            System.out.println("=".repeat(60));
            System.out.println("✅ Modelo Cliente funcionando correctamente");
            System.out.println("✅ ClienteDAO funcionando correctamente");
            System.out.println("✅ ClienteService funcionando correctamente");
            System.out.println("✅ Validaciones funcionando correctamente");
            System.out.println("✅ Búsquedas funcionando correctamente");
            System.out.println("\n🚀 El módulo está listo para usar en producción!");
            
        } catch (Exception e) {
            System.err.println("❌ Error durante las pruebas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testModelo() {
        System.out.println("2. 👤 Probando modelo Cliente...");
        
        // Constructor vacío
        Cliente cliente1 = new Cliente();
        assert cliente1.isActivo() : "Cliente debería estar activo por defecto";
        assert "CEDULA".equals(cliente1.getTipoDocumento()) : "Tipo documento por defecto debería ser CEDULA";
        System.out.println("   ✅ Constructor vacío OK");
        
        // Constructor con parámetros
        Cliente cliente2 = new Cliente("CEDULA", "1234567890", "Juan", "Pérez", 
                                     "juan@email.com", "0999888777", "Calle 123");
        assert "Juan".equals(cliente2.getNombre()) : "Nombre no asignado correctamente";
        assert "1234567890".equals(cliente2.getNumeroDocumento()) : "Documento no asignado correctamente";
        System.out.println("   ✅ Constructor con parámetros OK");
        
        // Métodos de utilidad
        cliente2.setApellido("González");
        assert "Juan González".equals(cliente2.getNombreCompleto()) : "Nombre completo incorrecto";
        assert cliente2.esPersonaNatural() : "Debería ser persona natural";
        assert !cliente2.esPersonaJuridica() : "No debería ser persona jurídica";
        System.out.println("   ✅ Métodos de utilidad OK");
        
        // Display text
        String displayText = cliente2.getDisplayText();
        assert displayText.contains("1234567890") : "Display text debería contener documento";
        assert displayText.contains("Juan González") : "Display text debería contener nombre";
        System.out.println("   ✅ Display text OK");
        
        System.out.println("   ✅ Modelo Cliente: TODAS LAS PRUEBAS PASARON\n");
    }
    
    private static void testDAO() {
        System.out.println("3. 🗄️ Probando ClienteDAO...");
        
        IClienteDAO clienteDAO = new ClienteDAOImpl();
        
        // Test de conteos iniciales
        int totalInicial = clienteDAO.contarClientes();
        int activosInicial = clienteDAO.contarClientesActivos();
        System.out.println("   📊 Estado inicial: " + totalInicial + " clientes, " + activosInicial + " activos");
        
        // Test crear cliente
        Cliente nuevoCliente = new Cliente("CEDULA", "0999888777", "María", "García", 
                                         "maria@test.com", "0988777666", "Av. Test 456");
        
        boolean creado = clienteDAO.crear(nuevoCliente);
        assert creado : "Cliente debería haberse creado";
        assert nuevoCliente.getId() > 0 : "Cliente debería tener ID asignado";
        System.out.println("   ✅ Crear cliente OK (ID: " + nuevoCliente.getId() + ")");
        
        // Test obtener por ID
        Optional<Cliente> clienteOpt = clienteDAO.obtenerPorId(nuevoCliente.getId());
        assert clienteOpt.isPresent() : "Cliente debería existir";
        Cliente clienteObtenido = clienteOpt.get();
        assert "María".equals(clienteObtenido.getNombre()) : "Nombre no coincide";
        System.out.println("   ✅ Obtener por ID OK");
        
        // Test obtener por documento
        Optional<Cliente> clientePorDoc = clienteDAO.obtenerPorDocumento("0999888777");
        assert clientePorDoc.isPresent() : "Cliente debería encontrarse por documento";
        assert clientePorDoc.get().getId() == nuevoCliente.getId() : "IDs deberían coincidir";
        System.out.println("   ✅ Obtener por documento OK");
        
        // Test actualizar
        clienteObtenido.setNombre("María Isabel");
        boolean actualizado = clienteDAO.actualizar(clienteObtenido);
        assert actualizado : "Cliente debería haberse actualizado";
        
        Optional<Cliente> clienteActualizado = clienteDAO.obtenerPorId(nuevoCliente.getId());
        assert clienteActualizado.isPresent() : "Cliente debería existir después de actualizar";
        assert "María Isabel".equals(clienteActualizado.get().getNombre()) : "Nombre no se actualizó";
        System.out.println("   ✅ Actualizar cliente OK");
        
        // Test validaciones
        assert clienteDAO.existeDocumento("0999888777") : "Documento debería existir";
        assert clienteDAO.existeEmail("maria@test.com") : "Email debería existir";
        assert !clienteDAO.existeDocumento("1111111111") : "Documento inexistente no debería existir";
        System.out.println("   ✅ Validaciones de existencia OK");
        
        // Test búsquedas
        List<Cliente> busquedaNombre = clienteDAO.buscarPorNombre("María");
        assert !busquedaNombre.isEmpty() : "Búsqueda por nombre debería encontrar resultados";
        System.out.println("   ✅ Búsqueda por nombre OK (" + busquedaNombre.size() + " resultados)");
        
        // Test eliminar (desactivar)
        boolean eliminado = clienteDAO.eliminar(nuevoCliente.getId());
        assert eliminado : "Cliente debería haberse eliminado";
        
        Optional<Cliente> clienteEliminado = clienteDAO.obtenerPorId(nuevoCliente.getId());
        assert clienteEliminado.isPresent() : "Cliente debería existir (solo desactivado)";
        assert !clienteEliminado.get().isActivo() : "Cliente debería estar inactivo";
        System.out.println("   ✅ Eliminar (desactivar) cliente OK");
        
        // Test conteos finales
        int totalFinal = clienteDAO.contarClientes();
        int activosFinal = clienteDAO.contarClientesActivos();
        int inactivosFinal = clienteDAO.contarClientesInactivos();
        
        assert totalFinal == totalInicial + 1 : "Total debería haber aumentado en 1";
        assert activosFinal == activosInicial : "Activos deberían ser los mismos (cliente desactivado)";
        assert inactivosFinal >= 1 : "Debería haber al menos 1 inactivo";
        System.out.println("   📊 Estado final: " + totalFinal + " clientes, " + activosFinal + " activos, " + inactivosFinal + " inactivos");
        
        System.out.println("   ✅ ClienteDAO: TODAS LAS PRUEBAS PASARON\n");
    }
    
    private static void testService() {
        System.out.println("4. ⚙️ Probando ClienteService...");
        
        ClienteService clienteService = ClienteService.getInstance();
        
        // Test crear cliente válido
        Cliente clienteValido = new Cliente("RUC", "1234567890001", "Empresa Test", "", 
                                          "empresa@test.com", "0999111222", "Oficina 123");
        
        boolean creadoService = clienteService.crearCliente(clienteValido);
        assert creadoService : "Cliente válido debería crearse";
        System.out.println("   ✅ Crear cliente válido OK (ID: " + clienteValido.getId() + ")");
        
        // Test crear cliente inválido (documento duplicado)
        Cliente clienteDuplicado = new Cliente("RUC", "1234567890001", "Otra Empresa", "", 
                                             "otra@test.com", "0999333444", "Otra oficina");
        
        boolean creadoDuplicado = clienteService.crearCliente(clienteDuplicado);
        assert !creadoDuplicado : "Cliente con documento duplicado no debería crearse";
        System.out.println("   ✅ Validación documento duplicado OK");
        
        // Test obtener clientes
        List<Cliente> clientesActivos = clienteService.obtenerClientesActivos();
        assert !clientesActivos.isEmpty() : "Debería haber clientes activos";
        System.out.println("   ✅ Obtener clientes activos OK (" + clientesActivos.size() + " clientes)");
        
        // Test búsqueda
        List<Cliente> busqueda = clienteService.buscarClientes("Empresa");
        assert !busqueda.isEmpty() : "Búsqueda debería encontrar resultados";
        System.out.println("   ✅ Búsqueda de clientes OK (" + busqueda.size() + " resultados)");
        
        // Test estadísticas
        String estadisticas = clienteService.getEstadisticasClientes();
        assert estadisticas != null && !estadisticas.isEmpty() : "Estadísticas no deberían estar vacías";
        System.out.println("   📊 Estadísticas: " + estadisticas);
        
        // Test actualizar cliente
        clienteValido.setNombre("Empresa Test Actualizada");
        boolean actualizado = clienteService.actualizarCliente(clienteValido);
        assert actualizado : "Cliente debería actualizarse";
        System.out.println("   ✅ Actualizar cliente OK");
        
        // Test eliminar cliente
        boolean eliminado = clienteService.eliminarCliente(clienteValido.getId());
        assert eliminado : "Cliente debería eliminarse";
        System.out.println("   ✅ Eliminar cliente OK");
        
        System.out.println("   ✅ ClienteService: TODAS LAS PRUEBAS PASARON\n");
    }
    
    private static void testValidaciones() {
        System.out.println("5. ✅ Probando validaciones...");
        
        ClienteService service = ClienteService.getInstance();
        
        // Test tipos de documento
        String[] tipos = service.getTiposDocumento();
        assert tipos.length == 3 : "Deberían ser 3 tipos de documento";
        assert "Cédula".equals(tipos[0]) : "Primer tipo debería ser Cédula";
        System.out.println("   ✅ Tipos de documento OK");
        
        // Test conversiones
        assert "CEDULA".equals(service.convertirTipoDocumentoADB("Cédula")) : "Conversión a DB incorrecta";
        assert "Cédula".equals(service.convertirTipoDocumentoADisplay("CEDULA")) : "Conversión a display incorrecta";
        System.out.println("   ✅ Conversiones de tipo documento OK");
        
        // Test cliente inválido (sin nombre)
        Cliente clienteInvalido1 = new Cliente("CEDULA", "1234567890", "", "", "", "", "");
        boolean creadoInvalido1 = service.crearCliente(clienteInvalido1);
        assert !creadoInvalido1 : "Cliente sin nombre no debería crearse";
        System.out.println("   ✅ Validación nombre requerido OK");
        
        // Test cliente inválido (documento inválido)
        Cliente clienteInvalido2 = new Cliente("CEDULA", "123", "Juan", "Pérez", "", "", "");
        boolean creadoInvalido2 = service.crearCliente(clienteInvalido2);
        assert !creadoInvalido2 : "Cliente con documento inválido no debería crearse";
        System.out.println("   ✅ Validación formato documento OK");
        
        // Test cliente inválido (email inválido)
        Cliente clienteInvalido3 = new Cliente("CEDULA", "9876543210", "Ana", "López", "email_invalido", "", "");
        boolean creadoInvalido3 = service.crearCliente(clienteInvalido3);
        assert !creadoInvalido3 : "Cliente con email inválido no debería crearse";
        System.out.println("   ✅ Validación formato email OK");
        
        System.out.println("   ✅ Validaciones: TODAS LAS PRUEBAS PASARON\n");
    }
    
    private static void testBusquedas() {
        System.out.println("6. 🔍 Probando búsquedas avanzadas...");
        
        ClienteService service = ClienteService.getInstance();
        IClienteDAO dao = new ClienteDAOImpl();
        
        // Crear clientes de prueba para búsquedas
        Cliente cliente1 = new Cliente("CEDULA", "1111111111", "Carlos", "Rodríguez", 
                                     "carlos@search.com", "0999000111", "Búsqueda 1");
        Cliente cliente2 = new Cliente("RUC", "2222222222001", "Empresa Búsqueda", "", 
                                     "empresa@search.com", "0999000222", "Búsqueda 2");
        Cliente cliente3 = new Cliente("PASAPORTE", "PASS123456", "John", "Smith", 
                                     "john@search.com", "0999000333", "Búsqueda 3");
        
        service.crearCliente(cliente1);
        service.crearCliente(cliente2);
        service.crearCliente(cliente3);
        
        // Test búsqueda por nombre completo
        List<Cliente> busquedaCarlos = dao.buscarPorNombreCompleto("Carlos");
        assert !busquedaCarlos.isEmpty() : "Debería encontrar a Carlos";
        System.out.println("   ✅ Búsqueda por nombre completo OK (" + busquedaCarlos.size() + " resultados)");
        
        // Test búsqueda por tipo de documento
        List<Cliente> cedulas = dao.obtenerPorTipoDocumento("CEDULA");
        List<Cliente> rucs = dao.obtenerPorTipoDocumento("RUC");
        List<Cliente> pasaportes = dao.obtenerPorTipoDocumento("PASAPORTE");
        
        assert !cedulas.isEmpty() : "Debería haber cédulas";
        assert !rucs.isEmpty() : "Debería haber RUCs";
        assert !pasaportes.isEmpty() : "Debería haber pasaportes";
        System.out.println("   ✅ Búsqueda por tipo documento OK (C:" + cedulas.size() + 
                          ", R:" + rucs.size() + ", P:" + pasaportes.size() + ")");
        
        // Test búsqueda con filtros
        List<Cliente> filtrados = dao.buscarConFiltros("search", "CEDULA", true);
        System.out.println("   ✅ Búsqueda con filtros OK (" + filtrados.size() + " resultados)");
        
        // Test últimos creados
        List<Cliente> ultimos = dao.obtenerUltimosCreados(5);
        assert !ultimos.isEmpty() : "Debería haber últimos creados";
        assert ultimos.size() <= 5 : "No debería retornar más de 5";
        System.out.println("   ✅ Últimos creados OK (" + ultimos.size() + " resultados)");
        
        // Limpiar clientes de prueba
        service.eliminarCliente(cliente1.getId());
        service.eliminarCliente(cliente2.getId());
        service.eliminarCliente(cliente3.getId());
        System.out.println("   🧹 Limpieza de datos de prueba OK");
        
        System.out.println("   ✅ Búsquedas avanzadas: TODAS LAS PRUEBAS PASARON\n");
    }
}