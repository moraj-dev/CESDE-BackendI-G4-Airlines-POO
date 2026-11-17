package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import java.util.Objects;


/* Representa la silla del pasajero en el avión
NÚMERO + FILA
*/
public final class Seat {
    private static final int MIN_SEAT_NUMBER = 1;
    private static final int MAX_SEAT_NUMBER = 40;
    private static final String ALLOWED_ROW_LETTERS = "[A-HJK]";

    private final String rowNumber;
    private final int seatNumber;

    public Seat (String rowNumber, int seatNumber) {
        this.seatNumber = (int) Validator.requireInRange(seatNumber,MIN_SEAT_NUMBER,MAX_SEAT_NUMBER,"número de silla");
        this.rowNumber = validateRowNumber(rowNumber);
    }

    private static String validateRowNumber(String value) {
        String row = Validator.requireText(value, "letra de silla");
        if (!row.matches(ALLOWED_ROW_LETTERS)) {
            throw new ValidationException("La letra de la silla solo puede ser " + ALLOWED_ROW_LETTERS);
        }
        return row;
    }

    public String getRowNumber() {
        return rowNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return seatNumber == seat.seatNumber && Objects.equals(rowNumber, seat.rowNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seatNumber, rowNumber);
    }

    @Override
    public String toString() {
        return seatNumber + rowNumber;
    }
}
