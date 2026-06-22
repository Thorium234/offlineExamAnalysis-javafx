package com.thorium.application.usecase.subject;

import com.thorium.application.dto.SubjectDto;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.SubjectRepository;
import com.thorium.domain.model.Subject;
import com.thorium.domain.value.SubjectColorPalette;

import java.util.List;

public class SubjectManagementUseCase {

    private final SubjectRepository subjectRepository;

    public SubjectManagementUseCase(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public SubjectDto create(SubjectDto dto) {
        validate(dto);
        Subject subject = EntityMapper.toEntity(dto);
        if (subject.getColor() == null || subject.getColor().isBlank()) {
            subject.setColor(SubjectColorPalette.colorForSubject(
                    subject.getId() != null ? subject.getId() : System.nanoTime()));
        }
        if (subject.isExaminable() && subject.getCbcDefaultLessons() <= 0) {
            subject.setCbcDefaultLessons(5);
        }
        return EntityMapper.toDto(subjectRepository.save(subject));
    }

    public SubjectDto update(SubjectDto dto) {
        if (dto.id() == null) {
            throw new IllegalArgumentException("Subject id is required for update");
        }
        validate(dto);
        return EntityMapper.toDto(subjectRepository.save(EntityMapper.toEntity(dto)));
    }

    public void delete(Long id) {
        subjectRepository.deleteById(id);
    }

    public List<SubjectDto> findAll() {
        return subjectRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    private void validate(SubjectDto dto) {
        if (dto.code() == null || dto.code().isBlank()) {
            throw new IllegalArgumentException("Subject code is required");
        }
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Subject name is required");
        }
    }
}
