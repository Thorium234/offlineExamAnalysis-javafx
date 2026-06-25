package com.thorium.application.usecase.teacher;

import com.thorium.application.dto.TeacherDto;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.TeacherRepository;
import com.thorium.domain.model.Teacher;

import java.util.List;

public class TeacherManagementUseCase {

    private final TeacherRepository teacherRepository;

    public TeacherManagementUseCase(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public TeacherDto create(TeacherDto dto) {
        validate(dto);
        Teacher saved = teacherRepository.save(EntityMapper.toEntity(dto));
        return EntityMapper.toDto(saved);
    }

    public TeacherDto update(TeacherDto dto) {
        if (dto.id() == null) {
            throw new IllegalArgumentException("Teacher id is required for update");
        }
        validate(dto);
        Teacher saved = teacherRepository.save(EntityMapper.toEntity(dto));
        return EntityMapper.toDto(saved);
    }

    public void delete(Long id) {
        teacherRepository.deleteById(id);
    }

    public List<TeacherDto> findAll() {
        return teacherRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    public TeacherDto findById(Long id) {
        return teacherRepository.findById(id)
                .map(EntityMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + id));
    }

    private void validate(TeacherDto dto) {
        if (dto.code() == null || dto.code().isBlank()) {
            throw new IllegalArgumentException("Teacher code is required");
        }
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Teacher name is required");
        }
    }
}
