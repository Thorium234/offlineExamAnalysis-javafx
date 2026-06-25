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
                null, "T001", "Jane Doe", true));

        assertNotNull(created.id());
        assertEquals("T001", created.code());
        assertEquals("Jane Doe", created.name());
        assertEquals(1, useCase.findAll().size());
    }

    @Test
    void updatesTeacher() {
        TeacherDto created = useCase.create(new TeacherDto(null, "T001", "Jane Doe", true));
        TeacherDto updated = useCase.update(new TeacherDto(
                created.id(), "T001", "Jane Smith", false));

        assertEquals("Jane Smith", updated.name());
        assertFalse(updated.active());
    }

    @Test
    void rejectsBlankCode() {
        assertThrows(IllegalArgumentException.class, () ->
                useCase.create(new TeacherDto(null, "  ", "Jane", true)));
    }

    @Test
    void deletesTeacher() {
        TeacherDto created = useCase.create(new TeacherDto(null, "T001", "Jane", true));
        useCase.delete(created.id());
        assertTrue(useCase.findAll().isEmpty());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        assertThrows(IllegalArgumentException.class, () -> useCase.findById(999L));
    }
}
