package com.airlinespoo.flights.ui;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.model.FlightReportRow;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** Menú de consola del módulo de vuelos. */
public class FlightMenu implements Menu {

    private static final int EXIT = 0;

    private final ConsoleInput console;
    private final FlightService flightService;
    private final LocationService locationService;
    private final AirlineService airlineService;

    public FlightMenu(ConsoleInput console, FlightService flightService,
                      LocationService locationService, AirlineService airlineService) {
        this.console = console;
        this.flightService = flightService;
        this.locationService = locationService;
        this.airlineService = airlineService;
    }

    @Override
    public String title() {
        return "Vuelos";
    }

    @Override
    public void run() {
        int option;
        do {
            printOptions();
            option = console.readInt("Elige una opción", 0, 8);
            try {
                execute(option);
            } catch (AirlinesException e) {
                System.out.println("No se pudo completar la operación: " + e.getMessage());
            }
        } while (option != EXIT);
    }

    private void printOptions() {
        System.out.println("\n=== Vuelos ===");
        System.out.println("1. Programar vuelo");
        System.out.println("2. Consultar por código");
        System.out.println("3. Listar vuelos");
        System.out.println("4. Reprogramar vuelo");
        System.out.println("5. Cancelar vuelo");
        System.out.println("6. Listar por aerolínea");
        System.out.println("7. Listar por rango de fechas");
        System.out.println("8. Reporte mensual por aerolínea");
        System.out.println("0. Volver");
    }

    private void execute(int option) {
        switch (option) {
            case 1 -> scheduleFlight();
            case 2 -> consultByName();
            case 3 -> printFlights(flightService.listFlights());
            case 4 -> rescheduleFlight();
            case 5 -> cancelFlight();
            case 6 -> listByAirline();
            case 7 -> listByDateRange();
            case 8 -> monthlyReport();
            default -> { }
        }
    }

    private void scheduleFlight() {
        String code = console.readRequiredText("Código del vuelo");
        Airport departure = chooseAirport("salida");
        Airport arrival = chooseAirport("llegada");
        LocalDate departureDate = console.readDate("Fecha de salida");
        LocalTime departureTime = console.readTime("Hora de salida");
        LocalDate arrivalDate = console.readDate("Fecha de llegada");
        LocalTime arrivalTime = console.readTime("Hora de llegada");
        Airline airline = chooseAirline();

        Flight flight = flightService.createFlight(code, departure.getId(), arrival.getId(),
                departureDate, departureTime, arrivalDate, arrivalTime, airline.getId());
        System.out.println("Vuelo programado: " + describe(flight));
    }

    private void consultByName() {
        String code = console.readRequiredText("Código del vuelo");
        System.out.println(describe(flightService.getFlightByName(code)));
    }

    private void rescheduleFlight() {
        String code = console.readRequiredText("Código del vuelo a reprogramar");
        Flight flight = flightService.getFlightByName(code);
        LocalDate departureDate = console.readDate("Nueva fecha de salida");
        LocalTime departureTime = console.readTime("Nueva hora de salida");
        LocalDate arrivalDate = console.readDate("Nueva fecha de llegada");
        LocalTime arrivalTime = console.readTime("Nueva hora de llegada");

        Flight updated = flightService.updateFlight(flight.getId(), flight.getFlightName(),
                flight.getDeparture().getId(), flight.getArrival().getId(),
                departureDate, departureTime, arrivalDate, arrivalTime, flight.getAirline().getId());
        System.out.println("Vuelo reprogramado: " + describe(updated));
    }

    private void cancelFlight() {
        String code = console.readRequiredText("Código del vuelo a cancelar");
        Flight flight = flightService.getFlightByName(code);
        if (console.confirm("¿Cancelar el vuelo " + flight.getFlightName() + "?")) {
            flightService.deleteFlight(flight.getId());
            System.out.println("Vuelo cancelado.");
        }
    }

    private void listByAirline() {
        Airline airline = chooseAirline();
        printFlights(flightService.listFlightsByAirline(airline.getId()));
    }

    private void listByDateRange() {
        LocalDate from = console.readDate("Fecha inicial");
        LocalDate to = console.readDate("Fecha final");
        printFlights(flightService.listFlightsBetween(from, to));
    }

    private void monthlyReport() {
        int year = console.readInt("Año", FlightService.MIN_YEAR, FlightService.MAX_YEAR);
        int month = console.readInt("Mes", 1, 12);
        Map<Airline, List<FlightReportRow>> report = flightService.listFlightsByAirlineInMonth(year, month);
        if (report.isEmpty()) {
            System.out.println("Sin vuelos en ese periodo.");
            return;
        }
        report.forEach((airline, rows) -> {
            System.out.println("Aerolínea: " + airline.getName());
            rows.forEach(r -> System.out.println("  %s | %s - %s | %s %s -> %s - %s | %s %s".formatted(
                    r.flightName(), r.departureCity(), r.departureAirport(),
                    r.departureDate(), r.departureTime(),
                    r.arrivalCity(), r.arrivalAirport(),
                    r.arrivalDate(), r.arrivalTime())));
        });
    }

    private Airline chooseAirline() {
        List<Airline> airlines = airlineService.listAirlines();
        if (airlines.isEmpty()) {
            throw new BusinessRuleException("No hay aerolíneas registradas.");
        }
        for (int i = 0; i < airlines.size(); i++) {
            System.out.println((i + 1) + ". " + airlines.get(i).getName());
        }
        return airlines.get(console.readInt("Elige una aerolínea", 1, airlines.size()) - 1);
    }

    private Airport chooseAirport(String role) {
        List<Airport> airports = locationService.listAirports();
        if (airports.isEmpty()) {
            throw new BusinessRuleException("No hay aeropuertos registrados.");
        }
        for (int i = 0; i < airports.size(); i++) {
            Airport a = airports.get(i);
            System.out.println((i + 1) + ". " + a.getName() + " (" + a.getCity().getName() + ")");
        }
        return airports.get(console.readInt("Elige el aeropuerto de " + role, 1, airports.size()) - 1);
    }

    private void printFlights(List<Flight> flights) {
        if (flights.isEmpty()) {
            System.out.println("No hay vuelos para mostrar.");
            return;
        }
        flights.forEach(f -> System.out.println(describe(f)));
    }

    private String describe(Flight flight) {
        return flight + " | duración " + formatDuration(flight.getDuration());
    }

    private String formatDuration(Duration duration) {
        return "%d h %d min".formatted(duration.toHours(), duration.toMinutesPart());
    }
}
