package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.InMemoryRepository;
import java.util.Optional;

/**
 * Repositorio en memoria para la entidad Airline.
 * Hereda las operaciones CRUD básicas de InMemoryRepository y agrega
 * las búsquedas por nombre requeridas por el negocio.
 */
public class AirlineRepository extends InMemoryRepository<Airline, Integer> {

/**
 * Define el nombre de la entidad administrada por este repositorio.
 * Es utilizado internamente por la clase base para construir los mensajes de error.
 *
 * @return El nombre legible de la entidad ("aerolínea").
 */
@Override
protected String entityName() {
    return "aerolínea";
}

/**
 * Busca una aerolínea por su nombre exacto, sin distinguir mayúsculas de minúsculas.
 * Equivale a: SELECT * FROM tblAirlines WHERE LOWER(name) = LOWER(?)
 *
 * @param name Nombre de la aerolínea a buscar.
 * @return un Optional con la aerolínea si existe, o vacío si no se encuentra.
 */
public Optional<Airline> findByName(String name) {
    if (name == null) {
        return Optional.empty();
    }
    return findAll().stream()
        .filter(airline -> airline.getName().equalsIgnoreCase(name.trim()))
        .findFirst();
}

/**
 * Comprueba si ya existe una aerolínea con el nombre especificado.
 * Equivale a: SELECT 1 FROM tblAirlines WHERE LOWER(name) = LOWER(?)
 *
 * @param name Nombre a comprobar.
 * @return true si ya existe, false en caso contrario.
 */
public boolean existsByName(String name) {
    return findByName(name).isPresent();
}
}
