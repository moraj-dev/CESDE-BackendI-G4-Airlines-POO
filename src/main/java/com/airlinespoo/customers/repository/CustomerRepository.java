package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.Customer;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CustomerRepository extends InMemoryRepository<Customer, String> {

    @Override
    protected String entityName() {
        return "cliente";
    }

    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String sought = email.trim().toLowerCase(Locale.ROOT);

        return values().stream()
                .filter(c -> c.getEmail() != null
                        && c.getEmail().equalsIgnoreCase(sought))
                .findFirst();
    }

    public List<Customer> findByLastName(String lastName) {
        String sought = lastName == null
                ? ""
                : lastName.trim().toLowerCase(Locale.ROOT);

        return values().stream()
                .filter(c -> c.getLastName()
                        .toLowerCase(Locale.ROOT).contains(sought))
                .toList();
    }

    public List<Customer> findByCountryId(Integer countryId) {
        return values().stream()
                .filter(c -> c.getCountry().getId().equals(countryId))
                .toList();
    }

    public List<Customer> findByDocumentTypeId(String typeId) {
        return values().stream()
                .filter(c -> c.getDocumentType().getId()
                        .equalsIgnoreCase(typeId))
                .toList();
    }

    public boolean existsByCountryId(Integer countryId) {
        return values().stream()
                .anyMatch(c -> c.getCountry().getId().equals(countryId));
    }

    public boolean existsByDocumentTypeId(String typeId) {
        return values().stream()
                .anyMatch(c -> c.getDocumentType().getId()
                        .equalsIgnoreCase(typeId));
    }
}