
package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class CustomerModelTest {

    private final Country colombia = new Country(1, "Colombia");
    private final DocumentType cc = new DocumentType("CC", "Cédula");

    private Customer customer(String phone, String email, String address) {
        return new Customer(
                "123",
                "Ana",
                "Pérez",
                cc,
                colombia,
                phone,
                email,
                address
        );
    }

    @Test
    void customerTextLimitsAreEnforced() {
        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "x".repeat(21), "Ana", "Pérez",
                        cc, colombia, "300", null, null
                )
        );

        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "123", "a".repeat(31), "Pérez",
                        cc, colombia, "300", null, null
                )
        );

        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "123", "Ana", "b".repeat(31),
                        cc, colombia, "300", null, null
                )
        );

        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "123", "Ana", "",
                        cc, colombia, "300", null, null
                )
        );
    }

    @Test
    void customerRequiresDocumentTypeAndCountry() {
        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "123", "Ana", "Pérez",
                        null, colombia, "300", null, null
                )
        );

        assertThrows(
                ValidationException.class,
                () -> new Customer(
                        "123", "Ana", "Pérez",
                        cc, null, "300", null, null
                )
        );
    }

    @Test
    void phoneAcceptsDigitsWithOptionalLeadingPlus() {
        assertEquals(
                "+573001112233",
                customer("+573001112233", null, null).getTelephoneNumber()
        );

        assertEquals(
                "123456789012345",
                customer("123456789012345", null, null).getTelephoneNumber()
        );
    }

    @Test
    void phoneRejectsInvalidFormatsAndLength() {
        assertThrows(
                ValidationException.class,
                () -> customer("1234567890123456", null, null)
        );

        assertThrows(
                ValidationException.class,
                () -> customer("30+01234", null, null)
        );

        assertThrows(
                ValidationException.class,
                () -> customer("300 123", null, null)
        );

        assertThrows(
                ValidationException.class,
                () -> customer("+", null, null)
        );

        assertThrows(
                ValidationException.class,
                () -> customer("", null, null)
        );
    }

    @Test
    void emailIsOptionalLowercasedAndValidated() {
        assertNull(customer("300", null, null).getEmail());

        assertNull(customer("300", "  ", null).getEmail());

        assertEquals(
                "ana@example.com",
                customer("300", " ANA@Example.com ", null).getEmail()
        );

        assertThrows(
                ValidationException.class,
                () -> customer("300", "ana@", null)
        );

        assertThrows(
                ValidationException.class,
                () -> customer("300", "ana example.com", null)
        );

        String tooLong = "a".repeat(69) + "@example.com";

        assertThrows(
                ValidationException.class,
                () -> customer("300", tooLong, null)
        );
    }

    @Test
    void addressIsOptionalAndMax40() {
        assertNull(customer("300", null, "").getAddress());

        assertEquals(
                "Calle 1",
                customer("300", null, " Calle   1 ").getAddress()
        );

        assertThrows(
                ValidationException.class,
                () -> customer("300", null, "x".repeat(41))
        );
    }

    @Test
    void contactSettersAreNotPublic() throws NoSuchMethodException {
        for (String name : new String[]{
                "setTelephoneNumber",
                "setEmail",
                "setAddress"
        }) {
            Method setter = Customer.class.getDeclaredMethod(
                    name, String.class
            );

            assertTrue(Modifier.isPrivate(setter.getModifiers()));
        }
    }

    @Test
    void toStringDoesNotExposeContactData() {
        String text = customer(
                "3001234567",
                "ana@example.com",
                "Calle 1"
        ).toString();

        assertFalse(text.contains("3001234567"));
        assertFalse(text.contains("ana@example.com"));
        assertFalse(text.contains("Calle 1"));
    }

    @Test
    void gettersReturnCustomerInformation() {
        Customer customer = customer(
                "3001234567",
                "ana@example.com",
                "Calle 1"
        );

        assertEquals("123", customer.getId());
        assertEquals("123", customer.getDocumentId());
        assertEquals("Ana", customer.getFirstName());
        assertEquals("Pérez", customer.getLastName());
        assertEquals("Ana Pérez", customer.getFullName());
        assertEquals(cc, customer.getDocumentType());
        assertEquals(colombia, customer.getCountry());
    }
}
