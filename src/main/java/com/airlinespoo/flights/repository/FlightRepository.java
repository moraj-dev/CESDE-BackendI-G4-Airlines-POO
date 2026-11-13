package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Contrato del repositorio de vuelos: lo cumplen la versión en memoria y la de JDBC. */
public interface FlightRepository extends Repository<Flight, Integer> {

    Optional<Flight> findByFlightName(String name);

    List<Flight> findByAirline(Airline airline);

    List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to);

    List<Flight> findByRoute(Airport departure, Airport arrival);

    long countByAirline(Airline airline);

    boolean existsByAirline(Airline airline);

    boolean existsByAirport(Airport airport);
}
