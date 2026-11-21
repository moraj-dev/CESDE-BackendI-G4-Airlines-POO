package com.airlinespoo.customers.repository;

import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.persistence.DatabaseConnection;
import com.airlinespoo.customers.persistence.PersistenceException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CountryRepositoryJdbc extends CountryRepository {

    @Override
    public Country create(Country country) {
        Objects.requireNonNull(country, "El país no puede ser null");

        String sql = "INSERT INTO tblCountries (CountryId, Name) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, country.getId());
            statement.setString(2, country.getName());
            statement.executeUpdate();

            return country;

        } catch (SQLException e) {
            if (isDuplicate(e)) {
                throw new DuplicateException("país", country.getId());
            }
            throw new PersistenceException("No fue posible crear el país.", e);
        }
    }

    @Override
    public Optional<Country> findById(Integer id) {
        String sql = "SELECT CountryId, Name FROM tblCountries WHERE CountryId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapCountry(result));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible consultar el país.", e);
        }
    }

    @Override
    public List<Country> findAll() {
        String sql = "SELECT CountryId, Name FROM tblCountries ORDER BY CountryId";
        List<Country> countries = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                countries.add(mapCountry(result));
            }

            return countries;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible listar los países.", e);
        }
    }

    @Override
    public Country update(Country country) {
        Objects.requireNonNull(country, "El país no puede ser null");

        String sql = "UPDATE tblCountries SET Name = ? WHERE CountryId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, country.getName());
            statement.setInt(2, country.getId());

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("país", country.getId());
            }

            return country;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible actualizar el país.", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM tblCountries WHERE CountryId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible eliminar el país.", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM tblCountries WHERE CountryId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible verificar el país.", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM tblCountries";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            result.next();
            return result.getLong(1);

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible contar los países.", e);
        }
    }

    private Country mapCountry(ResultSet result) throws SQLException {
        return new Country(
                result.getInt("CountryId"),
                result.getString("Name")
        );
    }

    private boolean isDuplicate(SQLException exception) {
        SQLException current = exception;

        while (current != null) {
            if (current.getErrorCode() == 2627 || current.getErrorCode() == 2601) {
                return true;
            }
            current = current.getNextException();
        }

        return false;
    }
}
