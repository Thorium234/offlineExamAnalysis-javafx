package com.thorium.domain.value;

import java.util.Arrays;
import java.util.List;

public enum DayOfWeek {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday");

    private final String displayName;

    DayOfWeek(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static List<DayOfWeek> workingDays() {
        return Arrays.asList(values());
    }

    public static DayOfWeek fromString(String value) {
        return valueOf(value.toUpperCase());
    }
}
