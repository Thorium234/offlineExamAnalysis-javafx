package com.thorium.domain.scheduling;

import com.thorium.domain.constraint.HardConstraintValidator;
import com.thorium.domain.constraint.SoftConstraintScorer;
import com.thorium.domain.model.LessonDuration;
import com.thorium.domain.model.ScheduleSlot;
import com.thorium.domain.model.TeachingAssignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class GreedyScheduler {

    private static final Logger LOG = Logger.getLogger(GreedyScheduler.class.getName());

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

        int rejectedCount = 0;
        for (int i = 0; i < workItems.size(); i++) {
            AssignmentWorkItem item = workItems.get(i);
            ScheduleSlot bestSlot = findBestSlot(item.assignment(), item.requiresConsecutive, schedule, context);
            if (bestSlot != null) {
                schedule.place(new PlacedLesson(item.assignment(), bestSlot));
                if (item.requiresConsecutive) {
                    ScheduleSlot second = new ScheduleSlot(bestSlot.dayOfWeek(), bestSlot.periodNumber() + 1);
                    schedule.place(new PlacedLesson(item.assignment(), second));
                }
            } else {
                rejectedCount++;
                if (rejectedCount == 1) {
                    LOG.warning("No slot found for first item (teacher=" + item.assignment().getTeacherId()
                            + ", subject=" + item.assignment().getSubjectId()
                            + ", class=" + item.assignment().getClassStreamId()
                            + ", lessons=" + item.assignment().getLessonsPerWeek()
                            + ") difficulty=" + item.difficulty);
                    logSlotDiagnostics(item.assignment(), context);
                } else if (rejectedCount <= 5 || i == workItems.size() - 1) {
                    LOG.warning("No slot for assignment " + item.assignment().getId()
                            + " (teacher=" + item.assignment().getTeacherId()
                            + ", subject=" + item.assignment().getSubjectId()
                            + ", class=" + item.assignment().getClassStreamId()
                            + ") after " + schedule.size() + " placed");
                }
            }
        }
        if (rejectedCount > 0) {
            LOG.warning("Greedy scheduler: " + rejectedCount + "/" + workItems.size()
                    + " items could not be placed (" + schedule.size() + " placed)");
        }
        return schedule;
    }

    private void logSlotDiagnostics(TeachingAssignment assignment, SchedulingContext context) {
        LOG.warning("Diagnosing all " + context.allSlots().size() + " slots for assignment " + assignment.getId());
        int rejected = 0;
        for (ScheduleSlot slot : context.allSlots()) {
            String reason = hardValidator.canPlaceReason(assignment, slot, new PartialSchedule(), context);
            if (reason != null) {
                rejected++;
                if (rejected <= 5) {
                    LOG.warning("  Slot " + slot + " rejected: " + reason);
                }
            }
        }
        LOG.warning("Diagnostic: " + rejected + "/" + context.allSlots().size() + " slots rejected for first item");
    }

    public List<AssignmentWorkItem> expandAssignments(SchedulingContext context) {
        List<AssignmentWorkItem> items = new ArrayList<>();
        for (TeachingAssignment assignment : context.assignments()) {
            int difficulty = computeDifficulty(assignment, context);
            boolean isDouble = assignment.getDuration() == LessonDuration.DOUBLE;
            int count = isDouble ? assignment.getLessonsPerWeek() / 2 : assignment.getLessonsPerWeek();
            for (int i = 0; i < count; i++) {
                items.add(new AssignmentWorkItem(assignment, difficulty, i, isDouble));
            }
        }
        return items;
    }

    private int computeDifficulty(TeachingAssignment assignment, SchedulingContext context) {
        int difficulty = assignment.getLessonsPerWeek() * 10;
        long unavailableCount = 0;
        long totalSlots = 0;
        boolean isDouble = assignment.getDuration() == LessonDuration.DOUBLE;
        for (var day : context.workingDays()) {
            int maxPeriod = isDouble ? context.periodsPerDay() - 1 : context.periodsPerDay();
            for (int p = 1; p <= maxPeriod; p++) {
                totalSlots++;
                if (context.isTeacherUnavailable(assignment.getTeacherId(), new ScheduleSlot(day, p))) {
                    unavailableCount++;
                }
                if (isDouble && context.isTeacherUnavailable(assignment.getTeacherId(), new ScheduleSlot(day, p + 1))) {
                    unavailableCount++;
                }
            }
        }
        if (totalSlots > 0) {
            difficulty += (int) ((unavailableCount * 100) / totalSlots);
        }
        return difficulty;
    }

    private ScheduleSlot findBestSlot(TeachingAssignment assignment, boolean requiresConsecutive,
                                       PartialSchedule schedule, SchedulingContext context) {
        ScheduleSlot best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        int maxPeriod = requiresConsecutive ? context.periodsPerDay() - 1 : context.periodsPerDay();
        for (ScheduleSlot slot : context.allSlots()) {
            if (slot.periodNumber() > maxPeriod) continue;
            if (!hardValidator.canPlace(assignment, slot, schedule, context)) {
                continue;
            }
            if (requiresConsecutive) {
                ScheduleSlot next = new ScheduleSlot(slot.dayOfWeek(), slot.periodNumber() + 1);
                if (!hardValidator.canPlace(assignment, next, schedule, context)) {
                    continue;
                }
            }
            double score = softScorer.scorePlacement(assignment, slot, schedule, context);
            if (score > bestScore) {
                bestScore = score;
                best = slot;
            }
        }
        return best;
    }

    public record AssignmentWorkItem(TeachingAssignment assignment, int difficulty, int lessonIndex, boolean requiresConsecutive) {
        public AssignmentWorkItem(TeachingAssignment assignment, int difficulty, int lessonIndex) {
            this(assignment, difficulty, lessonIndex, false);
        }
    }
}
