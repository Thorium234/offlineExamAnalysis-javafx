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
        String sql = "INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (?, ?, ?, ?, ?, ?)";
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
        String sql = "UPDATE periods SET period_number=?, start_time=?, end_time=?, label=?, type=?, break_id=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, period);
            ps.setLong(7, period.getId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Period period) throws SQLException {
        ps.setInt(1, period.getPeriodNumber());
        ps.setString(2, period.getStartTime().format(TIME_FORMAT));
        ps.setString(3, period.getEndTime().format(TIME_FORMAT));
        ps.setString(4, period.getLabel());
        ps.setString(5, period.getType());
        if (period.getBreakId() != null) {
            ps.setLong(6, period.getBreakId());
        } else {
            ps.setNull(6, java.sql.Types.INTEGER);
        }
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

    @Override
    public int countLessons() {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM periods WHERE type = ?")) {
            ps.setString(1, Period.TYPE_LESSON);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count lesson periods", e);
        }
    }

    @Override
    public List<Integer> findLessonPeriodNumbers() {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT period_number FROM periods WHERE type = ? ORDER BY period_number")) {
            ps.setString(1, Period.TYPE_LESSON);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> numbers = new ArrayList<>();
                while (rs.next()) {
                    numbers.add(rs.getInt("period_number"));
                }
                return numbers;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find lesson period numbers", e);
        }
    }

    private Period map(ResultSet rs) throws SQLException {
        long breakIdRaw = rs.getLong("break_id");
        Long breakId = rs.wasNull() ? null : breakIdRaw;
        return new Period(
                rs.getLong("id"),
                rs.getInt("period_number"),
                LocalTime.parse(rs.getString("start_time"), TIME_FORMAT),
                LocalTime.parse(rs.getString("end_time"), TIME_FORMAT),
                rs.getString("label"),
                rs.getString("type"),
                breakId
        );
    }
}
