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

    /** @param name código del vuelo
     *  @return el vuelo, si existe */
    Optional<Flight> findByFlightName(String name);

    /** @param airline aerolínea consultada
     *  @return vuelos de esa aerolínea */
    List<Flight> findByAirline(Airline airline);

    /** @param from fecha inicial
     *  @param to fecha final
     *  @return vuelos cuya salida está en el intervalo */
    List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to);

    /** @param departure aeropuerto de salida
     *  @param arrival aeropuerto de llegada
     *  @return vuelos que usan esa ruta */
    List<Flight> findByRoute(Airport departure, Airport arrival);

    /** @param airline aerolínea consultada
     *  @return cantidad de vuelos */
    long countByAirline(Airline airline);

    /** @param airline aerolínea consultada
     *  @return {@code true} si tiene vuelos */
    boolean existsByAirline(Airline airline);

    /** @param airport aeropuerto consultado
     *  @return {@code true} si aparece como salida o llegada */
    boolean existsByAirport(Airport airport);
}
