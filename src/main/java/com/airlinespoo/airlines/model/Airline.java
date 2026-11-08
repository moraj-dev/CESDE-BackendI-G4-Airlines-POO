package com.airlinespoo.airlines.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;
import java.util.Objects;

/**
 * Representa una aerolínea dentro del sistema.
 * Corresponde a la tabla tblAirlines del modelo de datos.
 */
public class Airline implements Identifiable<Integer> {

// Constante para evitar números mágicos (VARCHAR(50) en tblAirlines)
public static final int MAX_NAME_LENGTH = 50;

private final int id;
private String name;

/**
 * Constructor que deja la entidad en un estado 100% válido.
 *
 * @param id   Identificador numérico asignado por el generador (ej. 100, 110...).
 * @param name Nombre de la aerolínea (máximo 50 caracteres, no vacío).
 */
public Airline(int id, String name) {
    // Valida que el ID sea mayor que cero
    Validator.requirePositive(id, "ID de aerolínea");
    this.id = id;
    setName(name); // Reutiliza la validación y normalización del setter
}

@Override
public Integer getId() {
    return this.id;
}

public String getName() {
    return this.name;
}

/**
 * Permite modificar el nombre garantizando las reglas de negocio.
 *
 * @param name Nuevo nombre a asignar.
 */
public void setName(String name) {
    // requireText verifica no nulo, normaliza espacios y valida longitud máxima
    this.name = Validator.requireText(name, "Nombre de la aerolínea", MAX_NAME_LENGTH);
}

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Airline airline = (Airline) o;
    return id == airline.id; // La identidad se define por su clave primaria
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
//
    @Override
    public String toString() {
        return "Aerolínea [ID: %d, Nombre: %s]".formatted(id, name);
    }
}
