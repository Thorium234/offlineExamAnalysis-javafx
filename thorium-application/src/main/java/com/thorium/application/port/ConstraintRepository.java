package com.thorium.application.port;

import com.thorium.domain.model.Constraint;

import java.util.List;
import java.util.Optional;

public interface ConstraintRepository {

    Constraint save(Constraint constraint);

    Optional<Constraint> findByType(com.thorium.domain.value.ConstraintType type);

    List<Constraint> findAll();
}
