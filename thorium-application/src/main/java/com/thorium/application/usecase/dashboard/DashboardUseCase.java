package com.thorium.application.usecase.dashboard;

import com.thorium.application.dto.DashboardSummaryDto;
import com.thorium.application.port.*;
import com.thorium.domain.model.Teacher;
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

        var teachers = teacherRepository.findAll();
        long overloaded = 0;
        long nearCapacity = 0;

        for (Teacher teacher : teachers) {
            int assigned = assignments.stream()
                    .filter(a -> a.getTeacherId().equals(teacher.getId()))
                    .mapToInt(TeachingAssignment::getLessonsPerWeek)
                    .sum();
            int max = teacher.getMaxLessonsPerWeek();
            if (max > 0) {
                double pct = (double) assigned / max;
                if (pct > 0.9) overloaded++;
                else if (pct >= 0.7) nearCapacity++;
            }
        }

        return new DashboardSummaryDto(
                teacherRepository.count(),
                subjectRepository.count(),
                classStreamRepository.count(),
                assignmentRepository.count(),
                totalLessons,
                timetables.size(),
                roomRepository.count(),
                latestName,
                overloaded,
                nearCapacity,
                teachers.size() - overloaded - nearCapacity
        );
    }
}
