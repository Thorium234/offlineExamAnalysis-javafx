package com.thorium.infrastructure.persistence.repository;

import com.thorium.application.port.BreakRepository;
import com.thorium.domain.model.BreakPeriod;
import com.thorium.infrastructure.persistence.SQLiteConnectionProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteBreakRepository extends AbstractRepository implements BreakRepository {

    public SqliteBreakRepository(SQLiteConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public BreakPeriod save(BreakPeriod breakPeriod) {
        try (Connection conn = connection()) {
            if (breakPeriod.getId() == null) {
                insert(conn, breakPeriod);
            } else {
                update(conn, breakPeriod);
            }
            commit(conn);
            return breakPeriod;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save break", e);
        }
    }

    private void insert(Connection conn, BreakPeriod bp) throws SQLException {
        String sql = "INSERT INTO breaks (name, after_period, duration_minutes, sort_order) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, bp);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    bp.setId(keys.getLong(1));
                }
            }
        }
    }

    private void update(Connection conn, BreakPeriod bp) throws SQLException {
        String sql = "UPDATE breaks SET name=?, after_period=?, duration_minutes=?, sort_order=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, bp);
            ps.setLong(5, bp.getId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, BreakPeriod bp) throws SQLException {
        ps.setString(1, bp.getName());
        ps.setInt(2, bp.getAfterPeriod());
        ps.setInt(3, bp.getDurationMinutes());
        ps.setInt(4, bp.getSortOrder());
    }

    @Override
    public Optional<BreakPeriod> findById(Long id) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM breaks WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find break", e);
        }
    }

    @Override
    public List<BreakPeriod> findAll() {
        try (Connection conn = connection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM breaks ORDER BY sort_order, after_period")) {
            List<BreakPeriod> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list breaks", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM breaks WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            commit(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete break", e);
        }
    }

    private BreakPeriod map(ResultSet rs) throws SQLException {
        return new BreakPeriod(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getInt("after_period"),
                rs.getInt("duration_minutes"),
                rs.getInt("sort_order")
        );
    }
}
