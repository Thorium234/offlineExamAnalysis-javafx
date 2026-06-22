package com.thorium.application.usecase.timetable;

import com.thorium.application.dto.TimetableDto;
import com.thorium.application.dto.TimetableEntryDto;
import com.thorium.application.port.*;
import com.thorium.application.util.NameFormatter;
import com.thorium.domain.model.*;
import com.thorium.domain.scheduling.SchedulingContext;
import com.thorium.domain.scheduling.TimetableGenerationResult;
import com.thorium.domain.scheduling.TimetableGenerator;
import com.thorium.domain.value.SubjectColorPalette;
import com.thorium.domain.value.TimetableStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GenerateTimetableUseCase {

    private final TimetableRepository timetableRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassStreamRepository classStreamRepository;
    private final TeacherAvailabilityRepository availabilityRepository;
    private final PeriodRepository periodRepository;
    private final ConstraintRepository constraintRepository;
    private final RoomRepository roomRepository;
    private final TimetableGenerator generator;

    public GenerateTimetableUseCase(TimetableRepository timetableRepository,
                                    TeachingAssignmentRepository assignmentRepository,
                                    TeacherRepository teacherRepository,
                                    SubjectRepository subjectRepository,
                                    ClassStreamRepository classStreamRepository,
                                    TeacherAvailabilityRepository availabilityRepository,
                                    PeriodRepository periodRepository,
                                    ConstraintRepository constraintRepository,
                                    RoomRepository roomRepository) {
        this.timetableRepository = timetableRepository;
        this.assignmentRepository = assignmentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.classStreamRepository = classStreamRepository;
        this.availabilityRepository = availabilityRepository;
        this.periodRepository = periodRepository;
        this.constraintRepository = constraintRepository;
        this.roomRepository = roomRepository;
        this.generator = new TimetableGenerator();
    }

    public TimetableDto execute(String name) {
        List<TeachingAssignment> assignments = assignmentRepository.findAll();
        if (assignments.isEmpty()) {
            throw new IllegalStateException("No teaching assignments defined");
        }

        int periodsPerDay = periodRepository.count();
        if (periodsPerDay == 0) {
            throw new IllegalStateException("No periods configured");
        }

        SchedulingContext context = SchedulingContext.builder()
                .assignments(assignments)
                .teachers(teacherRepository.findAll())
                .subjects(subjectRepository.findAll())
                .classStreams(classStreamRepository.findAll())
                .teacherAvailability(availabilityRepository.findAll())
                .periodsPerDay(periodsPerDay)
                .constraints(constraintRepository.findAll())
                .build();

        List<String> preflight = generator.preflightChecks(context);
        TimetableGenerationResult result = generator.generate(context);

        if (!result.isSuccess() && !result.isComplete(context)) {
            List<String> messages = new ArrayList<>(result.errors());
            messages.addAll(result.warnings());
            messages.addAll(preflight);
            throw new IllegalStateException("Timetable generation failed: " + String.join("; ", messages));
        }

        Timetable timetable = new Timetable();
        timetable.setName(name != null && !name.isBlank() ? name : "Timetable " + LocalDateTime.now());
        timetable.setStatus(TimetableStatus.GENERATED);
        timetable.setCreatedAt(LocalDateTime.now());
        timetable.setQualityScore(result.qualityScore());

        List<TimetableEntry> entries = result.schedule().toEntries(null);
        Timetable saved = timetableRepository.saveWithEntries(timetable, entries);
        return toDto(timetableRepository.findByIdWithEntries(saved.getId())
                .orElseThrow(() -> new IllegalStateException("Failed to load saved timetable")));
    }

    public List<TimetableDto> findAll() {
        return timetableRepository.findAll().stream()
                .map(t -> timetableRepository.findByIdWithEntries(t.getId())
                        .map(this::toDto)
                        .orElse(toDtoWithoutEntries(t)))
                .toList();
    }

    public TimetableDto findById(Long id) {
        return timetableRepository.findByIdWithEntries(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + id));
    }

    private TimetableDto toDto(TimetableRepository.TimetableWithEntries data) {
        List<TimetableEntryDto> entryDtos = data.entries().stream()
                .map(this::toEntryDto)
                .toList();
        Timetable t = data.timetable();
        return new TimetableDto(t.getId(), t.getName(), t.getStatus(), t.getCreatedAt(), t.getQualityScore(), entryDtos);
    }

    private TimetableDto toDtoWithoutEntries(Timetable t) {
        return new TimetableDto(t.getId(), t.getName(), t.getStatus(), t.getCreatedAt(), t.getQualityScore(), List.of());
    }

    private TimetableEntryDto toEntryDto(TimetableEntry entry) {
        TeachingAssignment assignment = assignmentRepository.findById(entry.getTeachingAssignmentId())
                .orElseThrow();
        Teacher teacher = teacherRepository.findById(assignment.getTeacherId()).orElseThrow();
        Subject subject = subjectRepository.findById(assignment.getSubjectId()).orElseThrow();
        ClassStream classStream = classStreamRepository.findById(assignment.getClassStreamId()).orElseThrow();
        String roomCode = entry.getRoomId() != null
                ? roomRepository.findById(entry.getRoomId()).map(Room::getCode).orElse(null)
                : null;
        return new TimetableEntryDto(
                entry.getId(),
                entry.getTeachingAssignmentId(),
                teacher.getName(),
                NameFormatter.initials(teacher.getName()),
                subject.getName(),
                subject.getCode(),
                SubjectColorPalette.resolveColor(subject.getId(), subject.getColor()),
                classStream.getDisplayName(),
                classStream.getId(),
                roomCode,
                entry.getRoomId(),
                entry.getDayOfWeek(),
                entry.getPeriodNumber()
        );
    }
}
