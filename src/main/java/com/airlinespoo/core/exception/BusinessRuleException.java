package com.airlinespoo.core.exception;

/**
 * Se lanza cuando una regla de negocio que involucra varios objetos no se cumple:
 * silla ocupada, vuelo que ya salió, eliminar algo que está en uso, etc.
 *
 * <p>A diferencia de {@link ValidationException}, aquí cada dato por separado puede ser
 * válido; lo que falla es la relación entre ellos. Normalmente la lanza un servicio.</p>
 */
public class BusinessRuleException extends AirlinesException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message descripción de la regla que no se cumple
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
