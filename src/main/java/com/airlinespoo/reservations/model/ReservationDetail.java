package com.airlinespoo.reservations.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

/*  Detalle del pasajero asociado a una reserva  */
public class ReservationDetail implements Identifiable<Integer> {
    private final int id;
    private String passengerFullName;
    private String passengerDocument;
    private Seat seat;

    public ReservationDetail(int id, String passengerFullName, String passengerDocument, Seat seat) {
        this.id = (int) Validator.requirePositive(id,"identificación de los detalles de reserva");
        setPassengerFullName(passengerFullName);
        setPassengerDocument(passengerDocument);
        setSeat(seat);
    }

    private static final int MAX_LENGTH_NAME = 50;
    private static final int MAX_LENGTH_DOCUMENT = 20;

    @Override
    public Integer getId() {
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

    public void setPassengerFullName(String passengerFullName) {
        this.passengerFullName = Validator.requireText(passengerFullName, "nombre del pasajero", MAX_LENGTH_NAME);
    }

    public void setPassengerDocument(String passengerDocument) {
        this.passengerDocument = Validator.requireText(passengerDocument, "documento del pasajero", MAX_LENGTH_DOCUMENT);
    }

    public void setSeat(Seat seat) {
        this.seat = Validator.requireNotNull(seat, "silla");
    }
}
