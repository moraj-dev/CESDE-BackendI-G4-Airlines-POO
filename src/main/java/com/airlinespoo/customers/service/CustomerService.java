
package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CustomerService {

    private static final Comparator<Customer> BY_NAME =
            Comparator.comparing(Customer::getLastName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Customer::getFirstName, String.CASE_INSENSITIVE_ORDER);

    private final CustomerRepository customers;
    private final CountryRepository countries;
    private final DocumentTypeRepository documentTypes;

    public CustomerService(
            CustomerRepository customers,
            CountryRepository countries,
            DocumentTypeRepository documentTypes
    ) {
        this.customers = Validator.requireNotNull(customers, "repositorio de clientes");
        this.countries = Validator.requireNotNull(countries, "repositorio de países");
        this.documentTypes = Validator.requireNotNull(documentTypes, "repositorio de tipos de documento");
    }

    public Customer createCustomer(
            String document,
            String firstName,
            String lastName,
            String documentTypeId,
            int countryId,
            String telephoneNumber,
            String email,
            String address
    ) {
        String id = Validator.requireText(document, "documento del cliente");

        if (customers.existsById(id)) {
            throw new DuplicateException("Ya existe un cliente con ese documento.");
        }

        DocumentType type = documentTypes.findById(
                Validator.requireText(documentTypeId, "tipo de documento").toUpperCase(Locale.ROOT)
        ).orElseThrow(() -> new NotFoundException("No existe el tipo de documento indicado."));

        Country country = countries.findById(countryId)
                .orElseThrow(() -> new NotFoundException("No existe el país indicado."));

        String normalizedEmail = normalizeEmail(email);
        ensureEmailAvailable(normalizedEmail, null);

        return customers.create(new Customer(
                id,
                firstName,
                lastName,
                type,
                country,
                telephoneNumber,
                normalizedEmail,
                address
        ));
    }

    public Customer getCustomer(String document) {
        String id = Validator.requireText(document, "documento del cliente");

        return customers.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No se encontró el cliente con documento '" + id + "'."
                ));
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        String normalized = normalizeEmail(email);

        if (normalized == null) {
            return Optional.empty();
        }

        return customers.findByEmail(normalized);
    }

    public Customer updateCustomer(
            String document,
            String telephoneNumber,
            String email,
            String address
    ) {
        Customer customer = getCustomer(document);
        String normalizedEmail = normalizeEmail(email);

        ensureEmailAvailable(normalizedEmail, customer.getId());

        Customer replacement = new Customer(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getDocumentType(),
                customer.getCountry(),
                telephoneNumber,
                normalizedEmail,
                address
        );

        return customers.update(replacement);
    }

    public List<Customer> listCustomers() {
        return customers.findAll().stream()
                .sorted(BY_NAME)
                .toList();
    }

    public List<Customer> searchCustomersByLastName(String fragment) {
        String query = Validator.requireText(fragment, "apellido a buscar");

        return customers.findByLastName(query).stream()
                .sorted(BY_NAME)
                .toList();
    }

    public List<Customer> listCustomersByCountry(int countryId) {
        if (!countries.existsById(countryId)) {
            throw new NotFoundException("No existe el país indicado.");
        }

        return customers.findByCountryId(countryId).stream()
                .sorted(BY_NAME)
                .toList();
    }

    public List<Customer> listCustomersByDocumentType(String typeId) {
        String key = Validator.requireText(typeId, "tipo de documento")
                .toUpperCase(Locale.ROOT);

        if (!documentTypes.existsById(key)) {
            throw new NotFoundException("No existe el tipo de documento indicado.");
        }

        return customers.findByDocumentTypeId(key).stream()
                .sorted(BY_NAME)
                .toList();
    }

    public void deleteCustomer(String document) {
        Customer customer = getCustomer(document);
        customers.deleteById(customer.getId());
    }

    public long countCustomers() {
        return customers.count();
    }

    private void ensureEmailAvailable(String email, String currentCustomerId) {
        if (email == null) {
            return;
        }

        customers.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(currentCustomerId)) {
                throw new DuplicateException(
                        "El correo electrónico ya está registrado por otro cliente."
                );
            }
        });
    }

    private String normalizeEmail(String email) {
        String normalized = Validator.normalize(email);

        return normalized == null || normalized.isBlank()
                ? null
                : normalized.toLowerCase(Locale.ROOT);
    }
}
