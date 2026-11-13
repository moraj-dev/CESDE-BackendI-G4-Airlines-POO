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

    @Override
    public Integer getId() {
        return id;
    }

    public String getFlightName() {
        return flightName;
    }

    public Airport getDeparture() {
        return departure;
    }

    public Airport getArrival() {
        return arrival;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public Airline getAirline() {
        return airline;
    }

    public LocalDateTime getDepartureDateTime() {
        return LocalDateTime.of(departureDate, departureTime);
    }

    public LocalDateTime getArrivalDateTime() {
        return LocalDateTime.of(arrivalDate, arrivalTime);
    }

    public Duration getDuration() {
        return Duration.between(getDepartureDateTime(), getArrivalDateTime());
    }

    public boolean hasDeparted(LocalDateTime now) {
        return !getDepartureDateTime().isAfter(now);
    }

    @Override
    public int compareTo(Flight other) {
        int result = getDepartureDateTime().compareTo(other.getDepartureDateTime());
        return result != 0 ? result : Integer.compare(id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Flight other && id == other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "%s | %s -> %s | salida %s | llegada %s".formatted(
                flightName, departure.getName(), arrival.getName(),
                getDepartureDateTime(), getArrivalDateTime());
    }
}
