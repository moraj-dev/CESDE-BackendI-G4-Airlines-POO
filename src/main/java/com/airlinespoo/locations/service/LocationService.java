package com.airlinespoo.locations.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LocationService {

    private final CityRepository CityRepository;
    private final AirportRepository AirportRepository;
    private final IdGenerator cityIds;

    public LocationService(CityRepository cityRepository, AirportRepository airportRepository) {
        this.CityRepository = cityRepository;
        this.AirportRepository = airportRepository;
        this.cityIds = new IdGenerator(1, 1);
    }
    //Ciudades
    public City createCity(String name) {
        ensureCityNameAvailable(name, null);
        int id = cityIds.next();
        City city = new City(id, name);
        return CityRepository.create(city);
    }

    public City getCity(int id) {
        return CityRepository.findById(id).orElseThrow(() -> new NotFoundException("ciudad", id));
    }
    public List<City> listCities() {
        return CityRepository.findAll().stream()
            .sorted(Comparator.comparing(City::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public City updateCity(int id, String name) {
        City city = getCity(id);
        ensureCityNameAvailable(name, id);
        city.setName(name);
        return CityRepository.update(city);
    }

    public void deleteCity(int id) {
        City city = getCity(id);
        long airports = AirportRepository.countByCity(city);
        if (airports > 0) {
            throw new BusinessRuleException(
                "No se puede eliminar la ciudad '" + city.getName() + "' porque tiene " + airports + " aeropuerto(s) asociado(s)."
            );
        }
        CityRepository.deleteById(id);
    }

    public List<City> searchCitiesByName(String text) {
        return CityRepository.findByNameContaining(text).stream()
            .sorted(Comparator.comparing(City::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    //Aeropuertos
    public Airport createAirport(String name, int cityId) {
        City city = getCity(cityId);
        ensureAirportNameAvailable(city, name, null);
        Airport airport = new Airport(name, city);
        return AirportRepository.create(airport);
    }

    public Airport getAirport(UUID id) {
        return AirportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("aeropuerto", id));
    }

    public List<Airport> listAirports() {
        return AirportRepository.findAll().stream()
            .sorted(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public Airport updateAirport(UUID id, String name, int cityId) {
        Airport airport = getAirport(id);
        City city = getCity(cityId);
        ensureAirportNameAvailable(city, name, id);
        airport.setName(name);
        airport.setCity(city);
        return AirportRepository.update(airport);
    }

    public void deleteAirport(UUID id) {
        getAirport(id);
        AirportRepository.deleteById(id);
    }

    public List<Airport> listAirportsByCity(int cityId) {
        City city = getCity(cityId);
        return AirportRepository.findByCity(city).stream()
            .sorted(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public List<Airport> searchAirportsByName(String text) {
        return AirportRepository.findByNameContaining(text).stream()
            .sorted(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

    public long countAirportsByCity(int cityId) {
        City city = getCity(cityId);
        return AirportRepository.countByCity(city);
    }

    //Reglas privadas

    private void ensureCityNameAvailable(String name, Integer ownerId) {
        if (name == null || name.trim().isEmpty()) {
            return; // Dejará que City / Validator maneje el texto vacío o nulo
        }
        String normalized = name.trim();
        CityRepository.findByName(normalized).ifPresent(existing -> {
            if (ownerId == null || !existing.getId().equals(ownerId)) {
                throw new DuplicateException("Ya existe una ciudad registrada con el nombre '" + normalized + "'.");
            }
        });
    }

    private void ensureAirportNameAvailable(City city, String name, UUID ownerId) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        String normalized = name.trim();
        boolean duplicate = AirportRepository.findByCity(city).stream()
            .anyMatch(a -> a.getName().equalsIgnoreCase(normalized)
                && (ownerId == null || !a.getId().equals(ownerId)));
        if (duplicate) {
            throw new DuplicateException(
                "Ya existe un aeropuerto con el nombre '" + normalized + "' en la ciudad '" + city.getName() + "'."
            );
        }
    }
}


