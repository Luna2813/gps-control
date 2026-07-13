package com.apirest.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
/**
 * Clase utilitaria para obtener conexiones a PostgreSQL.
 * Centraliza los parámetros de conexión en un solo lugar.
 * Uso: Connection con = Conexion.obtener();
 */

public class Conexion {
	 
    // ── Parámetros de conexión ────────────────────────────────────
    // Formato: jdbc:postgresql://HOST:PUERTO/NOMBRE_BASE_DE_DATOS
	private static final String URL =
	        System.getenv().getOrDefault(
	                "DB_URL",
	                "jdbc:postgresql://localhost:5432/postgres");

	private static final String USER =
	        System.getenv().getOrDefault(
	                "DB_USER",
	                "postgres");

	private static final String PASSWORD =
	        System.getenv().getOrDefault(
	                "DB_PASSWORD",
	                "jordy");
 
   public static Connection obtener() throws SQLException {
    	System.out.println(">>>>CONECTANDO A BD postgres...");
    	try {
    		Class.forName("org.postgresql.Driver");
    	} catch(ClassNotFoundException e) {
    		System.out.println("DRIVER NO ENCONTRADO");
    		e.printStackTrace();
    	}
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
 
    public static boolean probarConexion() {
        try (Connection con = obtener()) {
            System.out.println("Conexión a PostgreSQL exitosa!");
            System.out.println("   Base de datos: " + con.getCatalog());
            return true;
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            System.err.println("   Verifica: PostgreSQL activo, credenciales, puerto 5433");
            return false;
        }
    }
}
	

 
