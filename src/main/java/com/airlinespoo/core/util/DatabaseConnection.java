package com.airlinespoo.core.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Administra la conexión JDBC hacia la base de datos SQL Server AeroFastTrack.
 */
public class DatabaseConnection {

private static final String URL = "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=AeroFastTrack;encrypt=false;trustServerCertificate=true;";
private static final String USER = "sa";
private static final String PASSWORD = "Password123*";

private DatabaseConnection() {
}

public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}
}
