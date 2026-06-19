package com.thorium.application.dto;

import com.thorium.domain.value.DayOfWeek;

public record TeacherAvailabilityDto(
        Long id,
        Long teacherId,
        String teacherName,
        DayOfWeek dayOfWeek,
        int periodNumber,
        boolean available
) {
}
