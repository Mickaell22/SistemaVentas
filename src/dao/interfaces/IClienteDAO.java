package dao.interfaces;

import models.Cliente;
import java.util.List;
import java.util.Optional;

public interface IClienteDAO {
    
    // ===== OPERACIONES CRUD BÁSICAS =====
    
    /**
     * Crea un nuevo cliente en la base de datos
     * @param cliente El cliente a crear
     * @return true si se creó exitosamente, false en caso contrario
     */
    boolean crear(Cliente cliente);
    
    /**
     * Obtiene un cliente por su ID
     * @param id El ID del cliente
     * @return Optional con el cliente si existe, Optional.empty() si no
     */
    Optional<Cliente> obtenerPorId(int id);
    
    /**
     * Actualiza un cliente existente
     * @param cliente El cliente con los datos actualizados
     * @return true si se actualizó exitosamente, false en caso contrario
     */
    boolean actualizar(Cliente cliente);
    
    /**
     * Elimina (desactiva) un cliente
     * @param id El ID del cliente a eliminar
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    boolean eliminar(int id);
    
    /**
     * Obtiene todos los clientes
     * @return Lista de todos los clientes
     */
    List<Cliente> obtenerTodos();
    
    // ===== BÚSQUEDAS ESPECÍFICAS =====
    
    /**
     * Obtiene un cliente por su número de documento
     * @param numeroDocumento El número de documento del cliente
     * @return Optional con el cliente si existe, Optional.empty() si no
     */
    Optional<Cliente> obtenerPorDocumento(String numeroDocumento);
    
    /**
     * Obtiene un cliente por su email
     * @param email El email del cliente
     * @return Optional con el cliente si existe, Optional.empty() si no
     */
    Optional<Cliente> obtenerPorEmail(String email);
    
    /**
     * Busca clientes por nombre (búsqueda parcial)
     * @param nombre El nombre o parte del nombre a buscar
     * @return Lista de clientes que coinciden con el criterio
     */
    List<Cliente> buscarPorNombre(String nombre);
    
    /**
     * Busca clientes por apellido (búsqueda parcial)
     * @param apellido El apellido o parte del apellido a buscar
     * @return Lista de clientes que coinciden con el criterio
     */
    List<Cliente> buscarPorApellido(String apellido);
    
    /**
     * Busca clientes por nombre completo (nombre y/o apellido)
     * @param termino El término de búsqueda
     * @return Lista de clientes que coinciden con el criterio
     */
    List<Cliente> buscarPorNombreCompleto(String termino);
    
    /**
     * Obtiene clientes por tipo de documento
     * @param tipoDocumento El tipo de documento (CEDULA, RUC, PASAPORTE)
     * @return Lista de clientes con ese tipo de documento
     */
    List<Cliente> obtenerPorTipoDocumento(String tipoDocumento);
    
    // ===== OPERACIONES DE ESTADO =====
    
    /**
     * Obtiene solo los clientes activos
     * @return Lista de clientes activos
     */
    List<Cliente> obtenerActivos();
    
    /**
     * Obtiene solo los clientes inactivos
     * @return Lista de clientes inactivos
     */
    List<Cliente> obtenerInactivos();
    
    /**
     * Activa un cliente
     * @param id El ID del cliente a activar
     * @return true si se activó exitosamente, false en caso contrario
     */
    boolean activar(int id);
    
    /**
     * Desactiva un cliente
     * @param id El ID del cliente a desactivar
     * @return true si se desactivó exitosamente, false en caso contrario
     */
    boolean desactivar(int id);
    
    // ===== VALIDACIONES =====
    
    /**
     * Verifica si existe un cliente con el número de documento dado
     * @param numeroDocumento El número de documento a verificar
     * @return true si existe, false si no
     */
    boolean existeDocumento(String numeroDocumento);
    
    /**
     * Verifica si existe un cliente con el email dado
     * @param email El email a verificar
     * @return true si existe, false si no
     */
    boolean existeEmail(String email);
    
    /**
     * Verifica si existe un documento para otro cliente (excluyendo uno específico)
     * @param numeroDocumento El número de documento a verificar
     * @param clienteId El ID del cliente a excluir de la verificación
     * @return true si existe en otro cliente, false si no
     */
    boolean existeDocumentoEnOtroCliente(String numeroDocumento, int clienteId);
    
    /**
     * Verifica si existe un email para otro cliente (excluyendo uno específico)
     * @param email El email a verificar
     * @param clienteId El ID del cliente a excluir de la verificación
     * @return true si existe en otro cliente, false si no
     */
    boolean existeEmailEnOtroCliente(String email, int clienteId);
    
    // ===== ESTADÍSTICAS Y CONTEOS =====
    
    /**
     * Cuenta el total de clientes
     * @return Número total de clientes
     */
    int contarClientes();
    
    /**
     * Cuenta el total de clientes activos
     * @return Número total de clientes activos
     */
    int contarClientesActivos();
    
    /**
     * Cuenta el total de clientes inactivos
     * @return Número total de clientes inactivos
     */
    int contarClientesInactivos();
    
    /**
     * Cuenta clientes por tipo de documento
     * @param tipoDocumento El tipo de documento
     * @return Número de clientes con ese tipo de documento
     */
    int contarPorTipoDocumento(String tipoDocumento);
    
    // ===== BÚSQUEDAS AVANZADAS =====
    
    /**
     * Busca clientes con múltiples criterios
     * @param termino Término de búsqueda (se busca en nombre, apellido, documento, email)
     * @param tipoDocumento Filtro por tipo de documento (null para todos)
     * @param soloActivos true para solo activos, false para todos
     * @return Lista de clientes que coinciden con los criterios
     */
    List<Cliente> buscarConFiltros(String termino, String tipoDocumento, boolean soloActivos);
    
    /**
     * Obtiene los últimos clientes creados
     * @param limite Número máximo de clientes a retornar
     * @return Lista de los últimos clientes creados
     */
    List<Cliente> obtenerUltimosCreados(int limite);
}