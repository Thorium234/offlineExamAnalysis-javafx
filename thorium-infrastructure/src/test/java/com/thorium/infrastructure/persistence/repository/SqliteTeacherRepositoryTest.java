package com.thorium.infrastructure.persistence.repository;

import com.thorium.domain.model.Teacher;
import com.thorium.infrastructure.persistence.SQLiteConnectionProvider;
import com.thorium.infrastructure.persistence.DatabaseInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SqliteTeacherRepositoryTest {

    @TempDir
    Path tempDir;

    private SqliteTeacherRepository repository;

    @BeforeEach
    void setUp() {
        Path dbPath = tempDir.resolve("test.db");
        SQLiteConnectionProvider provider = new SQLiteConnectionProvider(dbPath);
        new DatabaseInitializer(provider).initialize();
        repository = new SqliteTeacherRepository(provider);
    }

    @Test
    void savesAndFindsTeacher() {
        Teacher teacher = new Teacher(null, "T001", "Alice", true);
        Teacher saved = repository.save(teacher);

        assertNotNull(saved.getId());
        Teacher found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Alice", found.getName());
        assertEquals("T001", found.getCode());
    }

    @Test
    void updatesTeacher() {
        Teacher saved = repository.save(new Teacher(null, "T001", "Alice", true));
        saved.setName("Alice Updated");
        repository.save(saved);

        Teacher found = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Alice Updated", found.getName());
    }

    @Test
    void deletesTeacher() {
        Teacher saved = repository.save(new Teacher(null, "T001", "Alice", true));
        repository.deleteById(saved.getId());
        assertTrue(repository.findById(saved.getId()).isEmpty());
        assertEquals(0, repository.count());
    }

    @Test
    void findsByCode() {
        repository.save(new Teacher(null, "T001", "Alice", true));
        Teacher found = repository.findByCode("T001").orElseThrow();
        assertEquals("Alice", found.getName());
    }
}
