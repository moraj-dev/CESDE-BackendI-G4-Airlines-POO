
package com.airlinespoo.customers.service;

import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;

public final class CustomerSampleData {

    private CustomerSampleData() {
    }

    public static void load(
            CountryRepository countries,
            DocumentTypeRepository types,
            CustomerRepository customers
    ) {
        if (countries.count() == 0) {
            countries.create(new Country(1, "Colombia"));
            countries.create(new Country(2, "México"));
            countries.create(new Country(3, "Argentina"));
            countries.create(new Country(4, "Chile"));
            countries.create(new Country(5, "Perú"));
        }

        if (types.count() == 0) {
            types.create(new DocumentType("CC", "Cédula de ciudadanía"));
            types.create(new DocumentType("CE", "Cédula extranjería"));
            types.create(new DocumentType("PA", "Pasaporte"));
            types.create(new DocumentType("TI", "Tarjeta de identidad"));
        }

        if (customers.count() == 0) {
            var colombia = countries.findById(1).orElseThrow();
            var cc = types.findById("CC").orElseThrow();
            var pa = types.findById("PA").orElseThrow();

            customers.create(new Customer(
                    "1001001001",
                    "Laura",
                    "Gómez",
                    cc,
                    colombia,
                    "+573001112233",
                    "laura.gomez@example.com",
                    "Medellín"
            ));

            customers.create(new Customer(
                    "1002002002",
                    "Andrés",
                    "Pérez",
                    cc,
                    colombia,
                    "3002223344",
                    "andres.perez@example.com",
                    "Bogotá"
            ));

            customers.create(new Customer(
                    "AB123456",
                    "Camila",
                    "Restrepo",
                    pa,
                    colombia,
                    "+573104445566",
                    null,
                    null
            ));
        }
    }
}
