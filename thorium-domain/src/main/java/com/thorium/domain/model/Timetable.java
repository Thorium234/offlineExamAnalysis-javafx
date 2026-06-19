package com.thorium.domain.model;

import com.thorium.domain.value.TimetableStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class Timetable {

    private Long id;
    private String name;
    private TimetableStatus status;
    private LocalDateTime createdAt;
    private double qualityScore;

    public Timetable() {
        this.status = TimetableStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public Timetable(Long id, String name, TimetableStatus status, LocalDateTime createdAt, double qualityScore) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
        this.qualityScore = qualityScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimetableStatus getStatus() {
        return status;
    }

    public void setStatus(TimetableStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(double qualityScore) {
        this.qualityScore = qualityScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Timetable timetable)) {
            return false;
        }
        return Objects.equals(id, timetable.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
