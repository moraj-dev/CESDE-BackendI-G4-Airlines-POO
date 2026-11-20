package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Repositorio de vuelos en memoria. */
public class InMemoryFlightRepository extends InMemoryRepository<Flight, Integer>
        implements FlightRepository {

    /** {@inheritDoc} */
    @Override
    protected String entityName() {
        return "vuelo";
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Flight> findByFlightName(String name) {
        return values().stream()
                .filter(f -> f.getFlightName().equalsIgnoreCase(name))
                .findFirst();
    }

    /** {@inheritDoc} */
    @Override
    public List<Flight> findByAirline(Airline airline) {
        return values().stream()
                .filter(f -> f.getAirline().equals(airline))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to) {
        return values().stream()
                .filter(f -> !f.getDepartureDate().isBefore(from) && !f.getDepartureDate().isAfter(to))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<Flight> findByRoute(Airport departure, Airport arrival) {
        return values().stream()
                .filter(f -> f.getDeparture().equals(departure) && f.getArrival().equals(arrival))
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public long countByAirline(Airline airline) {
        return values().stream().filter(f -> f.getAirline().equals(airline)).count();
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByAirline(Airline airline) {
        return values().stream().anyMatch(f -> f.getAirline().equals(airline));
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByAirport(Airport airport) {
        return values().stream()
                .anyMatch(f -> f.getDeparture().equals(airport) || f.getArrival().equals(airport));
    }
}
