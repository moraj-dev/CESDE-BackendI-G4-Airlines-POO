package com.airlinespoo.reservations.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.reservations.model.*;
import com.airlinespoo.reservations.repository.IReservationRepository;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
*
* Reglas de negocio del módulo de reservas (M5)
* Regla central: una silla no puede asignarse a dos pasajeros del mismo vuelo
*
 */

public class ReservationService implements IReservationService{
    private final IReservationRepository reservationRepository;
    private final CustomerService customerService;
    private final FlightService flightService;
    private final IdGenerator reservationIds ;
    private final IdGenerator detailIds;

    private static final Comparator<Seat> SEAT_ORDER = Comparator.comparingInt(Seat::getSeatNumber)
        .thenComparing(Seat::getRowNumber);

    private static final Comparator<Reservation> BY_DEPARTURE =
        Comparator.comparing((Reservation r) -> r.getFlight().getDepartureDateTime())
                .thenComparingInt(Reservation::getId);

    public ReservationService(IReservationRepository reservationRepository, CustomerService customerService,
                              FlightService flightService, IdGenerator reservationIds, IdGenerator detailIds) {
        this.reservationRepository = reservationRepository;
        this.customerService = customerService;
        this.flightService = flightService;
        this.reservationIds = reservationIds;
        this.detailIds = detailIds;
    }

    @Override
    public Reservation createReservation(String customerId, int flightId, String passengerName, String passengerDocument, Seat seat) {
        Customer customer = customerService.getCustomer(customerId);
        Flight flight = flightService.getFlight(flightId);
        ensureFlightNotDeparted(flight);
        ensureSeatExists(flight, seat);
        ensureSeatFree(flight, seat, null);
        ReservationDetail first = new ReservationDetail(detailIds.next(), passengerName, passengerDocument, seat);
        return reservationRepository.create(new Reservation(reservationIds.next(), customer, flight, first));
    }

    @Override
    public Reservation getReservation(int id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("reserva", id));
    }

    // Returns una lista de las reservas ordenadas por id
    @Override
    public List<Reservation> listReservations() {
        List<Reservation> list = new ArrayList<>(reservationRepository.findAll());
        list.sort(Comparator.comparing(Reservation::getId));
        return list;
    }

    // Añadir pasajero a reserva existente
    @Override
    public Reservation addPassenger(int reservationId, String name, String document, Seat seat) {
        Reservation reservation = getReservation(reservationId);
        Flight flight = reservation.getFlight();
        ensureFlightNotDeparted(flight);
        ensureSeatExists(flight, seat);
        ensureSeatFree(flight, seat, null);
        reservation.addPassenger(new ReservationDetail(detailIds.next(), name, document, seat));
        return reservationRepository.update(reservation);
    }

    // Actualizar información del pasajero en la reserva (nombre o documento)
    @Override
    public Reservation updatePassenger(int reservationId, int detailId, String name, String document) {
        Reservation reservation = getReservation(reservationId);
        Flight flight = reservation.getFlight();
        ensureFlightNotDeparted(flight);
        ReservationDetail details = findDetail(reservation, detailId);
        for (ReservationDetail other : reservation.getDetails()) {
            if (other.getId() != detailId && document != null
                && other.getPassengerDocument().equalsIgnoreCase(document.trim())) {
                throw new BusinessRuleException("El documento " + document.trim() + " ya está en la reserva.");
            }
        }
        details.setPassengerFullName(name);
        details.setPassengerDocument(document);
        return reservationRepository.update(reservation);
    }

    // Quitar pasajero de una reserva, debe quedar al menos uno en ella
    @Override
    public Reservation removePassenger(int reservationId, int detailId) {
        Reservation reservation = getReservation(reservationId);
        Flight flight = reservation.getFlight();
        ensureFlightNotDeparted(flight);
        findDetail(reservation, detailId);
        reservation.removePassenger(detailId);
        return reservationRepository.update(reservation);
    }

    // Cambiar la silla de un pasajero en una reserva
    @Override
    public Reservation changeSeat(int reservationId, int detailId, Seat seat) {
        Reservation reservation = getReservation(reservationId);
        Flight flight = reservation.getFlight();
        ensureFlightNotDeparted(flight);
        ReservationDetail detail = findDetail(reservation, detailId);
        if (detail.getSeat().equals(seat)) {
            return reservation;
        }
        ensureSeatExists(flight, seat);
        ensureSeatFree(flight, seat, detailId);
        reservation.changeSeat(detailId, seat);
        return reservationRepository.update(reservation);
    }

    // Cambiar el vuelo de una reserva
    @Override
    public Reservation changeReservationFlight(int reservationId, int flightId) {
        Reservation reservation = getReservation(reservationId);
        Flight flight = reservation.getFlight();
        ensureFlightNotDeparted(flight);
        Flight newFlight = flightService.getFlight(flightId);
        if (newFlight.getId().equals(flight.getId())) {
            throw new BusinessRuleException("El nuevo vuelo es el mismo que esta en la reserva.");
        }
        ensureFlightNotDeparted(newFlight);
        for (Seat seat : reservation.getSeats()) {
            ensureSeatExists(newFlight, seat);
            ensureSeatFree(newFlight, seat, null);
        }
        reservation.changeFlight(newFlight);
        return reservationRepository.update(reservation);
    }

    // Eliminar un reserva, sus sillas queda libres
    @Override
    public void cancelReservation(int id) {
        Reservation reservation = getReservation(id);
        Flight flight = reservation.getFlight();
        ensureFlightNotDeparted(flight);
        reservationRepository.deleteById(id);
    }

    // Listar pasajeros de una reserva
    @Override
    public List<ReservationDetail> listPassengers(int reservationId) {
        return getReservation(reservationId).getDetails();
    }

    // Listar las reservas de un cliente
    @Override
    public List<Reservation> listReservationsByCustomer(String customerId) {
        List<Reservation> list = new ArrayList<>(reservationRepository.
            findByCustomer(customerService.getCustomer(customerId)));
        list.sort(BY_DEPARTURE);
        return list;
    }

    // Listar reservas por vuelo
    @Override
    public List<Reservation> listReservationsByFlight(int flightId) {
        return reservationRepository.findByFlight(flightService.getFlight(flightId));
    }

    // Valida si un cliente tiene reservas
    @Override
    public boolean customerHasReservations(String customerId) {
        return reservationRepository.existsByCustomer(customerService.getCustomer(customerId));
    }

    // Valida si un vuelo tiene reservas
    @Override
    public boolean flightHasReservations(int flightId) {
        return reservationRepository.existsByFlight(flightService.getFlight(flightId));
    }

    // Lista de asientos ocupados por vuelo, ordenadas por numero y letra
    @Override
    public List<Seat> listOccupiedSeats(int flightId) {
        List<Seat> seats = new ArrayList<>();
        for (Reservation r : listReservationsByFlight(flightId)) seats.addAll(r.getSeats());
        seats.sort(SEAT_ORDER);
        return seats;
    }

    // Regla central: indica si una silla ya fue asignada en un vuelo
    @Override
    public boolean isSeatOccupied(int flightId, Seat seat) {
        return listReservationsByFlight(flightId).stream()
            .anyMatch(r -> r.hasSeat(seat));
    }

    // Cuenta la cantidad de pasajeros con reserva en vuelo
    @Override
    public long countPassengersByFlight(int flightId) {
        return listReservationsByFlight(flightId).stream()
            .mapToLong(Reservation::getPassengerCount).sum();
    }

    // Manifiesto de vuelo ordenado por silla
    @Override
    public List<ManifestEntry> getFlightManifest(int flightId) {
        List<ManifestEntry> manifest = new ArrayList<>();
        for (Reservation r : listReservationsByFlight(flightId)) {
            for (ReservationDetail d : r.getDetails()) {
                manifest.add(new ManifestEntry(d.getPassengerFullName(),d.getPassengerDocument(),d.getSeat()));
            }
        }
        manifest.sort(Comparator.comparing(ManifestEntry::seat, SEAT_ORDER));
        return manifest;
    }

    // Valida que el asiento esté desocupado
    @Override
    public void ensureSeatFree(Flight flight, Seat seat, Integer ignoreDetailId) {
        for (Reservation r : reservationRepository.findByFlight(flight)) {
            for (ReservationDetail d : r.getDetails()) {
                boolean ignored = ignoreDetailId != null && d.getId() == ignoreDetailId;
                if (!ignored && d.getSeat().equals(seat)) {
                    throw new BusinessRuleException("La silla " + seat + " del vuelo " + flight.getFlightName() + " ya está ocupada");
                }
            }
        }
    }

    // Valida que el vuelo no haya salido
    @Override
    public void ensureFlightNotDeparted(Flight flight) {
        if (flight.hasDeparted(LocalDateTime.now())) {
            throw new BusinessRuleException("El vuelo " + flight.getFlightName() + " ya salió.");
        }
    }

    // Valida que, si se reprograma un vuelo y cambia la duracion, las sillas reservadas sigan haciendo parte del layout
    public void ensureSeatsFitDuration(int flightId, Duration newDuration) {
    SeatLayout newLayout = SeatLayout.fromDuration(newDuration);
    List<Seat> invalid = listOccupiedSeats(flightId).stream()
            .filter(seat -> !newLayout.contains(seat))
            .toList();
    if (!invalid.isEmpty()) {
        throw new BusinessRuleException("No se puede reprogramar el vuelo: las sillas " + invalid
                + " no existen en la nueva distribución (" + newLayout.getRows() + " filas x "
                + newLayout.getColumns() + " columnas). Cambie los asientos reservados primero.");
    }
}

    // --------------- Metodos privados de Servicio ----------------------------
    private void ensureSeatExists (Flight flight, Seat seat) {
        SeatLayout layout = SeatLayout.forFlight(flight);
        if (!layout.contains(seat)) {
            throw new BusinessRuleException("La silla " + seat + " no existe en el vuelo " + flight.getFlightName()
                + ". Este vuelo tiene filas desde 1 hasta " + layout.getRows() + " y columnas de la A a la "
                + layout.columnLetter(layout.getColumns()-1) + ".");
        }
    }

    private ReservationDetail findDetail(Reservation r, int detailId) {
        return r.getDetails().stream()
            .filter(d -> d.getId() == detailId)
            .findFirst()
            .orElseThrow(() -> new NotFoundException("pasajero", detailId));
    }
}
