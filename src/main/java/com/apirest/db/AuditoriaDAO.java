package com.apirest.db;

import com.apirest.modelo.Auditoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    public void registrar(long usuarioId, String usuario, String rol,
                          String metodo, String ruta, int estadoHttp,
                          String direccionIp, String navegador) {
        registrarInterno(usuarioId, usuario, rol, metodo, ruta, estadoHttp,
                direccionIp, navegador);
    }

    public void registrarSistema(String metodo, String ruta) {
        registrarInterno(null, "SISTEMA", "SISTEMA", metodo, ruta, 200,
                "local", "Tarea automática");
    }

    private void registrarInterno(Long usuarioId, String usuario, String rol,
                                  String metodo, String ruta, int estadoHttp,
                                  String direccionIp, String navegador) {
        String sql = "INSERT INTO auditoria "
                + "(usuario_id, usuario, rol, metodo, ruta, estado_http, "
                + "direccion_ip, navegador) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (usuarioId == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, usuarioId);
            }
            ps.setString(2, limitar(usuario, 80));
            ps.setString(3, limitar(rol, 20));
            ps.setString(4, limitar(metodo, 10));
            ps.setString(5, limitar(ruta, 300));
            ps.setInt(6, estadoHttp);
            ps.setString(7, limitar(direccionIp, 64));
            ps.setString(8, limitar(navegador, 300));
            ps.executeUpdate();
        } catch (SQLException e) {
            // La auditoría no debe convertir una operación ya exitosa en error.
            System.err.println("No fue posible registrar la auditoría");
        }
    }

    public List<Auditoria> obtenerRecientes(int limite) {
        List<Auditoria> lista = new ArrayList<>();
        String sql = "SELECT id, usuario_id, usuario, rol, metodo, ruta, "
                + "estado_http, direccion_ip, navegador, creado_en "
                + "FROM auditoria ORDER BY creado_en DESC LIMIT ?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, Math.min(limite, 500)));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible consultar la auditoría", e);
        }
    }

    private Auditoria mapear(ResultSet rs) throws SQLException {
        Auditoria a = new Auditoria();
        a.setId(rs.getLong("id"));
        long usuarioId = rs.getLong("usuario_id");
        a.setUsuarioId(rs.wasNull() ? null : usuarioId);
        a.setUsuario(rs.getString("usuario"));
        a.setRol(rs.getString("rol"));
        a.setMetodo(rs.getString("metodo"));
        a.setRuta(rs.getString("ruta"));
        a.setEstadoHttp(rs.getInt("estado_http"));
        a.setDireccionIp(rs.getString("direccion_ip"));
        a.setNavegador(rs.getString("navegador"));
        a.setCreadoEn(rs.getTimestamp("creado_en").toInstant().toString());
        return a;
    }

    private String limitar(String valor, int maximo) {
        if (valor == null) return null;
        return valor.length() <= maximo ? valor : valor.substring(0, maximo);
    }
}
