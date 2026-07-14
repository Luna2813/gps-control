package com.apirest.db;

import com.apirest.modelo.VehiculoGPS;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VehiculoGPSDAO {

    // ===============================
    // CALCULAR PROMOCIÓN
    // ===============================
    private String calcularPromocion(String fechaFinPromocion) {
        if (fechaFinPromocion == null || fechaFinPromocion.isBlank()) {
            return "Inactiva";
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaFin = LocalDate.parse(fechaFinPromocion);

        if (hoy.isAfter(fechaFin) || hoy.isEqual(fechaFin)) {
            return "Inactiva";
        }

        return "Activa";
    }

    // ===============================
    // MAPEAR RESULTSET
    // ===============================
    private VehiculoGPS mapear(ResultSet rs) throws SQLException {
        VehiculoGPS v = new VehiculoGPS();

        v.setId(rs.getInt("id"));
        v.setClienteId(rs.getInt("cliente_id"));

        v.setVehiculo(rs.getString("vehiculo"));
        v.setPlaca(rs.getString("placa"));
        v.setFechaInstalacion(rs.getString("fecha_instalacion"));

        v.setTipoGps(rs.getString("tipo_gps"));
        v.setImei(rs.getString("imei"));
        v.setTelefonia(rs.getString("telefonia"));
        v.setNumeroSim(rs.getString("numero_sim"));
        v.setNumeroTelefono(rs.getString("numero_telefono"));

        String fechaFin = rs.getString("fecha_fin_promocion");

        v.setFechaFinPromocion(fechaFin);
        v.setPromocion(calcularPromocion(fechaFin));
        v.setDescripcionPromocion(rs.getString("descripcion_promocion"));

        v.setMontoOriginal(rs.getDouble("monto_normal"));
        v.setMontoPromocion(rs.getDouble("monto_promocion"));

        return v;
    }

    // ===============================
    // OBTENER POR CLIENTE
    // ===============================
    public List<VehiculoGPS> obtenerPorCliente(int clienteId) {
        List<VehiculoGPS> lista = new ArrayList<>();

        String sql = "SELECT id, cliente_id, vehiculo, placa, fecha_instalacion, "
                   + "tipo_gps, imei, telefonia, numero_sim, numero_telefono, "
                   + "promocion, fecha_fin_promocion, descripcion_promocion, "
                   + "monto_normal, monto_promocion "
                   + "FROM vehiculos_gps "
                   + "WHERE cliente_id=? "
                   + "ORDER BY id";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener vehículos: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // ===============================
    // OBTENER POR ID
    // ===============================
    public VehiculoGPS obtenerPorId(int id) {
        String sql = "SELECT id, cliente_id, vehiculo, placa, fecha_instalacion, "
                   + "tipo_gps, imei, telefonia, numero_sim, numero_telefono, "
                   + "promocion, fecha_fin_promocion, descripcion_promocion, "
                   + "monto_normal, monto_promocion "
                   + "FROM vehiculos_gps "
                   + "WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar vehículo: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ===============================
    // CREAR
    // ===============================
    public VehiculoGPS crear(int clienteId, VehiculoGPS v) {
        String sql = "INSERT INTO vehiculos_gps "
                   + "(cliente_id, vehiculo, placa, fecha_instalacion, "
                   + "tipo_gps, imei, telefonia, numero_sim, numero_telefono, "
                   + "promocion, fecha_fin_promocion, descripcion_promocion, "
                   + "monto_normal, monto_promocion) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            v.setClienteId(clienteId);
            v.setPromocion(calcularPromocion(v.getFechaFinPromocion()));

            ps.setInt(1, clienteId);
            ps.setString(2, v.getVehiculo());
            ps.setString(3, v.getPlaca());

            if (v.getFechaInstalacion() == null
                    || v.getFechaInstalacion().isBlank()) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(v.getFechaInstalacion()));
            }

            ps.setString(5, v.getTipoGps());
            ps.setString(6, v.getImei());
            ps.setString(7, v.getTelefonia());
            ps.setString(8, v.getNumeroSim());
            ps.setString(9, v.getNumeroTelefono());

            ps.setString(10, v.getPromocion());

            if (v.getFechaFinPromocion() == null
                    || v.getFechaFinPromocion().isBlank()) {
                ps.setNull(11, Types.DATE);
            } else {
                ps.setDate(
                        11,
                        Date.valueOf(v.getFechaFinPromocion())
                );
            }

            ps.setString(12, v.getDescripcionPromocion());
            ps.setDouble(13, v.getMontoOriginal());
            ps.setDouble(14, v.getMontoPromocion());

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return null;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setId(rs.getInt(1));
                }
            }

            return v;

        } catch (SQLException e) {
            System.err.println("Error al crear vehículo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ===============================
    // ACTUALIZAR
    // ===============================
    public boolean actualizar(int id, VehiculoGPS v) {
        String sql = "UPDATE vehiculos_gps SET "
                   + "vehiculo=?, placa=?, fecha_instalacion=?, "
                   + "tipo_gps=?, imei=?, telefonia=?, numero_sim=?, "
                   + "numero_telefono=?, promocion=?, fecha_fin_promocion=?, "
                   + "descripcion_promocion=?, monto_normal=?, "
                   + "monto_promocion=? "
                   + "WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            v.setPromocion(calcularPromocion(v.getFechaFinPromocion()));

            ps.setString(1, v.getVehiculo());
            ps.setString(2, v.getPlaca());

            if (v.getFechaInstalacion() == null
                    || v.getFechaInstalacion().isBlank()) {
                ps.setNull(3, Types.DATE);
            } else {
                ps.setDate(3, Date.valueOf(v.getFechaInstalacion()));
            }

            ps.setString(4, v.getTipoGps());
            ps.setString(5, v.getImei());
            ps.setString(6, v.getTelefonia());
            ps.setString(7, v.getNumeroSim());
            ps.setString(8, v.getNumeroTelefono());

            ps.setString(9, v.getPromocion());

            if (v.getFechaFinPromocion() == null
                    || v.getFechaFinPromocion().isBlank()) {
                ps.setNull(10, Types.DATE);
            } else {
                ps.setDate(
                        10,
                        Date.valueOf(v.getFechaFinPromocion())
                );
            }

            ps.setString(11, v.getDescripcionPromocion());
            ps.setDouble(12, v.getMontoOriginal());
            ps.setDouble(13, v.getMontoPromocion());
            ps.setInt(14, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar vehículo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // ELIMINAR
    // ===============================
    public boolean eliminar(int id) {
        String sql = "DELETE FROM vehiculos_gps WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar vehículo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}