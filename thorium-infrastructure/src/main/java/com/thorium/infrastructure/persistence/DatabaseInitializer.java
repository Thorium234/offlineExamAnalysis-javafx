package com.thorium.infrastructure.persistence;

import com.thorium.domain.model.Constraint;
import com.thorium.domain.value.ConstraintType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DatabaseInitializer {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final SQLiteConnectionProvider connectionProvider;

    public DatabaseInitializer(SQLiteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void initialize() {
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : loadSchema().split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
            connection.commit();
            seedDefaults();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private void seedDefaults() {
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {

            var periodCount = connection.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM periods");
            if (periodCount.next() && periodCount.getInt(1) == 0) {
                LocalTime start = LocalTime.of(8, 0);
                for (int i = 1; i <= 8; i++) {
                    LocalTime end = start.plusMinutes(40);
                    statement.execute(String.format(
                            "INSERT INTO periods (period_number, start_time, end_time, label) VALUES (%d, '%s', '%s', 'P%d')",
                            i, start.format(TIME_FORMAT), end.format(TIME_FORMAT), i));
                    start = end;
                }
            }

            var constraintCount = connection.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM constraints");
            if (constraintCount.next() && constraintCount.getInt(1) == 0) {
                for (ConstraintType type : ConstraintType.values()) {
                    statement.execute(String.format(
                            "INSERT INTO constraints (constraint_type, enabled, parameters) VALUES ('%s', 1, NULL)",
                            type.name()));
                }
            }

            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to seed database defaults", e);
        }
    }

    private String loadSchema() {
        try (InputStream stream = getClass().getResourceAsStream("/schema.sql")) {
            if (stream == null) {
                throw new IllegalStateException("schema.sql not found on classpath");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read schema.sql", e);
        }
    }
}
