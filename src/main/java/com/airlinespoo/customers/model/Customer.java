
package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class Customer implements Identifiable<String> {
    private static final int MAX_DOCUMENT_LENGTH = 20;
    private static final int MAX_FIRST_NAME_LENGTH = 30;
    private static final int MAX_LAST_NAME_LENGTH = 30;
    private static final int MAX_TELEPHONE_LENGTH = 15;
    private static final int MAX_EMAIL_LENGTH = 80;
    private static final int MAX_ADDRESS_LENGTH = 40;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+\\d{1,14}|\\d{1,15})$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String id;
    private final String firstName;
    private final String lastName;
    private final DocumentType documentType;
    private final Country country;
    private String telephoneNumber;
    private String email;
    private String address;

    public Customer(String id, String firstName, String lastName,
                    DocumentType documentType, Country country,
                    String telephoneNumber, String email, String address) {
        this.id = Validator.requireText(id, "documento del cliente", MAX_DOCUMENT_LENGTH);
        this.firstName = Validator.requireText(firstName, "nombre del cliente", MAX_FIRST_NAME_LENGTH);
        this.lastName = Validator.requireText(lastName, "apellido del cliente", MAX_LAST_NAME_LENGTH);
        this.documentType = Validator.requireNotNull(documentType, "tipo de documento");
        this.country = Validator.requireNotNull(country, "país");
        setTelephoneNumber(telephoneNumber);
        setEmail(email);
        setAddress(address);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getDocumentId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public Country getCountry() {
        return country;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    private void setTelephoneNumber(String telephoneNumber) {
        String value = Validator.requireText(telephoneNumber, "teléfono", MAX_TELEPHONE_LENGTH);

        if (!PHONE_PATTERN.matcher(value).matches()) {
            throw new ValidationException("El teléfono debe contener dígitos y puede iniciar con +.");
        }

        this.telephoneNumber = value;
    }

    private void setEmail(String email) {
        String value = normalizeOptional(email);

        if (value != null) {
            value = value.toLowerCase(Locale.ROOT);

            if (value.length() > MAX_EMAIL_LENGTH || !EMAIL_PATTERN.matcher(value).matches()) {
                throw new ValidationException(
                        "El correo debe tener un formato válido y máximo %d caracteres."
                                .formatted(MAX_EMAIL_LENGTH));
            }
        }

        this.email = value;
    }

    private void setAddress(String address) {
        this.address = Validator.requireMaxLength(
                normalizeOptional(address),
                MAX_ADDRESS_LENGTH,
                "dirección"
        );
    }

    private static String normalizeOptional(String value) {
        String normalized = Validator.normalize(value);
        return normalized == null || normalized.isBlank() ? null : normalized;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Customer customer)) {
            return false;
        }

        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Customer{fullName='%s', documentType='%s', country='%s'}"
                .formatted(getFullName(), documentType.getId(), country.getName());
    }
}
