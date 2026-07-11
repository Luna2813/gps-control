package com.apirest.db;

import com.apirest.modelo.InstalacionGPS;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InstalacionGPSDAO {

    private String calcularPromocion(String fechaFinPromocion) {
        if (fechaFinPromocion == null || fechaFinPromocion.isBlank()) {
            return "Inactiva";
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaFin = LocalDate.parse(fechaFinPromocion);

        if (hoy.isAfter(fechaFin)) {
            return "Fin de promoción";
        } else {
            return "Activa";
        }
    }

    private InstalacionGPS mapear(ResultSet rs) throws SQLException {
        InstalacionGPS h = new InstalacionGPS();

        h.setId(rs.getInt("id"));
        h.setNombre(rs.getString("nombre"));
        h.setDpi(rs.getString("dpi"));
        h.setNit(rs.getString("nit"));
        h.setEmail(rs.getString("email"));
        h.setCantidadDispositivos(rs.getInt("cantidad_dispositivos"));

        h.setVehiculo(rs.getString("vehiculo"));
        h.setPlaca(rs.getString("placa"));
        h.setFechaInstalacion(rs.getString("fecha_instalacion"));

        h.setTipoGps(rs.getString("tipo_gps"));
        h.setImei(rs.getString("imei"));
        h.setTelefonia(rs.getString("telefonia"));
        h.setNumeroTelefono(rs.getString("numero_telefono"));
        h.setNumeroSim(rs.getString("numero_sim"));

        h.setPromocion(rs.getString("promocion"));
        h.setFechaFinPromocion(rs.getString("fecha_fin_promocion"));
        h.setMonto(rs.getDouble("monto"));

        return h;
    }

    public List<InstalacionGPS> obtenerTodos() {
        List<InstalacionGPS> lista = new ArrayList<>();

        String sql = "SELECT * FROM instalaciones_gps ORDER BY id";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public InstalacionGPS obtenerPorNumero(int id) {
        String sql = "SELECT * FROM instalaciones_gps WHERE id=?";

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

    public InstalacionGPS obtenerPorDpi(String dpi) {
        String sql = "SELECT * FROM instalaciones_gps WHERE dpi=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dpi);

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

    public InstalacionGPS crear(InstalacionGPS h) {
        String sql = "INSERT INTO instalaciones_gps "
                + "(nombre, dpi, nit, email, cantidad_dispositivos, vehiculo, placa, "
                + "fecha_instalacion, tipo_gps, imei, telefonia, numero_telefono, "
                + "numero_sim, promocion, fecha_fin_promocion, monto) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            h.setPromocion(calcularPromocion(h.getFechaFinPromocion()));

            ps.setString(1, h.getNombre());
            ps.setString(2, h.getDpi());
            ps.setString(3, h.getNit());
            ps.setString(4, h.getEmail());
            ps.setInt(5, h.getCantidadDispositivos());

            ps.setString(6, h.getVehiculo());
            ps.setString(7, h.getPlaca());
            ps.setDate(8, Date.valueOf(h.getFechaInstalacion()));

            ps.setString(9, h.getTipoGps());
            ps.setString(10, h.getImei());
            ps.setString(11, h.getTelefonia());
            ps.setString(12, h.getNumeroTelefono());
            ps.setString(13, h.getNumeroSim());

            ps.setString(14, h.getPromocion());

            if (h.getFechaFinPromocion() == null || h.getFechaFinPromocion().isBlank()) {
                ps.setNull(15, Types.DATE);
            } else {
                ps.setDate(15, Date.valueOf(h.getFechaFinPromocion()));
            }

            ps.setDouble(16, h.getMonto());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return h;
    }

    public boolean actualizar(int id, InstalacionGPS h) {
        String sql = "UPDATE instalaciones_gps SET "
                + "nombre=?, dpi=?, nit=?, email=?, cantidad_dispositivos=?, "
                + "vehiculo=?, placa=?, fecha_instalacion=?, tipo_gps=?, imei=?, "
                + "telefonia=?, numero_telefono=?, numero_sim=?, promocion=?, "
                + "fecha_fin_promocion=?, monto=? WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            h.setPromocion(calcularPromocion(h.getFechaFinPromocion()));

            ps.setString(1, h.getNombre());
            ps.setString(2, h.getDpi());
            ps.setString(3, h.getNit());
            ps.setString(4, h.getEmail());
            ps.setInt(5, h.getCantidadDispositivos());

            ps.setString(6, h.getVehiculo());
            ps.setString(7, h.getPlaca());
            ps.setDate(8, Date.valueOf(h.getFechaInstalacion()));

            ps.setString(9, h.getTipoGps());
            ps.setString(10, h.getImei());
            ps.setString(11, h.getTelefonia());
            ps.setString(12, h.getNumeroTelefono());
            ps.setString(13, h.getNumeroSim());

            ps.setString(14, h.getPromocion());

            if (h.getFechaFinPromocion() == null || h.getFechaFinPromocion().isBlank()) {
                ps.setNull(15, Types.DATE);
            } else {
                ps.setDate(15, Date.valueOf(h.getFechaFinPromocion()));
            }

            ps.setDouble(16, h.getMonto());
            ps.setInt(17, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int numero) {
        String sql = "DELETE FROM instalaciones_gps WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, numero);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
