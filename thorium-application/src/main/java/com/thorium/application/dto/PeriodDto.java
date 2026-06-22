package com.thorium.application.dto;

public record PeriodDto(
        Long id,
        int periodNumber,
        String startTime,
        String endTime,
        String label,
        String type,
        Long breakId
) {
}
