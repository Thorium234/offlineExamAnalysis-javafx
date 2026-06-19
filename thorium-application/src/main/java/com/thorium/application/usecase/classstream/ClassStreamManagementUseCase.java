package com.thorium.application.usecase.classstream;

import com.thorium.application.dto.ClassStreamDto;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.ClassStreamRepository;

import java.util.List;

public class ClassStreamManagementUseCase {

    private final ClassStreamRepository classStreamRepository;

    public ClassStreamManagementUseCase(ClassStreamRepository classStreamRepository) {
        this.classStreamRepository = classStreamRepository;
    }

    public ClassStreamDto create(ClassStreamDto dto) {
        validate(dto);
        return EntityMapper.toDto(classStreamRepository.save(EntityMapper.toEntity(dto)));
    }

    public ClassStreamDto update(ClassStreamDto dto) {
        if (dto.id() == null) {
            throw new IllegalArgumentException("Class id is required for update");
        }
        validate(dto);
        return EntityMapper.toDto(classStreamRepository.save(EntityMapper.toEntity(dto)));
    }

    public void delete(Long id) {
        classStreamRepository.deleteById(id);
    }

    public List<ClassStreamDto> findAll() {
        return classStreamRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    private void validate(ClassStreamDto dto) {
        if (dto.code() == null || dto.code().isBlank()) {
            throw new IllegalArgumentException("Class code is required");
        }
        if (dto.displayName() == null || dto.displayName().isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }
    }
}
