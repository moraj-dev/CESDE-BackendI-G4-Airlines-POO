package com.airlinespoo.flights.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Entrega conexiones a SQL Server. Los datos de acceso se leen de variables de entorno
 * para no subir contraseñas a GitHub.
 *
 * <p>Variables: {@code DB_PASSWORD} (obligatoria), {@code DB_HOST}, {@code DB_PORT},
 * {@code DB_NAME} y {@code DB_USER} (opcionales).</p>
 */
public class DbConnection {

    private static final String DEFAULT_HOST = "localhost\\SQLEXPRESS";
    private static final String DEFAULT_PORT = "1433";
    private static final String DEFAULT_DATABASE = "AeroFastTrack";
    private static final String DEFAULT_USER = "sa";

    private final String url;
    private final String user;
    private final String password;

    /**
     * Crea la configuración de conexión leyendo las variables de entorno o propiedades del sistema.
     */
    public DbConnection() {
        String host = env("DB_HOST", DEFAULT_HOST);
        String port = env("DB_PORT", DEFAULT_PORT);
        String database = env("DB_NAME", DEFAULT_DATABASE);
        this.url = "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false".formatted(host, port, database);
        this.user = env("DB_USER", DEFAULT_USER);
        this.password = env("DB_PASSWORD", null);
    }

    /**
     * Abre una conexión con la base de datos configurada.
     *
     * @return una conexión activa
     * @throws SQLException si falta la contraseña o no se puede conectar
     */
    public Connection getConnection() throws SQLException {
        if (password == null || password.isBlank()) {
            throw new SQLException("Falta la variable de entorno DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = System.getProperty(name);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
