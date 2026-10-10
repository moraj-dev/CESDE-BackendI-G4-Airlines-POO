package com.airlinespoo.core.ui;

/**
 * Contrato de las pantallas de consola del sistema.
 *
 * <p>Cada módulo tiene su propio menú ({@code CustomerMenu}, {@code FlightMenu}...) que
 * implementa esta interfaz. El menú principal guarda una <b>lista de {@code Menu}</b> y la
 * recorre sin saber de qué módulo es cada uno: eso es polimorfismo.</p>
 */
public interface Menu {

    /**
     * @return el título que se muestra como opción en el menú principal
     */
    String title();

    /**
     * Muestra el menú y atiende al usuario hasta que decida volver o salir.
     */
    void run();
}
