package com.thorium.application.dto;

import com.thorium.domain.value.TimetableStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TimetableDto(
        Long id,
        String name,
        TimetableStatus status,
        LocalDateTime createdAt,
        double qualityScore,
        List<TimetableEntryDto> entries
) {
}
