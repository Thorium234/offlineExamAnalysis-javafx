package com.thorium.domain.scheduling;

import com.thorium.domain.model.BreakPeriod;
import com.thorium.domain.model.Period;
import com.thorium.domain.model.timeblock.BreakBlock;
import com.thorium.domain.model.timeblock.LessonBlock;
import com.thorium.domain.model.timeblock.TimeBlock;

import java.time.LocalTime;
import java.util.*;

public final class DailyTimelineGenerator {

    private DailyTimelineGenerator() {}

    public static List<TimeBlock> generate(List<Period> periods, List<BreakPeriod> breaks) {
        return generate(periods, breaks, null);
    }

    public static List<TimeBlock> generate(List<Period> periods, List<BreakPeriod> breaks,
                                            LocalTime schoolStartTimeOverride) {
        List<TimeBlock> timeline = new ArrayList<>();

        Map<Integer, Period> periodMap = new TreeMap<>();
        for (Period p : periods) {
            periodMap.put(p.getPeriodNumber(), p);
        }

        if (periodMap.isEmpty()) {
            return timeline;
        }

        int maxPeriod = periodMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);

        LocalTime cursor = schoolStartTimeOverride;
        boolean useCursor = (schoolStartTimeOverride != null);

        for (int pn = 1; pn <= maxPeriod; pn++) {
            if (!periodMap.containsKey(pn)) continue;

            Period period = periodMap.get(pn);

            if (period.isBreak()) {
                LocalTime start = useCursor ? cursor : period.getStartTime();
                LocalTime end = useCursor ? start.plusMinutes(
                        (int) java.time.Duration.between(period.getStartTime(), period.getEndTime()).toMinutes())
                        : period.getEndTime();
                timeline.add(new BreakBlock(period.getLabel(), start, end, 0));
                cursor = end;
            } else {
                LocalTime start = useCursor ? cursor : period.getStartTime();
                LocalTime end = useCursor ? start.plusMinutes(
                        (int) java.time.Duration.between(period.getStartTime(), period.getEndTime()).toMinutes())
                        : period.getEndTime();
                timeline.add(new LessonBlock(pn, start, end));
                cursor = end;
            }
        }

        return Collections.unmodifiableList(timeline);
    }
}
