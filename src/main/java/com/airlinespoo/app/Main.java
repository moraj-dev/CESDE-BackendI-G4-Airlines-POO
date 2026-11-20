package com.airlinespoo.app;

import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.reservations.repository.ReservationRepository;
import com.airlinespoo.reservations.service.ReservationService;
import com.airlinespoo.reservations.ui.ReservationsMenu;


/**
 * Punto de entrada de Airlines-poo.
 *
 */
public final class Main {

    private Main() {
        // Clase de arranque: no se instancia.
    }

    public static void main(String[] args) {
        System.out.println("=== Airlines-poo ===");

        // 1. Instanciar el repositorio
        ReservationRepository reservationRepository = new ReservationRepository();

        // 2. Generador con configuración del módulo (arranque 1, paso 1)
        IdGenerator reservationIds = new IdGenerator(1, 1);
        IdGenerator detailIds = new IdGenerator(1, 1);

        // 3. Inyección de dependencias en el servicio
        ReservationService reservationService = new ReservationService(reservationRepository);

        // 4. Iniciar menú interactivo
        ReservationsMenu rMenu = new ReservationsMenu(reservationService);
        rMenu.run();

    }
}
