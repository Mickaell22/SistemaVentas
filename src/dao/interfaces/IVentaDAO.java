package dao.interfaces;

import models.Venta;
import models.DetalleVenta;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IVentaDAO {
    
    // ===== OPERACIONES CRUD BÁSICAS =====
    
    /**
     * Crea una nueva venta con sus detalles
     * @param venta La venta a crear (con lista de detalles)
     * @return true si se creó exitosamente, false en caso contrario
     */
    boolean crear(Venta venta);
    
    /**
     * Crea solo la cabecera de venta (sin detalles)
     * @param venta La venta a crear
     * @return true si se creó exitosamente, false en caso contrario
     */
    boolean crearSoloCabecera(Venta venta);
    
    /**
     * Obtiene una venta por su ID con todos sus detalles
     * @param id El ID de la venta
     * @return Optional con la venta si existe, Optional.empty() si no
     */
    Optional<Venta> obtenerPorId(int id);
    
    /**
     * Obtiene una venta por su número de factura
     * @param numeroFactura El número de factura
     * @return Optional con la venta si existe, Optional.empty() si no
     */
    Optional<Venta> obtenerPorNumeroFactura(String numeroFactura);
    
    /**
     * Actualiza una venta existente (solo cabecera)
     * @param venta La venta con los datos actualizados
     * @return true si se actualizó exitosamente, false en caso contrario
     */
    boolean actualizar(Venta venta);
    
    /**
     * Elimina una venta (cambio de estado, no eliminación física)
     * @param id El ID de la venta a eliminar
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    boolean eliminar(int id);
    
    /**
     * Obtiene todas las ventas
     * @return Lista de todas las ventas
     */
    List<Venta> obtenerTodas();
    
    // ===== OPERACIONES CON DETALLES =====
    
    /**
     * Agrega un detalle a una venta existente
     * @param detalle El detalle a agregar
     * @return true si se agregó exitosamente, false en caso contrario
     */
    boolean agregarDetalle(DetalleVenta detalle);
    
    /**
     * Actualiza un detalle de venta
     * @param detalle El detalle con los datos actualizados
     * @return true si se actualizó exitosamente, false en caso contrario
     */
    boolean actualizarDetalle(DetalleVenta detalle);
    
    /**
     * Elimina un detalle de venta
     * @param detalleId El ID del detalle a eliminar
     * @return true si se eliminó exitosamente, false en caso contrario
     */
    boolean eliminarDetalle(int detalleId);
    
    /**
     * Obtiene todos los detalles de una venta
     * @param ventaId El ID de la venta
     * @return Lista de detalles de la venta
     */
    List<DetalleVenta> obtenerDetallesPorVenta(int ventaId);
    
    /**
     * Elimina todos los detalles de una venta
     * @param ventaId El ID de la venta
     * @return true si se eliminaron exitosamente, false en caso contrario
     */
    boolean eliminarDetallesPorVenta(int ventaId);
    
    // ===== BÚSQUEDAS Y FILTROS =====
    
    /**
     * Obtiene ventas por cliente
     * @param clienteId El ID del cliente
     * @return Lista de ventas del cliente
     */
    List<Venta> obtenerPorCliente(int clienteId);
    
    /**
     * Obtiene ventas por usuario vendedor
     * @param usuarioId El ID del usuario vendedor
     * @return Lista de ventas del usuario
     */
    List<Venta> obtenerPorUsuario(int usuarioId);
    
    /**
     * Obtiene ventas por estado
     * @param estado El estado de las ventas (PENDIENTE, COMPLETADA, CANCELADA)
     * @return Lista de ventas con el estado especificado
     */
    List<Venta> obtenerPorEstado(String estado);
    
    /**
     * Obtiene ventas por método de pago
     * @param metodoPago El método de pago (EFECTIVO, TARJETA, TRANSFERENCIA)
     * @return Lista de ventas con el método de pago especificado
     */
    List<Venta> obtenerPorMetodoPago(String metodoPago);
    
    /**
     * Obtiene ventas en un rango de fechas
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de ventas en el rango especificado
     */
    List<Venta> obtenerPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Obtiene ventas de un día específico
     * @param fecha La fecha a consultar
     * @return Lista de ventas del día especificado
     */
    List<Venta> obtenerPorFecha(LocalDate fecha);
    
    /**
     * Busca ventas por número de factura (búsqueda parcial)
     * @param numero Parte del número de factura a buscar
     * @return Lista de ventas que coinciden con el criterio
     */
    List<Venta> buscarPorNumeroFactura(String numero);
    
    /**
     * Busca ventas por nombre o documento de cliente
     * @param clienteInfo Información del cliente a buscar
     * @return Lista de ventas que coinciden con el criterio
     */
    List<Venta> buscarPorCliente(String clienteInfo);
    
    // ===== CONSULTAS AVANZADAS =====
    
    /**
     * Obtiene ventas con filtros múltiples
     * @param clienteId ID del cliente (null para todos)
     * @param usuarioId ID del usuario (null para todos)
     * @param estado Estado de la venta (null para todos)
     * @param metodoPago Método de pago (null para todos)
     * @param fechaInicio Fecha de inicio (null sin límite)
     * @param fechaFin Fecha de fin (null sin límite)
     * @return Lista de ventas que coinciden con los filtros
     */
    List<Venta> obtenerConFiltros(Integer clienteId, Integer usuarioId, String estado, 
                                  String metodoPago, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Obtiene las últimas ventas realizadas
     * @param limite Número máximo de ventas a retornar
     * @return Lista de las últimas ventas ordenadas por fecha descendente
     */
    List<Venta> obtenerUltimasVentas(int limite);
    
    /**
     * Obtiene ventas por rango de total
     * @param totalMinimo Total mínimo (inclusive)
     * @param totalMaximo Total máximo (inclusive)
     * @return Lista de ventas en el rango de total especificado
     */
    List<Venta> obtenerPorRangoTotal(BigDecimal totalMinimo, BigDecimal totalMaximo);
    
    /**
     * Obtiene productos más vendidos en un período
     * @param fechaInicio Fecha de inicio del período
     * @param fechaFin Fecha de fin del período
     * @param limite Número máximo de productos a retornar
     * @return Lista de objetos con información de productos vendidos
     */
    List<Object[]> obtenerProductosMasVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite);
    
    /**
     * Obtiene clientes que más compran
     * @param limite Número máximo de clientes a retornar
     * @return Lista de objetos con información de clientes
     */
    List<Object[]> obtenerClientesMasCompradores(int limite);
    
    // ===== OPERACIONES DE ESTADO =====
    
    /**
     * Cambia el estado de una venta
     * @param ventaId ID de la venta
     * @param nuevoEstado Nuevo estado (PENDIENTE, COMPLETADA, CANCELADA)
     * @return true si se cambió exitosamente, false en caso contrario
     */
    boolean cambiarEstado(int ventaId, String nuevoEstado);
    
    /**
     * Completa una venta (cambia estado a COMPLETADA)
     * @param ventaId ID de la venta
     * @return true si se completó exitosamente, false en caso contrario
     */
    boolean completarVenta(int ventaId);
    
    /**
     * Cancela una venta (cambia estado a CANCELADA)
     * @param ventaId ID de la venta
     * @return true si se canceló exitosamente, false en caso contrario
     */
    boolean cancelarVenta(int ventaId);
    
    // ===== VALIDACIONES =====
    
    /**
     * Verifica si existe una venta con el número de factura dado
     * @param numeroFactura El número de factura a verificar
     * @return true si existe, false si no
     */
    boolean existeNumeroFactura(String numeroFactura);
    
    /**
     * Verifica si una venta tiene detalles
     * @param ventaId ID de la venta
     * @return true si tiene detalles, false si no
     */
    boolean tieneDetalles(int ventaId);
    
    /**
     * Verifica si hay stock suficiente para los productos de una venta
     * @param ventaId ID de la venta a verificar
     * @return true si hay stock suficiente, false si no
     */
    boolean verificarStockSuficiente(int ventaId);
    
    // ===== ESTADÍSTICAS Y CONTEOS =====
    
    /**
     * Cuenta el total de ventas
     * @return Número total de ventas
     */
    int contarVentas();
    
    /**
     * Cuenta ventas por estado
     * @param estado El estado a contar
     * @return Número de ventas con el estado especificado
     */
    int contarVentasPorEstado(String estado);
    
    /**
     * Cuenta ventas de un día específico
     * @param fecha La fecha a contar
     * @return Número de ventas del día
     */
    int contarVentasPorFecha(LocalDate fecha);
    
    /**
     * Obtiene el total de ventas en un período
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Total monetario de ventas en el período
     */
    BigDecimal obtenerTotalVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Obtiene el total de ventas del día actual
     * @return Total monetario de ventas del día
     */
    BigDecimal obtenerTotalVentasHoy();
    
    /**
     * Obtiene la venta promedio en un período
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Promedio monetario de ventas
     */
    BigDecimal obtenerPromedioVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    /**
     * Obtiene estadísticas de ventas por método de pago
     * @param fechaInicio Fecha de inicio (null para todos los tiempos)
     * @param fechaFin Fecha de fin (null para todos los tiempos)
     * @return Lista de objetos con estadísticas por método de pago
     */
    List<Object[]> obtenerEstadisticasPorMetodoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // ===== GENERACIÓN DE NÚMEROS =====
    
    /**
     * Genera el siguiente número de factura disponible
     * @return Nuevo número de factura único
     */
    String generarNumeroFactura();
    
    /**
     * Obtiene el último número de factura generado
     * @return Último número de factura o null si no hay ventas
     */
    String obtenerUltimoNumeroFactura();
    
    // ===== OPERACIONES DE ACTUALIZACIÓN MASIVA =====
    
    /**
     * Actualiza el stock de productos basado en una venta completada
     * @param ventaId ID de la venta
     * @return true si se actualizó exitosamente, false en caso contrario
     */
    boolean actualizarStockPorVenta(int ventaId);
    
    /**
     * Restaura el stock de productos al cancelar una venta
     * @param ventaId ID de la venta cancelada
     * @return true si se restauró exitosamente, false en caso contrario
     */
    boolean restaurarStockPorVenta(int ventaId);
    
    /**
     * Recalcula los totales de una venta basado en sus detalles
     * @param ventaId ID de la venta
     * @return true si se recalculó exitosamente, false en caso contrario
     */
    boolean recalcularTotales(int ventaId);
}