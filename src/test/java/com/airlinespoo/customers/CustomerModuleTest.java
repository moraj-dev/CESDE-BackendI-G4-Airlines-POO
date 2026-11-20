
package com.airlinespoo.customers;

import com.airlinespoo.core.exception.*;
import com.airlinespoo.customers.model.*;
import com.airlinespoo.customers.repository.*;
import com.airlinespoo.customers.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CustomerModuleTest {

    private CountryRepository countries;
    private DocumentTypeRepository types;
    private CustomerRepository customers;
    private CountryService countryService;
    private DocumentTypeService typeService;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        countries = new CountryRepository();
        types = new DocumentTypeRepository();
        customers = new CustomerRepository();

        countryService = new CountryService(countries, customers);
        typeService = new DocumentTypeService(types, customers);
        service = new CustomerService(customers, countries, types);

        countries.create(new Country(1, "Colombia"));
        countries.create(new Country(2, "México"));

        types.create(new DocumentType("CC", "Cédula"));
        types.create(new DocumentType("PA", "Pasaporte"));
    }

    private Customer add(
            String doc,
            String first,
            String last,
            String email
    ) {
        return service.createCustomer(
                doc,
                first,
                last,
                "CC",
                1,
                "3001234567",
                email,
                null
        );
    }

    @Test
    void cp01_countryNameOver20IsRejected() {
        assertThrows(
                ValidationException.class,
                () -> new Country(3, "abcdefghijklmnopqrstu")
        );
    }

    @Test
    void cp02_documentTypeCodeMustHaveTwoCharacters() {
        assertThrows(
                ValidationException.class,
                () -> new DocumentType("C", "Cédula")
        );
    }

    @Test
    void cp03_documentTypeCodeIsUppercase() {
        assertEquals(
                "CC",
                new DocumentType("cc", "Cédula").getId()
        );
    }

    @Test
    void cp04_emptyCustomerNameIsRejected() {
        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "123",
                        "",
                        "Pérez",
                        types.findById("CC").orElseThrow(),
                        countries.findById(1).orElseThrow(),
                        "3001234567",
                        null,
                        null
                )
        );
    }

    @Test
    void cp05_invalidPhoneIsRejected() {
        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "123",
                        "Ana",
                        "Pérez",
                        types.findById("CC").orElseThrow(),
                        countries.findById(1).orElseThrow(),
                        "abc123",
                        null,
                        null
                )
        );
    }

    @Test
    void cp06_duplicateDocumentIsRejected() {
        add("123", "Ana", "Pérez", null);

        assertThrows(
                DuplicateException.class,
                () -> add("123", "Luis", "Díaz", null)
        );
    }

    @Test
    void cp07_duplicateEmailIsRejectedIgnoringCase() {
        add("123", "Ana", "Pérez", "ana@example.com");

        assertThrows(
                DuplicateException.class,
                () -> add("456", "Luis", "Díaz", "ANA@example.com")
        );
    }

    @Test
    void cp08_nonexistentCountryIsRejected() {
        assertThrows(
                NotFoundException.class,
                () -> service.createCustomer(
                        "123",
                        "Ana",
                        "Pérez",
                        "CC",
                        999,
                        "3001234567",
                        null,
                        null
                )
        );
    }

    @Test
    void cp09_missingCustomerLookupIsNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> service.getCustomer("missing")
        );
    }

    @Test
    void cp10_customersAreSortedByLastNameThenFirstName() {
        add("1", "Zoe", "Zulu", null);
        add("2", "Ana", "Gómez", null);
        add("3", "Luis", "Pérez", null);

        assertEquals("Gómez", service.listCustomers().get(0).getLastName());
        assertEquals("Pérez", service.listCustomers().get(1).getLastName());
        assertEquals("Zulu", service.listCustomers().get(2).getLastName());
    }

    @Test
    void cp11_updatingToAnotherCustomersEmailIsRejected() {
        add("1", "Ana", "Pérez", "ana@example.com");
        add("2", "Luis", "Díaz", "luis@example.com");

        assertThrows(
                DuplicateException.class,
                () -> service.updateCustomer(
                        "2",
                        "3007654321",
                        "ana@example.com",
                        null
                )
        );
    }

    @Test
    void cp12_updatingWithSameEmailIsAllowed() {
        add("1", "Ana", "Pérez", "ana@example.com");

        Customer updated = service.updateCustomer(
                "1",
                "3007654321",
                "ANA@example.com",
                "Calle 1"
        );

        assertEquals("ana@example.com", updated.getEmail());
        assertEquals("3007654321", updated.getTelephoneNumber());
    }

    @Test
    void deletingCountryOrDocumentTypeInUseIsBlocked() {
        add("1", "Ana", "Pérez", null);

        assertThrows(
                BusinessRuleException.class,
                () -> countryService.deleteCountry(1)
        );

        assertThrows(
                BusinessRuleException.class,
                () -> typeService.deleteDocumentType("CC")
        );
    }

    @Test
    void optionalEmptyValuesAreStoredAsNullAndDoNotCollide() {
        add("1", "Ana", "Pérez", "");
        add("2", "Luis", "Díaz", "   ");

        assertNull(service.getCustomer("1").getEmail());
        assertNull(service.getCustomer("2").getEmail());
    }

    @Test
    void sampleDataLoadsFiveCountriesFourTypesAndThreeCustomers() {
        CountryRepository c = new CountryRepository();
        DocumentTypeRepository t = new DocumentTypeRepository();
        CustomerRepository u = new CustomerRepository();

        CustomerSampleData.load(c, t, u);

        assertEquals(5, c.count());
        assertEquals(4, t.count());
        assertEquals(3, u.count());

        CustomerSampleData.load(c, t, u);

        assertEquals(3, u.count());
    }

    private static List<String> fullNames(List<Customer> list) {
        return list.stream()
                .map(Customer::getFullName)
                .toList();
    }

    @Test
    void invalidPhoneUpdateKeepsPreviousContactData() {
        service.createCustomer(
                "1",
                "Ana",
                "Pérez",
                "CC",
                1,
                "3001234567",
                "ana@example.com",
                "Calle 1"
        );

        assertThrows(
                ValidationException.class,
                () -> service.updateCustomer(
                        "1",
                        "abc",
                        "nuevo@example.com",
                        "Calle 99"
                )
        );

        Customer stored = service.getCustomer("1");

        assertEquals("3001234567", stored.getTelephoneNumber());
        assertEquals("ana@example.com", stored.getEmail());
        assertEquals("Calle 1", stored.getAddress());
    }

    @Test
    void documentTypeLookupInLowercaseFindsIt() {
        assertEquals(
                "CC",
                typeService.getDocumentType("cc").getId()
        );
    }

    @Test
    void getByEmailReturnsEmptyWhenEmailDoesNotExist() {
        assertEquals(
                Optional.empty(),
                service.getCustomerByEmail("inexistente@example.com")
        );
    }

    @Test
    void listByCountryIsSortedByLastNameThenFirstName() {
        add("1", "Zoe", "Zulu", null);
        add("2", "Pedro", "Gómez", null);
        add("3", "Ana", "Gómez", null);

        service.createCustomer(
                "4",
                "Beto",
                "Arias",
                "CC",
                2,
                "3001234567",
                null,
                null
        );

        assertEquals(
                List.of("Ana Gómez", "Pedro Gómez", "Zoe Zulu"),
                fullNames(service.listCustomersByCountry(1))
        );
    }

    @Test
    void listByDocumentTypeIsSortedByLastNameThenFirstName() {
        service.createCustomer(
                "1", "Zoe", "Zulu", "PA", 1,
                "3001234567", null, null
        );

        service.createCustomer(
                "2", "Pedro", "Gómez", "PA", 1,
                "3001234567", null, null
        );

        service.createCustomer(
                "3", "Ana", "Gómez", "PA", 1,
                "3001234567", null, null
        );

        add("4", "Beto", "Arias", null);

        assertEquals(
                List.of("Ana Gómez", "Pedro Gómez", "Zoe Zulu"),
                fullNames(service.listCustomersByDocumentType("pa"))
        );
    }
}
