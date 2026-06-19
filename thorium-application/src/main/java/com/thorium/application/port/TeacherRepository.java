package com.thorium.application.port;

import com.thorium.domain.model.Teacher;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository {

    Teacher save(Teacher teacher);

    Optional<Teacher> findById(Long id);

    Optional<Teacher> findByCode(String code);

    List<Teacher> findAll();

    void deleteById(Long id);

    long count();
}
