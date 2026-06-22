package com.thorium.application.dto;

import com.thorium.domain.value.DayOfWeek;

public record LessonCardDto(
        Long entryId,
        Long teachingAssignmentId,
        String teacherInitials,
        String teacherName,
        String subjectCode,
        String subjectName,
        String subjectColor,
        String classStreamName,
        Long classStreamId,
        String roomCode,
        Long roomId,
        DayOfWeek dayOfWeek,
        int periodNumber,
        boolean unassigned,
        boolean roomRemoved
) {
    public static LessonCardDto unassigned(Long teachingAssignmentId, String teacherInitials, String teacherName,
                                           String subjectCode, String subjectName, String subjectColor,
                                           String classStreamName, Long classStreamId, String roomCode, Long roomId) {
        return new LessonCardDto(
                null, teachingAssignmentId, teacherInitials, teacherName,
                subjectCode, subjectName, subjectColor, classStreamName, classStreamId,
                roomCode, roomId, null, 0, true, false
        );
    }
}
