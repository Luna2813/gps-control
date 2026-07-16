package com.apirest.db;

import com.apirest.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final int MAX_INTENTOS = 5;
    private static final long MINUTOS_BLOQUEO = 15;

    public Usuario obtenerParaLogin(String nombreUsuario) {
        String sql = "SELECT id, nombre, usuario, password_hash, rol, activo, "
                + "intentos_fallidos, bloqueado_hasta "
                + "FROM usuarios WHERE LOWER(usuario)=LOWER(?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Timestamp bloqueadoHasta = rs.getTimestamp("bloqueado_hasta");
                if (bloqueadoHasta != null
                        && bloqueadoHasta.toInstant().isAfter(Instant.now())) {
                    return null;
                }

                return mapear(rs);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible consultar el usuario", e);
        }
    }

    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id, nombre, usuario, password_hash, rol, activo "
                + "FROM usuarios ORDER BY id";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = mapear(rs);
                usuario.setPasswordHash(null);
                usuarios.add(usuario);
            }
            return usuarios;
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible listar usuarios", e);
        }
    }

    public boolean existeNombreUsuario(String nombreUsuario) {
        String sql = "SELECT EXISTS (SELECT 1 FROM usuarios "
                + "WHERE LOWER(usuario)=LOWER(?))";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible validar el usuario", e);
        }
    }

    public boolean actualizarEstado(long id, boolean activo) {
        String sql = "UPDATE usuarios SET activo=?, actualizado_en=CURRENT_TIMESTAMP "
                + "WHERE id=?";
        return ejecutarCambio(sql, ps -> {
            ps.setBoolean(1, activo);
            ps.setLong(2, id);
        });
    }

    public boolean actualizarRol(long id, String rol) {
        String sql = "UPDATE usuarios SET rol=?, actualizado_en=CURRENT_TIMESTAMP "
                + "WHERE id=?";
        return ejecutarCambio(sql, ps -> {
            ps.setString(1, rol);
            ps.setLong(2, id);
        });
    }

    public boolean actualizarPassword(long id, String passwordHash) {
        String sql = "UPDATE usuarios SET password_hash=?, intentos_fallidos=0, "
                + "bloqueado_hasta=NULL, actualizado_en=CURRENT_TIMESTAMP WHERE id=?";
        return ejecutarCambio(sql, ps -> {
            ps.setString(1, passwordHash);
            ps.setLong(2, id);
        });
    }

    public void registrarLoginCorrecto(long usuarioId) {
        String sql = "UPDATE usuarios SET intentos_fallidos=0, bloqueado_hasta=NULL, "
                + "ultimo_acceso=CURRENT_TIMESTAMP, actualizado_en=CURRENT_TIMESTAMP "
                + "WHERE id=?";
        ejecutarActualizacion(sql, usuarioId);
    }

    public void registrarLoginFallido(String nombreUsuario) {
        String sql = "UPDATE usuarios SET "
                + "intentos_fallidos=intentos_fallidos+1, "
                + "bloqueado_hasta=CASE WHEN intentos_fallidos+1 >= ? "
                + "THEN CURRENT_TIMESTAMP + (? * INTERVAL '1 minute') "
                + "ELSE bloqueado_hasta END, actualizado_en=CURRENT_TIMESTAMP "
                + "WHERE LOWER(usuario)=LOWER(?) "
                + "AND (bloqueado_hasta IS NULL OR bloqueado_hasta <= CURRENT_TIMESTAMP)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, MAX_INTENTOS);
            ps.setLong(2, MINUTOS_BLOQUEO);
            ps.setString(3, nombreUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible registrar el intento", e);
        }
    }

    public boolean existenUsuarios() {
        String sql = "SELECT EXISTS (SELECT 1 FROM usuarios)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible comprobar usuarios", e);
        }
    }

    public Usuario crear(String nombre, String nombreUsuario,
                         String passwordHash, String rol) {
        String sql = "INSERT INTO usuarios "
                + "(nombre, usuario, password_hash, rol, activo) "
                + "VALUES (?, ?, ?, ?, TRUE)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, nombreUsuario.toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, rol);

            if (ps.executeUpdate() == 0) {
                return null;
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setUsuario(nombreUsuario.toLowerCase());
            usuario.setRol(rol);
            usuario.setActivo(true);

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }

            return usuario;
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible crear el usuario", e);
        }
    }

    private void ejecutarActualizacion(String sql, long usuarioId) {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible actualizar el usuario", e);
        }
    }

    private boolean ejecutarCambio(String sql, PrepararStatement preparar) {
        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preparar.aplicar(ps);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible actualizar el usuario", e);
        }
    }

    @FunctionalInterface
    private interface PrepararStatement {
        void aplicar(PreparedStatement ps) throws SQLException;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        return usuario;
    }
}
