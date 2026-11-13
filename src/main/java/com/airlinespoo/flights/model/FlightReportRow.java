package com.airlinespoo.flights.model;

import java.time.LocalDate;
import java.time.LocalTime;

/** Fila del reporte de vuelos por aerolínea y mes. */
public record FlightReportRow(
        String airlineName,
        String flightName,
        String departureCity,
        String departureAirport,
        LocalDate departureDate,
        LocalTime departureTime,
        String arrivalCity,
        String arrivalAirport,
        LocalDate arrivalDate,
        LocalTime arrivalTime) {
}
