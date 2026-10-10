package com.airlinespoo.core.exception;

/**
 * Se lanza cuando se intenta crear algo que ya existe: un id repetido o un valor
 * que debe ser único (nombre de aerolínea, código de vuelo, correo de cliente).
 *
 * <p>Equivale, en una base de datos, a violar una restricción {@code PRIMARY KEY} o
 * {@code UNIQUE}.</p>
 */
public class DuplicateException extends AirlinesException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje propio.
     *
     * @param message descripción del duplicado
     */
    public DuplicateException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje estándar para un id repetido.
     *
     * @param entityName nombre de la entidad en español (por ejemplo, "cliente")
     * @param id         identificador que ya existe
     */
    public DuplicateException(String entityName, Object id) {
        super("Ya existe un registro de %s con el id '%s'.".formatted(entityName, id));
    }
}
