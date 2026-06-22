package com.thorium.domain.scheduling;

import com.thorium.domain.constraint.HardConstraintValidator;
import com.thorium.domain.constraint.SoftConstraintScorer;
import com.thorium.domain.model.ScheduleSlot;
import com.thorium.domain.model.TeachingAssignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standard DFS backtracking scheduler.
 * Resolves timetabling conflicts by recursively searching slot assignments, sorting candidates
 * by soft constraints score, and backtracking on hard constraint violations.
 */
public class BacktrackingScheduler {

    private static final int MAX_ITERATIONS = 100_000;

    private final HardConstraintValidator hardValidator;
    private final SoftConstraintScorer softScorer;

    public BacktrackingScheduler(HardConstraintValidator hardValidator, SoftConstraintScorer softScorer) {
        this.hardValidator = hardValidator;
        this.softScorer = softScorer;
    }

    public TimetableGenerationResult resolve(SchedulingContext context, PartialSchedule initial) {
        Map<Long, Long> placedCounts = new HashMap<>();
        for (var lesson : initial.placedLessons()) {
            placedCounts.merge(lesson.assignment().getId(), 1L, Long::sum);
        }

        List<GreedyScheduler.AssignmentWorkItem> allItems = new GreedyScheduler(hardValidator, softScorer)
                .expandAssignments(context);
        List<GreedyScheduler.AssignmentWorkItem> workItems = new ArrayList<>();
        for (var item : allItems) {
            long placed = placedCounts.getOrDefault(item.assignment().getId(), 0L);
            if (item.lessonIndex() >= placed) {
                workItems.add(item);
            }
        }
        workItems.sort(Comparator.comparingInt(GreedyScheduler.AssignmentWorkItem::difficulty).reversed());

        PartialSchedule schedule = initial.copy();
        List<String> warnings = new ArrayList<>();
        int[] iterations = new int[1];

        boolean success = search(0, workItems, schedule, context, iterations);

        if (iterations[0] >= MAX_ITERATIONS) {
            warnings.add("Backtracking stopped after reaching safety limit of " + MAX_ITERATIONS + " search steps.");
        }

        double quality = softScorer.score(schedule, context);
        HardConstraintValidator.ValidationResult validation = hardValidator.validateComplete(schedule, context);

        if (!success) {
            return TimetableGenerationResult.failure(List.of("Backtracking solver failed to find a valid layout under hard constraints."));
        }

        if (!validation.isValid()) {
            return TimetableGenerationResult.failure(validation.errors());
        }

        TimetableGenerationResult result = TimetableGenerationResult.success(schedule, quality);
        if (!result.isComplete(context)) {
            List<String> countViolations = result.validateLessonCounts(context);
            return TimetableGenerationResult.partial(schedule, quality, merge(warnings, countViolations));
        }

        if (!warnings.isEmpty()) {
            return TimetableGenerationResult.partial(schedule, quality, warnings);
        }

        return result;
    }

    private boolean search(int index, List<GreedyScheduler.AssignmentWorkItem> workItems,
                           PartialSchedule schedule, SchedulingContext context, int[] iterations) {
        if (index >= workItems.size()) {
            return true;
        }

        iterations[0]++;
        if (iterations[0] >= MAX_ITERATIONS) {
            return false;
        }

        GreedyScheduler.AssignmentWorkItem item = workItems.get(index);
        List<ScheduleSlot> candidates = rankedSlots(item.assignment(), schedule, context);

        for (ScheduleSlot slot : candidates) {
            PlacedLesson placed = new PlacedLesson(item.assignment(), slot);
            schedule.place(placed);

            if (search(index + 1, workItems, schedule, context, iterations)) {
                return true;
            }

            schedule.removeLast(); // Backtrack
        }

        return false;
    }

    private List<ScheduleSlot> rankedSlots(TeachingAssignment assignment, PartialSchedule schedule,
                                           SchedulingContext context) {
        List<ScoredSlot> scored = new ArrayList<>();
        for (ScheduleSlot slot : context.allSlots()) {
            if (hardValidator.canPlace(assignment, slot, schedule, context)) {
                double score = softScorer.scorePlacement(assignment, slot, schedule, context);
                scored.add(new ScoredSlot(slot, score));
            }
        }
        scored.sort(Comparator.comparingDouble(ScoredSlot::score).reversed());
        return scored.stream().map(ScoredSlot::slot).toList();
    }

    private List<String> merge(List<String> a, List<String> b) {
        List<String> merged = new ArrayList<>(a);
        merged.addAll(b);
        return merged;
    }

    private record ScoredSlot(ScheduleSlot slot, double score) {
    }
}

