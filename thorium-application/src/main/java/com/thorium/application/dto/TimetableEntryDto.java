package com.thorium.application.dto;

import com.thorium.domain.value.DayOfWeek;

public record TimetableEntryDto(
        Long id,
        Long teachingAssignmentId,
        String teacherName,
        String teacherInitials,
        String subjectName,
        String subjectCode,
        String subjectColor,
        String classStreamName,
        Long classStreamId,
        String roomCode,
        Long roomId,
        DayOfWeek dayOfWeek,
        int periodNumber
) {
}
