package com.thorium.domain.model;

import java.time.LocalTime;
import java.util.Objects;

public class Period {

    public static final String TYPE_LESSON = "LESSON";
    public static final String TYPE_BREAK = "BREAK";

    private Long id;
    private int periodNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private String label;
    private String type = TYPE_LESSON;
    private Long breakId;

    public Period() {
    }

    public Period(Long id, int periodNumber, LocalTime startTime, LocalTime endTime, String label) {
        this.id = id;
        this.periodNumber = periodNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.label = label;
        this.type = TYPE_LESSON;
    }

    public Period(Long id, int periodNumber, LocalTime startTime, LocalTime endTime, String label, String type, Long breakId) {
        this.id = id;
        this.periodNumber = periodNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.label = label;
        this.type = type;
        this.breakId = breakId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(int periodNumber) {
        this.periodNumber = periodNumber;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getBreakId() {
        return breakId;
    }

    public void setBreakId(Long breakId) {
        this.breakId = breakId;
    }

    public boolean isBreak() {
        return TYPE_BREAK.equals(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Period period)) {
            return false;
        }
        return Objects.equals(id, period.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
