package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.util.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de persistencia para aerolíneas conectada a Microsoft SQL Server.
 */
public class SqlAirlineRepository extends AirlineRepository {

@Override
public Airline create(Airline airline) {
    String sql = "{call uspCrearAerolineas(?)}";
    try (Connection conn = DatabaseConnection.getConnection();
         CallableStatement cs = conn.prepareCall(sql)) {

        cs.setString(1, airline.getName());
        try (ResultSet rs = cs.executeQuery()) {
            if (rs.next()) {
                int generatedId = rs.getInt("airlineId");
                return new Airline(generatedId, airline.getName());
            }
        }
        return airline;
    } catch (SQLException e) {
        throw new RuntimeException("Error al registrar la aerolínea en la base de datos: " + e.getMessage(), e);
    }
}

@Override
public Optional<Airline> findById(Integer id) {
    String sql = "SELECT airlineId, name FROM tblAirlines WHERE airlineId = ?";
    try (Connection conn = DatabaseConnection.getConnection();
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
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            list.add(new Airline(rs.getInt("airlineId"), rs.getString("name")));
        }
        return list;
    } catch (SQLException e) {
        throw new RuntimeException("Error al listar aerolíneas desde la base de datos: " + e.getMessage(), e);
    }
}

@Override
public Optional<Airline> findByName(String name) {
    String sql = "SELECT airlineId, name FROM tblAirlines WHERE LOWER(name) = LOWER(?)";
    try (Connection conn = DatabaseConnection.getConnection();
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

@Override
public boolean existsByName(String name) {
    return findByName(name).isPresent();
}

@Override
public Airline update(Airline airline) {
    String sql = "UPDATE tblAirlines SET name = ? WHERE airlineId = ?";
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, airline.getName());
        ps.setInt(2, airline.getId());
        int rowsAffected = ps.executeUpdate();
        if (rowsAffected == 0) {
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
    try (Connection conn = DatabaseConnection.getConnection();
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
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
            return rs.getLong(1);
        }
        return 0;
    } catch (SQLException e) {
        throw new RuntimeException("Error al contar las aerolíneas: " + e.getMessage(), e);
    }
}
}
