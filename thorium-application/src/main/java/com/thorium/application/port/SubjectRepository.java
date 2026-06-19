package com.thorium.application.port;

import com.thorium.domain.model.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository {

    Subject save(Subject subject);

    Optional<Subject> findById(Long id);

    List<Subject> findAll();

    void deleteById(Long id);

    long count();
}
