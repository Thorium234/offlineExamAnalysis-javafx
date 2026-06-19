package com.thorium.domain.scheduling;

import com.thorium.domain.constraint.HardConstraintValidator;
import com.thorium.domain.constraint.SoftConstraintScorer;
import com.thorium.domain.model.ScheduleSlot;
import com.thorium.domain.model.TeachingAssignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BacktrackingScheduler {

    private static final int MAX_ITERATIONS = 100_000;

    private final HardConstraintValidator hardValidator;
    private final SoftConstraintScorer softScorer;

    public BacktrackingScheduler(HardConstraintValidator hardValidator, SoftConstraintScorer softScorer) {
        this.hardValidator = hardValidator;
        this.softScorer = softScorer;
    }

    public TimetableGenerationResult resolve(SchedulingContext context, PartialSchedule initial) {
        List<GreedyScheduler.AssignmentWorkItem> workItems = new GreedyScheduler(hardValidator, softScorer)
                .expandAssignments(context);
        workItems.sort(Comparator.comparingInt(GreedyScheduler.AssignmentWorkItem::difficulty).reversed());

        PartialSchedule schedule = initial.copy();
        List<String> warnings = new ArrayList<>();
        int iterations = 0;
        int index = schedule.size();

        while (index < workItems.size() && iterations < MAX_ITERATIONS) {
            iterations++;
            GreedyScheduler.AssignmentWorkItem item = workItems.get(index);
            List<ScheduleSlot> candidates = rankedSlots(item.assignment(), schedule, context);

            if (candidates.isEmpty()) {
                if (schedule.isEmpty()) {
                    return TimetableGenerationResult.failure(List.of(
                            "Unable to place assignment " + item.assignment().getId()
                                    + " — no valid slots and nothing to backtrack"));
                }
                schedule.removeLast();
                index--;
                continue;
            }

            schedule.place(new PlacedLesson(item.assignment(), candidates.getFirst()));
            index++;
        }

        if (index < workItems.size()) {
            warnings.add("Backtracking stopped after " + MAX_ITERATIONS + " iterations — schedule may be incomplete");
        }

        double quality = softScorer.score(schedule, context);
        HardConstraintValidator.ValidationResult validation = hardValidator.validateComplete(schedule, context);

        if (!validation.isValid()) {
            return TimetableGenerationResult.failure(validation.errors());
        }

        TimetableGenerationResult result = TimetableGenerationResult.success(schedule, quality);
        if (!result.isComplete(context)) {
            List<String> countViolations = result.validateLessonCounts(context);
            return TimetableGenerationResult.partial(schedule, quality,
                    merge(warnings, countViolations));
        }
        if (!warnings.isEmpty()) {
            return TimetableGenerationResult.partial(schedule, quality, warnings);
        }
        return result;
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
