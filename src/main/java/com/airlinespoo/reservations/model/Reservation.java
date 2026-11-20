package com.airlinespoo.reservations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;

import java.util.*;
import java.util.stream.Collectors;

/* Entidad principal del módulo de reservas
* contiene un pasajero principal inicial y admite una cantidad FINITA de pasajeros
* */

public class Reservation implements Identifiable<Integer> {

    private static final int MAX_PASSENGERS = 10;

    private final int id;
    private Customer customer;
    private Flight flight;
    private final List<ReservationDetail> details = new ArrayList<>();

    public Reservation(int id, Customer customer, Flight flight, ReservationDetail firstPassenger) {
        this.id = (int) Validator.requirePositive(id,"número de reserva");;
        this.customer = Validator.requireNotNull(customer, "cliente");
        this.flight = Validator.requireNotNull(flight, "vuelo");
        this.details.add(Validator.requireNotNull(firstPassenger,"primer pasajero"));
    }

    @Override
    public Integer getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Flight getFlight() {
        return flight;
    }

    // Devuelve copia por seguridad
    public List<ReservationDetail> getDetails() {
        return new ArrayList<>(details);
    }

    // Cantidad de pasajeros en la reserva
    public int getPassengerCount() {
        return details.size();
    }

    /* Agregar pasajero a la reserva:
        valida cantidad pasajeros, documento del pasajero, silla libre,
        si el id del detalle de reserva ya existe
     */
    public void addPassenger(ReservationDetail detail) {
        Validator.requireNotNull(detail, "información del pasajero");
        if (details.size() >= MAX_PASSENGERS) {
            throw new BusinessRuleException("La reserva no puede superar los " + MAX_PASSENGERS + " pasajeros.");
        }
        if (hasPassengerDocument(detail.getPassengerDocument())) {
            throw new ValidationException("El documento " + detail.getPassengerDocument() + " ya está en la reserva.");
        }
        if (hasSeat(detail.getSeat())) {
            throw new ValidationException("La silla " + detail.getSeat() + " ya está asignada en esta reserva.");
        }
        if (find(detail.getId()).isPresent()) {
            throw new ValidationException("Ya existe un registro de pasajero con el identificador " + detail.getId() + ".");
        }
        details.add(detail);
    }

    // Quitar un pasajero de la reserva. La reserva debe tener al menos un pasajero
    public void removePassenger(int detailId) {
        ReservationDetail detail = find(detailId)
            .orElseThrow(() -> new ValidationException("El pasajero " + detailId + " no está en la reserva."));
        if (details.size() <= 1) {
            throw new BusinessRuleException("La reserva debe mantener al menos un pasajero. Si necesita quitar el último pasajero, por favor cancele la reserva.");
        }
        details.remove(detail);
    }

    // Cambiar silla de un pasajero. Valida si el pasajero existe en la reseva y si la silla esta libre
    public void changeSeat(int detailId, Seat newSeat) {
        Validator.requireNotNull(newSeat,"Silla");
        ReservationDetail detail = find(detailId)
            .orElseThrow(() -> new ValidationException("El pasajero " + detailId + " no está en la reserva."));
        for (ReservationDetail other : details) {
            if (other.getId() != detailId && other.getSeat().equals(newSeat)) {
                throw new ValidationException("La silla " + newSeat + " ya está asignada en esta reserva.");
            }
        }
        detail.setSeat(newSeat);
    }

    // Cambiar el vuelo de la reserva
    public void changeFlight(Flight newFlight) {
        this.flight = Validator.requireNotNull(newFlight, "nuevo vuelo");
    }

    // Busca el documento del pasajero en la reserva
    public boolean hasPassengerDocument(String document) {
        if (document == null) return false;
        return details.stream().anyMatch(d ->
            d.getPassengerDocument().equalsIgnoreCase(document.trim()));
    }

    // Busca si algun pasajero de la reserva tiene la silla ocupada
    public boolean hasSeat(Seat seat) {
        return details.stream().anyMatch(d ->
            d.getSeat().equals(seat));
    }

    // Muestra las sillas ocupadas de una reserva, en orden lógico
    public Set<Seat> getSeats() {
        return details.stream()
            .map(ReservationDetail::getSeat)
            .sorted(Comparator.comparing(Seat::getSeatNumber)
                .thenComparing(Seat::getRowNumber))
            .collect(Collectors.toCollection(TreeSet::new));
    }

    // Buscar id del objeto ReservationDetail
    private Optional<ReservationDetail> find(int detailId) {
        return details.stream()
            .filter(d -> d.getId() == detailId)
            .findFirst();
    }

    // Compara si dos objetos tipo Reservation son iguales
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Reservation && id == ((Reservation) o).id);
    }

    // Codigo hash
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Convierte a String la informacion idReserva+fullName+vuelo+cantPasajeros+sillas
    @Override
    public String toString() {
        return "Reserva #" + id + " | " + customer.getFullName() + " | vuelo " + flight.getFlightName()
            + " | " + details.size() + " pasajero(s) | sillas " + getSeats();
    }
}
