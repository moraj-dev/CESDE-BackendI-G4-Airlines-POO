package com.airlinespoo.core.exception;

/**
 * Se lanza cuando se pide un objeto que no existe.
 *
 * <p>Por diseño, <b>la lanza el servicio y no el repositorio</b>: el repositorio solo
 * informa la ausencia (con {@link java.util.Optional#empty()}) y el servicio decide
 * si eso es un error en el contexto de la operación.</p>
 */
public class NotFoundException extends AirlinesException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje propio.
     *
     * @param message descripción de lo que no se encontró
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje estándar.
     *
     * @param entityName nombre de la entidad en español (por ejemplo, "vuelo")
     * @param id         identificador buscado
     */
    public NotFoundException(String entityName, Object id) {
        super("No existe un registro de %s con el id '%s'.".formatted(entityName, id));
    }
}
