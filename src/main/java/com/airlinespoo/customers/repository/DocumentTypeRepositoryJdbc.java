package com.airlinespoo.customers.repository;

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
import java.util.Objects;
import java.util.Optional;

public class DocumentTypeRepositoryJdbc extends DocumentTypeRepository {

    @Override
    public DocumentType create(DocumentType documentType) {
        Objects.requireNonNull(documentType, "El tipo de documento no puede ser null");

        String sql = "INSERT INTO tblDocumentsType (TypeId, Description) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, documentType.getId());
            statement.setString(2, documentType.getDescription());
            statement.executeUpdate();

            return documentType;

        } catch (SQLException e) {
            if (isDuplicate(e)) {
                throw new DuplicateException("tipo de documento", documentType.getId());
            }
            throw new PersistenceException("No fue posible crear el tipo de documento.", e);
        }
    }

    @Override
    public Optional<DocumentType> findById(String id) {
        String sql = "SELECT TypeId, Description FROM tblDocumentsType WHERE TypeId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapDocumentType(result));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible consultar el tipo de documento.", e);
        }
    }

    @Override
    public List<DocumentType> findAll() {
        String sql = "SELECT TypeId, Description FROM tblDocumentsType ORDER BY TypeId";
        List<DocumentType> documentTypes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                documentTypes.add(mapDocumentType(result));
            }

            return documentTypes;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible listar los tipos de documento.", e);
        }
    }

    @Override
    public DocumentType update(DocumentType documentType) {
        Objects.requireNonNull(documentType, "El tipo de documento no puede ser null");

        String sql = "UPDATE tblDocumentsType SET Description = ? WHERE TypeId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, documentType.getDescription());
            statement.setString(2, documentType.getId());

            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("tipo de documento", documentType.getId());
            }

            return documentType;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible actualizar el tipo de documento.", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM tblDocumentsType WHERE TypeId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible eliminar el tipo de documento.", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM tblDocumentsType WHERE TypeId = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible verificar el tipo de documento.", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM tblDocumentsType";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            result.next();
            return result.getLong(1);

        } catch (SQLException e) {
            throw new PersistenceException("No fue posible contar los tipos de documento.", e);
        }
    }

    private DocumentType mapDocumentType(ResultSet result) throws SQLException {
        return new DocumentType(
                result.getString("TypeId").trim(),
                result.getString("Description")
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
