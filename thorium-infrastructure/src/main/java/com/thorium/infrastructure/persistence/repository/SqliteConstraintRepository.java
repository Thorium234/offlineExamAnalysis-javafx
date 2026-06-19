package com.thorium.infrastructure.persistence.repository;

import com.thorium.application.port.ConstraintRepository;
import com.thorium.domain.model.Constraint;
import com.thorium.domain.value.ConstraintType;
import com.thorium.infrastructure.persistence.SQLiteConnectionProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteConstraintRepository extends AbstractRepository implements ConstraintRepository {

    public SqliteConstraintRepository(SQLiteConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Constraint save(Constraint constraint) {
        try (Connection conn = connection()) {
            String sql = """
                    INSERT INTO constraints (constraint_type, enabled, parameters)
                    VALUES (?, ?, ?)
                    ON CONFLICT(constraint_type) DO UPDATE SET enabled=excluded.enabled, parameters=excluded.parameters
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, constraint.getConstraintType().name());
                ps.setInt(2, constraint.isEnabled() ? 1 : 0);
                ps.setString(3, constraint.getParameters());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        constraint.setId(keys.getLong(1));
                    }
                }
            }
            if (constraint.getId() == null) {
                findByType(constraint.getConstraintType()).ifPresent(existing -> constraint.setId(existing.getId()));
            }
            commit(conn);
            return constraint;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save constraint", e);
        }
    }

    @Override
    public Optional<Constraint> findByType(ConstraintType type) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM constraints WHERE constraint_type = ?")) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find constraint", e);
        }
    }

    @Override
    public List<Constraint> findAll() {
        try (Connection conn = connection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM constraints ORDER BY constraint_type")) {
            List<Constraint> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list constraints", e);
        }
    }

    private Constraint map(ResultSet rs) throws SQLException {
        return new Constraint(
                rs.getLong("id"),
                ConstraintType.valueOf(rs.getString("constraint_type")),
                rs.getInt("enabled") == 1,
                rs.getString("parameters")
        );
    }
}
