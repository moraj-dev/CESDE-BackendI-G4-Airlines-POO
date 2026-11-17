package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.repository.Repository;
import java.sql.*;

public class ReservationRepository extends Repository {



    public Connection getSqlServerConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=bibliotecadb;encrypt=false"; // esta es la conexion local de cada portatil
        String user = "sa"; // tu usuario
        String password = "TuPassword123!"; // tu contraseña
        return DriverManager.getConnection(url, user, password);
    }
}
