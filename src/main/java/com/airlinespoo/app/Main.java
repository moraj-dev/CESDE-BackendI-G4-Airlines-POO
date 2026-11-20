package com.airlinespoo.app;

import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.airlines.ui.AirlineMenu;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.IdGenerator;

import java.util.List;

/**
 * Punto de entrada de Airlines-poo.
 * Ensambla todos los módulos del sistema y arranca el menú principal.
 */
public final class Main {

private Main() {
    // Clase de arranque: no se instancia.
}

public static void main(String[] args) {
    System.out.println("=== Airlines-poo ===");
    System.out.println("Iniciando sistema con persistencia en SQL Server...");

    // 1. Repositorios y Servicios independientes
    // --- Módulo 3: Aerolíneas ---
    AirlineRepository airlineRepository = new AirlineRepository();
    IdGenerator airlineIds = new IdGenerator(100, 10);
    AirlineService airlineService = new AirlineService(airlineRepository, airlineIds);

    // --- Módulo 1: Clientes (descomentar cuando suban customers) ---
    // CustomerRepository customerRepository = new CustomerRepository();
    // CustomerService customerService = new CustomerService(customerRepository);

    // --- Módulo 2: Ubicaciones (descomentar cuando suban locations) ---
    // LocationRepository locationRepository = new LocationRepository();
    // LocationService locationService = new LocationService(locationRepository);

    // 2. Servicios dependientes
    // --- Módulo 4: Vuelos (descomentar cuando suban flights) ---
    // FlightRepository flightRepository = new FlightRepository();
    // FlightService flightService = new FlightService(flightRepository, locationService, airlineService);

    // --- Módulo 5: Reservas (descomentar cuando estén Clientes y Vuelos) ---
    // ReservationRepository reservationRepository = new ReservationRepository();
    // ReservationService reservationService = new ReservationService(reservationRepository, customerService, flightService);

    // 3. Crear los menús de cada módulo
    Menu airlineMenu = new AirlineMenu(airlineService);
    // Menu customerMenu = new CustomerMenu(customerService);
    // Menu locationMenu = new LocationMenu(locationService);
    // Menu flightMenu = new FlightMenu(flightService);
    // Menu reservationMenu = new ReservationsMenu(reservationService, customerService, flightService);

    // 4. Entregar los menús al Menú Principal y ejecutar
    MainMenu mainMenu = new MainMenu(List.of(
        airlineMenu
        // customerMenu,
        // locationMenu,
        // flightMenu,
        // reservationMenu
    ));
    mainMenu.run();
}
}

