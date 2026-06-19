package com.thorium.application.usecase.breaks;

import com.thorium.application.dto.BreakDto;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.BreakRepository;
import com.thorium.domain.model.BreakPeriod;

import java.util.List;

public class BreakConfigurationUseCase {

    private final BreakRepository breakRepository;

    public BreakConfigurationUseCase(BreakRepository breakRepository) {
        this.breakRepository = breakRepository;
    }

    public BreakDto create(BreakDto dto) {
        validate(dto);
        return EntityMapper.toDto(breakRepository.save(toEntity(dto)));
    }

    public BreakDto update(BreakDto dto) {
        if (dto.id() == null) {
            throw new IllegalArgumentException("Break id is required for update");
        }
        validate(dto);
        return EntityMapper.toDto(breakRepository.save(toEntity(dto)));
    }

    public void delete(Long id) {
        breakRepository.deleteById(id);
    }

    public List<BreakDto> findAll() {
        return breakRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    private BreakPeriod toEntity(BreakDto dto) {
        BreakPeriod breakPeriod = new BreakPeriod();
        breakPeriod.setId(dto.id());
        breakPeriod.setName(dto.name());
        breakPeriod.setAfterPeriod(dto.afterPeriod());
        breakPeriod.setDurationMinutes(dto.durationMinutes());
        breakPeriod.setSortOrder(dto.sortOrder());
        return breakPeriod;
    }

    private void validate(BreakDto dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Break name is required");
        }
        if (dto.durationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
    }
}
