package com.airlinespoo.core.model;

/**
 * Contrato mínimo de toda entidad con identidad propia: ofrece su identificador.
 *
 * <p>Lo implementan {@code Country}, {@code DocumentType}, {@code Customer}, {@code City},
 * {@code Airport}, {@code Airline}, {@code Flight} y {@code Reservation}.</p>
 *
 * <p>El tipo del identificador varía según la entidad (entero, texto, {@link java.util.UUID}),
 * por eso la interfaz es genérica: {@code ID} es un <i>parámetro de tipo</i>.</p>
 *
 * @param <ID> tipo del identificador de la entidad
 */
public interface Identifiable<ID> {

    /**
     * @return el identificador de la entidad; nunca cambia después de creada
     */
    ID getId();
}
