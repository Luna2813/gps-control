package com.apirest.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Proporciona conexiones PostgreSQL mediante un pool compartido.
 *
 * Las credenciales deben configurarse mediante variables de entorno.
 * La aplicación no incluye usuarios, contraseñas ni direcciones de respaldo.
 */
public final class Conexion {

    private static final HikariDataSource DATA_SOURCE = crearDataSource();

    private Conexion() {
    }

    private static HikariDataSource crearDataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(variableRequerida("DB_URL"));
        config.setUsername(variableRequerida("DB_USER"));
        config.setPassword(variableRequerida("DB_PASSWORD"));
        config.setDriverClassName("org.postgresql.Driver");

        config.setPoolName("ApiRestPostgresPool");
        config.setMaximumPoolSize(enteroOpcional("DB_POOL_MAX_SIZE", 10));
        config.setMinimumIdle(enteroOpcional("DB_POOL_MIN_IDLE", 2));
        config.setConnectionTimeout(enteroOpcional("DB_CONNECTION_TIMEOUT_MS", 10_000));
        config.setValidationTimeout(5_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);

        return new HikariDataSource(config);
    }

    private static String variableRequerida(String nombre) {
        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException(
                    "Falta la variable de entorno requerida: " + nombre
            );
        }

        return valor.trim();
    }

    private static int enteroOpcional(String nombre, int valorPredeterminado) {
        String valor = System.getenv(nombre);

        if (valor == null || valor.isBlank()) {
            return valorPredeterminado;
        }

        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "La variable " + nombre + " debe ser un número entero",
                    e
            );
        }
    }

    public static Connection obtener() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static boolean probarConexion() {
        try (Connection ignored = obtener()) {
            return true;
        } catch (SQLException e) {
            System.err.println("No fue posible conectar con PostgreSQL");
            return false;
        }
    }
}
