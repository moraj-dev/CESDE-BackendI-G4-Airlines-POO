package com.airlinespoo.customers.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            throw new PersistenceException(
                    "Faltan las variables de entorno para conectar con SQL Server.",
                    null
            );
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new PersistenceException(
                    "No fue posible conectar con la base de datos AeroFastTrack.",
                    e
            );
        }
    }
}
