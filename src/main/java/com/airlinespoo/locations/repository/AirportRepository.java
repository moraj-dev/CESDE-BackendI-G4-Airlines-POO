package com.airlinespoo.locations.repository;

import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AirportRepository extends InMemoryRepository<Airport, UUID> {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;databaseName=dbAeroFastTrack;encrypt=false;trustServerCertificate=true";
    private static final String USER = "Proyec_Backen";
    private static final String PASSWORD = "901012";

    @Override
    protected String entityName() {
        return "aeropuerto";
    }

    @Override
    public Airport create(Airport entity) {
        Validator.requireNotNull(entity, entityName());
        City city = Validator.requireNotNull(entity.getCity(), "ciudad del " + entityName());
        Integer cityId = Validator.requireNotNull(city.getId(), "id de ciudad");

        UUID id = entity.getId() != null ? entity.getId() : UUID.randomUUID();
        String sql = "INSERT INTO dbo.tblAirports (AirportId, name, CityId) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            ps.setString(2, entity.getName());
            ps.setInt(3, cityId);
            ps.executeUpdate();

            return new Airport(id, entity.getName(), city);
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear " + entityName() + " en SQL Server", e);
        }
    }

    @Override
    public Optional<Airport> findById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = """
                SELECT a.AirportId, a.name AS airport_name, c.CityId, c.name AS city_name
                FROM dbo.tblAirports a
                INNER JOIN dbo.tblCities c ON a.CityId = c.CityId
                WHERE a.AirportId = ?
                """;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar " + entityName() + " por id en SQL Server", e);
        }

        return Optional.empty();
    }

    @Override
    public List<Airport> findAll() {
        String sql = """
                SELECT a.AirportId, a.name AS airport_name, c.CityId, c.name AS city_name
                FROM dbo.tblAirports a
                INNER JOIN dbo.tblCities c ON a.CityId = c.CityId
                """;
        List<Airport> airports = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                airports.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar aeropuertos en SQL Server", e);
        }

        return airports;
    }

    @Override
    public Airport update(Airport entity) {
        Validator.requireNotNull(entity, entityName());
        UUID id = Validator.requireNotNull(entity.getId(), "id de " + entityName());
        City city = Validator.requireNotNull(entity.getCity(), "ciudad del " + entityName());
        Integer cityId = Validator.requireNotNull(city.getId(), "id de ciudad");

        if (!existsById(id)) {
            throw new NotFoundException(entityName(), id);
        }

        String sql = "UPDATE dbo.tblAirports SET name = ?, CityId = ? WHERE AirportId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, entity.getName());
            ps.setInt(2, cityId);
            ps.setObject(3, id);
            ps.executeUpdate();

            return new Airport(id, entity.getName(), city);
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar " + entityName() + " en SQL Server", e);
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        if (id == null) {
            return false;
        }
        String sql = "DELETE FROM dbo.tblAirports WHERE AirportId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar " + entityName() + " en SQL Server", e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM dbo.tblAirports";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar aeropuertos en SQL Server", e);
        }

        return 0L;
    }

    public List<Airport> findByCityId(int cityId) {
        String sql = """
                SELECT a.AirportId, a.name AS airport_name, c.CityId, c.name AS city_name
                FROM dbo.tblAirports a
                INNER JOIN dbo.tblCities c ON a.CityId = c.CityId
                WHERE a.CityId = ?
                """;
        List<Airport> airports = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cityId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    airports.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar aeropuertos por ciudad en SQL Server", e);
        }

        return airports;
    }

    public Optional<Airport> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return findAll().stream()
            .filter(a -> a.getName().equalsIgnoreCase(name.trim()))
            .findFirst();
    }

    public List<Airport> findByNameContaining(String text) {
        String query = text.toLowerCase().trim();
        return findAll().stream()
            .filter(a -> a.getName().toLowerCase().contains(query))
            .collect(Collectors.toList());
    }

    private Airport mapRow(ResultSet rs) throws SQLException {
        UUID airportId = UUID.fromString(rs.getString("AirportId").trim());
        String airportName = rs.getString("airport_name");
        int cityId = rs.getInt("CityId");
        String cityName = rs.getString("city_name");

        City city = new City(cityId, cityName);
        return new Airport(airportId, airportName, city);
    }
    public List<Airport> findByCity(City city) {
        if (city == null || city.getId() == null) {
            return new ArrayList<>();
        }
        return findByCityId(city.getId());
    }

    public long countByCity(City city) {
        if (city == null || city.getId() == null) {
            return 0L;
        }
        String sql = "SELECT COUNT(*) FROM dbo.tblAirports WHERE CityId = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, city.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar aeropuertos por ciudad en SQL Server", e);
        }

        return 0L;
    }
}

