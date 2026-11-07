package com.airlinespoo.airlines.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de aerolíneas.
 * Coordina las validaciones de unicidad, generación de IDs y persistencia.
 */
public class AirlineService {

private final AirlineRepository repository;
private final IdGenerator idGenerator;

/**
 * Constructor con inyección de dependencias.
 *
 * @param repository  Repositorio para el acceso a datos.
 * @param idGenerator Generador de IDs configurado (arranque 100, paso 10).
 */
public AirlineService(AirlineRepository repository, IdGenerator idGenerator) {
    this.repository = Validator.requireNotNull(repository, "repositorio de aerolíneas");
    this.idGenerator = Validator.requireNotNull(idGenerator, "generador de IDs");
}

public Airline createAirline(String name) {
    Validator.requireText(name, "nombre de la aerolínea", Airline.MAX_NAME_LENGTH);
    if (repository.existsByName(name)) {
        throw new IllegalArgumentException("Ya existe una aerolínea con el nombre: " + name);
    }
    int id = idGenerator.next();
    Airline airline = new Airline(id, name);
    return repository.create(airline);
}

public Airline getAirline(int id) {
    return repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("No se encontró la aerolínea con ID: " + id));
}

public List<Airline> listAirlines() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(Airline::getName, String.CASE_INSENSITIVE_ORDER))
        .toList();
}

public Airline renameAirline(int id, String newName) {
    Airline existing = getAirline(id);
    Validator.requireText(newName, "nombre de la aerolínea", Airline.MAX_NAME_LENGTH);

    repository.findByName(newName).ifPresent(other -> {
        if (!other.getId().equals(existing.getId())) {
            throw new IllegalArgumentException("Ya existe otra aerolínea con el nombre: " + newName);
        }
    });

    existing.setName(newName);
    return repository.update(existing);
}

public boolean deleteAirline(int id) {
    getAirline(id);
    return repository.deleteById(id);
}

public List<Airline> searchAirlinesByName(String query) {
    if (query == null || query.isBlank()) {
        return List.of();
    }
    String cleanQuery = query.trim().toLowerCase();
    return repository.findAll().stream()
        .filter(airline -> airline.getName().toLowerCase().contains(cleanQuery))
        .sorted(Comparator.comparing(Airline::getName, String.CASE_INSENSITIVE_ORDER))
        .toList();
}

public long countAirlines() {
    return repository.count();
}
}
