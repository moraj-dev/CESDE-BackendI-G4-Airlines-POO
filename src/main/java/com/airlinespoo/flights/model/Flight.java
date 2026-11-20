package com.airlinespoo.flights.model;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/** Vuelo entre dos aeropuertos operado por una aerolínea. */
public class Flight implements Identifiable<Integer>, Comparable<Flight> {

    private static final int MAX_FLIGHT_NAME_LENGTH = 8;
    private static final String FLIGHT_NAME_PATTERN = "[A-Z0-9]+";

    private final int id;
    private final String flightName;
    private final Airport departure;
    private final Airport arrival;
    private final LocalDate departureDate;
    private final LocalTime departureTime;
    private final LocalDate arrivalDate;
    private final LocalTime arrivalTime;
    private final Airline airline;

    /**
     * Crea un vuelo validando sus datos y que la llegada sea posterior a la salida.
     *
     * @param id identificador positivo del vuelo
     * @param flightName código alfanumérico del vuelo
     * @param departure aeropuerto de salida
     * @param arrival aeropuerto de llegada
     * @param departureDate fecha de salida
     * @param departureTime hora de salida
     * @param arrivalDate fecha de llegada
     * @param arrivalTime hora de llegada
     * @param airline aerolínea operadora
     * @throws ValidationException si algún dato es inválido
     */
    public Flight(int id, String flightName, Airport departure, Airport arrival,
                  LocalDate departureDate, LocalTime departureTime,
                  LocalDate arrivalDate, LocalTime arrivalTime, Airline airline) {
        this.id = (int) Validator.requirePositive(id, "id del vuelo");
        this.flightName = validateFlightName(flightName);
        this.departure = Validator.requireNotNull(departure, "aeropuerto de salida");
        this.arrival = Validator.requireNotNull(arrival, "aeropuerto de llegada");
        if (departure.equals(arrival)) {
            throw new ValidationException("El aeropuerto de salida y el de llegada deben ser distintos.");
        }
        this.departureDate = Validator.requireNotNull(departureDate, "fecha de salida");
        this.departureTime = Validator.requireNotNull(departureTime, "hora de salida");
        this.arrivalDate = Validator.requireNotNull(arrivalDate, "fecha de llegada");
        this.arrivalTime = Validator.requireNotNull(arrivalTime, "hora de llegada");
        if (!getArrivalDateTime().isAfter(getDepartureDateTime())) {
            throw new ValidationException("La llegada no puede ser anterior a la salida.");
        }
        this.airline = Validator.requireNotNull(airline, "aerolínea");
    }

    private static String validateFlightName(String value) {
        String code = Validator.requireText(value, "código del vuelo", MAX_FLIGHT_NAME_LENGTH).toUpperCase();
        if (!code.matches(FLIGHT_NAME_PATTERN)) {
            throw new ValidationException("El código del vuelo solo puede tener letras y dígitos.");
        }
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @return código normalizado del vuelo
     */
    public String getFlightName() {
        return flightName;
    }

    /**
     * @return aeropuerto de salida
     */
    public Airport getDeparture() {
        return departure;
    }

    /**
     * @return aeropuerto de llegada
     */
    public Airport getArrival() {
        return arrival;
    }

    /**
     * @return fecha de salida
     */
    public LocalDate getDepartureDate() {
        return departureDate;
    }

    /**
     * @return hora de salida
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * @return fecha de llegada
     */
    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    /**
     * @return hora de llegada
     */
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    /**
     * @return aerolínea operadora
     */
    public Airline getAirline() {
        return airline;
    }

    /**
     * @return fecha y hora completas de salida
     */
    public LocalDateTime getDepartureDateTime() {
        return LocalDateTime.of(departureDate, departureTime);
    }

    /**
     * @return fecha y hora completas de llegada
     */
    public LocalDateTime getArrivalDateTime() {
        return LocalDateTime.of(arrivalDate, arrivalTime);
    }

    /**
     * @return duración entre la salida y la llegada
     */
    public Duration getDuration() {
        return Duration.between(getDepartureDateTime(), getArrivalDateTime());
    }

    /**
     * Indica si el vuelo ya salió en el instante indicado.
     *
     * @param now instante de referencia
     * @return {@code true} si la salida ya ocurrió
     */
    public boolean hasDeparted(LocalDateTime now) {
        return !getDepartureDateTime().isAfter(now);
    }

    /**
     * Compara primero por fecha de salida y luego por identificador.
     *
     * @param other vuelo con el que se compara
     * @return valor negativo, cero o positivo según el orden
     */
    @Override
    public int compareTo(Flight other) {
        int result = getDepartureDateTime().compareTo(other.getDepartureDateTime());
        return result != 0 ? result : Integer.compare(id, other.id);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Flight other && id == other.id);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "%s | %s -> %s | salida %s | llegada %s".formatted(
                flightName, departure.getName(), arrival.getName(),
                getDepartureDateTime(), getArrivalDateTime());
    }
}
