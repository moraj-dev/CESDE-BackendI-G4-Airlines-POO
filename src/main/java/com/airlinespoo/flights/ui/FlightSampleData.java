package com.airlinespoo.flights.ui;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.LocalDateTime;
import java.util.List;

/** Carga 8 vuelos de ejemplo en diciembre de 2026 y enero de 2027. */
public class FlightSampleData {

    private final FlightService flightService;
    private final LocationService locationService;
    private final AirlineService airlineService;

    public FlightSampleData(FlightService flightService, LocationService locationService,
                            AirlineService airlineService) {
        this.flightService = flightService;
        this.locationService = locationService;
        this.airlineService = airlineService;
    }

    public void load() {
        List<Airport> ap = locationService.listAirports();
        List<Airline> al = airlineService.listAirlines();

        add("AV101", ap.get(0), ap.get(1), al.get(0),
                LocalDateTime.of(2026, 12, 5, 8, 0), LocalDateTime.of(2026, 12, 5, 9, 10));
        add("AV102", ap.get(1), ap.get(0), al.get(0),
                LocalDateTime.of(2026, 12, 5, 18, 0), LocalDateTime.of(2026, 12, 5, 19, 15));
        add("LA201", ap.get(0), ap.get(2), al.get(1),
                LocalDateTime.of(2026, 12, 10, 23, 30), LocalDateTime.of(2026, 12, 11, 1, 10));
        add("LA202", ap.get(2), ap.get(3), al.get(1),
                LocalDateTime.of(2026, 12, 18, 6, 45), LocalDateTime.of(2026, 12, 18, 8, 30));
        add("CM301", ap.get(3), ap.get(4), al.get(2),
                LocalDateTime.of(2026, 12, 24, 14, 0), LocalDateTime.of(2026, 12, 24, 16, 20));
        add("AV103", ap.get(1), ap.get(4), al.get(0),
                LocalDateTime.of(2027, 1, 8, 7, 30), LocalDateTime.of(2027, 1, 8, 9, 0));
        add("LA203", ap.get(4), ap.get(5), al.get(1),
                LocalDateTime.of(2027, 1, 15, 12, 0), LocalDateTime.of(2027, 1, 15, 13, 45));
        add("CM302", ap.get(5), ap.get(0), al.get(2),
                LocalDateTime.of(2027, 1, 22, 21, 15), LocalDateTime.of(2027, 1, 22, 23, 40));
    }

    private void add(String code, Airport departure, Airport arrival, Airline airline,
                     LocalDateTime departureAt, LocalDateTime arrivalAt) {
        flightService.createFlight(code, departure.getId(), arrival.getId(),
                departureAt.toLocalDate(), departureAt.toLocalTime(),
                arrivalAt.toLocalDate(), arrivalAt.toLocalTime(), airline.getId());
    }
}
