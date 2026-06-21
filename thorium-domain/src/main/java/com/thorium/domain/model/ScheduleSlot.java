package com.thorium.domain.model;

import com.thorium.domain.value.DayOfWeek;

import java.util.Objects;

public final class ScheduleSlot {

    private final DayOfWeek dayOfWeek;
    private final int periodNumber;

    public ScheduleSlot(DayOfWeek dayOfWeek, int periodNumber) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("dayOfWeek is required");
        }
        if (periodNumber < 1) {
            throw new IllegalArgumentException("periodNumber must be positive");
        }
        this.dayOfWeek = dayOfWeek;
        this.periodNumber = periodNumber;
    }

    public DayOfWeek dayOfWeek() {
        return dayOfWeek;
    }

    public int periodNumber() {
        return periodNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScheduleSlot that)) {
            return false;
        }
        return periodNumber == that.periodNumber && dayOfWeek == that.dayOfWeek;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, periodNumber);
    }

    @Override
    public String toString() {
        return dayOfWeek + " P" + periodNumber;
    }
}
