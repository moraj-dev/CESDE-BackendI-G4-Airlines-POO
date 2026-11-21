package com.airlinespoo.app;

import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.airlines.ui.AirlineMenu;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;
import com.airlinespoo.customers.service.CountryService;
import com.airlinespoo.customers.service.CustomerSampleData;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.service.DocumentTypeService;
import com.airlinespoo.customers.ui.CountryMenu;
import com.airlinespoo.customers.ui.CustomerMenu;
import com.airlinespoo.customers.ui.DocumentTypeMenu;
import com.airlinespoo.flights.repository.DbConnection;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.flights.repository.JdbcFlightRepository;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.flights.ui.FlightMenu;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;
import com.airlinespoo.locations.service.LocationService;
import com.airlinespoo.locations.ui.LocationMenu;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.repository.ReservationRepository;
import com.airlinespoo.reservations.service.ReservationService;
import com.airlinespoo.reservations.ui.ReservationsMenu;

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
    System.out.println("Iniciando sistema...");

    ConsoleInput console = new ConsoleInput();

    // Módulo 1: Clientes y catálogos
    CustomerRepository customerRepository = new CustomerRepository();
    CountryRepository countryRepository = new CountryRepository();
    DocumentTypeRepository documentTypeRepository = new DocumentTypeRepository();
    CountryService countryService = new CountryService(countryRepository, customerRepository);
    DocumentTypeService documentTypeService = new DocumentTypeService(documentTypeRepository, customerRepository);
    CustomerService customerService = new CustomerService(
        customerRepository, countryRepository, documentTypeRepository);
    CustomerSampleData.load(countryRepository, documentTypeRepository, customerRepository);

    // Módulo 2: Ubicaciones
    CityRepository cityRepository = new CityRepository();
    AirportRepository airportRepository = new AirportRepository();
    LocationService locationService = new LocationService(cityRepository, airportRepository);

    // Módulo 3: Aerolíneas
    AirlineRepository airlineRepository = new AirlineRepository();
    IdGenerator airlineIds = new IdGenerator(100, 10);
    AirlineService airlineService = new AirlineService(airlineRepository, airlineIds);

    // Módulo 4: Vuelos
    DbConnection dbConnection = new DbConnection();
    FlightRepository flightRepository = new JdbcFlightRepository(dbConnection);
    FlightService flightService = new FlightService(
        flightRepository, locationService, airlineService, new IdGenerator(1, 1));

    // Módulo 5: Reservas
    ReservationRepository reservationRepository = new ReservationRepository(customerRepository, flightRepository);

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

    ReservationService reservationService = new ReservationService(
        reservationRepository, customerService, flightService, reservationIds, detailIds);

    // Menús de los módulos
    Menu customerMenu = new CustomerMenu(customerService, console);
    Menu countryMenu = new CountryMenu(countryService, console);
    Menu documentTypeMenu = new DocumentTypeMenu(documentTypeService, console);
    Menu locationMenu = new LocationMenu(console, locationService);
    Menu airlineMenu = new AirlineMenu(airlineService);
    Menu flightMenu = new FlightMenu(console, flightService, locationService, airlineService, reservationService);
    Menu reservationMenu = new ReservationsMenu(console, reservationService, customerService, flightService);

    MainMenu mainMenu = new MainMenu(List.of(
        customerMenu,
        countryMenu,
        documentTypeMenu,
        locationMenu,
        airlineMenu,
        flightMenu,
        reservationMenu
    ));
    mainMenu.run();
}
}
