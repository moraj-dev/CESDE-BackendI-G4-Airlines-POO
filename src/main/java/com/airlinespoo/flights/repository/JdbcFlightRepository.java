package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositorio de vuelos sobre SQL Server con JDBC (tabla tblFlights). */
public class JdbcFlightRepository implements FlightRepository {

    // Códigos de error de SQL Server
    private static final int UNIQUE_CONSTRAINT_ERROR = 2627;
    private static final int UNIQUE_INDEX_ERROR = 2601;
    private static final int FOREIGN_KEY_ERROR = 547;

    private static final String SELECT_FLIGHTS =
            "SELECT f.FlightId, f.FlightName, f.DateDeparture, f.HourDeparture, "
            + "f.DateArrival, f.HourArrival, "
            + "d.AirportId AS DepId, d.name AS DepName, dc.CityId AS DepCityId, dc.name AS DepCityName, "
            + "a.AirportId AS ArrId, a.name AS ArrName, ac.CityId AS ArrCityId, ac.name AS ArrCityName, "
            + "al.airlineId AS AirlineId, al.name AS AirlineName "
            + "FROM tblFlights f "
            + "INNER JOIN tblAirports d ON f.Departure = d.AirportId "
            + "INNER JOIN tblCities dc ON d.CityId = dc.CityId "
            + "INNER JOIN tblAirports a ON f.Arrival = a.AirportId "
            + "INNER JOIN tblCities ac ON a.CityId = ac.CityId "
            + "INNER JOIN tblAirlines al ON f.AirlineId = al.airlineId ";

    private static final String INSERT_FLIGHT =
            "INSERT INTO tblFlights (FlightName, Departure, Arrival, DateDeparture, HourDeparture, "
            + "DateArrival, HourArrival, AirlineId) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_FLIGHT =
            "UPDATE tblFlights SET FlightName = ?, Departure = ?, Arrival = ?, DateDeparture = ?, "
            + "HourDeparture = ?, DateArrival = ?, HourArrival = ?, AirlineId = ? WHERE FlightId = ?";

    private final DbConnection db;

    public JdbcFlightRepository(DbConnection db) {
        this.db = db;
    }

    /** INSERT. El id lo genera SQL Server (IDENTITY), así que se devuelve un vuelo con el id real. */
    @Override
    public Flight create(Flight flight) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_FLIGHT, Statement.RETURN_GENERATED_KEYS)) {
            fillFlight(ps, flight);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new AirlinesException("La base de datos no devolvió el id del vuelo.");
                }
                return withId(flight, keys.getInt(1));
            }
        } catch (SQLException e) {
            throw translate("guardar el vuelo", e);
        }
    }

    @Override
    public Optional<Flight> findById(Integer id) {
        return query(SELECT_FLIGHTS + "WHERE f.FlightId = ?", "buscar el vuelo", id)
                .stream().findFirst();
    }

    @Override
    public List<Flight> findAll() {
        return query(SELECT_FLIGHTS + "ORDER BY f.DateDeparture, f.HourDeparture", "listar los vuelos");
    }

    /** UPDATE. Si no se afectó ninguna fila, el vuelo no existe. */
    @Override
    public Flight update(Flight flight) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_FLIGHT)) {
            fillFlight(ps, flight);
            ps.setInt(9, flight.getId());
            if (ps.executeUpdate() == 0) {
                throw new NotFoundException("vuelo", flight.getId());
            }
            return flight;
        } catch (SQLException e) {
            throw translate("actualizar el vuelo", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM tblFlights WHERE FlightId = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw translate("eliminar el vuelo", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        return scalar("SELECT COUNT(*) FROM tblFlights WHERE FlightId = ?", "consultar el vuelo", id) > 0;
    }

    @Override
    public long count() {
        return scalar("SELECT COUNT(*) FROM tblFlights", "contar los vuelos");
    }

    @Override
    public Optional<Flight> findByFlightName(String name) {
        return query(SELECT_FLIGHTS + "WHERE f.FlightName = ?", "buscar el vuelo por código", name)
                .stream().findFirst();
    }

    @Override
    public List<Flight> findByAirline(Airline airline) {
        return query(SELECT_FLIGHTS + "WHERE f.AirlineId = ?", "listar vuelos de la aerolínea",
                airline.getId());
    }

    @Override
    public List<Flight> findByDepartureDateBetween(LocalDate from, LocalDate to) {
        return query(SELECT_FLIGHTS + "WHERE f.DateDeparture BETWEEN ? AND ?",
                "listar vuelos por fechas", from, to);
    }

    @Override
    public List<Flight> findByRoute(Airport departure, Airport arrival) {
        return query(SELECT_FLIGHTS + "WHERE f.Departure = ? AND f.Arrival = ?",
                "listar vuelos por ruta", departure.getId().toString(), arrival.getId().toString());
    }

    @Override
    public long countByAirline(Airline airline) {
        return scalar("SELECT COUNT(*) FROM tblFlights WHERE AirlineId = ?",
                "contar vuelos de la aerolínea", airline.getId());
    }

    @Override
    public boolean existsByAirline(Airline airline) {
        return countByAirline(airline) > 0;
    }

    @Override
    public boolean existsByAirport(Airport airport) {
        String id = airport.getId().toString();
        return scalar("SELECT COUNT(*) FROM tblFlights WHERE Departure = ? OR Arrival = ?",
                "consultar vuelos del aeropuerto", id, id) > 0;
    }

    // ---------- Métodos de apoyo ----------

    /** Ejecuta un SELECT y convierte cada fila en un Flight. */
    private List<Flight> query(String sql, String action, Object... params) {
        List<Flight> flights = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flights.add(mapFlight(rs));
                }
            }
        } catch (SQLException e) {
            throw translate(action, e);
        }
        return flights;
    }

    /** Ejecuta un SELECT que devuelve un solo número (COUNT). */
    private long scalar(String sql, String action, Object... params) {
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw translate(action, e);
        }
    }

    private void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /** Llena los 8 parámetros comunes del INSERT y del UPDATE. El UUID viaja como texto. */
    private void fillFlight(PreparedStatement ps, Flight flight) throws SQLException {
        ps.setString(1, flight.getFlightName());
        ps.setString(2, flight.getDeparture().getId().toString());
        ps.setString(3, flight.getArrival().getId().toString());
        ps.setObject(4, flight.getDepartureDate());
        ps.setObject(5, flight.getDepartureTime());
        ps.setObject(6, flight.getArrivalDate());
        ps.setObject(7, flight.getArrivalTime());
        ps.setInt(8, flight.getAirline().getId());
    }

    /** Convierte una fila del JOIN en un Flight con sus objetos relacionados. */
    private Flight mapFlight(ResultSet rs) throws SQLException {
        City departureCity = new City(rs.getInt("DepCityId"), rs.getString("DepCityName"));
        Airport departure = new Airport(UUID.fromString(rs.getString("DepId")),
                rs.getString("DepName"), departureCity);
        City arrivalCity = new City(rs.getInt("ArrCityId"), rs.getString("ArrCityName"));
        Airport arrival = new Airport(UUID.fromString(rs.getString("ArrId")),
                rs.getString("ArrName"), arrivalCity);
        Airline airline = new Airline(rs.getInt("AirlineId"), rs.getString("AirlineName"));

        return new Flight(rs.getInt("FlightId"), rs.getString("FlightName"), departure, arrival,
                rs.getObject("DateDeparture", LocalDate.class),
                rs.getObject("HourDeparture", LocalTime.class),
                rs.getObject("DateArrival", LocalDate.class),
                rs.getObject("HourArrival", LocalTime.class),
                airline);
    }

    private Flight withId(Flight f, int newId) {
        return new Flight(newId, f.getFlightName(), f.getDeparture(), f.getArrival(),
                f.getDepartureDate(), f.getDepartureTime(),
                f.getArrivalDate(), f.getArrivalTime(), f.getAirline());
    }

    /** Convierte un SQLException en una excepción del sistema, con mensaje en español. */
    private AirlinesException translate(String action, SQLException e) {
        int code = e.getErrorCode();
        if (code == UNIQUE_CONSTRAINT_ERROR || code == UNIQUE_INDEX_ERROR) {
            return new DuplicateException("Ya existe un vuelo con ese código.");
        }
        if (code == FOREIGN_KEY_ERROR) {
            return new BusinessRuleException(
                    "El vuelo está relacionado con otros registros (aeropuertos, aerolínea o reservas).");
        }
        return new AirlinesException("No se pudo " + action + " en la base de datos.", e);
    }
}
