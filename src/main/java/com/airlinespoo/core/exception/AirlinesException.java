package com.airlinespoo.core.exception;

/**
 * Excepción base de todo el sistema Airlines-poo.
 *
 * <p>Hereda de {@link RuntimeException}, por lo que es <b>no verificada</b> (unchecked):
 * los métodos no están obligados a declararla con {@code throws} ni a capturarla.
 * Todas las excepciones propias del proyecto heredan de esta clase, de modo que
 * la capa de interfaz puede capturar cualquier error del sistema con un solo
 * {@code catch (AirlinesException e)}.</p>
 *
 * <p>Los mensajes se escriben en español porque se muestran al usuario final.</p>
 */
public class AirlinesException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con un mensaje para el usuario.
     *
     * @param message descripción del problema, en español
     */
    public AirlinesException(String message) {
        super(message);
    }

    /**
     * Crea la excepción con un mensaje y la causa original del error.
     *
     * @param message descripción del problema, en español
     * @param cause   excepción que originó este error
     */
    public AirlinesException(String message, Throwable cause) {
        super(message, cause);
    }
}
