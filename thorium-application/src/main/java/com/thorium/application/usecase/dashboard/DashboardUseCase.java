package com.thorium.application.usecase.dashboard;

import com.thorium.application.dto.DashboardSummaryDto;
import com.thorium.application.port.*;

public class DashboardUseCase {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassStreamRepository classStreamRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final TimetableRepository timetableRepository;

    public DashboardUseCase(TeacherRepository teacherRepository,
                            SubjectRepository subjectRepository,
                            ClassStreamRepository classStreamRepository,
                            TeachingAssignmentRepository assignmentRepository,
                            TimetableRepository timetableRepository) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.classStreamRepository = classStreamRepository;
        this.assignmentRepository = assignmentRepository;
        this.timetableRepository = timetableRepository;
    }

    public DashboardSummaryDto getSummary() {
        var timetables = timetableRepository.findAll();
        String latestName = timetables.isEmpty() ? "None" : timetables.getLast().getName();
        return new DashboardSummaryDto(
                teacherRepository.count(),
                subjectRepository.count(),
                classStreamRepository.count(),
                assignmentRepository.count(),
                timetables.size(),
                latestName
        );
    }
}
