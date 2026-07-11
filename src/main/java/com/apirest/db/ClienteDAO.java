package com.apirest.db;

import com.apirest.modelo.Cliente;
import java.sql.*;

public class ClienteDAO {

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();

        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setDpi(rs.getString("dpi"));
        c.setNit(rs.getString("nit"));
        c.setEmail(rs.getString("email"));
        c.setTelefono(rs.getString("telefono"));
        c.setTelefonia(rs.getString("telefonia"));
        c.setNumeroSim(rs.getString("numero_sim"));

        return c;
    }

    public Cliente obtenerPorDpi(String dpi) {
        String sql = "SELECT * FROM clientes WHERE dpi=?";

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

    public Cliente obtenerPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id=?";

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

    public Cliente crear(Cliente c) {
        String sql = "INSERT INTO clientes "
                + "(nombre, dpi, nit, email, telefono, telefonia, numero_sim) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDpi());
            ps.setString(3, c.getNit());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getTelefonia());
            ps.setString(7, c.getNumeroSim());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return c;
    }

    public boolean actualizar(int id, Cliente c) {
        String sql = "UPDATE clientes SET "
                + "nombre=?, dpi=?, nit=?, email=?, telefono=?, telefonia=?, numero_sim=? "
                + "WHERE id=?";

        try (Connection con = Conexion.obtener();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDpi());
            ps.setString(3, c.getNit());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getTelefonia());
            ps.setString(7, c.getNumeroSim());
            ps.setInt(8, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean eliminar(int id){

        String sql="DELETE FROM clientes WHERE id=?";

        try(Connection con=Conexion.obtener();
            PreparedStatement ps=con.prepareStatement(sql)){

            ps.setInt(1,id);

            return ps.executeUpdate()>0;

        }catch(SQLException e){
            e.printStackTrace();
        }

        return false;

    }
    


	}
   

