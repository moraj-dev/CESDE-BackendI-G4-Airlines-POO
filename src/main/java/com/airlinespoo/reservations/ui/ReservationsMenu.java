package com.airlinespoo.reservations.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.reservations.service.IReservationService;
import com.airlinespoo.reservations.model.*;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/*
* Menu de consola del módulo de RESERVAS
*
 */

public class ReservationsMenu implements Menu {
    private final ConsoleInput console;
    private final IReservationService reservationService;
    private final CustomerService customerService;
    private final FlightService flightService;

    public ReservationMenu(ConsoleInput console, IReservationService reservationService,
                           CustomerService customerService, FlightService flightService) {
        this.console = console;
        this.reservationService = reservationService;
        this.customerService = customerService;
        this.flightService = flightService;
    }

    @Override
    public String title() {
        return "Reservas";
    }

    @Override
    public void run() {
        int option;
        do {
            System.out.println("\n=== RESERVAS ===");
            System.out.println("1. Crear reserva");
            System.out.println("2. Agregar pasajero");
            System.out.println("3. Quitar pasajero");
            System.out.println("4. Cambiar silla");
            System.out.println("5. Cancelar reserva");
            System.out.println("6. Reservas de un cliente");
            System.out.println("7. Ver sillas ocupadas de un vuelo");
            System.out.println("8. Manifiesto de un vuelo");
            System.out.println("0. Volver");
            option = console.readInt("Elija una opción", 0, 8);
            try {
                switch (option) {
                    case 1 -> createReservation();
                    case 2 -> addPassenger();
                    case 3 -> removePassenger();
                    case 4 -> changeSeat();
                    case 5 -> cancelReservation();
                    case 6 -> listByCustomer();
                    case 7 -> showOccupiedSeats();
                    case 8 -> showManifest();
                    default -> { }
                }
            } catch (AirlinesException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (option != 0);
    }

    // ------------ Opciones del menu -------------------
    private void createReservation() {
        Customer customer = customerService.getCustomer(console.readRequiredText("Documento del cliente"));
        System.out.println("Cliente: " + customer.getFullName());
        Flight flight = chooseFlight();
        printSeatMap(flight);
        System.out.println("Primer pasajero:");
        String name = console.readRequiredText("Nombre completo");
        String document = console.readRequiredText("Documento");
        Seat seat = readSeat();
        Reservation reservation = reservationService.createReservation(
                customer.getId(), flight.getId(), name, document, seat);
        System.out.println("Reserva creada.");
        while (reservation.getPassengerCount() < Reservation.MAX_PASSENGERS
                && console.confirm("¿Agregar otro pasajero?")) {
            try {
                reservation = askAndAddPassenger(reservation.getId());
            } catch (AirlinesException e) {
                System.out.println("No se pudo agregar: " + e.getMessage());
            }
        }
        printReservation(reservationService.getReservation(reservation.getId()));
    }

    private void addPassenger() {
        int id = console.readInt("Número de reserva", 1, Integer.MAX_VALUE);
        Reservation reservation = reservationService.getReservation(id);
        printSeatMap(reservation.getFlight());
        printReservation(askAndAddPassenger(id));
    }

    private Reservation askAndAddPassenger(int reservationId) {
        String name = console.readRequiredText("Nombre completo del pasajero");
        String document = console.readRequiredText("Documento del pasajero");
        Seat seat = readSeat();
        return reservationService.addPassenger(reservationId, name, document, seat);
    }

    private void removePassenger() {
        Reservation reservation = reservationService.getReservation(
                console.readInt("Número de reserva", 1, Integer.MAX_VALUE));
        printPassengers(reservation);
        int detailId = console.readInt("Id del pasajero a quitar", 1, Integer.MAX_VALUE);
        if (console.confirm("¿Quitar al pasajero " + detailId + "?")) {
            printReservation(reservationService.removePassenger(reservation.getId(), detailId));
        }
    }

    private void changeSeat() {
        Reservation reservation = reservationService.getReservation(
                console.readInt("Número de reserva", 1, Integer.MAX_VALUE));
        printPassengers(reservation);
        int detailId = console.readInt("Id del pasajero", 1, Integer.MAX_VALUE);
        printSeatMap(reservation.getFlight());
        Seat seat = readSeat();
        printReservation(reservationService.changeSeat(reservation.getId(), detailId, seat));
    }

    private void cancelReservation() {
        Reservation reservation = reservationService.getReservation(
                console.readInt("Número de reserva", 1, Integer.MAX_VALUE));
        printReservation(reservation);
        if (console.confirm("¿Cancelar esta reserva?")) {
            reservationService.cancelReservation(reservation.getId());
            System.out.println("Reserva cancelada; las sillas quedaron libres.");
        }
    }

    private void listByCustomer() {
        List<Reservation> list = reservationService.listReservationsByCustomer(
                console.readRequiredText("Documento del cliente"));
        if (list.isEmpty()) {
            System.out.println("El cliente no tiene reservas.");
            return;
        }
        list.forEach(this::printReservation);
    }

    private void showOccupiedSeats() {
        printSeatMap(chooseFlight());
    }

    private void showManifest() {
        Flight flight = chooseFlight();
        List<ManifestEntry> manifest = reservationService.getFlightManifest(flight.getId());
        if (manifest.isEmpty()) {
            System.out.println("El vuelo no tiene pasajeros.");
            return;
        }
        System.out.printf("%n%-6s %-30s %-15s%n", "SILLA", "PASAJERO", "DOCUMENTO");
        for (ManifestEntry e : manifest) {
            System.out.printf("%-6s %-30s %-15s%n", e.seat(), e.passengerFullName(), e.passengerDocument());
        }
        System.out.println("Total de pasajeros: " + manifest.size());
    }

    // ---------- ayudas de pantalla ----------

    private Flight chooseFlight() {
        List<Flight> flights = flightService.listFlights();
        if (flights.isEmpty()) {
            throw new BusinessRuleException("No hay vuelos registrados.");
        }
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            System.out.printf("%d. %s | %s -> %s | salida %s | %s%n", i + 1, f.getFlightName(),
                    f.getDeparture().getName(), f.getArrival().getName(),
                    f.getDepartureDateTime(), formatDuration(f.getDuration()));
        }
        return flights.get(console.readInt("Elija el vuelo", 1, flights.size()) - 1);
    }

    private Seat readSeat() {
        while (true) {
            try {
                return Seat.parse(console.readRequiredText("Silla (ejemplo 15A)"));
            } catch (ValidationException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void printSeatMap(Flight flight) {
        SeatLayout layout = SeatLayout.forFlight(flight);
        Set<Seat> occupied = new HashSet<>(reservationService.listOccupiedSeats(flight.getId()));
        System.out.printf("%nVuelo %s: %d sillas (%d filas x %d columnas) | ocupadas: %d | [XXX] = ocupada%n",
                flight.getFlightName(), layout.getTotalSeats(), layout.getRows(), layout.getColumns(),
                occupied.size());
        StringBuilder header = new StringBuilder("     ");
        for (int c = 0; c < layout.getColumns(); c++) {
            header.append(String.format("  %s   ", layout.columnLetter(c)));
            if (isAisle(layout, c)) header.append("   ");
        }
        System.out.println(header);
        for (int n = 1; n <= layout.getRows(); n++) {
            StringBuilder line = new StringBuilder(String.format("%3d  ", n));
            for (int c = 0; c < layout.getColumns(); c++) {
                Seat seat = new Seat(n, layout.columnLetter(c));
                line.append(occupied.contains(seat) ? "[XXX]" : String.format("[%-3s]", seat));
                line.append(' ');
                if (isAisle(layout, c)) line.append("   ");
            }
            System.out.println(line);
        }
    }

    private boolean isAisle(SeatLayout layout, int c) {
        return (layout.getColumns() == 6 && c == 2) || (layout.getColumns() == 10 && (c == 2 || c == 6));
    }

    private void printPassengers(Reservation reservation) {
        for (ReservationDetail d : reservation.getDetails()) {
            System.out.println("  [" + d.getId() + "] " + d);
        }
    }

    private void printReservation(Reservation reservation) {
        System.out.println("\n" + reservation);
        printPassengers(reservation);
    }

    private String formatDuration(Duration d) {
        return d.toHours() + " h " + d.toMinutesPart() + " min";
    }

}
