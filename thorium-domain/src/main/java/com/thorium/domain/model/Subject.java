package com.thorium.domain.model;

import java.util.Objects;

public class Subject {

    private Long id;
    private String code;
    private String name;
    private boolean examinable;
    private boolean cbcSubject;
    private int cbcDefaultLessons;
    private boolean allowsDoublePeriod;
    private boolean requiresDoublePeriod;
    private String color;

    public Subject() {
        this.cbcSubject = true;
        this.cbcDefaultLessons = 5;
    }

    public Subject(Long id, String code, String name, boolean examinable, int cbcDefaultLessons,
                   boolean allowsDoublePeriod, boolean requiresDoublePeriod, String color) {
        this(id, code, name, examinable, examinable, cbcDefaultLessons, allowsDoublePeriod, requiresDoublePeriod, color);
    }

    public Subject(Long id, String code, String name, boolean examinable, boolean cbcSubject, int cbcDefaultLessons,
                   boolean allowsDoublePeriod, boolean requiresDoublePeriod, String color) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.examinable = examinable;
        this.cbcSubject = cbcSubject;
        this.cbcDefaultLessons = cbcDefaultLessons;
        this.allowsDoublePeriod = allowsDoublePeriod;
        this.requiresDoublePeriod = requiresDoublePeriod;
        this.color = color;
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

    public boolean isExaminable() {
        return examinable;
    }

    public void setExaminable(boolean examinable) {
        this.examinable = examinable;
    }

    public int getCbcDefaultLessons() {
        return cbcDefaultLessons;
    }

    public void setCbcDefaultLessons(int cbcDefaultLessons) {
        this.cbcDefaultLessons = cbcDefaultLessons;
    }

    public boolean isAllowsDoublePeriod() {
        return allowsDoublePeriod;
    }

    public void setAllowsDoublePeriod(boolean allowsDoublePeriod) {
        this.allowsDoublePeriod = allowsDoublePeriod;
    }

    public boolean isRequiresDoublePeriod() {
        return requiresDoublePeriod;
    }

    public void setRequiresDoublePeriod(boolean requiresDoublePeriod) {
        this.requiresDoublePeriod = requiresDoublePeriod;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isCbcSubject() {
        return cbcSubject;
    }

    public void setCbcSubject(boolean cbcSubject) {
        this.cbcSubject = cbcSubject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Subject subject)) {
            return false;
        }
        return Objects.equals(id, subject.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
