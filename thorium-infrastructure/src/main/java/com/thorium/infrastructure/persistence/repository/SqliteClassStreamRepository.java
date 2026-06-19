package com.thorium.infrastructure.persistence.repository;

import com.thorium.application.port.ClassStreamRepository;
import com.thorium.domain.model.ClassStream;
import com.thorium.infrastructure.persistence.SQLiteConnectionProvider;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteClassStreamRepository extends AbstractRepository implements ClassStreamRepository {

    public SqliteClassStreamRepository(SQLiteConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public ClassStream save(ClassStream classStream) {
        try (Connection conn = connection()) {
            if (classStream.getId() == null) {
                insert(conn, classStream);
            } else {
                update(conn, classStream);
            }
            commit(conn);
            return classStream;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save class stream", e);
        }
    }

    private void insert(Connection conn, ClassStream cs) throws SQLException {
        String sql = "INSERT INTO class_streams (code, form, stream, display_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, cs);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    cs.setId(keys.getLong(1));
                }
            }
        }
    }

    private void update(Connection conn, ClassStream cs) throws SQLException {
        String sql = "UPDATE class_streams SET code=?, form=?, stream=?, display_name=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, cs);
            ps.setLong(5, cs.getId());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, ClassStream cs) throws SQLException {
        ps.setString(1, cs.getCode());
        ps.setInt(2, cs.getForm());
        ps.setString(3, cs.getStream());
        ps.setString(4, cs.getDisplayName());
    }

    @Override
    public Optional<ClassStream> findById(Long id) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM class_streams WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find class stream", e);
        }
    }

    @Override
    public List<ClassStream> findAll() {
        try (Connection conn = connection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM class_streams ORDER BY form, stream")) {
            List<ClassStream> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list class streams", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM class_streams WHERE id = ?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            commit(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete class stream", e);
        }
    }

    @Override
    public long count() {
        try (Connection conn = connection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM class_streams")) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count class streams", e);
        }
    }

    private ClassStream map(ResultSet rs) throws SQLException {
        return new ClassStream(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getInt("form"),
                rs.getString("stream"),
                rs.getString("display_name")
        );
    }
}
