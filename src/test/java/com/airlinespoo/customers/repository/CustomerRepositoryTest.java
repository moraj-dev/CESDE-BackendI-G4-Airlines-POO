package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRepositoryTest {

    private final Country colombia = new Country(1, "Colombia");
    private final Country mexico = new Country(2, "México");
    private final DocumentType cc = new DocumentType("CC", "Cédula");
    private final DocumentType pa = new DocumentType("PA", "Pasaporte");

    private CustomerRepository customers;

    @BeforeEach
    void setUp() {
        customers = new CustomerRepository();

        customers.create(new Customer(
                "1", "Ana", "Pérez", cc, colombia,
                "300", "ana@example.com", null
        ));

        customers.create(new Customer(
                "2", "Luis", "Gómez", pa, mexico,
                "301", null, null
        ));
    }

    @Test
    void catalogRepositoriesStoreAndRejectDuplicateIds() {
        CountryRepository countries = new CountryRepository();
        DocumentTypeRepository types = new DocumentTypeRepository();

        countries.create(colombia);
        types.create(cc);

        assertTrue(countries.existsById(1));
        assertTrue(types.existsById("CC"));

        assertThrows(
                DuplicateException.class,
                () -> countries.create(new Country(1, "Otro"))
        );

        assertThrows(
                DuplicateException.class,
                () -> types.create(new DocumentType("cc", "Otro"))
        );
    }

    @Test
    void findByEmailIgnoresCaseAndBlank() {
        assertEquals(
                "1",
                customers.findByEmail("  ANA@example.COM ")
                        .orElseThrow().getId()
        );

        assertTrue(customers.findByEmail("").isEmpty());
        assertTrue(customers.findByEmail(null).isEmpty());
        assertTrue(customers.findByEmail("otro@example.com").isEmpty());
    }

    @Test
    void findByLastNameMatchesFragmentIgnoringCase() {
        List<Customer> found = customers.findByLastName("PÉR");

        assertEquals(1, found.size());
        assertEquals("1", found.get(0).getId());
    }

    @Test
    void filtersAndExistenceByCountryAndDocumentType() {
        assertEquals(1, customers.findByCountryId(2).size());
        assertEquals(1, customers.findByDocumentTypeId("PA").size());

        assertTrue(customers.existsByCountryId(1));
        assertFalse(customers.existsByCountryId(99));

        assertTrue(customers.existsByDocumentTypeId("CC"));
        assertFalse(customers.existsByDocumentTypeId("TI"));
    }

    @Test
    void findAllReturnsCopy() {
        customers.findAll().clear();

        assertEquals(2, customers.count());
    }
}