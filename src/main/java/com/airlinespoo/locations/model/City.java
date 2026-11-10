package com.airlinespoo.locations.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;


public class City implements Identifiable<Integer> {
    private final int id;
    private String name;

    public City(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        // Valida que el dato no sea null o este vacio
        String validatedName = Validator.requireText(name, "nombre de ciudad");

        // Valida que el dato no tenga mas de 20 caracteres
        if (validatedName.trim().length() > 20) {
            throw new IllegalArgumentException("El nombre de la ciudad no puede superar los 20 caracteres.");
        }
        // Normalización elimina espacios sobrantes al inicio o al final
        this.name = validatedName.trim();
    }
    @Override
    public boolean equals(Object o) { //Equals se usa para comparar dos objetos o saber si son iguales
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return id == city.id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public String toString() {
        return "City{id=" + id + ", name='" + name + "'}";
    }
}
