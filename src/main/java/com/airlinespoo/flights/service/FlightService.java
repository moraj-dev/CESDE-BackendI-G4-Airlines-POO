package com.airlinespoo.flights.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.model.FlightReportRow;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Reglas de negocio de vuelos. */
public class FlightService {

    /** Año mínimo permitido en los reportes. */
    public static final int MIN_YEAR = 2000;
    /** Año máximo permitido en los reportes. */
    public static final int MAX_YEAR = 2100;

    private final FlightRepository flightRepository;
    private final LocationService locationService;
    private final AirlineService airlineService;
    private final IdGenerator flightIds;

    /**
     * Crea el servicio de vuelos con sus dependencias.
     *
     * @param flightRepository repositorio de vuelos
     * @param locationService servicio de ubicaciones
     * @param airlineService servicio de aerolíneas
     * @param flightIds generador de identificadores
     */
    public FlightService(FlightRepository flightRepository, LocationService locationService,
                         AirlineService airlineService, IdGenerator flightIds) {
        this.flightRepository = flightRepository;
        this.locationService = locationService;
        this.airlineService = airlineService;
        this.flightIds = flightIds;
    }

    /**
     * Crea y guarda un vuelo.
     *
     * @param flightName código del vuelo
     * @param departureAirportId identificador del aeropuerto de salida
     * @param arrivalAirportId identificador del aeropuerto de llegada
     * @param departureDate fecha de salida
     * @param departureTime hora de salida
     * @param arrivalDate fecha de llegada
     * @param arrivalTime hora de llegada
     * @param airlineId identificador de la aerolínea
     * @return el vuelo creado
     */
    public Flight createFlight(String flightName, UUID departureAirportId, UUID arrivalAirportId,
                               LocalDate departureDate, LocalTime departureTime,
                               LocalDate arrivalDate, LocalTime arrivalTime, int airlineId) {
        // peekNext: id provisional; con JDBC el repositorio devuelve el id real de la base de datos
        Flight flight = buildFlight(flightIds.peekNext(), flightName, departureAirportId, arrivalAirportId,
                departureDate, departureTime, arrivalDate, arrivalTime, airlineId);
        ensureFlightNameAvailable(flight.getFlightName(), null);
        Flight saved = flightRepository.create(flight);
        flightIds.syncWith(saved.getId());
        return saved;
    }

    /**
     * Busca un vuelo por identificador.
     *
     * @param id identificador del vuelo
     * @return el vuelo encontrado
     * @throws NotFoundException si no existe
     */
    public Flight getFlight(int id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("vuelo", id));
    }

    /**
     * Busca un vuelo por código.
     *
     * @param flightName código del vuelo
     * @return el vuelo encontrado
     * @throws NotFoundException si no existe
     */
    public Flight getFlightByName(String flightName) {
        String code = Validator.requireText(flightName, "código del vuelo");
        return flightRepository.findByFlightName(code)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un vuelo con el código '%s'.".formatted(code)));
    }

    /** @return todos los vuelos ordenados por salida e identificador */
    public List<Flight> listFlights() {
        return sorted(flightRepository.findAll());
    }

    /**
     * Actualiza los datos de un vuelo existente.
     *
     * @param id identificador del vuelo
     * @param flightName código del vuelo
     * @param departureAirportId identificador del aeropuerto de salida
     * @param arrivalAirportId identificador del aeropuerto de llegada
     * @param departureDate fecha de salida
     * @param departureTime hora de salida
     * @param arrivalDate fecha de llegada
     * @param arrivalTime hora de llegada
     * @param airlineId identificador de la aerolínea
     * @return el vuelo actualizado
     */
    public Flight updateFlight(int id, String flightName, UUID departureAirportId, UUID arrivalAirportId,
                               LocalDate departureDate, LocalTime departureTime,
                               LocalDate arrivalDate, LocalTime arrivalTime, int airlineId) {
        getFlight(id);
        Flight updated = buildFlight(id, flightName, departureAirportId, arrivalAirportId,
                departureDate, departureTime, arrivalDate, arrivalTime, airlineId);
        ensureFlightNameAvailable(updated.getFlightName(), id);
        return flightRepository.update(updated);
    }

    /**
     * Elimina un vuelo existente.
     *
     * @param id identificador del vuelo
     * @throws NotFoundException si el vuelo no existe
     */
    public void deleteFlight(int id) {
        getFlight(id);
        // Pendiente de integración: impedir si el vuelo tiene reservas (flightHasReservations)
        flightRepository.deleteById(id);
    }

    /** @param airlineId identificador de la aerolínea
     *  @return vuelos de la aerolínea */
    public List<Flight> listFlightsByAirline(int airlineId) {
        Airline airline = airlineService.getAirline(airlineId);
        return sorted(flightRepository.findByAirline(airline));
    }

    /**
     * Lista los vuelos cuya salida ocurre dentro del rango indicado.
     *
     * @param from fecha inicial
     * @param to fecha final
     * @return vuelos encontrados
     */
    public List<Flight> listFlightsBetween(LocalDate from, LocalDate to) {
        Validator.requireNotNull(from, "fecha inicial");
        Validator.requireNotNull(to, "fecha final");
        if (from.isAfter(to)) {
            throw new ValidationException("La fecha inicial no puede ser posterior a la final.");
        }
        return sorted(flightRepository.findByDepartureDateBetween(from, to));
    }

    /**
     * Lista los vuelos de una ruta específica.
     *
     * @param departureId identificador del aeropuerto de salida
     * @param arrivalId identificador del aeropuerto de llegada
     * @return vuelos encontrados
     */
    public List<Flight> listFlightsByRoute(UUID departureId, UUID arrivalId) {
        Airport departure = locationService.getAirport(departureId);
        Airport arrival = locationService.getAirport(arrivalId);
        return sorted(flightRepository.findByRoute(departure, arrival));
    }

    /** @param airlineId identificador de la aerolínea
     *  @return cantidad de vuelos */
    public long countFlightsByAirline(int airlineId) {
        return flightRepository.countByAirline(airlineService.getAirline(airlineId));
    }

    /** @param airlineId identificador de la aerolínea
     *  @return {@code true} si la aerolínea tiene vuelos */
    public boolean airlineHasFlights(int airlineId) {
        return flightRepository.existsByAirline(airlineService.getAirline(airlineId));
    }

    /** @param airportId identificador del aeropuerto
     *  @return {@code true} si el aeropuerto aparece en algún vuelo */
    public boolean airportHasFlights(UUID airportId) {
        return flightRepository.existsByAirport(locationService.getAirport(airportId));
    }

    /**
     * Genera el reporte mensual de vuelos agrupados por aerolínea.
     *
     * @param year año del reporte
     * @param month mes del reporte, entre 1 y 12
     * @return vuelos del mes agrupados por aerolínea
     */
    public Map<Airline, List<FlightReportRow>> listFlightsByAirlineInMonth(int year, int month) {
        Validator.requireInRange(year, MIN_YEAR, MAX_YEAR, "año");
        Validator.requireInRange(month, 1, 12, "mes");
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<Flight> flights = flightRepository.findByDepartureDateBetween(from, to).stream()
                .sorted(Comparator
                        .comparing((Flight f) -> f.getAirline().getName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Comparator.naturalOrder()))
                .toList();

        Map<Airline, List<FlightReportRow>> report = new LinkedHashMap<>();
        for (Flight f : flights) {
            report.computeIfAbsent(f.getAirline(), a -> new ArrayList<>()).add(toRow(f));
        }
        return report;
    }

    private Flight buildFlight(int id, String flightName, UUID departureAirportId, UUID arrivalAirportId,
                               LocalDate departureDate, LocalTime departureTime,
                               LocalDate arrivalDate, LocalTime arrivalTime, int airlineId) {
        Airport departure = locationService.getAirport(departureAirportId);
        Airport arrival = locationService.getAirport(arrivalAirportId);
        Airline airline = airlineService.getAirline(airlineId);
        return new Flight(id, flightName, departure, arrival,
                departureDate, departureTime, arrivalDate, arrivalTime, airline);
    }

    private void ensureFlightNameAvailable(String flightName, Integer ownerId) {
        flightRepository.findByFlightName(flightName)
                .filter(f -> !f.getId().equals(ownerId))
                .ifPresent(f -> {
                    throw new DuplicateException(
                            "Ya existe un vuelo con el código '%s'.".formatted(flightName));
                });
    }

    private List<Flight> sorted(List<Flight> flights) {
        return flights.stream().sorted().toList();
    }

    private FlightReportRow toRow(Flight f) {
        return new FlightReportRow(
                f.getAirline().getName(), f.getFlightName(),
                f.getDeparture().getCity().getName(), f.getDeparture().getName(),
                f.getDepartureDate(), f.getDepartureTime(),
                f.getArrival().getCity().getName(), f.getArrival().getName(),
                f.getArrivalDate(), f.getArrivalTime());
    }
}
