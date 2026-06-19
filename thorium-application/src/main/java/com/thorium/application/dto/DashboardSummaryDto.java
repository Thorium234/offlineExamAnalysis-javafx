package com.thorium.application.dto;

public record DashboardSummaryDto(
        long teacherCount,
        long subjectCount,
        long classStreamCount,
        long assignmentCount,
        long timetableCount,
        String latestTimetableName
) {
}
