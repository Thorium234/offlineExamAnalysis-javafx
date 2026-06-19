package com.thorium.domain.model;

import java.util.Objects;

public class Teacher {

    private Long id;
    private String code;
    private String name;
    private int maxLessonsPerDay;
    private int maxLessonsPerWeek;
    private boolean active;

    public Teacher() {
        this.maxLessonsPerDay = 6;
        this.maxLessonsPerWeek = 30;
        this.active = true;
    }

    public Teacher(Long id, String code, String name, int maxLessonsPerDay, int maxLessonsPerWeek, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.maxLessonsPerDay = maxLessonsPerDay;
        this.maxLessonsPerWeek = maxLessonsPerWeek;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxLessonsPerDay() {
        return maxLessonsPerDay;
    }

    public void setMaxLessonsPerDay(int maxLessonsPerDay) {
        this.maxLessonsPerDay = maxLessonsPerDay;
    }

    public int getMaxLessonsPerWeek() {
        return maxLessonsPerWeek;
    }

    public void setMaxLessonsPerWeek(int maxLessonsPerWeek) {
        this.maxLessonsPerWeek = maxLessonsPerWeek;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Teacher teacher)) {
            return false;
        }
        return Objects.equals(id, teacher.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
