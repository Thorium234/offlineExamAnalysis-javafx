package com.thorium.domain.scheduling;

import com.thorium.domain.constraint.HardConstraintValidator;
import com.thorium.domain.constraint.SoftConstraintScorer;
import com.thorium.domain.scheduling.optimization.HillClimbingStrategy;
import com.thorium.domain.scheduling.optimization.OptimizationStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class TimetableGenerator {

    private static final Logger LOG = Logger.getLogger(TimetableGenerator.class.getName());

    private final GreedyScheduler greedyScheduler;
    private final BacktrackingScheduler backtrackingScheduler;
    private final HardConstraintValidator hardValidator;
    private final SoftConstraintScorer softScorer;
    private final Optional<OptimizationStrategy> optimizationStrategy;

    public TimetableGenerator() {
        this(new HillClimbingStrategy());
    }

    public TimetableGenerator(OptimizationStrategy optimizationStrategy) {
        this.hardValidator = new HardConstraintValidator();
        this.softScorer = new SoftConstraintScorer();
        this.greedyScheduler = new GreedyScheduler(hardValidator, softScorer);
        this.backtrackingScheduler = new BacktrackingScheduler(hardValidator, softScorer);
        this.optimizationStrategy = Optional.ofNullable(optimizationStrategy);
    }

    public TimetableGenerationResult generate(SchedulingContext context) {
        LOG.info("Starting timetable generation with " + context.assignments().size() + " assignments");
        long start = System.currentTimeMillis();

        PartialSchedule greedyResult = greedyScheduler.schedule(context);
        int required = context.assignments().stream()
                .mapToInt(a -> a.getLessonsPerWeek()).sum();
        LOG.fine("Greedy scheduler placed " + greedyResult.size() + "/" + required + " lessons");

        TimetableGenerationResult result;
        if (greedyResult.size() >= required) {
            HardConstraintValidator.ValidationResult validation =
                    hardValidator.validateComplete(greedyResult, context);
            if (validation.isValid()) {
                double quality = softScorer.score(greedyResult, context);
                result = TimetableGenerationResult.success(greedyResult, quality);
                LOG.info("Greedy schedule succeeded (quality=" + String.format("%.3f", quality) + ")");
            } else {
                LOG.fine("Greedy validation failed, falling back to backtracking from scratch");
                result = backtrackingScheduler.resolve(context, new PartialSchedule());
            }
        } else {
            LOG.fine("Greedy incomplete (" + greedyResult.size() + "/" + required + "), resolving with backtracking");
            result = backtrackingScheduler.resolve(context, greedyResult);
        }

        if (result.isSuccess() && optimizationStrategy.isPresent()) {
            LOG.fine("Running optimization strategy");
            result = optimizationStrategy.get().optimize(result, context);
        }

        long elapsed = System.currentTimeMillis() - start;
        LOG.info("Generation finished in " + elapsed + "ms: success=" + result.isSuccess()
                + ", placed=" + result.schedule().size() + "/" + required
                + ", quality=" + String.format("%.3f", result.qualityScore()));
        return result;
    }

    public List<String> preflightChecks(SchedulingContext context) {
        List<String> issues = new ArrayList<>();
        int totalRequired = context.assignments().stream()
                .mapToInt(a -> a.getLessonsPerWeek()).sum();
        long classCount = countDistinctClasses(context);
        if (totalRequired > (long) context.totalSlots() * classCount) {
            issues.add("Total required lessons (" + totalRequired
                    + ") may exceed available class slots");
        }
        return issues;
    }

    private long countDistinctClasses(SchedulingContext context) {
        return context.assignments().stream()
                .map(a -> a.getClassStreamId())
                .distinct()
                .count();
    }
}
