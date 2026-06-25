package com.thorium.application.usecase.dashboard;

import com.thorium.application.dto.DashboardSummaryDto;
import com.thorium.application.port.*;
import com.thorium.domain.model.TeachingAssignment;

public class DashboardUseCase {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassStreamRepository classStreamRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final TimetableRepository timetableRepository;
    private final RoomRepository roomRepository;

    public DashboardUseCase(TeacherRepository teacherRepository,
                            SubjectRepository subjectRepository,
                            ClassStreamRepository classStreamRepository,
                            TeachingAssignmentRepository assignmentRepository,
                            TimetableRepository timetableRepository,
                            RoomRepository roomRepository) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.classStreamRepository = classStreamRepository;
        this.assignmentRepository = assignmentRepository;
        this.timetableRepository = timetableRepository;
        this.roomRepository = roomRepository;
    }

    public DashboardSummaryDto getSummary() {
        var timetables = timetableRepository.findAll();
        String latestName = timetables.isEmpty() ? "None" : timetables.getLast().getName();

        var assignments = assignmentRepository.findAll();
        long totalLessons = assignments.stream()
                .mapToLong(TeachingAssignment::getLessonsPerWeek)
                .sum();

        return new DashboardSummaryDto(
                teacherRepository.count(),
                subjectRepository.count(),
                classStreamRepository.count(),
                assignmentRepository.count(),
                totalLessons,
                timetables.size(),
                roomRepository.count(),
                latestName
        );
    }
}
