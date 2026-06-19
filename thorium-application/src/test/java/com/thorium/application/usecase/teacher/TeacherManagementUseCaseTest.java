package com.thorium.application.usecase.teacher;

import com.thorium.application.dto.TeacherDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeacherManagementUseCaseTest {

    private TeacherManagementUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TeacherManagementUseCase(new InMemoryTeacherRepository());
    }

    @Test
    void createsTeacher() {
        TeacherDto created = useCase.create(new TeacherDto(
                null, "T001", "Jane Doe", 6, 30, true));

        assertNotNull(created.id());
        assertEquals("T001", created.code());
        assertEquals("Jane Doe", created.name());
        assertEquals(1, useCase.findAll().size());
    }

    @Test
    void updatesTeacher() {
        TeacherDto created = useCase.create(new TeacherDto(null, "T001", "Jane Doe", 6, 30, true));
        TeacherDto updated = useCase.update(new TeacherDto(
                created.id(), "T001", "Jane Smith", 5, 25, false));

        assertEquals("Jane Smith", updated.name());
        assertEquals(5, updated.maxLessonsPerDay());
        assertFalse(updated.active());
    }

    @Test
    void rejectsBlankCode() {
        assertThrows(IllegalArgumentException.class, () ->
                useCase.create(new TeacherDto(null, "  ", "Jane", 6, 30, true)));
    }

    @Test
    void rejectsNonPositiveLimits() {
        assertThrows(IllegalArgumentException.class, () ->
                useCase.create(new TeacherDto(null, "T002", "Jane", 0, 30, true)));
    }

    @Test
    void deletesTeacher() {
        TeacherDto created = useCase.create(new TeacherDto(null, "T001", "Jane", 6, 30, true));
        useCase.delete(created.id());
        assertTrue(useCase.findAll().isEmpty());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        assertThrows(IllegalArgumentException.class, () -> useCase.findById(999L));
    }
}
