package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/* Clase inmutable para la establecer la Silla de un vuelo
NÚMERO + LETRA DE FILA
rowNumber es una letra del alfabeto
seatNumber es el número de la fila
*/

public final class Seat {

    private static final Pattern FORMAT = Pattern.compile("(\\d{1,2})([A-HJK])");
    private static final int MAX_LENGTH_ROW = 2;

    private final String rowNumber;
    private final int seatNumber;

    public Seat (int seatNumber, String rowNumber) {
        this.seatNumber = (int) Validator.requirePositive(seatNumber,"número de la silla");
        this.rowNumber = Validator.requireText(rowNumber,"fila de la silla",MAX_LENGTH_ROW).toUpperCase();
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    // Convierte un String en un Seat, siempre que cumpla el formato de Seat
    public static Seat parse(String text) {
        String clean = Validator.requireText(text, "silla").trim().toUpperCase();
        Matcher m = FORMAT.matcher(clean);
        if (!m.matches()) {
            throw new ValidationException("La silla debe escribirse como número y letra, por ejemplo 15A.");
        }
        return new Seat(Integer.parseInt(m.group(1)), m.group(2));
    }

    // Compara si dos objetos tipo Seat son iguales
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat)) return false;
        Seat other = (Seat) o;
        return seatNumber == other.seatNumber && rowNumber.equals(other.rowNumber);
    }

    // Codigo hash
    @Override
    public int hashCode() {
        return Objects.hash(seatNumber, rowNumber);
    }

    // Convierte a String la informacion en seat+row
    @Override
    public String toString() {
        return seatNumber + rowNumber;
    }
}
