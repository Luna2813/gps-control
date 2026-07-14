package com.apirest.db;

import com.apirest.modelo.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    // ===============================
    // MAPEAR RESULTSET A CLIENTE
    // ===============================
    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();

        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setDpi(rs.getString("dpi"));
        c.setNit(rs.getString("nit"));
        c.setEmail(rs.getString("email"));
        c.setTelefono(rs.getString("telefono"));
        c.setCantidadDispositivos(rs.getInt("cantidad_dispositivos"));

        return c;
    }

    // ===============================
    // OBTENER TODOS
    // ===============================
    public List<Cliente> obtenerTodos() {
        List<Cliente> lista = new ArrayList<>();

        String sql = "SELECT id, nombre, dpi, nit, email, telefono, "
                   + "cantidad_dispositivos "
                   + "FROM clientes ORDER BY id";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener clientes: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    // ===============================
    // OBTENER POR DPI
    // ===============================
    public Cliente obtenerPorDpi(String dpi) {
        String sql = "SELECT id, nombre, dpi, nit, email, telefono, "
                   + "cantidad_dispositivos "
                   + "FROM clientes WHERE dpi=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dpi);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por DPI: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ===============================
    // OBTENER POR ID
    // ===============================
    public Cliente obtenerPorId(int id) {
        String sql = "SELECT id, nombre, dpi, nit, email, telefono, "
                   + "cantidad_dispositivos "
                   + "FROM clientes WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ===============================
    // CREAR
    // ===============================
    public Cliente crear(Cliente c) {
        String sql = "INSERT INTO clientes "
                   + "(nombre, dpi, nit, email, telefono, cantidad_dispositivos) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDpi());
            ps.setString(3, c.getNit());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getTelefono());
            ps.setInt(6, c.getCantidadDispositivos());

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return null;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }

            return c;

        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ===============================
    // ACTUALIZAR
    // ===============================
    public boolean actualizar(int id, Cliente c) {
        String sql = "UPDATE clientes SET "
                   + "nombre=?, dpi=?, nit=?, email=?, telefono=?, "
                   + "cantidad_dispositivos=? "
                   + "WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDpi());
            ps.setString(3, c.getNit());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getTelefono());
            ps.setInt(6, c.getCantidadDispositivos());
            ps.setInt(7, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ===============================
    // ELIMINAR
    // ===============================
    public boolean eliminar(int id) {
        String sql = "DELETE FROM clientes WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
   

