package com.thorium.domain.scheduling;

import com.thorium.domain.model.ScheduleSlot;
import com.thorium.domain.model.TeachingAssignment;

public record PlacedLesson(
        TeachingAssignment assignment,
        ScheduleSlot slot
) {
}
