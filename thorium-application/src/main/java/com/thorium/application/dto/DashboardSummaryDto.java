package com.thorium.application.dto;

public record DashboardSummaryDto(
        long teacherCount,
        long subjectCount,
        long classStreamCount,
        long assignmentCount,
        long totalLessonsPerWeek,
        long timetableCount,
        long roomCount,
        String latestTimetableName,
        long teachersOverloaded,
        long teachersNearCapacity,
        long teachersAvailable
) {
}
