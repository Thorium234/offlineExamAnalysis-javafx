package com.thorium.infrastructure.persistence;

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
    private static final int SCHEMA_VERSION = 10;

    private final SQLiteConnectionProvider connectionProvider;

    public DatabaseInitializer(SQLiteConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void initialize() {
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {
            createSchemaVersionTable(statement);
            int currentVersion = getCurrentVersion(connection);

            if (currentVersion < 1) {
                for (String sql : loadSchema().split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        statement.execute(trimmed);
                    }
                }
                setVersion(statement, 1);
            }
            if (currentVersion < 2) {
                runMigrationV2(statement);
                setVersion(statement, 2);
            }
            if (currentVersion < 3) {
                runMigrationV3(statement);
                setVersion(statement, 3);
            }
            if (currentVersion < 4) {
                runMigrationV4(statement);
                setVersion(statement, 4);
            }
            if (currentVersion < 5) {
                runMigrationV5(statement);
                setVersion(statement, 5);
            }
            if (currentVersion < 6) {
                runMigrationV6(statement);
                setVersion(statement, 6);
            }
            if (currentVersion < 7) {
                runMigrationV7(statement);
                setVersion(statement, 7);
            }
            if (currentVersion < 8) {
                runMigrationV8(statement);
                setVersion(statement, 8);
            }
            if (currentVersion < 9) {
                runMigrationV9(statement);
                setVersion(statement, 9);
            }
            if (currentVersion < 10) {
                runMigrationV10(statement);
                setVersion(statement, 10);
            }

            connection.commit();
            seedDefaults();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private void createSchemaVersionTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS schema_version (version INTEGER NOT NULL)");
    }

    private int getCurrentVersion(Connection connection) throws SQLException {
        try (var rs = connection.createStatement().executeQuery("SELECT MAX(version) FROM schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void setVersion(Statement statement, int version) throws SQLException {
        statement.execute("INSERT INTO schema_version (version) VALUES (" + version + ")");
    }

    private void runMigrationV2(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE subjects ADD COLUMN requires_double_period INTEGER NOT NULL DEFAULT 0 CHECK (requires_double_period IN (0, 1))");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) {
                throw e;
            }
        }
    }

    private void runMigrationV3(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    code     TEXT    NOT NULL UNIQUE,
                    name     TEXT    NOT NULL,
                    type     TEXT    NOT NULL DEFAULT 'CLASSROOM' CHECK (type IN ('CLASSROOM', 'LAB')),
                    capacity INTEGER NOT NULL DEFAULT 30 CHECK (capacity > 0)
                )
                """);
        try {
            statement.execute("ALTER TABLE timetable_entries ADD COLUMN room_id INTEGER REFERENCES rooms(id) ON DELETE SET NULL");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) {
                throw e;
            }
        }
    }

    private void runMigrationV4(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE subjects ADD COLUMN color TEXT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) {
                throw e;
            }
        }
    }

    private void runMigrationV5(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS school_settings (
                    id                  INTEGER PRIMARY KEY CHECK (id = 1),
                    total_periods       INTEGER NOT NULL DEFAULT 8,
                    school_start_time   TEXT    NOT NULL DEFAULT '08:00',
                    school_end_time     TEXT    NOT NULL DEFAULT '16:00',
                    period_duration_min INTEGER NOT NULL DEFAULT 40
                )
                """);
    }

    private void runMigrationV6(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE school_settings ADD COLUMN school_end_time TEXT NOT NULL DEFAULT '16:00'");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
    }

    private void runMigrationV7(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE breaks ADD COLUMN start_time TEXT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
        try {
            statement.execute("ALTER TABLE breaks ADD COLUMN end_time TEXT");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
    }

    private void runMigrationV8(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE breaks ADD COLUMN is_before_period_one INTEGER NOT NULL DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
    }

    private void runMigrationV9(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE breaks ADD COLUMN slotable INTEGER NOT NULL DEFAULT 0");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
        try {
            statement.execute("ALTER TABLE timetable_entries ADD COLUMN slot_type TEXT NOT NULL DEFAULT 'PERIOD'");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
        try {
            statement.execute("ALTER TABLE timetable_entries ADD COLUMN break_id INTEGER REFERENCES breaks(id) ON DELETE SET NULL");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
        statement.execute("UPDATE breaks SET slotable = 1 WHERE name = 'Assembly'");
        statement.execute("""
                CREATE TABLE timetable_entries_v2 (
                    id                     INTEGER PRIMARY KEY AUTOINCREMENT,
                    timetable_id           INTEGER NOT NULL REFERENCES timetables(id) ON DELETE CASCADE,
                    teaching_assignment_id INTEGER NOT NULL REFERENCES teaching_assignments(id) ON DELETE CASCADE,
                    day_of_week            TEXT    NOT NULL,
                    period_number          INTEGER NOT NULL CHECK (period_number > 0),
                    room_id                INTEGER REFERENCES rooms(id) ON DELETE SET NULL,
                    slot_type              TEXT    NOT NULL DEFAULT 'PERIOD',
                    break_id               INTEGER REFERENCES breaks(id) ON DELETE SET NULL,
                    UNIQUE (timetable_id, teaching_assignment_id, day_of_week, period_number)
                )
                """);
        statement.execute("INSERT INTO timetable_entries_v2 (timetable_id, teaching_assignment_id, day_of_week, period_number, room_id, slot_type) SELECT timetable_id, teaching_assignment_id, day_of_week, period_number + 1, room_id, 'PERIOD' FROM timetable_entries");
        statement.execute("DROP TABLE timetable_entries");
        statement.execute("ALTER TABLE timetable_entries_v2 RENAME TO timetable_entries");
        statement.execute("CREATE INDEX IF NOT EXISTS idx_tt_entries_timetable ON timetable_entries(timetable_id)");
        statement.execute("CREATE INDEX IF NOT EXISTS idx_tt_entries_slot ON timetable_entries(timetable_id, day_of_week, period_number)");
        statement.execute("""
                CREATE TABLE teacher_availability_v2 (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    teacher_id    INTEGER NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
                    day_of_week   TEXT    NOT NULL,
                    period_number INTEGER NOT NULL CHECK (period_number > 0),
                    available     INTEGER NOT NULL DEFAULT 1 CHECK (available IN (0, 1)),
                    UNIQUE (teacher_id, day_of_week, period_number)
                )
                """);
        statement.execute("INSERT INTO teacher_availability_v2 (teacher_id, day_of_week, period_number, available) SELECT teacher_id, day_of_week, period_number + 1, available FROM teacher_availability");
        statement.execute("DROP TABLE teacher_availability");
        statement.execute("ALTER TABLE teacher_availability_v2 RENAME TO teacher_availability");
    }

    private void runMigrationV10(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE periods ADD COLUMN type TEXT NOT NULL DEFAULT 'LESSON' CHECK (type IN ('LESSON', 'BREAK'))");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
        try {
            statement.execute("ALTER TABLE periods ADD COLUMN break_id INTEGER REFERENCES breaks(id) ON DELETE SET NULL");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) throw e;
        }
    }

    private void seedDefaults() {
        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {

            var settingsCount = connection.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM school_settings");
            if (settingsCount.next() && settingsCount.getInt(1) == 0) {
                statement.execute("INSERT INTO school_settings (id, total_periods, school_start_time, school_end_time, period_duration_min) VALUES (1, 15, '07:00', '18:25', 40)");
            }

            var breakCount = connection.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM breaks");
            if (breakCount.next() && breakCount.getInt(1) == 0) {
                statement.execute("INSERT INTO breaks (name, after_period, duration_minutes, sort_order, is_before_period_one, slotable, start_time, end_time) VALUES ('Assembly', 0, 50, 1, 1, 1, '07:00', '07:50')");
                statement.execute("INSERT INTO breaks (name, after_period, duration_minutes, sort_order, is_before_period_one, slotable, start_time, end_time) VALUES ('Tea Break', 3, 20, 2, 0, 0, '09:50', '10:10')");
                statement.execute("INSERT INTO breaks (name, after_period, duration_minutes, sort_order, is_before_period_one, slotable, start_time, end_time) VALUES ('Short Break', 5, 10, 3, 0, 0, '11:20', '11:30')");
                statement.execute("INSERT INTO breaks (name, after_period, duration_minutes, sort_order, is_before_period_one, slotable, start_time, end_time) VALUES ('Lunch Break', 7, 50, 4, 0, 0, '12:50', '13:40')");
                statement.execute("INSERT INTO breaks (name, after_period, duration_minutes, sort_order, is_before_period_one, slotable, start_time, end_time) VALUES ('Games Time', 10, 165, 5, 0, 0, '16:00', '18:45')");
            }

            var periodCount = connection.createStatement()
                    .executeQuery("SELECT COUNT(*) FROM periods");
            if (periodCount.next() && periodCount.getInt(1) == 0) {
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (1, '07:00', '07:50', 'Assembly', 'BREAK', 1)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (2, '07:50', '08:30', 'P1', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (3, '08:30', '09:10', 'P2', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (4, '09:10', '09:50', 'P3', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (5, '09:50', '10:10', 'Tea Break', 'BREAK', 2)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (6, '10:10', '10:50', 'P4', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (7, '10:50', '11:20', 'P5', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (8, '11:20', '11:30', 'Short Break', 'BREAK', 3)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (9, '11:30', '12:10', 'P6', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (10, '12:10', '12:50', 'P7', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (11, '12:50', '13:40', 'Lunch Break', 'BREAK', 4)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (12, '13:40', '14:20', 'P8', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (13, '14:20', '15:00', 'P9', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (14, '15:00', '15:40', 'P10', 'LESSON', NULL)");
                statement.execute("INSERT INTO periods (period_number, start_time, end_time, label, type, break_id) VALUES (15, '15:40', '18:25', 'Games Time', 'BREAK', 5)"); // 165 min
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
