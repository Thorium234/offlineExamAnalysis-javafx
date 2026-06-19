package com.thorium.domain.scheduling.optimization;

import com.thorium.domain.scheduling.SchedulingContext;
import com.thorium.domain.scheduling.TimetableGenerationResult;

/**
 * Extension point for Phase 3 optimization algorithms.
 * Implementations: Genetic Algorithm, Tabu Search, Simulated Annealing.
 */
public interface OptimizationStrategy {

    TimetableGenerationResult optimize(TimetableGenerationResult initial, SchedulingContext context);
}
