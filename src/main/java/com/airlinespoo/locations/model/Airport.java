package com.airlinespoo.locations.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;
import java.util.UUID;

public class Airport implements Identifiable<UUID>{
    private final UUID id;
    private String name;
    private City city;

//Constructor para nuevo aeropuerto (Genera UUID automatico)
public Airport(String name, City city){
    this.id = UUID.randomUUID();
    setName(name);
    setCity(city);
}
//Constructor para un aeropuerto existente
public Airport (UUID id, String name, City city) {
    this.id = Objects.requireNonNull(id, "El ID del aeropuerto no puede ser nulo");
    setName(name);
    setCity(city);
}
    @Override
    public UUID getId() {
    return id;
}
    public String getName() {
    return name;
}
    public void setName(String name) {
    this.name = Validator.requireText(name, "nombre de aeropuerto");
}
    public City getCity() {
    return city;
}
    public void setCity(City city) {
    this.city = Objects.requireNonNull(city, "La ciudad del aeropuerto no puede ser nula");
}

@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Airport airport = (Airport) o;
    return id.equals(airport.id);
}

@Override
public int hashCode() {
    return Objects.hash(id);
}

@Override
public String toString() {
    return "Airport{id=" + id + ", name='" + name + "', city=" + (city != null ? city.getName() : "null") + "}";
}
}
