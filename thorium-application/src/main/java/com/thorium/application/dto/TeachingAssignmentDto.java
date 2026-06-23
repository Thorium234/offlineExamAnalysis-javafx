package com.thorium.application.dto;

import com.thorium.domain.model.LessonDuration;

public record TeachingAssignmentDto(
        Long id,
        Long teacherId,
        String teacherName,
        Long subjectId,
        String subjectName,
        Long classStreamId,
        String classStreamName,
        int lessonsPerWeek,
        LessonDuration duration
) {
    public TeachingAssignmentDto {
        if (duration == null) duration = LessonDuration.SINGLE;
    }
}
