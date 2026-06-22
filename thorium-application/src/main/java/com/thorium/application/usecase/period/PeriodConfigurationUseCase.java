package com.thorium.application.usecase.period;

import com.thorium.application.dto.BreakDto;
import com.thorium.application.dto.PeriodDto;
import com.thorium.application.dto.SchoolSettingsDto;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.PeriodRepository;
import com.thorium.domain.model.Period;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
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

    public void recalculate(SchoolSettingsDto settings, List<BreakDto> breaks) {
        for (PeriodDto p : findAll()) {
            delete(p.id());
        }
        List<BreakDto> sorted = breaks.stream()
                .sorted(Comparator.comparingInt(BreakDto::afterPeriod)
                        .thenComparingInt(BreakDto::sortOrder))
                .toList();
        LocalTime cursor = parseTime(settings.startTime(), "startTime");
        for (int i = 1; i <= settings.totalPeriods(); i++) {
            for (BreakDto b : sorted) {
                if (b.afterPeriod() == i - 1) {
                    cursor = cursor.plusMinutes(b.durationMinutes());
                }
            }
            LocalTime start = cursor;
            LocalTime end = start.plusMinutes(settings.periodDurationMinutes());
            create(new PeriodDto(null, i, start.format(TIME_FORMAT), end.format(TIME_FORMAT), "P" + i));
            cursor = end;
        }
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
