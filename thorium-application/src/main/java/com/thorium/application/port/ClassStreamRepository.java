package com.thorium.application.port;

import com.thorium.domain.model.ClassStream;

import java.util.List;
import java.util.Optional;

public interface ClassStreamRepository {

    ClassStream save(ClassStream classStream);

    Optional<ClassStream> findById(Long id);

    List<ClassStream> findAll();

    void deleteById(Long id);

    long count();
}
