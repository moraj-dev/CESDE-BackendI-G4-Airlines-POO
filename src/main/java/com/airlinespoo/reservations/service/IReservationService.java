package com.airlinespoo.reservations.service;

import com.airlinespoo.reservations.model.ManifestEntry;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.flights.model.Flight;

import java.util.List;

public interface IReservationService {
    Reservation createReservation(String customerId, int flightId, String passengerName, String passengerDocument, Seat seat);
    Reservation getReservation(int id);
    List<Reservation> listReservations();
    Reservation addPassenger(int reservationId, String name, String document, Seat seat);
    Reservation updatePassenger(int reservationId, int detailId, String name, String document);
    Reservation removePassenger(int reservationId, int detailId);
    Reservation changeSeat(int reservationId, int detailId, Seat seat);
    Reservation changeReservationFlight(int reservationId, int flightId);
    void cancelReservation(int id);
    List<ReservationDetail> listPassengers(int reservationId);
    List<Reservation> listReservationsByCustomer(String customerId);
    List<Reservation> listReservationsByFlight(int flightId);
    boolean customerHasReservations(String customerId);
    boolean flightHasReservations(int flightId);
    List<Seat> listOccupiedSeats(int flightId);
    boolean isSeatOccupied( int flightId, Seat seat);
    long countPassengersByFlight(int flightId);
    List<ManifestEntry> getFlightManifest(int flightId);
    void ensureSeatFree(Flight flight, Seat seat, Integer ignoreDetailId);
    void ensureFlightNotDeparted(Flight flight);
}
