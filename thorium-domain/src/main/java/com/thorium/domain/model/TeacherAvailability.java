package com.thorium.domain.model;

import com.thorium.domain.value.DayOfWeek;

import java.util.Objects;

public class TeacherAvailability {

    private Long id;
    private Long teacherId;
    private DayOfWeek dayOfWeek;
    private int periodNumber;
    private boolean available;

    public TeacherAvailability() {
        this.available = true;
    }

    public TeacherAvailability(Long id, Long teacherId, DayOfWeek dayOfWeek, int periodNumber, boolean available) {
        this.id = id;
        this.teacherId = teacherId;
        this.dayOfWeek = dayOfWeek;
        this.periodNumber = periodNumber;
        this.available = available;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(int periodNumber) {
        this.periodNumber = periodNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public ScheduleSlot toSlot() {
        return new ScheduleSlot(dayOfWeek, periodNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeacherAvailability that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
