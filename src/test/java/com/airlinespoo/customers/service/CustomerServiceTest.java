
package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerServiceTest {

    private CustomerService service;

    @BeforeEach
    void setUp() {
        CountryRepository countries = new CountryRepository();
        DocumentTypeRepository types = new DocumentTypeRepository();
        CustomerRepository customers = new CustomerRepository();

        countries.create(new Country(1, "Colombia"));
        countries.create(new Country(2, "México"));

        types.create(new DocumentType("CC", "Cédula"));
        types.create(new DocumentType("PA", "Pasaporte"));

        service = new CustomerService(customers, countries, types);
    }

    private static List<String> fullNames(List<Customer> list) {
        return list.stream()
                .map(Customer::getFullName)
                .toList();
    }

    private void addUnsorted(String type, int country) {
        service.createCustomer("10", "Zoe", "Zulu", type, country, "300", null, null);
        service.createCustomer("11", "Pedro", "Gómez", type, country, "300", null, null);
        service.createCustomer("12", "Ana", "Gómez", type, country, "300", null, null);
        service.createCustomer("13", "Luis", "Pérez", type, country, "300", null, null);
    }

    @Test
    void getByEmailFindsCustomerIgnoringCaseAndHandlesBlank() {
        service.createCustomer(
                "1", "Ana", "Pérez", "CC", 1,
                "300", "ana@example.com", null
        );

        assertEquals(
                "1",
                service.getCustomerByEmail(" ANA@EXAMPLE.COM ")
                        .orElseThrow().getId()
        );

        assertTrue(service.getCustomerByEmail(null).isEmpty());
        assertTrue(service.getCustomerByEmail("   ").isEmpty());
    }

    @Test
    void listAndSearchUseSameOrder() {
        addUnsorted("CC", 1);

        List<String> expected = List.of(
                "Ana Gómez",
                "Pedro Gómez",
                "Luis Pérez",
                "Zoe Zulu"
        );

        assertEquals(expected, fullNames(service.listCustomers()));

        assertEquals(
                List.of("Ana Gómez", "Pedro Gómez"),
                fullNames(service.searchCustomersByLastName("gó"))
        );
    }

    @Test
    void registerAcceptsLowercaseDocumentType() {
        assertEquals(
                "CC",
                service.createCustomer(
                        "1", "Ana", "Pérez", "cc", 1,
                        "300", null, null
                ).getDocumentType().getId()
        );
    }

    @Test
    void nonexistentDocumentTypeIsRejected() {
        assertThrows(
                NotFoundException.class,
                () -> service.createCustomer(
                        "1", "Ana", "Pérez", "TI", 1,
                        "300", null, null
                )
        );

        assertThrows(
                NotFoundException.class,
                () -> service.listCustomersByDocumentType("TI")
        );

        assertThrows(
                NotFoundException.class,
                () -> service.listCustomersByCountry(99)
        );
    }

    @Test
    void emptyEmailAndAddressOnUpdateRemoveThem() {
        service.createCustomer(
                "1", "Ana", "Pérez", "CC", 1,
                "300", "ana@example.com", "Calle 1"
        );

        Customer updated = service.updateCustomer("1", "311", "", "");

        assertNull(updated.getEmail());
        assertNull(updated.getAddress());
        assertNull(service.getCustomer("1").getEmail());
    }

    @Test
    void deleteRemovesCustomerAndMissingIsNotFound() {
        service.createCustomer(
                "1", "Ana", "Pérez", "CC", 1,
                "300", null, null
        );

        service.deleteCustomer("1");

        assertEquals(0, service.countCustomers());

        assertThrows(
                NotFoundException.class,
                () -> service.deleteCustomer("1")
        );
    }
}
