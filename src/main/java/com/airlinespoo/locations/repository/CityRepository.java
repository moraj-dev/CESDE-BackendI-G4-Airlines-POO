package com.airlinespoo.locations.repository;

import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.City;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CityRepository extends InMemoryRepository<City, Integer> {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;databaseName=dbAeroFastTrack;encrypt=false;trustServerCertificate=true";
    private static final String USER = "Proyec_Backen";
    private static final String PASSWORD = "901012";

    @Override
    protected String entityName() {
        return "ciudad";
    }

    @Override
    public City create(City entity) {
        Validator.requireNotNull(entity, entityName());
        String sql = "INSERT INTO dbo.tblCities (name) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getName());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    return new City(generatedId, entity.getName());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear " + entityName() + " en SQL Server", e);
        }

        return entity;
    }

    @Override
    public Optional<City> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT CityId, name FROM dbo.tblCities WHERE CityId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new City(rs.getInt("CityId"), rs.getString("name")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar " + entityName() + " por id en SQL Server", e);
        }

        return Optional.empty();
    }

    @Override
    public List<City> findAll() {
        String sql = "SELECT CityId, name FROM dbo.tblCities";
        List<City> cities = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cities.add(new City(rs.getInt("CityId"), rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar ciudades en SQL Server", e);
        }

        return cities;
    }

    @Override
    public City update(City entity) {
        Validator.requireNotNull(entity, entityName());
        Integer id = Validator.requireNotNull(entity.getId(), "id de " + entityName());
        if (!existsById(id)) {
            throw new NotFoundException(entityName(), id);
        }

        String sql = "UPDATE dbo.tblCities SET name = ? WHERE CityId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getName());
            ps.setInt(2, id);
            ps.executeUpdate();
            return new City(id, entity.getName());
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar " + entityName() + " en SQL Server", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM dbo.tblCities WHERE CityId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar " + entityName() + " en SQL Server", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM dbo.tblCities";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar ciudades en SQL Server", e);
        }

        return 0L;
    }

    public Optional<City> findByName(String name) {
        return findAll().stream()
            .filter(c -> c.getName().equalsIgnoreCase(name.trim()))
            .findFirst();
    }

    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }

    public List<City> findByNameContaining(String text) {
        String query = text.toLowerCase().trim();
        return findAll().stream()
            .filter(c -> c.getName().toLowerCase().contains(query))
            .collect(Collectors.toList());
    }
}
