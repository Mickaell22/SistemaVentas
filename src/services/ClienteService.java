package services;

import dao.impl.ClienteDAOImpl;
import dao.interfaces.IClienteDAO;
import models.Cliente;
import java.util.List;
import java.util.Optional;

public class ClienteService {
    
    private IClienteDAO clienteDAO;
    private static ClienteService instance;
    
    private ClienteService() {
        this.clienteDAO = new ClienteDAOImpl();
    }
    
    public static ClienteService getInstance() {
        if (instance == null) {
            instance = new ClienteService();
        }
        return instance;
    }
    
    // ===== OPERACIONES PRINCIPALES =====
    
    /**
     * Crea un nuevo cliente con validaciones completas
     */
    public boolean crearCliente(Cliente cliente) {
        try {
            // Validaciones de negocio
            String validationResult = validarCliente(cliente);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Verificar documento único
            if (clienteDAO.existeDocumento(cliente.getNumeroDocumento())) {
                System.err.println("Ya existe un cliente con el documento: " + cliente.getNumeroDocumento());
                return false;
            }
            
            // Verificar email único (si se proporciona)
            if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
                if (clienteDAO.existeEmail(cliente.getEmail())) {
                    System.err.println("Ya existe un cliente con el email: " + cliente.getEmail());
                    return false;
                }
            }
            
            // Crear cliente
            boolean resultado = clienteDAO.crear(cliente);
            if (resultado) {
                System.out.println("Cliente creado exitosamente: " + cliente.getDisplayText());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza un cliente existente
     */
    public boolean actualizarCliente(Cliente cliente) {
        try {
            // Validaciones básicas
            if (cliente.getId() <= 0) {
                System.err.println("ID de cliente inválido");
                return false;
            }
            
            String validationResult = validarClienteParaActualizacion(cliente);
            if (!validationResult.equals("OK")) {
                System.err.println("Error de validación: " + validationResult);
                return false;
            }
            
            // Verificar documento único en otros clientes
            if (clienteDAO.existeDocumentoEnOtroCliente(cliente.getNumeroDocumento(), cliente.getId())) {
                System.err.println("Ya existe otro cliente con el documento: " + cliente.getNumeroDocumento());
                return false;
            }
            
            // Verificar email único en otros clientes (si se proporciona)
            if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
                if (clienteDAO.existeEmailEnOtroCliente(cliente.getEmail(), cliente.getId())) {
                    System.err.println("Ya existe otro cliente con el email: " + cliente.getEmail());
                    return false;
                }
            }
            
            boolean resultado = clienteDAO.actualizar(cliente);
            if (resultado) {
                System.out.println("Cliente actualizado: " + cliente.getDisplayText());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina (desactiva) un cliente
     */
    public boolean eliminarCliente(int id) {
        try {
            // Verificar que el cliente existe
            Optional<Cliente> clienteOpt = clienteDAO.obtenerPorId(id);
            if (!clienteOpt.isPresent()) {
                System.err.println("Cliente no encontrado con ID: " + id);
                return false;
            }
            
            // TODO: Verificar que no tenga ventas pendientes o historial importante
            // Esta validación se implementará cuando tengamos el módulo de ventas
            
            boolean resultado = clienteDAO.eliminar(id);
            if (resultado) {
                System.out.println("Cliente eliminado: " + clienteOpt.get().getDisplayText());
            }
            
            return resultado;
            
        } catch (Exception e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }
    
    // ===== CONSULTAS Y BÚSQUEDAS =====
    
    /**
     * Obtiene todos los clientes
     */
    public List<Cliente> obtenerTodosLosClientes() {
        return clienteDAO.obtenerTodos();
    }
    
    /**
     * Obtiene solo clientes activos
     */
    public List<Cliente> obtenerClientesActivos() {
        return clienteDAO.obtenerActivos();
    }
    
    /**
     * Obtiene un cliente por ID
     */
    public Optional<Cliente> obtenerClientePorId(int id) {
        return clienteDAO.obtenerPorId(id);
    }
    
    /**
     * Obtiene un cliente por documento
     */
    public Optional<Cliente> obtenerClientePorDocumento(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return Optional.empty();
        }
        return clienteDAO.obtenerPorDocumento(numeroDocumento.trim());
    }
    
    /**
     * Busca clientes por múltiples criterios
     */
    public List<Cliente> buscarClientes(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerClientesActivos();
        }
        return clienteDAO.buscarPorNombreCompleto(termino.trim());
    }
    
    /**
     * Búsqueda avanzada con filtros
     */
    public List<Cliente> buscarClientesConFiltros(String termino, String tipoDocumento, boolean soloActivos) {
        return clienteDAO.buscarConFiltros(termino, tipoDocumento, soloActivos);
    }
    
    /**
     * Obtiene clientes por tipo de documento
     */
    public List<Cliente> obtenerClientesPorTipo(String tipoDocumento) {
        return clienteDAO.obtenerPorTipoDocumento(tipoDocumento);
    }
    
    /**
     * Obtiene los últimos clientes creados
     */
    public List<Cliente> obtenerUltimosClientes(int limite) {
        return clienteDAO.obtenerUltimosCreados(limite);
    }
    
    // ===== OPERACIONES DE ESTADO =====
    
    /**
     * Activa un cliente
     */
    public boolean activarCliente(int id) {
        Optional<Cliente> clienteOpt = clienteDAO.obtenerPorId(id);
        if (!clienteOpt.isPresent()) {
            System.err.println("Cliente no encontrado para activar: " + id);
            return false;
        }
        
        boolean resultado = clienteDAO.activar(id);
        if (resultado) {
            System.out.println("Cliente activado: " + clienteOpt.get().getDisplayText());
        }
        return resultado;
    }
    
    /**
     * Desactiva un cliente
     */
    public boolean desactivarCliente(int id) {
        Optional<Cliente> clienteOpt = clienteDAO.obtenerPorId(id);
        if (!clienteOpt.isPresent()) {
            System.err.println("Cliente no encontrado para desactivar: " + id);
            return false;
        }
        
        boolean resultado = clienteDAO.desactivar(id);
        if (resultado) {
            System.out.println("Cliente desactivado: " + clienteOpt.get().getDisplayText());
        }
        return resultado;
    }
    
    // ===== VALIDACIONES =====
    
    /**
     * Verifica si existe un documento
     */
    public boolean existeDocumento(String numeroDocumento) {
        return clienteDAO.existeDocumento(numeroDocumento);
    }
    
    /**
     * Verifica si existe un email
     */
    public boolean existeEmail(String email) {
        return clienteDAO.existeEmail(email);
    }
    
    // ===== ESTADÍSTICAS =====
    
    /**
     * Obtiene estadísticas generales de clientes
     */
    public String getEstadisticasClientes() {
        int total = clienteDAO.contarClientes();
        int activos = clienteDAO.contarClientesActivos();
        int inactivos = clienteDAO.contarClientesInactivos();
        int cedulas = clienteDAO.contarPorTipoDocumento("CEDULA");
        int rucs = clienteDAO.contarPorTipoDocumento("RUC");
        int pasaportes = clienteDAO.contarPorTipoDocumento("PASAPORTE");
        
        return String.format("Clientes: %d total, %d activos, %d inactivos | Tipos: %d cédulas, %d RUCs, %d pasaportes",
                           total, activos, inactivos, cedulas, rucs, pasaportes);
    }
    
    /**
     * Obtiene conteo de clientes activos
     */
    public int contarClientesActivos() {
        return clienteDAO.contarClientesActivos();
    }
    
    /**
     * Obtiene conteo total de clientes
     */
    public int contarTotalClientes() {
        return clienteDAO.contarClientes();
    }
    
    // ===== MÉTODOS DE VALIDACIÓN PRIVADOS =====
    
    /**
     * Valida un cliente para creación
     */
    private String validarCliente(Cliente cliente) {
        if (cliente == null) {
            return "Cliente no puede ser null";
        }
        
        // Documento requerido
        if (cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().trim().isEmpty()) {
            return "Número de documento es requerido";
        }
        
        // Validar documento según tipo
        String errorDocumento = validarDocumentoPorTipo(cliente.getTipoDocumento(), cliente.getNumeroDocumento());
        if (!errorDocumento.equals("OK")) {
            return errorDocumento;
        }
        
        // Nombre requerido
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            return "Nombre es requerido";
        }
        
        // Validar nombre (solo letras, espacios y algunos caracteres especiales)
        if (!cliente.getNombre().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s.-]+$")) {
            return "Nombre contiene caracteres no válidos";
        }
        
        // Validar apellido si se proporciona
        if (cliente.getApellido() != null && !cliente.getApellido().trim().isEmpty()) {
            if (!cliente.getApellido().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s.-]+$")) {
                return "Apellido contiene caracteres no válidos";
            }
        }
        
        // Validar email si se proporciona
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty()) {
            if (!esEmailValido(cliente.getEmail())) {
                return "Email no tiene formato válido";
            }
        }
        
        // Validar teléfono si se proporciona
        if (cliente.getTelefono() != null && !cliente.getTelefono().trim().isEmpty()) {
            if (!cliente.getTelefono().matches("^[0-9+\\-\\s()]+$")) {
                return "Teléfono contiene caracteres no válidos";
            }
        }
        
        return "OK";
    }
    
    /**
     * Valida un cliente para actualización (sin verificar duplicados)
     */
    private String validarClienteParaActualizacion(Cliente cliente) {
        return validarCliente(cliente); // Mismas validaciones que para creación
    }
    
    /**
     * Valida documento según su tipo
     */
    private String validarDocumentoPorTipo(String tipoDocumento, String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return "Número de documento es requerido";
        }
        
        String documento = numeroDocumento.trim();
        
        switch (tipoDocumento) {
            case "CEDULA":
                return validarCedula(documento);
            case "RUC":
                return validarRuc(documento);
            case "PASAPORTE":
                return validarPasaporte(documento);
            default:
                return "Tipo de documento no válido";
        }
    }
    
    /**
     * Validación básica de cédula ecuatoriana
     */
    private String validarCedula(String cedula) {
        // Debe tener exactamente 10 dígitos
        if (!cedula.matches("^\\d{10}$")) {
            return "Cédula debe tener exactamente 10 dígitos";
        }
        
        // Validación básica: los dos primeros dígitos deben corresponder a una provincia (01-24)
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if (provincia < 1 || provincia > 24) {
            return "Cédula no corresponde a una provincia válida";
        }
        
        // El tercer dígito debe ser menor a 6 (personas naturales)
        int tercerDigito = Integer.parseInt(cedula.substring(2, 3));
        if (tercerDigito >= 6) {
            return "Cédula no corresponde a una persona natural";
        }
        
        return "OK";
    }
    
    /**
     * Validación básica de RUC ecuatoriano
     */
    private String validarRuc(String ruc) {
        // RUC puede ser de 13 dígitos
        if (!ruc.matches("^\\d{13}$")) {
            return "RUC debe tener exactamente 13 dígitos";
        }
        
        // Los tres últimos dígitos deben ser 001 para la mayoría de casos
        if (!ruc.endsWith("001")) {
            return "RUC debe terminar en 001";
        }
        
        // Validación básica: los dos primeros dígitos deben corresponder a una provincia (01-24)
        int provincia = Integer.parseInt(ruc.substring(0, 2));
        if (provincia < 1 || provincia > 24) {
            return "RUC no corresponde a una provincia válida";
        }
        
        return "OK";
    }
    
    /**
     * Validación básica de pasaporte
     */
    private String validarPasaporte(String pasaporte) {
        // Pasaporte debe tener entre 6 y 15 caracteres alfanuméricos
        if (!pasaporte.matches("^[A-Za-z0-9]{6,15}$")) {
            return "Pasaporte debe tener entre 6 y 15 caracteres alfanuméricos";
        }
        
        return "OK";
    }
    
    /**
     * Validación básica de email
     */
    private boolean esEmailValido(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    // ===== MÉTODOS DE UTILIDAD =====
    
    /**
     * Obtiene los tipos de documento disponibles
     */
    public String[] getTiposDocumento() {
        return new String[]{"CEDULA", "RUC", "PASAPORTE"};
    }
    
    /**
     * Obtiene los tipos de documento formateados para mostrar
     */
    public String[] getTiposDocumentoFormateados() {
        return new String[]{"Cédula", "RUC", "Pasaporte"};
    }
    
    /**
     * Convierte tipo de documento de formato display a base de datos
     */
    public String convertirTipoDocumentoADB(String tipoDisplay) {
        switch (tipoDisplay) {
            case "Cédula": return "CEDULA";
            case "RUC": return "RUC";
            case "Pasaporte": return "PASAPORTE";
            default: return tipoDisplay;
        }
    }
    
    /**
     * Convierte tipo de documento de base de datos a formato display
     */
    public String convertirTipoDocumentoADisplay(String tipoDB) {
        switch (tipoDB) {
            case "CEDULA": return "Cédula";
            case "RUC": return "RUC";
            case "PASAPORTE": return "Pasaporte";
            default: return tipoDB;
        }
    }
}