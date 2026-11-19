package com.airlinespoo.locations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.locations.model.City;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CityRepository extends InMemoryRepository<City, Integer> {
    @Override
    protected String entityName() {
        return "Ciudad";
    }
    public Optional<City> findByName(String name) {
        return findAll().stream().filter(c -> c.getName().equalsIgnoreCase(name.trim())).findFirst();
    }
    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }
    public List<City> findByNameContaining(String text) {
        String query = text.toLowerCase().trim();
        return findAll().stream().filter(c -> c.getName().toLowerCase().contains(query)).collect(Collectors.toList());

    }
}
