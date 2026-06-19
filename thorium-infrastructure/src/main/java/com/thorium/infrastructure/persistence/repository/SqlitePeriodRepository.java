package com.thorium.infrastructure.persistence.repository;

import com.thorium.application.port.PeriodRepository;
import com.thorium.domain.model.Period;
import com.thorium.infrastructure.persistence.SQLiteConnectionProvider;

import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlitePeriodRepository extends AbstractRepository implements PeriodRepository {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public SqlitePeriodRepository(SQLiteConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Period save(Period period) {
        try (Connection conn = connection()) {
            if (period.getId() == null) {
                insert(conn, period);
            } else {
                update(conn, period);
            }
            commit(conn);
            return period;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save period", e);
        }
    }

    private void insert(Connection conn, Period period) throws SQLException {
        String sql = "INSERT INTO periods (period_number, start_time, end_time, label) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, period);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    period.setId(keys.getLong(1));
                }
            }
        }
    }

    private void update(Connection conn, Period period) throws SQLException {
        String sql = "UPDATE periods SET period_number=?, start_time=?, end_time=?, label=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, period);
            ps.setLong(5, period.getId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Period period) throws SQLException {
        ps.setInt(1, period.getPeriodNumber());
        ps.setString(2, period.getStartTime().format(TIME_FORMAT));
        ps.setString(3, period.getEndTime().format(TIME_FORMAT));
        ps.setString(4, period.getLabel());
    }

    @Override
    public Optional<Period> findById(Long id) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM periods WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find period", e);
        }
    }

    @Override
    public List<Period> findAll() {
        try (Connection conn = connection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM periods ORDER BY period_number")) {
            List<Period> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list periods", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM periods WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            commit(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete period", e);
        }
    }

    @Override
    public int count() {
        try (Connection conn = connection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM periods")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count periods", e);
        }
    }

    private Period map(ResultSet rs) throws SQLException {
        return new Period(
                rs.getLong("id"),
                rs.getInt("period_number"),
                LocalTime.parse(rs.getString("start_time"), TIME_FORMAT),
                LocalTime.parse(rs.getString("end_time"), TIME_FORMAT),
                rs.getString("label")
        );
    }
}
