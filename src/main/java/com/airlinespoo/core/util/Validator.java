package com.airlinespoo.core.util;

import com.airlinespoo.core.exception.ValidationException;

/**
 * Utilidades de validación compartidas por todos los modelos.
 *
 * <p>Clase con <b>solo métodos estáticos</b>: no se instancia (constructor privado) ni se
 * hereda ({@code final}). Todas las validaciones lanzan {@link ValidationException} e
 * incluyen el <b>nombre del campo</b> en el mensaje, por ejemplo:
 * "El campo 'nombre del país' es obligatorio."</p>
 *
 * <p>Cada método hace una sola cosa; las validaciones compuestas las arma quien las usa.</p>
 */
public final class Validator {

    private Validator() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Exige que el valor no sea {@code null}.
     *
     * @param value valor a validar
     * @param field nombre del campo, para el mensaje de error
     * @param <T>   tipo del valor
     * @return el mismo valor, para poder encadenar la llamada
     * @throws ValidationException si el valor es {@code null}
     */
    public static <T> T requireNotNull(T value, String field) {
        if (value == null) {
            throw new ValidationException("El campo '%s' es obligatorio.".formatted(field));
        }
        return value;
    }

    /**
     * Exige un texto con contenido y lo devuelve normalizado (ver {@link #normalize(String)}).
     *
     * @param value texto a validar
     * @param field nombre del campo
     * @return el texto sin espacios sobrantes
     * @throws ValidationException si es {@code null}, vacío o solo espacios
     */
    public static String requireText(String value, String field) {
        String normalized = normalize(value);
        if (normalized == null || normalized.isEmpty()) {
            throw new ValidationException("El campo '%s' no puede estar vacío.".formatted(field));
        }
        return normalized;
    }

    /**
     * Exige un texto con contenido y con una longitud máxima.
     *
     * @param value     texto a validar
     * @param field     nombre del campo
     * @param maxLength longitud máxima permitida, ya normalizado el texto
     * @return el texto normalizado
     * @throws ValidationException si está vacío o supera la longitud máxima
     */
    public static String requireText(String value, String field, int maxLength) {
        String normalized = requireText(value, field);
        requireMaxLength(normalized, maxLength, field);
        return normalized;
    }

    /**
     * Exige que un texto no supere una longitud máxima. Un {@code null} se acepta, porque
     * así se validan los campos opcionales.
     *
     * @param value     texto a validar (puede ser {@code null})
     * @param maxLength longitud máxima permitida
     * @param field     nombre del campo
     * @return el mismo texto
     * @throws ValidationException si el texto es más largo que el máximo
     */
    public static String requireMaxLength(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(
                    "El campo '%s' no puede tener más de %d caracteres.".formatted(field, maxLength));
        }
        return value;
    }

    /**
     * Exige un número mayor que cero.
     *
     * @param value valor a validar
     * @param field nombre del campo
     * @return el mismo valor
     * @throws ValidationException si es cero o negativo
     */
    public static long requirePositive(long value, String field) {
        if (value <= 0) {
            throw new ValidationException("El campo '%s' debe ser mayor que cero.".formatted(field));
        }
        return value;
    }

    /**
     * Exige un número dentro de un intervalo cerrado {@code [min, max]}.
     *
     * @param value valor a validar
     * @param min   mínimo permitido (inclusive)
     * @param max   máximo permitido (inclusive)
     * @param field nombre del campo
     * @return el mismo valor
     * @throws ValidationException si está fuera del intervalo
     */
    public static long requireInRange(long value, long min, long max, String field) {
        if (value < min || value > max) {
            throw new ValidationException(
                    "El campo '%s' debe estar entre %d y %d.".formatted(field, min, max));
        }
        return value;
    }

    /**
     * Normaliza un texto: quita los espacios al inicio y al final y reduce los espacios
     * repetidos del medio a uno solo.
     *
     * <p>Ejemplo: {@code "  Medellín   Antioquia "} se convierte en {@code "Medellín Antioquia"}.</p>
     *
     * @param value texto a normalizar (puede ser {@code null})
     * @return el texto normalizado, o {@code null} si el valor era {@code null}
     */
    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
