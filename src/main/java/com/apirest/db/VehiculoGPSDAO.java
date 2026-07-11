package com.apirest.db;

import com.apirest.modelo.VehiculoGPS;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehiculoGPSDAO {

    private String calcularPromocion(String fechaFinPromocion) {
        if (fechaFinPromocion == null || fechaFinPromocion.isBlank()) {
            return "Inactiva";
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaFin = LocalDate.parse(fechaFinPromocion);

        if (hoy.isAfter(fechaFin) || hoy.isEqual(fechaFin)) {
            return "Inactiva";
        } else {
            return "Activa";
        }
    }

    private VehiculoGPS mapear(ResultSet rs) throws SQLException {
        VehiculoGPS v = new VehiculoGPS();

        v.setId(rs.getInt("id"));
        v.setClienteId(rs.getInt("cliente_id"));
        v.setCantidadDispositivos(rs.getInt("cantidad_dispositivos"));

        v.setVehiculo(rs.getString("vehiculo"));
        v.setPlaca(rs.getString("placa"));
        v.setFechaInstalacion(rs.getString("fecha_instalacion"));

        v.setTipoGps(rs.getString("tipo_gps"));
        v.setImei(rs.getString("imei"));

        v.setPromocion(calcularPromocion(rs.getString("fecha_fin_promocion")));
        v.setFechaFinPromocion(rs.getString("fecha_fin_promocion"));
        v.setDescripcionPromocion(rs.getString("descripcion_promocion"));

        v.setMontoNormal(rs.getDouble("monto_normal"));
        v.setMontoPromocion(rs.getDouble("monto_promocion"));

        return v;
    }

    public List<VehiculoGPS> obtenerPorCliente(int clienteId) {
        List<VehiculoGPS> lista = new ArrayList<>();

        String sql = "SELECT * FROM vehiculos_gps WHERE cliente_id=? ORDER BY id";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public VehiculoGPS obtenerPorId(int id) {
        String sql = "SELECT * FROM vehiculos_gps WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public VehiculoGPS crear(int clienteId, VehiculoGPS v) {
        String sql = "INSERT INTO vehiculos_gps "
                + "(cliente_id, cantidad_dispositivos, vehiculo, placa, fecha_instalacion, "
                + "tipo_gps, imei, promocion, fecha_fin_promocion, descripcion_promocion, "
                + "monto_normal, monto_promocion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            v.setPromocion(calcularPromocion(v.getFechaFinPromocion()));
            v.setClienteId(clienteId);

            ps.setInt(1, clienteId);
            ps.setInt(2, v.getCantidadDispositivos());
            ps.setString(3, v.getVehiculo());
            ps.setString(4, v.getPlaca());
            ps.setDate(5, Date.valueOf(v.getFechaInstalacion()));

            ps.setString(6, v.getTipoGps());
            ps.setString(7, v.getImei());
            ps.setString(8, v.getPromocion());

            if (v.getFechaFinPromocion() == null || v.getFechaFinPromocion().isBlank()) {
                ps.setNull(9, Types.DATE);
            } else {
                ps.setDate(9, Date.valueOf(v.getFechaFinPromocion()));
            }

            ps.setString(10, v.getDescripcionPromocion());
            ps.setDouble(11, v.getMontoNormal());
            ps.setDouble(12, v.getMontoPromocion());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return v;
    }

    public boolean actualizar(int id, VehiculoGPS v) {
        String sql = "UPDATE vehiculos_gps SET "
                + "cantidad_dispositivos=?, vehiculo=?, placa=?, fecha_instalacion=?, "
                + "tipo_gps=?, imei=?, promocion=?, fecha_fin_promocion=?, "
                + "descripcion_promocion=?, monto_normal=?, monto_promocion=? "
                + "WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            v.setPromocion(calcularPromocion(v.getFechaFinPromocion()));

            ps.setInt(1, v.getCantidadDispositivos());
            ps.setString(2, v.getVehiculo());
            ps.setString(3, v.getPlaca());
            ps.setDate(4, Date.valueOf(v.getFechaInstalacion()));

            ps.setString(5, v.getTipoGps());
            ps.setString(6, v.getImei());
            ps.setString(7, v.getPromocion());

            if (v.getFechaFinPromocion() == null || v.getFechaFinPromocion().isBlank()) {
                ps.setNull(8, Types.DATE);
            } else {
                ps.setDate(8, Date.valueOf(v.getFechaFinPromocion()));
            }

            ps.setString(9, v.getDescripcionPromocion());
            ps.setDouble(10, v.getMontoNormal());
            ps.setDouble(11, v.getMontoPromocion());
            ps.setInt(12, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM vehiculos_gps WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}