package com.airlinespoo.flights.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Fila del reporte de vuelos por aerolínea y mes.
 *
 * @param airlineName nombre de la aerolínea
 * @param flightName código del vuelo
 * @param departureCity ciudad de salida
 * @param departureAirport nombre del aeropuerto de salida
 * @param departureDate fecha de salida
 * @param departureTime hora de salida
 * @param arrivalCity ciudad de llegada
 * @param arrivalAirport nombre del aeropuerto de llegada
 * @param arrivalDate fecha de llegada
 * @param arrivalTime hora de llegada
 */
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
