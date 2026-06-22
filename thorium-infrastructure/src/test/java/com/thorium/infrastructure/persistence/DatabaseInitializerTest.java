package com.thorium.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerTest {

    @TempDir
    Path tempDir;

    @Test
    void createsSchemaAndSeedsDefaults() throws Exception {
        Path dbPath = tempDir.resolve("init.db");
        SQLiteConnectionProvider provider = new SQLiteConnectionProvider(dbPath);
        new DatabaseInitializer(provider).initialize();

        try (Connection conn = provider.getConnection()) {
            assertTableExists(conn, "teachers");
            assertTableExists(conn, "periods");
            assertTableExists(conn, "constraints");

            try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM periods")) {
                assertTrue(rs.next());
                assertEquals(15, rs.getInt(1), "Default 15 periods should be seeded");
            }

            try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM constraints")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 1, "Default constraints should be seeded");
            }
        }
    }

    private void assertTableExists(Connection conn, String table) throws Exception {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, table, null)) {
            assertTrue(rs.next(), "Table " + table + " should exist");
        }
    }
}
