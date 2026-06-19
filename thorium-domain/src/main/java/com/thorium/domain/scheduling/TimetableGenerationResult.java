package com.thorium.domain.scheduling;

import com.thorium.domain.model.Subject;
import com.thorium.domain.model.TeachingAssignment;
import com.thorium.domain.value.DayOfWeek;

import java.util.ArrayList;
import java.util.List;

public final class TimetableGenerationResult {

    private final boolean success;
    private final PartialSchedule schedule;
    private final double qualityScore;
    private final List<String> errors;
    private final List<String> warnings;

    private TimetableGenerationResult(boolean success, PartialSchedule schedule, double qualityScore,
                                      List<String> errors, List<String> warnings) {
        this.success = success;
        this.schedule = schedule;
        this.qualityScore = qualityScore;
        this.errors = List.copyOf(errors);
        this.warnings = List.copyOf(warnings);
    }

    public static TimetableGenerationResult success(PartialSchedule schedule, double qualityScore) {
        return new TimetableGenerationResult(true, schedule, qualityScore, List.of(), List.of());
    }

    public static TimetableGenerationResult failure(List<String> errors) {
        return new TimetableGenerationResult(false, new PartialSchedule(), 0.0, errors, List.of());
    }

    public static TimetableGenerationResult partial(PartialSchedule schedule, double qualityScore, List<String> warnings) {
        return new TimetableGenerationResult(false, schedule, qualityScore, List.of(), warnings);
    }

    public boolean isSuccess() {
        return success;
    }

    public PartialSchedule schedule() {
        return schedule;
    }

    public double qualityScore() {
        return qualityScore;
    }

    public List<String> errors() {
        return errors;
    }

    public List<String> warnings() {
        return warnings;
    }

    public int requiredPlacements(SchedulingContext context) {
        return context.assignments().stream().mapToInt(TeachingAssignment::getLessonsPerWeek).sum();
    }

    public boolean isComplete(SchedulingContext context) {
        return schedule.size() == requiredPlacements(context);
    }

    public List<String> validateLessonCounts(SchedulingContext context) {
        List<String> violations = new ArrayList<>();
        for (TeachingAssignment assignment : context.assignments()) {
            long placed = schedule.placedLessons().stream()
                    .filter(p -> p.assignment().getId().equals(assignment.getId()))
                    .count();
            if (placed != assignment.getLessonsPerWeek()) {
                Subject subject = context.subject(assignment.getSubjectId()).orElse(null);
                String subjectName = subject != null ? subject.getName() : "Unknown";
                violations.add(String.format(
                        "Assignment %d (%s) requires %d lessons but has %d",
                        assignment.getId(), subjectName, assignment.getLessonsPerWeek(), placed));
            }
        }
        return violations;
    }
}
