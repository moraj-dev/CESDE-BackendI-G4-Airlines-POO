package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para aerolíneas conectado a Microsoft SQL Server.
 */
public class AirlineRepository implements Repository<Airline, Integer> {

private static final String URL = "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=AirlinesPOO;encrypt=false;trustServerCertificate=true;";
private static final String USER = "sa";
private static final String PASSWORD = "Password123*";

private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}

@Override
public Airline create(Airline airline) {
    String sql = "{call uspCrearAerolineas(?)}";
    try (Connection conn = getConnection();
         CallableStatement cs = conn.prepareCall(sql)) {

        cs.setString(1, airline.getName());
        try (ResultSet rs = cs.executeQuery()) {
            if (rs.next()) {
                return new Airline(rs.getInt("airlineId"), airline.getName());
            }
        }
        return airline;
    } catch (SQLException e) {
        throw new RuntimeException("Error al registrar la aerolínea: " + e.getMessage(), e);
    }
}

@Override
public Optional<Airline> findById(Integer id) {
    String sql = "SELECT airlineId, name FROM tblAirlines WHERE airlineId = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Airline(rs.getInt("airlineId"), rs.getString("name")));
            }
        }
        return Optional.empty();
    } catch (SQLException e) {
        throw new RuntimeException("Error al consultar aerolínea por ID: " + e.getMessage(), e);
    }
}

@Override
public List<Airline> findAll() {
    String sql = "SELECT airlineId, name FROM tblAirlines ORDER BY name";
    List<Airline> list = new ArrayList<>();
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            list.add(new Airline(rs.getInt("airlineId"), rs.getString("name")));
        }
        return list;
    } catch (SQLException e) {
        throw new RuntimeException("Error al listar aerolíneas: " + e.getMessage(), e);
    }
}

public Optional<Airline> findByName(String name) {
    String sql = "SELECT airlineId, name FROM tblAirlines WHERE LOWER(name) = LOWER(?)";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, name.trim());
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Airline(rs.getInt("airlineId"), rs.getString("name")));
            }
        }
        return Optional.empty();
    } catch (SQLException e) {
        throw new RuntimeException("Error al buscar aerolínea por nombre: " + e.getMessage(), e);
    }
}

public boolean existsByName(String name) {
    return findByName(name).isPresent();
}

@Override
public Airline update(Airline airline) {
    String sql = "UPDATE tblAirlines SET name = ? WHERE airlineId = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, airline.getName());
        ps.setInt(2, airline.getId());
        if (ps.executeUpdate() == 0) {
            throw new IllegalArgumentException("No se encontró la aerolínea con ID: " + airline.getId());
        }
        return airline;
    } catch (SQLException e) {
        throw new RuntimeException("Error al actualizar la aerolínea: " + e.getMessage(), e);
    }
}

@Override
public boolean deleteById(Integer id) {
    String sql = "DELETE FROM tblAirlines WHERE airlineId = ?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        throw new RuntimeException("Error al eliminar la aerolínea: " + e.getMessage(), e);
    }
}

@Override
public boolean existsById(Integer id) {
    return findById(id).isPresent();
}

@Override
public long count() {
    String sql = "SELECT COUNT(*) FROM tblAirlines";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        return rs.next() ? rs.getLong(1) : 0;
    } catch (SQLException e) {
        throw new RuntimeException("Error al contar aerolíneas: " + e.getMessage(), e);
    }
}
}
