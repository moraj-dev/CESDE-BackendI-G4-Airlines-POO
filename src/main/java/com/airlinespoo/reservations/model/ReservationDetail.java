package com.airlinespoo.reservations.model;


import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/*  Detalle del pasajero asociado a una reserva
* */

public class ReservationDetail {

    private static final int MAX_LENGTH_NAME = 50;
    private static final int MAX_LENGTH_DOCUMENT = 20

    private final int id;
    private String passengerFullName;
    private String passengerDocument;
    private Seat seat;

    public ReservationDetail(int id, String passengerFullName, String passengerDocument, Seat seat) {
        this.id = (int) Validator.requirePositive(id, "identificador del detalle de reserva");
        this.passengerFullName = Validator.requireText(passengerFullName, "nombre completo del pasajero", MAX_LENGTH_NAME);
        this.passengerDocument = Validator.requireText(passengerDocument, "documento del pasajero", MAX_LENGTH_DOCUMENT);
        this.seat = Validator.requireNotNull(seat, "silla");
    }

    public int getId() {
        return id;
    }

    public String getPassengerFullName() {
        return passengerFullName;
    }

    public String getPassengerDocument() {
        return passengerDocument;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setPassengerFullName(String fullName) {
        this.passengerFullName = Validator.requireText(fullName, "nombre del pasajero", MAX_LENGTH_NAME);
    }

    public void setPassengerDocument(String document) {
        this.passengerDocument = Validator.requireText(document, "documento del pasajero", MAX_LENGTH_DOCUMENT);
    }

    public void setSeat(Seat seat) {
        this.seat = Validator.requireNotNull(seat, "silla");
    }

    // Compara si dos objetos tipo ReservationDetail son iguales basandose en su id
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ReservationDetail
            && id == ((ReservationDetail) o).id);
    }

    // Codigo hash
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Convierte a String la información del pasajero name+doc+seat
    @Override
    public String toString() {
        return passengerFullName + " (" + passengerDocument + ") - silla " + seat;
    }

}
