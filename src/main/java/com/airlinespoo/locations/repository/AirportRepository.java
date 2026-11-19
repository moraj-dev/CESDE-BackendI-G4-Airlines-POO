package com.airlinespoo.locations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AirportRepository extends InMemoryRepository<Airport, UUID> {

    @Override
    protected String entityName() {
        return "aeropuerto";
    }

    public List<Airport> findByCity(City city) {
        return findAll().stream()
            .filter(a -> a.getCity().equals(city))
            .collect(Collectors.toList());
    }

    public List<Airport> findByNameContaining(String text) {
        String query = text.toLowerCase().trim();
        return findAll().stream()
            .filter(a -> a.getName().toLowerCase().contains(query))
            .collect(Collectors.toList());
    }

    public boolean existsByCityAndName(City city, String name) {
        String query = name.trim();
        return findByCity(city).stream()
            .anyMatch(a -> a.getName().equalsIgnoreCase(query));
    }

    public boolean existsByCity(City city) {
        return !findByCity(city).isEmpty();
    }

    public long countByCity(City city) {
        return findByCity(city).size();
    }
}

