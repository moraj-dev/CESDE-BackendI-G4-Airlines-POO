package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;

import java.time.Duration;

/* Clase inmutable para establecer la distribución de asientos en el avión.
* El tamaño del avión depende de la duración del vuelo:
*   hasta 30 min ---------------------> 30 fil x 3 col  = 90 sillas
*   entre 30 min hasta 1.5 h ---------> 30 fil x 6 col  = 180 sillas
*   entre 1.5 hasta 6 horas ----------> 40 fil x 6 col  = 240 sillas
*   más de 6 h -----------------------> 40 fil x 10 col = 400 sillas
*
* Las filas son los números (desde 1 hasta 40) y las columnas son las letras
*   (A,B,C,E,F,G,H,J,K,L)
 */

public final class SeatLayout {

    private final int rows;
    private final int columns;

    public SeatLayout(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    // Cálculo de la distribución según la duración del vuelo
    public static SeatLayout fromDuration(Duration duration) {
        Validator.requireNotNull(duration, "duración del vuelo");
        if (duration.isZero() || duration.isNegative()) {
            throw new ValidationException("La duración del vuelo debe ser mayor que cero.");
        }
        if (duration.compareTo(Duration.ofMinutes(30)) <= 0) return new SeatLayout(30, 3);
        if (duration.compareTo(Duration.ofMinutes(90)) <= 0) return new SeatLayout(30, 6);
        if (duration.compareTo(Duration.ofHours(6)) <= 0) return new SeatLayout(40, 6);
        return new SeatLayout(40, 10);
    }

    // Determina la distribucion de un vuelo específico
    public static SeatLayout forFlight(Flight flight) {
        return fromDuration(Validator.requireNotNull(flight, "vuelo").getDuration());
    }

    // Total de sillas del avión
    public int getTotalSeats() {
        return rows * columns;
    }

    // Buscar la letra de la columna - index: posición de la columna (0 = primera letra habilitada)
    public String columnLetter(int index) {
        return String.valueOf(Seat.LETTERS.charAt(index));
    }

    // Indica las letras válidas en un avión
    public String validLetters() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns; i++) {
            if (i > 0) sb.append(", ");
            sb.append(columnLetter(i));
        }
        return sb.toString();
    }

    // Valida si el asiento existe y es válido dentro de la distribución del avión
    public boolean contains(Seat seat) {
        String letter = seat.getRowNumber();
        int col = Seat.LETTERS.indexOf(letter); // letra está prohibida da -1
        return col >= 0
            && col < columns
            && seat.getSeatNumber() <= rows;
    }
}
