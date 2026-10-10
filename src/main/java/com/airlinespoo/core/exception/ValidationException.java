package com.airlinespoo.core.exception;

/**
 * Se lanza cuando un dato no cumple una regla de formato o de rango:
 * texto vacío, longitud excedida, número negativo, valor fuera de rango, etc.
 *
 * <p>Normalmente la lanzan los constructores y setters del modelo, apoyándose en
 * {@link com.airlinespoo.core.util.Validator}.</p>
 */
public class ValidationException extends AirlinesException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message descripción del dato inválido, incluyendo el nombre del campo
     */
    public ValidationException(String message) {
        super(message);
    }
}
