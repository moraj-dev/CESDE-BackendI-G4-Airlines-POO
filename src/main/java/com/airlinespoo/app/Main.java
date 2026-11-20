package com.airlinespoo.app;

import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.airlines.ui.AirlineMenu;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.flights.repository.InMemoryFlightRepository;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.flights.ui.FlightMenu;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;
import com.airlinespoo.locations.service.LocationService;
import com.airlinespoo.locations.ui.LocationMenu;

import java.util.List;

/**
 * Punto de entrada de Airlines-poo.
 * Ensambla los módulos y arranca el menú principal.
 */
public final class Main {

    private Main() {
        // Clase de arranque: no se instancia.
    }

    public static void main(String[] args) {
        System.out.println("=== Airlines-poo ===");
        System.out.println("Iniciando sistema con persistencia en SQL Server...");

        // Módulo 3: Aerolíneas
        AirlineRepository airlineRepository = new AirlineRepository();
        IdGenerator airlineIds = new IdGenerator(100, 10);
        AirlineService airlineService = new AirlineService(airlineRepository, airlineIds);

        // Módulo 1: Clientes (pendiente de integrar)
        // CustomerRepository customerRepository = new CustomerRepository();
        // CustomerService customerService = new CustomerService(customerRepository);

        // Módulo 2: Ubicaciones
        CityRepository cityRepository = new CityRepository();
        AirportRepository airportRepository = new AirportRepository();
        LocationService locationService = new LocationService(cityRepository, airportRepository);

        // Módulo 4: Vuelos
        FlightRepository flightRepository = new InMemoryFlightRepository();
        FlightService flightService = new FlightService(
                flightRepository, locationService, airlineService, new IdGenerator(1, 1));

        // Módulo 5: Reservas (pendiente de integrar con Clientes)
        // ReservationRepository reservationRepository = new ReservationRepository();
        // ReservationService reservationService =
        //         new ReservationService(reservationRepository, customerService, flightService);

        Menu airlineMenu = new AirlineMenu(airlineService);
        Menu locationMenu = new LocationMenu(new ConsoleInput(), locationService);
        Menu flightMenu = new FlightMenu(
                new ConsoleInput(), flightService, locationService, airlineService);
        // Menu customerMenu = new CustomerMenu(customerService);
        // Menu reservationMenu =
        //         new ReservationsMenu(reservationService, customerService, flightService);

        MainMenu mainMenu = new MainMenu(List.of(
                airlineMenu,
                // customerMenu,
                locationMenu,
                flightMenu
                // reservationMenu
        ));
        mainMenu.run();
    }
}
