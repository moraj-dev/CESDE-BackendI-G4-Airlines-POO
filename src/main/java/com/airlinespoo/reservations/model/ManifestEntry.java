package com.airlinespoo.reservations.model;

public record ManifestEntry(
    String passengerFullName,
    String passengerDocument,
    Seat seat) {}
