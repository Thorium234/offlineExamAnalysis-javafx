package com.thorium.domain.model;

import java.time.LocalTime;
import java.util.Objects;

public class Period {

    private Long id;
    private int periodNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private String label;

    public Period() {
    }

    public Period(Long id, int periodNumber, LocalTime startTime, LocalTime endTime, String label) {
        this.id = id;
        this.periodNumber = periodNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.label = label;
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
