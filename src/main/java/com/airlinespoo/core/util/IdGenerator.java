package com.airlinespoo.core.util;

/**
 * Generador de identificadores numéricos consecutivos, configurable por valor de
 * arranque y paso.
 *
 * <p>En el modelo de datos original el motor numera automáticamente (columnas
 * {@code IDENTITY}); aquí lo hace este objeto. Configuración del proyecto:</p>
 * <ul>
 *   <li>Aerolíneas: arranque 100, paso 10 (100, 110, 120...).</li>
 *   <li>Ciudades, vuelos, reservas y detalles: arranque 1, paso 1 (1, 2, 3...).</li>
 * </ul>
 *
 * <p>Cada tabla tiene su <b>propia instancia</b>; por eso no se usa un contador estático
 * compartido.</p>
 */
public class IdGenerator {

    private int next;
    private final int step;

    /**
     * Crea un generador.
     *
     * @param start primer valor que entregará (mayor que cero)
     * @param step  incremento entre un valor y el siguiente (mayor que cero)
     * @throws com.airlinespoo.core.exception.ValidationException si algún valor no es positivo
     */
    public IdGenerator(int start, int step) {
        this.next = (int) Validator.requirePositive(start, "valor de arranque");
        this.step = (int) Validator.requirePositive(step, "paso");
    }

    /**
     * Entrega el valor actual y avanza al siguiente: primero se <i>entrega</i>, luego se
     * <i>avanza</i>.
     *
     * @return el siguiente identificador disponible
     */
    public synchronized int next() {
        int current = next;
        next += step;
        return current;
    }

    /**
     * Consulta el valor que entregaría la próxima llamada a {@link #next()}, sin avanzar.
     *
     * @return el próximo identificador
     */
    public synchronized int peekNext() {
        return next;
    }

    /**
     * Se sincroniza con ids ya existentes (por ejemplo, después de cargar datos de
     * ejemplo) para no repetir identificadores.
     *
     * <p>Si el mayor id existente es igual o superior al próximo valor, el generador salta
     * a {@code maxExistingId + paso}. Si no, no cambia.</p>
     *
     * @param maxExistingId mayor identificador que ya está en uso
     */
    public synchronized void syncWith(int maxExistingId) {
        if (maxExistingId >= next) {
            next = maxExistingId + step;
        }
    }
}
