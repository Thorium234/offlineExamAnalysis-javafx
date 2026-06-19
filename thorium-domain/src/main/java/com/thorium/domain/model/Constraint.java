package com.thorium.domain.model;

import com.thorium.domain.value.ConstraintType;

import java.util.Objects;

public class Constraint {

    private Long id;
    private ConstraintType constraintType;
    private boolean enabled;
    private String parameters;

    public Constraint() {
        this.enabled = true;
    }

    public Constraint(Long id, ConstraintType constraintType, boolean enabled, String parameters) {
        this.id = id;
        this.constraintType = constraintType;
        this.enabled = enabled;
        this.parameters = parameters;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(ConstraintType constraintType) {
        this.constraintType = constraintType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Constraint that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
