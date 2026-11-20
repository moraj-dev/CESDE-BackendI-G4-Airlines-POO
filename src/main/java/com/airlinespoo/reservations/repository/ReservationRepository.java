package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.exception.*;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* Repositorio de persistencia para el módulo de reservas conectado a SQL Server
 */

public class ReservationRepository implements IReservationRepository {

    private final CustomerRepository customerRepository;
    private final FlightRepository flightRepository;

    public ReservationRepository(CustomerRepository customerRepository,
                                 FlightRepository flightRepository) {
        this.customerRepository = customerRepository;
        this.flightRepository = flightRepository;
    }

    // Nombres de las tablas en SQL Server
    private static final String SQL_TABLE_RESERVATION = "tblReservations";
    private static final String SQL_TABLE_DETAIL = "tblDetailsReservation";

    private record Brief (int id, String customerId, Integer flightId) { }


    // --------------- Conexion a SQL Server ------------------
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=AirlinesDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "TuPassword123";

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new AirlinesException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }

    // ---------------- Metodos CRUD -----------------------
    @Override
    public Reservation create(Reservation reservation) {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + SQL_TABLE_RESERVATION + " (ReservationId, CustomerId, FlightId) VALUES (?, ?, ?)")) {
                    ps.setInt(1, reservation.getId());
                    ps.setString(2, reservation.getCustomer().getId());
                    ps.setInt(3, reservation.getFlight().getId());
                    ps.executeUpdate();
                }
                insertDetails(c, reservation);
                c.commit();
                return reservation;
            } catch (SQLException e) {
                c.rollback();
                throw translate(e, reservation);
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error de base de datos al guardar la reserva" + e.getMessage(), e);
        }
    }

    @Override
    public Reservation update(Reservation reservation) {
        if (!existsById(reservation.getId())) {
            throw new NotFoundException("reserva", reservation.getId());
        }
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE " + SQL_TABLE_RESERVATION + " SET CustomerId = ?, FlightId = ? WHERE ReservationId = ?")) {
                    ps.setString(1, reservation.getCustomer().getId());
                    ps.setInt(2, reservation.getFlight().getId());
                    ps.setInt(3, reservation.getId());
                    ps.executeUpdate();
                }
                deleteDetails(c, reservation.getId());
                insertDetails(c, reservation);
                c.commit();
                return reservation;
            } catch (SQLException e) {
                c.rollback();
                throw translate(e, reservation);
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error de base de datos al actualizar la reserva" + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Reservation> findById(Integer id) {
        List<Reservation> list = query("SELECT ReservationId, CustomerId, FlightId FROM " + SQL_TABLE_RESERVATION + " WHERE ReservationId = ?", id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Reservation> findAll() {
        return query("SELECT ReservationId, CustomerId, FlightId FROM " + SQL_TABLE_RESERVATION + " ORDER BY ReservationId", null);
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                deleteDetails(c, id);
                int rows;
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SQL_TABLE_RESERVATION + " WHERE ReservationId = ?")) {
                    ps.setInt(1, id);
                    rows = ps.executeUpdate();
                }
                c.commit();
                return rows > 0;
            } catch (SQLException e) {
                c.rollback();
                throw new AirlinesException("Error de base de datos al eliminar la reserva" + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error de base de datos al eliminar la reserva" + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        return exists("SELECT 1 FROM " + SQL_TABLE_RESERVATION + " WHERE ReservationId = ?", id);
    }

    @Override
    public long count() {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM " + SQL_TABLE_RESERVATION);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new AirlinesException("Error de base de datos al contar las reservas" + e.getMessage(), e);
        }
    }

    // ------------ Metodos de Reservas ----------------------
    @Override
    public List<Reservation> findByCustomer(Customer customer) {
        return query("SELECT ReservationId, CustomerId, FlightId FROM " + SQL_TABLE_RESERVATION + " WHERE CustomerId = ? ORDER BY ReservationId", customer.getId());
    }

    @Override
    public List<Reservation> findByFlight(Flight flight) {
        return query("SELECT ReservationId, CustomerId, FlightId FROM " + SQL_TABLE_RESERVATION + " WHERE FlightId = ? ORDER BY ReservationId", flight.getId());
    }

    @Override
    public boolean existsByCustomer(Customer customer) {
        return exists("SELECT 1 FROM ReservationId, CustomerId, FlightId WHERE CustomerId = ?", customer.getId());
    }

    @Override
    public boolean existsByFlight(Flight flight) {
        return exists("SELECT 1 FROM ReservationId, CustomerId, FlightId WHERE FlightId = ?", flight.getId());
    }

    // ------------ Metodos privados del Repositorio------------------------
    private List<Reservation> query(String sql, Object param) {
        try (Connection c = getConnection()) {
            List<Brief> brief = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (param != null) ps.setObject(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        brief.add(new Brief(rs.getInt("ReservationId"),
                                rs.getString("CustomerId"), rs.getObject("FlightId", Integer.class)));
                    }
                }
            }
            List<Reservation> result = new ArrayList<>();
            for (Brief b : brief) result.add(build(c, b));
            return result;
        } catch (SQLException e) {
            throw new AirlinesException("Error de base de datos al consultar reservas" + e.getMessage(), e);
        }
    }

    private Reservation build(Connection c, Brief b) throws SQLException {
        if (b.customerId() == null || b.customerId().isBlank()) {
            throw new AirlinesException("La reserva #" + b.id() + " no tiene cliente en la base de datos.");
        }
        if (b.flightId() == null) {
            throw new AirlinesException("La reserva #" + b.id() + " no tiene vuelo en la base de datos.");
        }
        Customer customer = customerRepository.findById(b.customerId().trim())
                .orElseThrow(() -> new NotFoundException("cliente", b.customerId()));
        Flight flight = flightRepository.findById(b.flightId())
                .orElseThrow(() -> new NotFoundException("vuelo", b.flightId()));
        List<ReservationDetail> details = loadDetails(c, b.id());
        if (details.isEmpty()) {
            throw new AirlinesException("La reserva #" + b.id() + " no tiene pasajeros en la base de datos.");
        }
        Reservation reservation = new Reservation(b.id(), customer, flight, details.get(0));
        for (int i = 1; i < details.size(); i++) reservation.addPassenger(details.get(i));
        return reservation;
    }

    private List<ReservationDetail> loadDetails(Connection c, int reservationId) throws SQLException {
        String sql = "SELECT DetailID, PassangerFullName, PassagerDocument, SeatNumer, RowNumber "
        + "FROM " + SQL_TABLE_DETAIL + " WHERE ReservationId = ? ORDER BY DetailId";
        List<ReservationDetail> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int detailId = rs.getInt("DetailId");
                    String row = rs.getString("RowNumber");
                    int number = rs.getInt("SeatNumer");

                    if (row == null || row.isBlank() || rs.wasNull()) {
                        throw new AirlinesException("El pasajero #" + detailId + " de la reserva #" + reservationId + " no tiene silla completa en la base de datos.");
                    }
                    list.add(new ReservationDetail(detailId,
                            rs.getString("PassangerFullName"),
                        rs.getString("PassagerDocument"),
                            new Seat(number, row.trim())));
                }
            }
        }
        return list;
    }

    private void insertDetails(Connection c, Reservation r) throws SQLException {
        String sql = "INSERT INTO " + SQL_TABLE_DETAIL + " (DetailId, ReservationId, PassangerFullName, "
                + "PassagerDocument, SeatNumer, RowNumber) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (ReservationDetail d : r.getDetails()) {
                ps.setInt(1, d.getId());
                ps.setInt(2, r.getId());
                ps.setString(3, d.getPassengerFullName());
                ps.setString(4, d.getPassengerDocument());
                ps.setString(5, d.getSeat().getRowNumber());
                ps.setInt(6, d.getSeat().getSeatNumber());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteDetails(Connection c, int reservationId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + SQL_TABLE_DETAIL + " WHERE ReservationId = ?")) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    private boolean exists(String sql, Object param) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error de base de datos al consultar reservas" + e.getMessage(), e);
        }
    }

    /* Validacion de que no haya duplicados en llaves primarias
    * Si el cliente o el vuelo no existen en la base de datos, hay violación de clave foránea
    * Texto más largo que la columna (nombre > 50 o documento > 20)
     */
    private RuntimeException translate(SQLException e, Reservation r) {
        int code = e.getErrorCode();
        String msg = String.valueOf(e.getMessage());
        if (code == 2627 || code == 2601) {
            if (msg.contains("tblDetailsReservation")) {
                return new BusinessRuleException("Ya existe un pasajero con el mismo identificador de detalle. "
                    + "Verifique que el generador de ids esté sincronizado con la base de datos.");
            }
            return new DuplicateException("reserva", r.getId());
        }
        if (code == 547) {
            return new BusinessRuleException("El cliente o el vuelo de la reserva no existen en la base de datos.");
        }
        if (code == 8152) {
            return new ValidationException("El nombre o el documento de un pasajero es demasiado largo.");
        }
        return new AirlinesException("Error de base de datos al guardar la reserva" + e.getMessage(), e);
    }


}
