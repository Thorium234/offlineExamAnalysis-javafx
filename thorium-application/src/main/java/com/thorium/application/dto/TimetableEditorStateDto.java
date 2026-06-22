package com.thorium.application.dto;

import java.util.List;

public record TimetableEditorStateDto(
        Long timetableId,
        String timetableName,
        Long classStreamId,
        List<PeriodDto> periods,
        List<LessonCardDto> gridLessons,
        List<LessonCardDto> unassignedLessons,
        EntityTreeDto entityTree
) {
}
