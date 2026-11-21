package com.airlinespoo.customers.repository;

import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class CustomerRepositoryJdbc extends CustomerRepository {

    private static final String SELECT_CUSTOMER =
            "SELECT cu.customerId AS CustomerId, " +
            "cu.FirtName AS FirstName, " +
            "cu.Lastname AS LastName, " +
            "cu.TelephoneNumber AS TelephoneNumber, " +
            "cu.Email AS Email, " +
            "cu.Address AS Address, " +
            "dt.TypeId AS DocumentTypeId, " +
            "dt.Description AS DocumentTypeDescription, " +
            "c.CountryId AS CountryId, " +
            "c.Name AS CountryName " +
            "FROM dbo.tblCustomers cu " +
            "INNER JOIN dbo.tblDocumentsType dt ON cu.TypeId = dt.TypeId " +
            "INNER JOIN dbo.tblCountries c ON cu.CountryId = c.CountryId ";

    @Override
    public Customer create(Customer customer) {
        Objects.requireNonNull(customer, "El cliente no puede ser null");

        String sql = "INSERT INTO dbo.tblCustomers " +
                "(customerId, FirtName, Lastname, TypeId, TelephoneNumber, Email, CountryId, Address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getId());
            statement.setNString(2, customer.getFirstName());
            statement.setNString(3, customer.getLastName());
            statement.setString(4, customer.getDocumentType().getId());
            statement.setString(5, customer.getTelephoneNumber());
            statement.setNString(6, customer.getEmail());
            statement.setInt(7, customer.getCountry().getId());
            statement.setNString(8, customer.getAddress());

            statement.executeUpdate();
            return customer;

        } catch (SQLException e) {
            if (isDuplicate(e)) {
                throw new DuplicateException("cliente", customer.getId());
            }
            throw new PersistenceException("No fue posible crear el cliente.", e);
        }
    }

    @Override
    public Optional<Customer> findById(String id) {
        String sql = SELECT_CUSTOMER + "WHERE cu.customerId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapCustomer(result));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible consultar el cliente.", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        String sql = SELECT_CUSTOMER + "ORDER BY cu.customerId";
        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                customers.add(mapCustomer(result));
            }
            return customers;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible listar los clientes.", e);
        }
    }

    @Override
    public Customer update(Customer customer) {
        Objects.requireNonNull(customer, "El cliente no puede ser null");

        String sql = "UPDATE dbo.tblCustomers SET " +
                "FirtName = ?, Lastname = ?, TypeId = ?, " +
                "TelephoneNumber = ?, Email = ?, CountryId = ?, Address = ? " +
                "WHERE customerId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setNString(1, customer.getFirstName());
            statement.setNString(2, customer.getLastName());
            statement.setString(3, customer.getDocumentType().getId());
            statement.setString(4, customer.getTelephoneNumber());
            statement.setNString(5, customer.getEmail());
            statement.setInt(6, customer.getCountry().getId());
            statement.setNString(7, customer.getAddress());
            statement.setString(8, customer.getId());

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("cliente", customer.getId());
            }

            return customer;

        } catch (SQLException e) {
            if (isDuplicate(e)) {
                throw new DuplicateException("cliente", customer.getId());
            }
            throw new PersistenceException("No fue posible actualizar el cliente.", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM dbo.tblCustomers WHERE customerId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible eliminar el cliente.", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM dbo.tblCustomers WHERE customerId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible verificar el cliente.", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM dbo.tblCustomers";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            result.next();
            return result.getLong(1);

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible contar los clientes.", e);
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String sql = SELECT_CUSTOMER + "WHERE LOWER(cu.Email) = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setNString(1, email.trim().toLowerCase(Locale.ROOT));

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapCustomer(result));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible consultar el cliente por correo.", e);
        }
    }

    @Override
    public List<Customer> findByLastName(String lastName) {
        String sought = lastName == null
                ? ""
                : lastName.trim().toLowerCase(Locale.ROOT);

        String sql = SELECT_CUSTOMER +
                "WHERE CHARINDEX(?, LOWER(cu.Lastname)) > 0 " +
                "ORDER BY cu.Lastname";

        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setNString(1, sought);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    customers.add(mapCustomer(result));
                }
            }

            return customers;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible consultar los clientes por apellido.", e);
        }
    }

    @Override
    public List<Customer> findByCountryId(Integer countryId) {
        if (countryId == null) {
            return List.of();
        }

        String sql = SELECT_CUSTOMER +
                "WHERE cu.CountryId = ? " +
                "ORDER BY cu.customerId";

        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, countryId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    customers.add(mapCustomer(result));
                }
            }

            return customers;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible consultar los clientes por país.", e);
        }
    }

    @Override
    public List<Customer> findByDocumentTypeId(String typeId) {
        if (typeId == null) {
            return List.of();
        }

        String sql = SELECT_CUSTOMER +
                "WHERE UPPER(RTRIM(cu.TypeId)) = ? " +
                "ORDER BY cu.customerId";

        List<Customer> customers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, typeId.trim().toUpperCase(Locale.ROOT));

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    customers.add(mapCustomer(result));
                }
            }

            return customers;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible consultar los clientes por tipo de documento.", e);
        }
    }

    @Override
    public boolean existsByCountryId(Integer countryId) {
        if (countryId == null) {
            return false;
        }

        String sql = "SELECT 1 FROM dbo.tblCustomers WHERE CountryId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, countryId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible verificar los clientes por país.", e);
        }
    }

    @Override
    public boolean existsByDocumentTypeId(String typeId) {
        if (typeId == null) {
            return false;
        }

        String sql = "SELECT 1 FROM dbo.tblCustomers WHERE UPPER(RTRIM(TypeId)) = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, typeId.trim().toUpperCase(Locale.ROOT));

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible verificar los clientes por tipo de documento.", e);
        }
    }

    private Customer mapCustomer(ResultSet result) throws SQLException {
        DocumentType documentType = new DocumentType(
                result.getString("DocumentTypeId").trim(),
                result.getString("DocumentTypeDescription")
        );

        Country country = new Country(
                result.getInt("CountryId"),
                result.getString("CountryName")
        );

        return new Customer(
                result.getString("CustomerId"),
                result.getNString("FirstName"),
                result.getNString("LastName"),
                documentType,
                country,
                result.getString("TelephoneNumber"),
                result.getNString("Email"),
                result.getNString("Address")
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
