package com.thorium.application.port;

import com.thorium.domain.model.TeacherAvailability;

import java.util.List;

public interface TeacherAvailabilityRepository {

    TeacherAvailability save(TeacherAvailability availability);

    List<TeacherAvailability> findByTeacherId(Long teacherId);

    List<TeacherAvailability> findAll();

    void deleteByTeacherId(Long teacherId);

    void deleteById(Long id);
}
