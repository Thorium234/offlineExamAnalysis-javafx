package com.thorium.application.dto;

import com.thorium.domain.value.DayOfWeek;

public record TimetableEntryDto(
        Long id,
        Long teachingAssignmentId,
        String teacherName,
        String subjectName,
        String classStreamName,
        DayOfWeek dayOfWeek,
        int periodNumber
) {
}
