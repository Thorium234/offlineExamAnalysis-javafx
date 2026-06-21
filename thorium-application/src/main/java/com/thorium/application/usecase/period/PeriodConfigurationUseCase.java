package com.thorium.application.usecase.period;

import com.thorium.application.dto.PeriodDto;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.PeriodRepository;
import com.thorium.domain.model.Period;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PeriodConfigurationUseCase {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final PeriodRepository periodRepository;

    public PeriodConfigurationUseCase(PeriodRepository periodRepository) {
        this.periodRepository = periodRepository;
    }

    public PeriodDto create(PeriodDto dto) {
        Period period = toEntity(dto);
        return EntityMapper.toDto(periodRepository.save(period));
    }

    public PeriodDto update(PeriodDto dto) {
        if (dto.id() == null) {
            throw new IllegalArgumentException("Period id is required for update");
        }
        return EntityMapper.toDto(periodRepository.save(toEntity(dto)));
    }

    public void delete(Long id) {
        periodRepository.deleteById(id);
    }

    public List<PeriodDto> findAll() {
        return periodRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    public int periodsPerDay() {
        return periodRepository.count();
    }

    private Period toEntity(PeriodDto dto) {
        Period period = new Period();
        period.setId(dto.id());
        period.setPeriodNumber(dto.periodNumber());
        period.setStartTime(parseTime(dto.startTime(), "startTime"));
        period.setEndTime(parseTime(dto.endTime(), "endTime"));
        period.setLabel(dto.label());
        return period;
    }

    private LocalTime parseTime(String time, String fieldName) {
        try {
            return LocalTime.parse(time, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    fieldName + " must be in HH:mm format (e.g., 08:30), got: " + time);
        }
    }
}
