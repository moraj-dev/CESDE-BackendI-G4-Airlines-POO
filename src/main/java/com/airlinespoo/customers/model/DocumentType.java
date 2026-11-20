
package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Locale;
import java.util.Objects;

public class DocumentType implements Identifiable<String> {
    private static final int CODE_LENGTH = 2;
    private static final int MAX_DESCRIPTION_LENGTH = 20;
    private final String id;
    private String description;

    public DocumentType(String id, String description) {
        String normalized = Validator.requireText(id, "código del tipo de documento");
        normalized = normalized.toUpperCase(Locale.ROOT);

        if (normalized.length() != CODE_LENGTH) {
            throw new ValidationException("El código del tipo de documento debe tener exactamente 2 caracteres.");
        }

        this.id = normalized;
        setDescription(description);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Validator.requireText(
            description,
            "descripción del tipo de documento",
            MAX_DESCRIPTION_LENGTH
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DocumentType that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DocumentType{id='%s', description='%s'}".formatted(id, description);
    }
}
