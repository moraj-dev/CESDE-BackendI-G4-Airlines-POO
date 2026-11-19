package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.flights.model.Customer;
import com.airlinespoo.flights.model.Flight;

import java.util.List;

/* Contrato de persistencia de reservas
 */

public interface IReservationRepository extends Repository<Reservation, Integer> {
    List<Reservation> findByCustomer(Customer customer);
    List<Reservation> findByFlight(Flight flight);
    boolean existsByCustomer(Customer customer);
    boolean existsByFlight(Flight flight);
}
