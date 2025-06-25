package dao.impl;

import dao.interfaces.IVentaDAO;
import models.Venta;
import models.DetalleVenta;
import config.DatabaseConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaDAOImpl implements IVentaDAO {

    private Connection connection;

    public VentaDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ===== OPERACIONES CRUD BÁSICAS =====

    @Override
    public boolean crear(Venta venta) {
        try {
            System.out.println("=== DEBUG CREAR VENTA ===");
            System.out.println("Creando venta para cliente ID: " + venta.getClienteId());
            System.out.println("Número de factura: " + venta.getNumeroFactura());
            System.out
                    .println("Cantidad de detalles: " + (venta.getDetalles() != null ? venta.getDetalles().size() : 0));

            // Generar número de factura si no tiene
            if (venta.getNumeroFactura() == null || venta.getNumeroFactura().isEmpty()) {
                venta.setNumeroFactura(generarNumeroFactura());
            }

            // 1. Crear cabecera PRIMERO
            if (!crearSoloCabecera(venta)) {
                System.err.println("ERROR: No se pudo crear la cabecera de venta");
                return false;
            }

            System.out.println("✅ Cabecera creada con ID: " + venta.getId());

            // 2. Ahora que tenemos el ID de la venta, crear los detalles
            if (venta.getDetalles() != null && !venta.getDetalles().isEmpty()) {
                System.out.println("Insertando " + venta.getDetalles().size() + " detalles...");

                for (int i = 0; i < venta.getDetalles().size(); i++) {
                    DetalleVenta detalle = venta.getDetalles().get(i);

                    // CRÍTICO: Asignar el ID de la venta al detalle
                    detalle.setVentaId(venta.getId());

                    System.out.printf("Insertando detalle %d: VentaID=%d, ProductoID=%d, Cantidad=%d%n",
                            i + 1, detalle.getVentaId(), detalle.getProductoId(), detalle.getCantidad());

                    if (!agregarDetalle(detalle)) {
                        System.err.println("ERROR: Falló insertar detalle " + (i + 1));
                        connection.rollback();
                        return false;
                    }

                    System.out.println("✅ Detalle " + (i + 1) + " insertado con ID: " + detalle.getId());
                }
            }

            // 3. Recalcular totales
            System.out.println("Recalculando totales...");
            recalcularTotales(venta.getId());

            // 4. Si está completada, actualizar stock
            if ("COMPLETADA".equals(venta.getEstado())) {
                System.out.println("Actualizando stock...");
                if (!actualizarStockPorVenta(venta.getId())) {
                    System.err.println("ERROR: No se pudo actualizar el stock");
                    connection.rollback();
                    return false;
                }
            }

            // 5. Commit final
            connection.commit();
            System.out.println("✅ VENTA CREADA EXITOSAMENTE: " + venta.getNumeroFactura());
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error SQL al crear venta: " + e.getMessage());
            System.err.println("Código error: " + e.getErrorCode());
            System.err.println("Estado SQL: " + e.getSQLState());
            e.printStackTrace();

            try {
                connection.rollback();
                System.out.println("Rollback ejecutado");
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error general al crear venta: " + e.getMessage());
            e.printStackTrace();

            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
            return false;
        }
    }

    @Override
    public boolean crearSoloCabecera(Venta venta) {
        String sql = "INSERT INTO ventas (numero_factura, cliente_id, usuario_id, fecha_venta, " +
                "subtotal, descuento, impuestos, total, estado, metodo_pago, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, venta.getNumeroFactura());
            stmt.setInt(2, venta.getClienteId());
            stmt.setInt(3, venta.getUsuarioId());
            stmt.setTimestamp(4, Timestamp.valueOf(venta.getFechaVenta()));
            stmt.setBigDecimal(5, venta.getSubtotal());
            stmt.setBigDecimal(6, venta.getDescuento());
            stmt.setBigDecimal(7, venta.getImpuestos());
            stmt.setBigDecimal(8, venta.getTotal());
            stmt.setString(9, venta.getEstado());
            stmt.setString(10, venta.getMetodoPago());
            stmt.setString(11, venta.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    venta.setId(rs.getInt(1));
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al crear cabecera de venta: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<Venta> obtenerPorId(int id) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Venta venta = mapearVenta(rs);

                // Cargar detalles
                venta.setDetalles(obtenerDetallesPorVenta(id));

                return Optional.of(venta);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener venta por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Venta> obtenerPorNumeroFactura(String numeroFactura) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.numero_factura = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroFactura);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Venta venta = mapearVenta(rs);
                venta.setDetalles(obtenerDetallesPorVenta(venta.getId()));
                return Optional.of(venta);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener venta por número de factura: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean actualizar(Venta venta) {
        String sql = "UPDATE ventas SET numero_factura = ?, cliente_id = ?, usuario_id = ?, " +
                "fecha_venta = ?, subtotal = ?, descuento = ?, impuestos = ?, total = ?, " +
                "estado = ?, metodo_pago = ?, observaciones = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, venta.getNumeroFactura());
            stmt.setInt(2, venta.getClienteId());
            stmt.setInt(3, venta.getUsuarioId());
            stmt.setTimestamp(4, Timestamp.valueOf(venta.getFechaVenta()));
            stmt.setBigDecimal(5, venta.getSubtotal());
            stmt.setBigDecimal(6, venta.getDescuento());
            stmt.setBigDecimal(7, venta.getImpuestos());
            stmt.setBigDecimal(8, venta.getTotal());
            stmt.setString(9, venta.getEstado());
            stmt.setString(10, venta.getMetodoPago());
            stmt.setString(11, venta.getObservaciones());
            stmt.setInt(12, venta.getId());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al actualizar venta: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean eliminar(int id) {
        // Eliminación lógica: cambiar estado a CANCELADA
        return cancelarVenta(id);
    }

    @Override
    public List<Venta> obtenerTodas() {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "ORDER BY v.fecha_venta DESC";

        return ejecutarConsultaVentas(sql);
    }

    // ===== OPERACIONES CON DETALLES =====

    @Override
    public boolean agregarDetalle(DetalleVenta detalle) {
        String sql = "INSERT INTO detalle_venta (venta_id, producto_id, cantidad, precio_unitario, " +
                "descuento, subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, detalle.getVentaId());
            stmt.setInt(2, detalle.getProductoId());
            stmt.setInt(3, detalle.getCantidad());
            stmt.setBigDecimal(4, detalle.getPrecioUnitario());
            stmt.setBigDecimal(5, detalle.getDescuento());
            stmt.setBigDecimal(6, detalle.getSubtotal());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    detalle.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al agregar detalle de venta: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean actualizarDetalle(DetalleVenta detalle) {
        String sql = "UPDATE detalle_venta SET producto_id = ?, cantidad = ?, precio_unitario = ?, " +
                "descuento = ?, subtotal = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, detalle.getProductoId());
            stmt.setInt(2, detalle.getCantidad());
            stmt.setBigDecimal(3, detalle.getPrecioUnitario());
            stmt.setBigDecimal(4, detalle.getDescuento());
            stmt.setBigDecimal(5, detalle.getSubtotal());
            stmt.setInt(6, detalle.getId());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar detalle de venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminarDetalle(int detalleId) {
        String sql = "DELETE FROM detalle_venta WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, detalleId);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar detalle de venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<DetalleVenta> obtenerDetallesPorVenta(int ventaId) {
        String sql = "SELECT dv.*, " +
                "p.codigo as producto_codigo, p.nombre as producto_nombre, " +
                "p.unidad_medida as producto_unidad_medida, p.stock_actual as producto_stock_actual " +
                "FROM detalle_venta dv " +
                "LEFT JOIN productos p ON dv.producto_id = p.id " +
                "WHERE dv.venta_id = ? " +
                "ORDER BY dv.id";

        List<DetalleVenta> detalles = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                detalles.add(mapearDetalleVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener detalles de venta: " + e.getMessage());
        }

        return detalles;
    }

    @Override
    public boolean eliminarDetallesPorVenta(int ventaId) {
        String sql = "DELETE FROM detalle_venta WHERE venta_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al eliminar detalles de venta: " + e.getMessage());
            return false;
        }
    }

    // ===== BÚSQUEDAS Y FILTROS =====

    @Override
    public List<Venta> obtenerPorCliente(int clienteId) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.cliente_id = ? " +
                "ORDER BY v.fecha_venta DESC";

        return ejecutarConsultaVentasConParametro(sql, clienteId);
    }

    @Override
    public List<Venta> obtenerPorUsuario(int usuarioId) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.usuario_id = ? " +
                "ORDER BY v.fecha_venta DESC";

        return ejecutarConsultaVentasConParametro(sql, usuarioId);
    }

    @Override
    public List<Venta> obtenerPorEstado(String estado) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.estado = ? " +
                "ORDER BY v.fecha_venta DESC";

        return ejecutarConsultaVentasConParametroString(sql, estado);
    }

    @Override
    public List<Venta> obtenerPorMetodoPago(String metodoPago) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.metodo_pago = ? " +
                "ORDER BY v.fecha_venta DESC";

        return ejecutarConsultaVentasConParametroString(sql, metodoPago);
    }

    @Override
    public List<Venta> obtenerPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.fecha_venta BETWEEN ? AND ? " +
                "ORDER BY v.fecha_venta DESC";

        List<Venta> ventas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por rango de fechas: " + e.getMessage());
        }

        return ventas;
    }

    @Override
    public List<Venta> obtenerPorFecha(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);
        return obtenerPorRangoFechas(inicio, fin);
    }

    @Override
    public List<Venta> buscarPorNumeroFactura(String numero) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.numero_factura LIKE ? " +
                "ORDER BY v.fecha_venta DESC";

        return ejecutarConsultaVentasConParametroString(sql, "%" + numero + "%");
    }

    @Override
    public List<Venta> buscarPorCliente(String clienteInfo) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE c.nombre LIKE ? OR c.numero_documento LIKE ? " +
                "ORDER BY v.fecha_venta DESC";

        List<Venta> ventas = new ArrayList<>();
        String patron = "%" + clienteInfo + "%";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patron);
            stmt.setString(2, patron);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar ventas por cliente: " + e.getMessage());
        }

        return ventas;
    }

    // ===== CONSULTAS AVANZADAS =====

    @Override
    public List<Venta> obtenerConFiltros(Integer clienteId, Integer usuarioId, String estado,
            String metodoPago, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        StringBuilder sql = new StringBuilder("SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE 1=1");

        List<Object> parametros = new ArrayList<>();

        if (clienteId != null && clienteId > 0) {
            sql.append(" AND v.cliente_id = ?");
            parametros.add(clienteId);
        }

        if (usuarioId != null && usuarioId > 0) {
            sql.append(" AND v.usuario_id = ?");
            parametros.add(usuarioId);
        }

        if (estado != null && !estado.trim().isEmpty()) {
            sql.append(" AND v.estado = ?");
            parametros.add(estado);
        }

        if (metodoPago != null && !metodoPago.trim().isEmpty()) {
            sql.append(" AND v.metodo_pago = ?");
            parametros.add(metodoPago);
        }

        if (fechaInicio != null) {
            sql.append(" AND v.fecha_venta >= ?");
            parametros.add(Timestamp.valueOf(fechaInicio));
        }

        if (fechaFin != null) {
            sql.append(" AND v.fecha_venta <= ?");
            parametros.add(Timestamp.valueOf(fechaFin));
        }

        sql.append(" ORDER BY v.fecha_venta DESC");

        List<Venta> ventas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener ventas con filtros: " + e.getMessage());
        }

        return ventas;
    }

    @Override
    public List<Venta> obtenerUltimasVentas(int limite) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "ORDER BY v.fecha_venta DESC LIMIT ?";

        return ejecutarConsultaVentasConParametro(sql, limite);
    }

    @Override
    public List<Venta> obtenerPorRangoTotal(BigDecimal totalMinimo, BigDecimal totalMaximo) {
        String sql = "SELECT v.*, " +
                "c.nombre as cliente_nombre, c.numero_documento as cliente_documento, " +
                "CONCAT(u.nombre, ' ', u.apellido) as usuario_nombre " +
                "FROM ventas v " +
                "LEFT JOIN clientes c ON v.cliente_id = c.id " +
                "LEFT JOIN usuarios u ON v.usuario_id = u.id " +
                "WHERE v.total BETWEEN ? AND ? " +
                "ORDER BY v.fecha_venta DESC";

        List<Venta> ventas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, totalMinimo);
            stmt.setBigDecimal(2, totalMaximo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener ventas por rango de total: " + e.getMessage());
        }

        return ventas;
    }

    @Override
    public List<Object[]> obtenerProductosMasVendidos(LocalDateTime fechaInicio, LocalDateTime fechaFin, int limite) {
        String sql = "SELECT p.codigo, p.nombre, SUM(dv.cantidad) as total_vendido, " +
                "SUM(dv.subtotal) as total_ingresos " +
                "FROM detalle_venta dv " +
                "INNER JOIN productos p ON dv.producto_id = p.id " +
                "INNER JOIN ventas v ON dv.venta_id = v.id " +
                "WHERE v.estado = 'COMPLETADA' " +
                "AND v.fecha_venta BETWEEN ? AND ? " +
                "GROUP BY p.id, p.codigo, p.nombre " +
                "ORDER BY total_vendido DESC " +
                "LIMIT ?";

        List<Object[]> resultados = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            stmt.setInt(3, limite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] fila = {
                        rs.getString("codigo"),
                        rs.getString("nombre"),
                        rs.getInt("total_vendido"),
                        rs.getBigDecimal("total_ingresos")
                };
                resultados.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener productos más vendidos: " + e.getMessage());
        }

        return resultados;
    }

    @Override
    public List<Object[]> obtenerClientesMasCompradores(int limite) {
        String sql = "SELECT c.numero_documento, c.nombre, COUNT(v.id) as total_compras, " +
                "SUM(v.total) as total_gastado " +
                "FROM ventas v " +
                "INNER JOIN clientes c ON v.cliente_id = c.id " +
                "WHERE v.estado = 'COMPLETADA' " +
                "GROUP BY c.id, c.numero_documento, c.nombre " +
                "ORDER BY total_gastado DESC " +
                "LIMIT ?";

        List<Object[]> resultados = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] fila = {
                        rs.getString("numero_documento"),
                        rs.getString("nombre"),
                        rs.getInt("total_compras"),
                        rs.getBigDecimal("total_gastado")
                };
                resultados.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener clientes más compradores: " + e.getMessage());
        }

        return resultados;
    }

    // ===== OPERACIONES DE ESTADO =====

    @Override
    public boolean cambiarEstado(int ventaId, String nuevoEstado) {
        String sql = "UPDATE ventas SET estado = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, ventaId);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                connection.commit();
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado de venta: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean completarVenta(int ventaId) {
        try {
            // Verificar stock antes de completar
            if (!verificarStockSuficiente(ventaId)) {
                System.err.println("No hay stock suficiente para completar la venta");
                return false;
            }

            // Cambiar estado a COMPLETADA
            if (!cambiarEstado(ventaId, "COMPLETADA")) {
                return false;
            }

            // Actualizar stock
            if (!actualizarStockPorVenta(ventaId)) {
                // Si falla, revertir estado
                cambiarEstado(ventaId, "PENDIENTE");
                return false;
            }

            connection.commit();
            return true;

        } catch (Exception e) {
            System.err.println("Error al completar venta: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
            return false;
        }
    }

    @Override
    public boolean cancelarVenta(int ventaId) {
        try {
            // Obtener estado actual
            Optional<Venta> ventaOpt = obtenerPorId(ventaId);
            if (!ventaOpt.isPresent()) {
                return false;
            }

            String estadoAnterior = ventaOpt.get().getEstado();

            // Cambiar estado a CANCELADA
            if (!cambiarEstado(ventaId, "CANCELADA")) {
                return false;
            }

            // Si estaba completada, restaurar stock
            if ("COMPLETADA".equals(estadoAnterior)) {
                if (!restaurarStockPorVenta(ventaId)) {
                    // Si falla, revertir estado
                    cambiarEstado(ventaId, estadoAnterior);
                    return false;
                }
            }

            connection.commit();
            return true;

        } catch (Exception e) {
            System.err.println("Error al cancelar venta: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error en rollback: " + rollbackEx.getMessage());
            }
            return false;
        }
    }

    // ===== VALIDACIONES =====

    @Override
    public boolean existeNumeroFactura(String numeroFactura) {
        String sql = "SELECT COUNT(*) FROM ventas WHERE numero_factura = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, numeroFactura);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar número de factura: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean tieneDetalles(int ventaId) {
        String sql = "SELECT COUNT(*) FROM detalle_venta WHERE venta_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al verificar detalles de venta: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean verificarStockSuficiente(int ventaId) {
        String sql = "SELECT dv.producto_id, dv.cantidad, p.stock_actual " +
                "FROM detalle_venta dv " +
                "INNER JOIN productos p ON dv.producto_id = p.id " +
                "WHERE dv.venta_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int cantidadRequerida = rs.getInt("cantidad");
                int stockActual = rs.getInt("stock_actual");

                if (cantidadRequerida > stockActual) {
                    return false;
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error al verificar stock suficiente: " + e.getMessage());
            return false;
        }
    }

    // ===== ESTADÍSTICAS Y CONTEOS =====

    @Override
    public int contarVentas() {
        return ejecutarConteo("SELECT COUNT(*) FROM ventas");
    }

    @Override
    public int contarVentasPorEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM ventas WHERE estado = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, estado);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al contar ventas por estado: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public int contarVentasPorFecha(LocalDate fecha) {
        String sql = "SELECT COUNT(*) FROM ventas WHERE DATE(fecha_venta) = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(fecha));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al contar ventas por fecha: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public BigDecimal obtenerTotalVentasPorPeriodo(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas " +
                "WHERE estado = 'COMPLETADA' AND fecha_venta BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener total de ventas por período: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal obtenerTotalVentasHoy() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.atTime(23, 59, 59);
        return obtenerTotalVentasPorPeriodo(inicio, fin);
    }

    @Override
    public BigDecimal obtenerPromedioVentas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = "SELECT COALESCE(AVG(total), 0) FROM ventas " +
                "WHERE estado = 'COMPLETADA' AND fecha_venta BETWEEN ? AND ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal(1);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener promedio de ventas: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public List<Object[]> obtenerEstadisticasPorMetodoPago(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        StringBuilder sql = new StringBuilder(
                "SELECT metodo_pago, COUNT(*) as cantidad, SUM(total) as total_ingresos " +
                        "FROM ventas WHERE estado = 'COMPLETADA'");

        List<Object> parametros = new ArrayList<>();

        if (fechaInicio != null) {
            sql.append(" AND fecha_venta >= ?");
            parametros.add(Timestamp.valueOf(fechaInicio));
        }

        if (fechaFin != null) {
            sql.append(" AND fecha_venta <= ?");
            parametros.add(Timestamp.valueOf(fechaFin));
        }

        sql.append(" GROUP BY metodo_pago ORDER BY total_ingresos DESC");

        List<Object[]> resultados = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] fila = {
                        rs.getString("metodo_pago"),
                        rs.getInt("cantidad"),
                        rs.getBigDecimal("total_ingresos")
                };
                resultados.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas por método de pago: " + e.getMessage());
        }

        return resultados;
    }

    // ===== GENERACIÓN DE NÚMEROS =====

    @Override
    public String generarNumeroFactura() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(numero_factura, 5) AS UNSIGNED)), 0) + 1 " +
                "FROM ventas WHERE numero_factura LIKE 'FACT%'";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int siguienteNumero = rs.getInt(1);
                return String.format("FACT%06d", siguienteNumero);
            }

        } catch (SQLException e) {
            System.err.println("Error al generar número de factura: " + e.getMessage());
        }

        // Fallback: usar timestamp
        return "FACT" + System.currentTimeMillis();
    }

    @Override
    public String obtenerUltimoNumeroFactura() {
        String sql = "SELECT numero_factura FROM ventas ORDER BY id DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getString("numero_factura");
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener último número de factura: " + e.getMessage());
        }
        return null;
    }

    // ===== OPERACIONES DE ACTUALIZACIÓN MASIVA =====

    @Override
    public boolean actualizarStockPorVenta(int ventaId) {
        String sql = "UPDATE productos p " +
                "INNER JOIN detalle_venta dv ON p.id = dv.producto_id " +
                "SET p.stock_actual = p.stock_actual - dv.cantidad " +
                "WHERE dv.venta_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al actualizar stock por venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean restaurarStockPorVenta(int ventaId) {
        String sql = "UPDATE productos p " +
                "INNER JOIN detalle_venta dv ON p.id = dv.producto_id " +
                "SET p.stock_actual = p.stock_actual + dv.cantidad " +
                "WHERE dv.venta_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al restaurar stock por venta: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean recalcularTotales(int ventaId) {
        String sql = "UPDATE ventas SET " +
                "subtotal = (SELECT COALESCE(SUM(subtotal), 0) FROM detalle_venta WHERE venta_id = ?), " +
                "total = subtotal - descuento + impuestos " +
                "WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ventaId);
            stmt.setInt(2, ventaId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al recalcular totales: " + e.getMessage());
            return false;
        }
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Ejecuta una consulta que retorna lista de ventas
     */
    private List<Venta> ejecutarConsultaVentas(String sql) {
        List<Venta> ventas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error en consulta de ventas: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Ejecuta una consulta de ventas con un parámetro entero
     */
    private List<Venta> ejecutarConsultaVentasConParametro(String sql, int parametro) {
        List<Venta> ventas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parametro);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error en consulta de ventas con parámetro: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Ejecuta una consulta de ventas con un parámetro string
     */
    private List<Venta> ejecutarConsultaVentasConParametroString(String sql, String parametro) {
        List<Venta> ventas = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, parametro);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventas.add(mapearVenta(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error en consulta de ventas con parámetro string: " + e.getMessage());
        }

        return ventas;
    }

    /**
     * Ejecuta una consulta de conteo
     */
    private int ejecutarConteo(String sql) {
        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Error en conteo: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Mapea un ResultSet a un objeto Venta
     */
    private Venta mapearVenta(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setId(rs.getInt("id"));
        venta.setNumeroFactura(rs.getString("numero_factura"));
        venta.setClienteId(rs.getInt("cliente_id"));
        venta.setClienteNombre(rs.getString("cliente_nombre"));
        venta.setClienteDocumento(rs.getString("cliente_documento"));
        venta.setUsuarioId(rs.getInt("usuario_id"));
        venta.setUsuarioNombre(rs.getString("usuario_nombre"));

        Timestamp fechaVenta = rs.getTimestamp("fecha_venta");
        if (fechaVenta != null) {
            venta.setFechaVenta(fechaVenta.toLocalDateTime());
        }

        venta.setSubtotal(rs.getBigDecimal("subtotal"));
        venta.setDescuento(rs.getBigDecimal("descuento"));
        venta.setImpuestos(rs.getBigDecimal("impuestos"));
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setEstado(rs.getString("estado"));
        venta.setMetodoPago(rs.getString("metodo_pago"));
        venta.setObservaciones(rs.getString("observaciones"));

        return venta;
    }

    /**
     * Mapea un ResultSet a un objeto DetalleVenta
     */
    private DetalleVenta mapearDetalleVenta(ResultSet rs) throws SQLException {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(rs.getInt("id"));
        detalle.setVentaId(rs.getInt("venta_id"));
        detalle.setProductoId(rs.getInt("producto_id"));
        detalle.setProductoCodigo(rs.getString("producto_codigo"));
        detalle.setProductoNombre(rs.getString("producto_nombre"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        detalle.setDescuento(rs.getBigDecimal("descuento"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        detalle.setProductoUnidadMedida(rs.getString("producto_unidad_medida"));
        detalle.setProductoStockActual(rs.getInt("producto_stock_actual"));

        return detalle;
    }
}