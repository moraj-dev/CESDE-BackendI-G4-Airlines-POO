package com.airlinespoo.reservations.model;

/* Clase inmutable para generar fila del manifiesto de un vuelo
* */

public record ManifestEntry(
    String passengerFullName,
    String passengerDocument,
    Seat seat) {}
