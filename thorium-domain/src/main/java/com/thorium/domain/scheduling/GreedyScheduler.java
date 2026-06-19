package com.thorium.domain.scheduling;

import com.thorium.domain.constraint.HardConstraintValidator;
import com.thorium.domain.constraint.SoftConstraintScorer;
import com.thorium.domain.model.ScheduleSlot;
import com.thorium.domain.model.TeachingAssignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyScheduler {

    private final HardConstraintValidator hardValidator;
    private final SoftConstraintScorer softScorer;

    public GreedyScheduler(HardConstraintValidator hardValidator, SoftConstraintScorer softScorer) {
        this.hardValidator = hardValidator;
        this.softScorer = softScorer;
    }

    public PartialSchedule schedule(SchedulingContext context) {
        PartialSchedule schedule = new PartialSchedule();
        List<AssignmentWorkItem> workItems = expandAssignments(context);
        workItems.sort(Comparator.comparingInt(AssignmentWorkItem::difficulty).reversed());

        for (AssignmentWorkItem item : workItems) {
            ScheduleSlot bestSlot = findBestSlot(item.assignment(), schedule, context);
            if (bestSlot != null) {
                schedule.place(new PlacedLesson(item.assignment(), bestSlot));
            }
        }
        return schedule;
    }

    public List<AssignmentWorkItem> expandAssignments(SchedulingContext context) {
        List<AssignmentWorkItem> items = new ArrayList<>();
        for (TeachingAssignment assignment : context.assignments()) {
            int difficulty = computeDifficulty(assignment, context);
            for (int i = 0; i < assignment.getLessonsPerWeek(); i++) {
                items.add(new AssignmentWorkItem(assignment, difficulty, i));
            }
        }
        return items;
    }

    private int computeDifficulty(TeachingAssignment assignment, SchedulingContext context) {
        int difficulty = assignment.getLessonsPerWeek() * 10;
        long unavailableCount = context.isTeacherUnavailable(assignment.getTeacherId(), new ScheduleSlot(
                context.workingDays().getFirst(), 1)) ? 1 : 0;
        difficulty += (int) unavailableCount * 5;
        return difficulty;
    }

    private ScheduleSlot findBestSlot(TeachingAssignment assignment, PartialSchedule schedule, SchedulingContext context) {
        ScheduleSlot best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (ScheduleSlot slot : context.allSlots()) {
            if (!hardValidator.canPlace(assignment, slot, schedule, context)) {
                continue;
            }
            double score = softScorer.scorePlacement(assignment, slot, schedule, context);
            if (score > bestScore) {
                bestScore = score;
                best = slot;
            }
        }
        return best;
    }

    public record AssignmentWorkItem(TeachingAssignment assignment, int difficulty, int lessonIndex) {
    }
}
