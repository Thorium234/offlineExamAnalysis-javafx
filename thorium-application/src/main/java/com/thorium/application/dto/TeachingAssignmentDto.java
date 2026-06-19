package com.thorium.application.dto;

public record TeachingAssignmentDto(
        Long id,
        Long teacherId,
        String teacherName,
        Long subjectId,
        String subjectName,
        Long classStreamId,
        String classStreamName,
        int lessonsPerWeek
) {
}
