package com.airlinespoo.reservations.model;

import com.airlinespoo.core.model.Identifiable;

import java.util.List;

/* Entidad principal del módulo que contiene un pasajero principal inicial y admite hasta 10 pasajeros en total */

public class Reservation implements Identifiable<Integer> {
    private final int id;
    private Customer customer;
    private Flight flight;
    private final List<ReservationDetail> details;

    private static final int MAX_PASSENGERS = 10;



    @Override
    public Integer getId() {
        return 0;
    }

}
