package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.Country;

public class CountryRepository extends InMemoryRepository<Country, Integer> {

    @Override
    protected String entityName() {
        return "país";
    }
}