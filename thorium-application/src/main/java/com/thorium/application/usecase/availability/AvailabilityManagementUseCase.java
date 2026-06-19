package com.thorium.application.usecase.availability;

import com.thorium.application.dto.TeacherAvailabilityDto;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.TeacherAvailabilityRepository;
import com.thorium.application.port.TeacherRepository;
import com.thorium.domain.model.TeacherAvailability;

import java.util.List;

public class AvailabilityManagementUseCase {

    private final TeacherAvailabilityRepository availabilityRepository;
    private final TeacherRepository teacherRepository;

    public AvailabilityManagementUseCase(TeacherAvailabilityRepository availabilityRepository,
                                         TeacherRepository teacherRepository) {
        this.availabilityRepository = availabilityRepository;
        this.teacherRepository = teacherRepository;
    }

    public TeacherAvailabilityDto save(TeacherAvailabilityDto dto) {
        if (dto.teacherId() == null) {
            throw new IllegalArgumentException("Teacher is required");
        }
        teacherRepository.findById(dto.teacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        TeacherAvailability availability = new TeacherAvailability();
        availability.setId(dto.id());
        availability.setTeacherId(dto.teacherId());
        availability.setDayOfWeek(dto.dayOfWeek());
        availability.setPeriodNumber(dto.periodNumber());
        availability.setAvailable(dto.available());

        TeacherAvailability saved = availabilityRepository.save(availability);
        String teacherName = teacherRepository.findById(saved.getTeacherId())
                .map(t -> t.getName()).orElse("");
        return EntityMapper.toDto(saved, teacherName);
    }

    public void delete(Long id) {
        availabilityRepository.deleteById(id);
    }

    public List<TeacherAvailabilityDto> findAll() {
        return availabilityRepository.findAll().stream()
                .map(a -> EntityMapper.toDto(a,
                        teacherRepository.findById(a.getTeacherId()).map(t -> t.getName()).orElse("")))
                .toList();
    }

    public List<TeacherAvailabilityDto> findByTeacher(Long teacherId) {
        return availabilityRepository.findByTeacherId(teacherId).stream()
                .map(a -> EntityMapper.toDto(a,
                        teacherRepository.findById(a.getTeacherId()).map(t -> t.getName()).orElse("")))
                .toList();
    }
}
