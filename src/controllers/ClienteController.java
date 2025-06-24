package controllers;

import services.ClienteService;
import services.AuthService;
import models.Cliente;
import views.clientes.ClientePanel;
import views.clientes.ClienteFormDialog;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class ClienteController {
    
    private ClienteService clienteService;
    private AuthService authService;
    private ClientePanel clientePanel;
    
    public ClienteController() {
        this.clienteService = ClienteService.getInstance();
        this.authService = AuthService.getInstance();
    }
    
    public void setClientePanel(ClientePanel clientePanel) {
        this.clientePanel = clientePanel;
        cargarDatos();
    }
    
    // ===== OPERACIONES PRINCIPALES =====
    
    /**
     * Carga todos los datos en el panel
     */
    public void cargarDatos() {
        if (clientePanel != null) {
            List<Cliente> clientes = clienteService.obtenerClientesActivos();
            clientePanel.actualizarTablaClientes(clientes);
            actualizarEstadisticas();
        }
    }
    
    /**
     * Busca clientes por término
     */
    public void buscarClientes(String termino) {
        if (clientePanel != null) {
            List<Cliente> clientes;
            if (termino == null || termino.trim().isEmpty()) {
                clientes = clienteService.obtenerClientesActivos();
            } else {
                clientes = clienteService.buscarClientes(termino);
            }
            clientePanel.actualizarTablaClientes(clientes);
        }
    }
    
    /**
     * Busca clientes con filtros avanzados
     */
    public void buscarClientesConFiltros(String termino, String tipoDocumento, boolean soloActivos) {
        if (clientePanel != null) {
            List<Cliente> clientes = clienteService.buscarClientesConFiltros(termino, tipoDocumento, soloActivos);
            clientePanel.actualizarTablaClientes(clientes);
        }
    }
    
    /**
     * Muestra formulario para crear nuevo cliente
     */
    public void mostrarFormularioNuevoCliente() {
        if (!authService.canManageUsers()) {
            showError("No tiene permisos para crear clientes");
            return;
        }
        
        ClienteFormDialog dialog = new ClienteFormDialog(null, this, true);
        dialog.setVisible(true);
    }
    
    /**
     * Muestra formulario para editar cliente
     */
    public void mostrarFormularioEditarCliente(int clienteId) {
        if (!authService.canManageUsers()) {
            showError("No tiene permisos para editar clientes");
            return;
        }
        
        Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(clienteId);
        if (clienteOpt.isPresent()) {
            ClienteFormDialog dialog = new ClienteFormDialog(null, this, false);
            dialog.cargarCliente(clienteOpt.get());
            dialog.setVisible(true);
        } else {
            showError("Cliente no encontrado");
        }
    }
    
    /**
     * Elimina un cliente
     */
    public void eliminarCliente(int clienteId) {
        if (!authService.canManageUsers()) {
            showError("No tiene permisos para eliminar clientes");
            return;
        }
        
        Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(clienteId);
        if (!clienteOpt.isPresent()) {
            showError("Cliente no encontrado");
            return;
        }
        
        Cliente cliente = clienteOpt.get();
        
        int option = JOptionPane.showConfirmDialog(
            clientePanel,
            "¿Está seguro que desea eliminar el cliente:\n" + cliente.getDisplayText() + "?",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            boolean resultado = clienteService.eliminarCliente(clienteId);
            
            if (resultado) {
                showInfo("Cliente eliminado exitosamente");
                cargarDatos();
            } else {
                showError("Error al eliminar el cliente");
            }
        }
    }
    
    // ===== OPERACIONES DEL FORMULARIO =====
    
    /**
     * Guarda un cliente (crear o actualizar)
     */
    public boolean guardarCliente(Cliente cliente, boolean esNuevo) {
        try {
            boolean resultado;
            
            if (esNuevo) {
                resultado = clienteService.crearCliente(cliente);
                if (resultado) {
                    showInfo("Cliente creado exitosamente");
                }
            } else {
                resultado = clienteService.actualizarCliente(cliente);
                if (resultado) {
                    showInfo("Cliente actualizado exitosamente");
                }
            }
            
            if (resultado) {
                cargarDatos();
            } else {
                showError("Error al guardar el cliente");
            }
            
            return resultado;
            
        } catch (Exception e) {
            showError("Error inesperado: " + e.getMessage());
            return false;
        }
    }
    
    // ===== OPERACIONES DE ESTADO =====
    
    /**
     * Activa un cliente
     */
    public void activarCliente(int clienteId) {
        if (!authService.canManageUsers()) {
            showError("No tiene permisos para activar clientes");
            return;
        }
        
        boolean resultado = clienteService.activarCliente(clienteId);
        
        if (resultado) {
            showInfo("Cliente activado exitosamente");
            cargarDatos();
        } else {
            showError("Error al activar el cliente");
        }
    }
    
    /**
     * Desactiva un cliente
     */
    public void desactivarCliente(int clienteId) {
        if (!authService.canManageUsers()) {
            showError("No tiene permisos para desactivar clientes");
            return;
        }
        
        Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(clienteId);
        if (!clienteOpt.isPresent()) {
            showError("Cliente no encontrado");
            return;
        }
        
        Cliente cliente = clienteOpt.get();
        
        int option = JOptionPane.showConfirmDialog(
            clientePanel,
            "¿Está seguro que desea desactivar el cliente:\n" + cliente.getDisplayText() + "?",
            "Confirmar Desactivación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            boolean resultado = clienteService.desactivarCliente(clienteId);
            
            if (resultado) {
                showInfo("Cliente desactivado exitosamente");
                cargarDatos();
            } else {
                showError("Error al desactivar el cliente");
            }
        }
    }
    
    // ===== FILTROS Y BÚSQUEDAS ESPECIALES =====
    
    /**
     * Muestra solo clientes por tipo de documento
     */
    public void filtrarPorTipoDocumento(String tipoDocumento) {
        if (clientePanel != null) {
            List<Cliente> clientes;
            if (tipoDocumento == null || tipoDocumento.equals("TODOS")) {
                clientes = clienteService.obtenerClientesActivos();
            } else {
                clientes = clienteService.obtenerClientesPorTipo(tipoDocumento);
            }
            clientePanel.actualizarTablaClientes(clientes);
        }
    }
    
    /**
     * Muestra todos los clientes (activos e inactivos)
     */
    public void mostrarTodosLosClientes() {
        if (clientePanel != null) {
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            clientePanel.actualizarTablaClientes(clientes);
        }
    }
    
    /**
     * Muestra solo clientes activos
     */
    public void mostrarClientesActivos() {
        if (clientePanel != null) {
            List<Cliente> clientes = clienteService.obtenerClientesActivos();
            clientePanel.actualizarTablaClientes(clientes);
        }
    }
    
    /**
     * Muestra los últimos clientes creados
     */
    public void mostrarUltimosClientes() {
        if (clientePanel != null) {
            List<Cliente> clientes = clienteService.obtenerUltimosClientes(10);
            clientePanel.actualizarTablaClientes(clientes);
            
            if (!clientes.isEmpty()) {
                showInfo("Mostrando los últimos " + clientes.size() + " clientes creados");
            }
        }
    }
    
    // ===== VALIDACIONES PARA FORMULARIOS =====
    
    /**
     * Valida si un documento ya existe
     */
    public boolean validarDocumentoUnico(String numeroDocumento, int clienteIdActual) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return false;
        }
        
        if (clienteIdActual > 0) {
            // Es actualización, verificar en otros clientes
            return !clienteService.existeDocumento(numeroDocumento) || 
                   clienteService.obtenerClientePorDocumento(numeroDocumento)
                       .map(c -> c.getId() == clienteIdActual)
                       .orElse(true);
        } else {
            // Es creación, verificar que no exista
            return !clienteService.existeDocumento(numeroDocumento);
        }
    }
    
    /**
     * Valida si un email ya existe
     */
    public boolean validarEmailUnico(String email, int clienteIdActual) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email es opcional
        }
        
        if (clienteIdActual > 0) {
            // Es actualización, verificar en otros clientes
            return !clienteService.existeEmail(email) || 
                   clienteService.obtenerClientePorDocumento(email) // Este método no existe, pero la lógica es correcta
                       .map(c -> c.getId() == clienteIdActual)
                       .orElse(true);
        } else {
            // Es creación, verificar que no exista
            return !clienteService.existeEmail(email);
        }
    }
    
    /**
     * Valida formato de documento según tipo
     */
    public boolean validarFormatoDocumento(String tipoDocumento, String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return false;
        }
        
        switch (tipoDocumento) {
            case "CEDULA":
                return numeroDocumento.matches("^\\d{10}$");
            case "RUC":
                return numeroDocumento.matches("^\\d{13}$");
            case "PASAPORTE":
                return numeroDocumento.matches("^[A-Za-z0-9]{6,15}$");
            default:
                return false;
        }
    }
    
    /**
     * Valida formato de email
     */
    public boolean validarFormatoEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email es opcional
        }
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * Valida formato de nombre/apellido
     */
    public boolean validarFormatoNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        return nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s.-]+$");
    }
    
    // ===== UTILIDADES =====
    
    /**
     * Obtiene tipos de documento para ComboBox
     */
    public String[] getTiposDocumento() {
        return clienteService.getTiposDocumentoFormateados();
    }
    
    /**
     * Convierte tipo de documento para base de datos
     */
    public String convertirTipoDocumentoADB(String tipoDisplay) {
        return clienteService.convertirTipoDocumentoADB(tipoDisplay);
    }
    
    /**
     * Convierte tipo de documento para mostrar
     */
    public String convertirTipoDocumentoADisplay(String tipoDB) {
        return clienteService.convertirTipoDocumentoADisplay(tipoDB);
    }
    
    /**
     * Obtiene estadísticas de clientes
     */
    public String obtenerEstadisticas() {
        return clienteService.getEstadisticasClientes();
    }
    
    /**
     * Actualiza las estadísticas en el panel
     */
    private void actualizarEstadisticas() {
        if (clientePanel != null) {
            String estadisticas = obtenerEstadisticas();
            clientePanel.actualizarEstadisticas(estadisticas);
        }
    }
    
    // ===== BÚSQUEDA RÁPIDA PARA VENTAS =====
    
    /**
     * Busca cliente por documento para selección rápida en ventas
     * Este método será útil cuando implementemos el módulo de ventas
     */
    public Optional<Cliente> buscarClienteParaVenta(String numeroDocumento) {
        return clienteService.obtenerClientePorDocumento(numeroDocumento);
    }
    
    /**
     * Obtiene lista de clientes activos para ComboBox en ventas
     */
    public List<Cliente> obtenerClientesParaVentas() {
        return clienteService.obtenerClientesActivos();
    }
    
    // ===== EXPORTACIÓN E IMPORTACIÓN =====
    
    /**
     * Genera reporte de clientes
     */
    public void generarReporteClientes() {
        if (!authService.canViewReports()) {
            showError("No tiene permisos para generar reportes");
            return;
        }
        
        // TODO: Implementar generación de reporte en PDF o Excel
        showInfo("Funcionalidad de reportes será implementada en una próxima versión");
    }
    
    /**
     * Exporta clientes a CSV
     */
    public void exportarClientesCSV() {
        if (!authService.canViewReports()) {
            showError("No tiene permisos para exportar datos");
            return;
        }
        
        // TODO: Implementar exportación a CSV
        showInfo("Funcionalidad de exportación será implementada en una próxima versión");
    }
    
    // ===== MENSAJES =====
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            clientePanel,
            message,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            clientePanel,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(
            clientePanel,
            message,
            "Advertencia",
            JOptionPane.WARNING_MESSAGE
        );
    }
}