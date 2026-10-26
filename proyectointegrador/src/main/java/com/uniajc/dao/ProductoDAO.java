// Autor: Alejandro Mosquera | Commit 3: DAO de Producto (sin edicion directa de stock fuera del Kardex)
package com.uniajc.dao;

import com.uniajc.config.ConexionPostgresDatabase;
import com.uniajc.modelo.EstadoProducto;
import com.uniajc.modelo.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    /**
     * Regla de Oro (RN-01): Al crear un producto, su stock_actual y stock_reservado inician en 0.
     * Solo un MovimientoInventario autorizado en el Kardex puede modificar el stock.
     */
    public boolean insertarFichaCatalogo(Producto p) throws SQLException {
        String sql = "INSERT INTO public.producto (nombre_producto, descripcion, categoria, material, color, " +
                     "precio_unitario, stock_actual, stock_reservado, stock_minimo, id_est_producto) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, ?) RETURNING id_producto";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setString(2, p.getDescripcion());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getMaterial());
            ps.setString(5, p.getColor());
            ps.setBigDecimal(6, p.getPrecioUnitario());
            ps.setInt(7, p.getStockMinimo());
            ps.setInt(8, p.getEstado().getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p.setIdProducto(rs.getInt(1));
                    p.setStockActual(0);
                    p.setStockReservado(0);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Actualiza únicamente datos descriptivos y precio del catálogo. NUNCA actualiza stock_actual directamente.
     */
    public boolean actualizarDatosCatalogo(Producto p) throws SQLException {
        String sql = "UPDATE public.producto SET nombre_producto = ?, descripcion = ?, categoria = ?, " +
                     "material = ?, color = ?, precio_unitario = ?, stock_minimo = ?, id_est_producto = ? " +
                     "WHERE id_producto = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombreProducto());
            ps.setString(2, p.getDescripcion());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getMaterial());
            ps.setString(5, p.getColor());
            ps.setBigDecimal(6, p.getPrecioUnitario());
            ps.setInt(7, p.getStockMinimo());
            ps.setInt(8, p.getEstado().getId());
            ps.setInt(9, p.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    public Producto buscarPorId(int idProducto) throws SQLException {
        String sql = "SELECT id_producto, nombre_producto, descripcion, categoria, material, color, " +
                     "precio_unitario, stock_actual, stock_reservado, stock_minimo, id_est_producto " +
                     "FROM public.producto WHERE id_producto = ?";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, nombre_producto, descripcion, categoria, material, color, " +
                     "precio_unitario, stock_actual, stock_reservado, stock_minimo, id_est_producto " +
                     "FROM public.producto ORDER BY id_producto";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Producto> buscarPorCriterio(String filtro) throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT id_producto, nombre_producto, descripcion, categoria, material, color, " +
                     "precio_unitario, stock_actual, stock_reservado, stock_minimo, id_est_producto " +
                     "FROM public.producto " +
                     "WHERE LOWER(nombre_producto) LIKE LOWER(?) OR LOWER(descripcion) LIKE LOWER(?) " +
                     "ORDER BY id_producto";
        Connection conn = ConexionPostgresDatabase.getInstancia().getConexion();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String patron = "%" + (filtro != null ? filtro.trim() : "") + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("nombre_producto"),
                rs.getString("descripcion"),
                rs.getString("categoria"),
                rs.getString("material"),
                rs.getString("color"),
                rs.getBigDecimal("precio_unitario"),
                rs.getInt("stock_actual"),
                rs.getInt("stock_reservado"),
                rs.getInt("stock_minimo"),
                EstadoProducto.desdeId(rs.getInt("id_est_producto"))
        );
    }
}
