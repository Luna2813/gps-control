package com.apirest.db;

import com.apirest.modelo.Notificacion;
import com.apirest.seguridad.WebPushService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    public int generarNotificacionesDeVencimiento() {
        String promociones = "INSERT INTO notificaciones "
                + "(tipo,titulo,mensaje,vehiculo_id,cliente_id,fecha_evento) "
                + "SELECT 'PROMOCION_VENCIDA','Promoción finalizada', "
                + "'Finalizó la promoción de ' || v.vehiculo || ' (' || v.placa "
                + "|| ') del cliente ' || c.nombre, v.id,c.id,v.fecha_fin_promocion "
                + "FROM vehiculos_gps v JOIN clientes c ON c.id=v.cliente_id "
                + "WHERE v.fecha_fin_promocion IS NOT NULL "
                + "AND v.fecha_fin_promocion <= CURRENT_DATE "
                + "ON CONFLICT (tipo,vehiculo_id,fecha_evento) DO NOTHING";

        String planes = "INSERT INTO notificaciones "
                + "(tipo,titulo,mensaje,vehiculo_id,cliente_id,fecha_evento) "
                + "SELECT 'PLAN_ANUAL_VENCIDO','Plan anual finalizado', "
                + "'Finalizó el plan anual de ' || v.vehiculo || ' (' || v.placa "
                + "|| ') del cliente ' || c.nombre, v.id,c.id,v.fecha_fin_plan_anual "
                + "FROM vehiculos_gps v JOIN clientes c ON c.id=v.cliente_id "
                + "WHERE v.tipo_plan='ANUAL' AND v.fecha_fin_plan_anual IS NOT NULL "
                + "AND v.fecha_fin_plan_anual <= CURRENT_DATE "
                + "ON CONFLICT (tipo,vehiculo_id,fecha_evento) DO NOTHING";

        String actualizarPlanes = "UPDATE vehiculos_gps "
                + "SET estado_plan_anual='VENCIDO' "
                + "WHERE tipo_plan='ANUAL' AND fecha_fin_plan_anual IS NOT NULL "
                + "AND fecha_fin_plan_anual <= CURRENT_DATE "
                + "AND estado_plan_anual<>'VENCIDO'";

        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try (PreparedStatement p1 = con.prepareStatement(promociones);
                 PreparedStatement p2 = con.prepareStatement(planes);
                 PreparedStatement p3 = con.prepareStatement(actualizarPlanes)) {
                int creadas = p1.executeUpdate() + p2.executeUpdate();
                p3.executeUpdate();
                con.commit();
                new WebPushService().enviarPendientes();
                return creadas;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("No fue posible generar notificaciones: " + e.getMessage());
            return 0;
        }
    }

    public List<Notificacion> obtenerRecientes() {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT id,tipo,titulo,mensaje,vehiculo_id,cliente_id,"
                + "fecha_evento,leida,creado_en FROM notificaciones "
                + "ORDER BY leida, creado_en DESC LIMIT 100";
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible consultar notificaciones", e);
        }
    }

    public boolean marcarLeida(long id) {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE notificaciones SET leida=TRUE WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private Notificacion mapear(ResultSet rs) throws SQLException {
        Notificacion n = new Notificacion();
        n.setId(rs.getLong("id")); n.setTipo(rs.getString("tipo"));
        n.setTitulo(rs.getString("titulo")); n.setMensaje(rs.getString("mensaje"));
        int vehiculo = rs.getInt("vehiculo_id"); n.setVehiculoId(rs.wasNull() ? null : vehiculo);
        int cliente = rs.getInt("cliente_id"); n.setClienteId(rs.wasNull() ? null : cliente);
        n.setFechaEvento(rs.getString("fecha_evento")); n.setLeida(rs.getBoolean("leida"));
        n.setCreadoEn(rs.getTimestamp("creado_en").toInstant().toString());
        return n;
    }
}
