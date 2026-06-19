package com.thorium.infrastructure.persistence.repository;

import com.thorium.application.port.TeacherAvailabilityRepository;
import com.thorium.domain.model.TeacherAvailability;
import com.thorium.domain.value.DayOfWeek;
import com.thorium.infrastructure.persistence.SQLiteConnectionProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteTeacherAvailabilityRepository extends AbstractRepository implements TeacherAvailabilityRepository {

    public SqliteTeacherAvailabilityRepository(SQLiteConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public TeacherAvailability save(TeacherAvailability availability) {
        try (Connection conn = connection()) {
            if (availability.getId() == null) {
                insert(conn, availability);
            } else {
                update(conn, availability);
            }
            commit(conn);
            return availability;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save teacher availability", e);
        }
    }

    private void insert(Connection conn, TeacherAvailability a) throws SQLException {
        String sql = """
                INSERT INTO teacher_availability (teacher_id, day_of_week, period_number, available)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(teacher_id, day_of_week, period_number)
                DO UPDATE SET available=excluded.available
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, a);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setId(keys.getLong(1));
                }
            }
            if (a.getId() == null) {
                try (PreparedStatement find = conn.prepareStatement(
                        "SELECT id FROM teacher_availability WHERE teacher_id=? AND day_of_week=? AND period_number=?")) {
                    find.setLong(1, a.getTeacherId());
                    find.setString(2, a.getDayOfWeek().name());
                    find.setInt(3, a.getPeriodNumber());
                    try (ResultSet rs = find.executeQuery()) {
                        if (rs.next()) {
                            a.setId(rs.getLong(1));
                        }
                    }
                }
            }
        }
    }

    private void update(Connection conn, TeacherAvailability a) throws SQLException {
        String sql = "UPDATE teacher_availability SET teacher_id=?, day_of_week=?, period_number=?, available=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, a);
            ps.setLong(5, a.getId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, TeacherAvailability a) throws SQLException {
        ps.setLong(1, a.getTeacherId());
        ps.setString(2, a.getDayOfWeek().name());
        ps.setInt(3, a.getPeriodNumber());
        ps.setInt(4, a.isAvailable() ? 1 : 0);
    }

    @Override
    public List<TeacherAvailability> findByTeacherId(Long teacherId) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM teacher_availability WHERE teacher_id = ? ORDER BY day_of_week, period_number")) {
            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapAll(rs);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find availability", e);
        }
    }

    @Override
    public List<TeacherAvailability> findAll() {
        try (Connection conn = connection();
             ResultSet rs = conn.createStatement().executeQuery(
                     "SELECT * FROM teacher_availability ORDER BY teacher_id, day_of_week, period_number")) {
            return mapAll(rs);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list availability", e);
        }
    }

    @Override
    public void deleteByTeacherId(Long teacherId) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM teacher_availability WHERE teacher_id = ?")) {
            ps.setLong(1, teacherId);
            ps.executeUpdate();
            commit(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete availability", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM teacher_availability WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            commit(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete availability", e);
        }
    }

    private List<TeacherAvailability> mapAll(ResultSet rs) throws SQLException {
        List<TeacherAvailability> list = new ArrayList<>();
        while (rs.next()) {
            list.add(map(rs));
        }
        return list;
    }

    private TeacherAvailability map(ResultSet rs) throws SQLException {
        return new TeacherAvailability(
                rs.getLong("id"),
                rs.getLong("teacher_id"),
                DayOfWeek.fromString(rs.getString("day_of_week")),
                rs.getInt("period_number"),
                rs.getInt("available") == 1
        );
    }
}
