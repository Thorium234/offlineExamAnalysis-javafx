package com.thorium.application.usecase.assignment;

import com.thorium.application.dto.TeachingAssignmentDto;
import com.thorium.application.port.ClassStreamRepository;
import com.thorium.application.port.SubjectRepository;
import com.thorium.application.port.TeacherRepository;
import com.thorium.application.port.TeachingAssignmentRepository;
import com.thorium.domain.model.TeachingAssignment;

import java.util.List;

public class AssignmentManagementUseCase {

    private final TeachingAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassStreamRepository classStreamRepository;

    public AssignmentManagementUseCase(TeachingAssignmentRepository assignmentRepository,
                                       TeacherRepository teacherRepository,
                                       SubjectRepository subjectRepository,
                                       ClassStreamRepository classStreamRepository) {
        this.assignmentRepository = assignmentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.classStreamRepository = classStreamRepository;
    }

    public TeachingAssignmentDto create(TeachingAssignmentDto dto) {
        validate(dto);
        TeachingAssignment assignment = toEntity(dto);
        return toDto(assignmentRepository.save(assignment));
    }

    public TeachingAssignmentDto update(TeachingAssignmentDto dto) {
        if (dto.id() == null) {
            throw new IllegalArgumentException("Assignment id is required for update");
        }
        validate(dto);
        return toDto(assignmentRepository.save(toEntity(dto)));
    }

    public void delete(Long id) {
        assignmentRepository.deleteById(id);
    }

    public List<TeachingAssignmentDto> findAll() {
        return assignmentRepository.findAll().stream().map(this::toDto).toList();
    }

    private TeachingAssignmentDto toDto(TeachingAssignment assignment) {
        String teacherName = teacherRepository.findById(assignment.getTeacherId())
                .map(t -> t.getName()).orElse("Unknown");
        String subjectName = subjectRepository.findById(assignment.getSubjectId())
                .map(s -> s.getName()).orElse("Unknown");
        String className = classStreamRepository.findById(assignment.getClassStreamId())
                .map(c -> c.getDisplayName()).orElse("Unknown");
        return new TeachingAssignmentDto(
                assignment.getId(),
                assignment.getTeacherId(),
                teacherName,
                assignment.getSubjectId(),
                subjectName,
                assignment.getClassStreamId(),
                className,
                assignment.getLessonsPerWeek()
        );
    }

    private TeachingAssignment toEntity(TeachingAssignmentDto dto) {
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setId(dto.id());
        assignment.setTeacherId(dto.teacherId());
        assignment.setSubjectId(dto.subjectId());
        assignment.setClassStreamId(dto.classStreamId());
        assignment.setLessonsPerWeek(dto.lessonsPerWeek());
        return assignment;
    }

    private void validate(TeachingAssignmentDto dto) {
        if (dto.teacherId() == null || dto.subjectId() == null || dto.classStreamId() == null) {
            throw new IllegalArgumentException("Teacher, subject, and class are required");
        }
        if (dto.lessonsPerWeek() <= 0) {
            throw new IllegalArgumentException("Lessons per week must be positive");
        }
        teacherRepository.findById(dto.teacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        subjectRepository.findById(dto.subjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));
        classStreamRepository.findById(dto.classStreamId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
    }
}
