package com.airlinespoo.app;

import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.repository.ReservationRepository;
import com.airlinespoo.reservations.service.ReservationService;
import com.airlinespoo.reservations.ui.ReservationsMenu;

import java.io.Console;


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
        ReservationRepository reservationRepository = new ReservationRepository(customerRepository, flightRepository);

        // 2. Generador con configuración del módulo (arranque 1, paso 1)
        IdGenerator reservationIds = new IdGenerator(1, 1);
        IdGenerator detailIds = new IdGenerator(1, 1);

        // Buscar los IDs máximos existentes usando ciclos for
        int maxReservationId = 0;
        int maxDetailId = 0;

        for (Reservation r : reservationRepository.findAll()) {
            if (r.getId() > maxReservationId) {
                maxReservationId = r.getId();
            }
            for (ReservationDetail d : r.getDetails()) {
                if (d.getId() > maxDetailId) {
                    maxDetailId = d.getId();
                }
            }
        }

        reservationIds.syncWith(maxReservationId);
        detailIds.syncWith(maxDetailId);

        // 3. Inyección de dependencias en el servicio
        ReservationService reservationService = new ReservationService(reservationRepository, reservationRepository, customerService, flightService);

        // 4. Iniciar menú interactivo
        ConsoleInput console = new ConsoleInput();
        ReservationsMenu reservationMenu = new ReservationsMenu(console, reservationService, customerService, flightService);
        reservationMenu.run();

    }
}
