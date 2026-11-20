package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;

import java.util.Comparator;
import java.util.List;

public class CountryService {

    private final CountryRepository countries;
    private final CustomerRepository customers;

    public CountryService(CountryRepository countries, CustomerRepository customers) {
        this.countries = Validator.requireNotNull(countries, "repositorio de países");
        this.customers = Validator.requireNotNull(customers, "repositorio de clientes");
    }

    public Country createCountry(int id, String name) {
        return countries.create(new Country(id, name));
    }

    public Country getCountry(int id) {
        return countries.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe el país con id '" + id + "'."
                ));
    }

    public List<Country> listCountries() {
        return countries.findAll().stream()
                .sorted(Comparator.comparing(
                        Country::getName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
    }

    public Country updateCountry(int id, String name) {
        Country country = getCountry(id);
        country.setName(name);
        return countries.update(country);
    }

    public void deleteCountry(int id) {
        Country country = getCountry(id);

        if (customers.existsByCountryId(id)) {
            throw new BusinessRuleException(
                    "No se puede eliminar el país porque tiene clientes asociados."
            );
        }

        countries.deleteById(country.getId());
    }

    public long count() {
        return countries.count();
    }
}