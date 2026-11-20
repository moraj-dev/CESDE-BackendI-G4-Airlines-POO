
package com.airlinespoo.customers.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

public class Country implements Identifiable<Integer> {
    private static final int MAX_NAME_LENGTH = 20;
    private final Integer id;
    private String name;

    public Country(int id, String name) {
        this.id = (int) Validator.requirePositive(id, "id del país");
        setName(name);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Validator.requireText(name, "nombre del país", MAX_NAME_LENGTH);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Country country)) return false;
        return Objects.equals(id, country.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Country{id=%d, name='%s'}".formatted(id, name);
    }
}
