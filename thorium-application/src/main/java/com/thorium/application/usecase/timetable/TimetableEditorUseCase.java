package com.thorium.application.usecase.timetable;

import com.thorium.application.dto.*;
import com.thorium.application.mapper.EntityMapper;
import com.thorium.application.port.*;
import com.thorium.application.util.NameFormatter;
import com.thorium.domain.constraint.HardConstraintValidator;
import com.thorium.domain.model.*;
import com.thorium.domain.scheduling.PartialSchedule;
import com.thorium.domain.scheduling.PlacedLesson;
import com.thorium.domain.scheduling.SchedulingContext;
import com.thorium.domain.value.DayOfWeek;
import com.thorium.domain.value.SubjectColorPalette;

import java.util.*;
import java.util.stream.Collectors;

public class TimetableEditorUseCase {

    private final TimetableRepository timetableRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final ClassStreamRepository classStreamRepository;
    private final TeacherAvailabilityRepository availabilityRepository;
    private final PeriodRepository periodRepository;
    private final ConstraintRepository constraintRepository;
    private final RoomRepository roomRepository;
    private final HardConstraintValidator validator;

    public TimetableEditorUseCase(TimetableRepository timetableRepository,
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
        this.validator = new HardConstraintValidator();
    }

    public TimetableEditorStateDto loadState(Long timetableId, Long classStreamId) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));

        List<PeriodDto> periods = periodRepository.findAll().stream()
                .map(EntityMapper::toDto)
                .toList();

        EditorContext ctx = buildContext(data.entries());
        List<LessonCardDto> gridLessons = new ArrayList<>();
        List<LessonCardDto> unassigned = new ArrayList<>();

        for (TimetableEntry entry : data.entries()) {
            TeachingAssignment assignment = ctx.assignment(entry.getTeachingAssignmentId());
            if (classStreamId != null && !classStreamId.equals(assignment.getClassStreamId())) {
                continue;
            }
            gridLessons.add(toCard(entry, assignment, ctx, false));
        }

        for (TeachingAssignment assignment : ctx.assignments()) {
            if (classStreamId != null && !classStreamId.equals(assignment.getClassStreamId())) {
                continue;
            }
            long placed = data.entries().stream()
                    .filter(e -> e.getTeachingAssignmentId().equals(assignment.getId()))
                    .count();
            int remaining = assignment.getLessonsPerWeek() - (int) placed;
            for (int i = 0; i < remaining; i++) {
                unassigned.add(toUnassignedCard(assignment, ctx));
            }
        }

        EntityTreeDto tree = buildEntityTree();

        return new TimetableEditorStateDto(
                timetableId,
                data.timetable().getName(),
                classStreamId,
                periods,
                gridLessons,
                unassigned,
                tree
        );
    }

    public PlacementValidationDto validatePlacement(Long timetableId, Long teachingAssignmentId,
                                                    DayOfWeek day, int period, Long excludeEntryId,
                                                    Long roomId) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));

        EditorContext ctx = buildContext(data.entries());
        TeachingAssignment assignment = ctx.assignment(teachingAssignmentId);
        ScheduleSlot slot = new ScheduleSlot(day, period);
        PartialSchedule schedule = ctx.partialScheduleExcluding(excludeEntryId);

        if (!validator.canPlace(assignment, slot, schedule, ctx.schedulingContext(), excludeEntryId)) {
            return PlacementValidationDto.invalid(describeConflict(assignment, slot, schedule, ctx, excludeEntryId));
        }
        if (!validator.isRoomAvailable(roomId, slot, data.entries(), excludeEntryId)) {
            return PlacementValidationDto.invalid("Room is already booked at this time");
        }
        return PlacementValidationDto.ok();
    }

    public LessonCardDto placeLesson(Long timetableId, Long teachingAssignmentId,
                                     DayOfWeek day, int period, Long roomId) {
        PlacementValidationDto validation = validatePlacement(
                timetableId, teachingAssignmentId, day, period, null, roomId);
        if (!validation.valid()) {
            throw new IllegalStateException(validation.reason());
        }

        TimetableEntry entry = new TimetableEntry();
        entry.setTimetableId(timetableId);
        entry.setTeachingAssignmentId(teachingAssignmentId);
        entry.setDayOfWeek(day);
        entry.setPeriodNumber(period);
        entry.setRoomId(roomId);

        TimetableEntry saved = timetableRepository.saveEntry(entry);
        EditorContext ctx = buildContext(timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow().entries());
        return toCard(saved, ctx.assignment(teachingAssignmentId), ctx, false);
    }

    public LessonCardDto moveEntry(Long timetableId, Long entryId, DayOfWeek day, int period, Long roomId) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));

        TimetableEntry entry = data.entries().stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entryId));

        PlacementValidationDto validation = validatePlacement(
                timetableId, entry.getTeachingAssignmentId(), day, period, entryId, roomId);
        if (!validation.valid()) {
            throw new IllegalStateException(validation.reason());
        }

        entry.setDayOfWeek(day);
        entry.setPeriodNumber(period);
        entry.setRoomId(roomId);
        TimetableEntry saved = timetableRepository.saveEntry(entry);

        EditorContext ctx = buildContext(timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow().entries());
        return toCard(saved, ctx.assignment(entry.getTeachingAssignmentId()), ctx, false);
    }

    public void swapEntries(Long timetableId, Long entryId1, Long entryId2) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));

        TimetableEntry entry1 = findEntry(data.entries(), entryId1);
        TimetableEntry entry2 = findEntry(data.entries(), entryId2);

        DayOfWeek day1 = entry1.getDayOfWeek();
        int period1 = entry1.getPeriodNumber();
        DayOfWeek day2 = entry2.getDayOfWeek();
        int period2 = entry2.getPeriodNumber();

        PlacementValidationDto v1 = validatePlacement(
                timetableId, entry1.getTeachingAssignmentId(), day2, period2, entryId1, entry1.getRoomId());
        PlacementValidationDto v2 = validatePlacement(
                timetableId, entry2.getTeachingAssignmentId(), day1, period1, entryId2, entry2.getRoomId());

        if (!v1.valid()) {
            throw new IllegalStateException("Cannot swap: " + v1.reason());
        }
        if (!v2.valid()) {
            throw new IllegalStateException("Cannot swap: " + v2.reason());
        }

        entry1.setDayOfWeek(day2);
        entry1.setPeriodNumber(period2);
        entry2.setDayOfWeek(day1);
        entry2.setPeriodNumber(period1);

        timetableRepository.saveEntry(entry1);
        timetableRepository.saveEntry(entry2);
    }

    public LessonCardDto unassignEntry(Long timetableId, Long entryId) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));

        TimetableEntry entry = findEntry(data.entries(), entryId);
        TeachingAssignment assignment = assignmentRepository.findById(entry.getTeachingAssignmentId())
                .orElseThrow();
        EditorContext ctx = buildContext(data.entries());

        LessonCardDto unassigned = toUnassignedCard(assignment, ctx);
        if (entry.getRoomId() != null) {
            unassigned = new LessonCardDto(
                    null, unassigned.teachingAssignmentId(), unassigned.teacherInitials(),
                    unassigned.teacherName(), unassigned.subjectCode(), unassigned.subjectName(),
                    unassigned.subjectColor(), unassigned.classStreamName(), unassigned.classStreamId(),
                    unassigned.roomCode(), null, null, 0, true, true
            );
        }

        timetableRepository.deleteEntry(entryId);
        return unassigned;
    }

    public LessonCardDto removeRoom(Long timetableId, Long entryId) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));

        TimetableEntry entry = findEntry(data.entries(), entryId);
        entry.setRoomId(null);
        TimetableEntry saved = timetableRepository.saveEntry(entry);

        EditorContext ctx = buildContext(data.entries());
        return toCard(saved, ctx.assignment(entry.getTeachingAssignmentId()), ctx, true);
    }

    public LessonCardDto assignRoom(Long timetableId, Long entryId, Long roomId) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));

        TimetableEntry entry = findEntry(data.entries(), entryId);
        ScheduleSlot slot = new ScheduleSlot(entry.getDayOfWeek(), entry.getPeriodNumber());

        if (roomId != null && !validator.isRoomAvailable(roomId, slot, data.entries(), entryId)) {
            throw new IllegalStateException("Room is already booked at this time");
        }

        entry.setRoomId(roomId);
        TimetableEntry saved = timetableRepository.saveEntry(entry);

        EditorContext ctx = buildContext(timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow().entries());
        return toCard(saved, ctx.assignment(entry.getTeachingAssignmentId()), ctx, false);
    }

    private TimetableEntry findEntry(List<TimetableEntry> entries, Long entryId) {
        return entries.stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entry not found: " + entryId));
    }

    private String describeConflict(TeachingAssignment assignment, ScheduleSlot slot,
                                    PartialSchedule schedule, EditorContext ctx, Long excludeEntryId) {
        if (ctx.schedulingContext().isTeacherUnavailable(assignment.getTeacherId(), slot)) {
            return "Teacher is unavailable at this time";
        }
        PartialSchedule checkSchedule = ctx.partialScheduleExcluding(excludeEntryId);
        if (checkSchedule.placedLessons().stream()
                .anyMatch(p -> p.assignment().getTeacherId().equals(assignment.getTeacherId())
                        && p.slot().equals(slot))) {
            return "Teacher is already teaching at this time";
        }
        if (checkSchedule.placedLessons().stream()
                .anyMatch(p -> p.assignment().getClassStreamId().equals(assignment.getClassStreamId())
                        && p.slot().equals(slot))) {
            return "Class already has a lesson at this time";
        }
        return "This placement violates scheduling constraints";
    }

    private LessonCardDto toCard(TimetableEntry entry, TeachingAssignment assignment,
                                 EditorContext ctx, boolean roomRemoved) {
        Teacher teacher = ctx.teacher(assignment.getTeacherId());
        Subject subject = ctx.subject(assignment.getSubjectId());
        ClassStream classStream = ctx.classStream(assignment.getClassStreamId());
        String roomCode = entry.getRoomId() != null
                ? ctx.roomCode(entry.getRoomId())
                : null;
        String color = SubjectColorPalette.resolveColor(subject.getId(), subject.getColor());

        return new LessonCardDto(
                entry.getId(),
                assignment.getId(),
                NameFormatter.initials(teacher.getName()),
                teacher.getName(),
                subject.getCode(),
                subject.getName(),
                color,
                classStream.getDisplayName(),
                classStream.getId(),
                roomCode,
                entry.getRoomId(),
                entry.getDayOfWeek(),
                entry.getPeriodNumber(),
                false,
                roomRemoved
        );
    }

    private LessonCardDto toUnassignedCard(TeachingAssignment assignment, EditorContext ctx) {
        Teacher teacher = ctx.teacher(assignment.getTeacherId());
        Subject subject = ctx.subject(assignment.getSubjectId());
        ClassStream classStream = ctx.classStream(assignment.getClassStreamId());
        String color = SubjectColorPalette.resolveColor(subject.getId(), subject.getColor());

        return LessonCardDto.unassigned(
                assignment.getId(),
                NameFormatter.initials(teacher.getName()),
                teacher.getName(),
                subject.getCode(),
                subject.getName(),
                color,
                classStream.getDisplayName(),
                classStream.getId(),
                null,
                null
        );
    }

    private EntityTreeDto buildEntityTree() {
        List<EntityTreeNodeDto> teachers = teacherRepository.findAll().stream()
                .map(t -> new EntityTreeNodeDto(t.getId(), t.getName(), t.getCode()))
                .toList();
        List<EntityTreeNodeDto> subjects = subjectRepository.findAll().stream()
                .map(s -> new EntityTreeNodeDto(
                        s.getId(), s.getName(), s.getCode()))
                .toList();
        List<EntityTreeNodeDto> classes = classStreamRepository.findAll().stream()
                .map(c -> new EntityTreeNodeDto(c.getId(), c.getDisplayName(), c.getCode()))
                .toList();
        List<EntityTreeNodeDto> rooms = roomRepository.findAll().stream()
                .map(r -> new EntityTreeNodeDto(r.getId(), r.getName(), r.getCode()))
                .toList();
        return new EntityTreeDto(teachers, subjects, classes, rooms);
    }

    private EditorContext buildContext(List<TimetableEntry> entries) {
        List<TeachingAssignment> assignments = assignmentRepository.findAll();
        Map<Long, TeachingAssignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(TeachingAssignment::getId, a -> a));

        Map<Long, Teacher> teachers = teacherRepository.findAll().stream()
                .collect(Collectors.toMap(Teacher::getId, t -> t));
        Map<Long, Subject> subjects = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getId, s -> s));
        Map<Long, ClassStream> classStreams = classStreamRepository.findAll().stream()
                .collect(Collectors.toMap(ClassStream::getId, c -> c));
        Map<Long, String> roomCodes = roomRepository.findAll().stream()
                .collect(Collectors.toMap(Room::getId, Room::getCode));

        int periodsPerDay = periodRepository.count();
        SchedulingContext schedulingContext = SchedulingContext.builder()
                .assignments(assignments)
                .teachers(new ArrayList<>(teachers.values()))
                .subjects(new ArrayList<>(subjects.values()))
                .classStreams(new ArrayList<>(classStreams.values()))
                .teacherAvailability(availabilityRepository.findAll())
                .periodsPerDay(periodsPerDay)
                .constraints(constraintRepository.findAll())
                .build();

        return new EditorContext(entries, assignmentMap, teachers, subjects, classStreams, roomCodes, schedulingContext);
    }

    private record EditorContext(
            List<TimetableEntry> entries,
            Map<Long, TeachingAssignment> assignmentMap,
            Map<Long, Teacher> teachers,
            Map<Long, Subject> subjects,
            Map<Long, ClassStream> classStreams,
            Map<Long, String> roomCodes,
            SchedulingContext schedulingContext
    ) {
        List<TeachingAssignment> assignments() {
            return new ArrayList<>(assignmentMap.values());
        }

        TeachingAssignment assignment(Long id) {
            return assignmentMap.get(id);
        }

        Teacher teacher(Long id) {
            return teachers.get(id);
        }

        Subject subject(Long id) {
            return subjects.get(id);
        }

        ClassStream classStream(Long id) {
            return classStreams.get(id);
        }

        String roomCode(Long roomId) {
            return roomCodes.get(roomId);
        }

        PartialSchedule partialScheduleExcluding(Long excludeEntryId) {
            PartialSchedule schedule = new PartialSchedule();
            for (TimetableEntry entry : entries) {
                if (excludeEntryId != null && excludeEntryId.equals(entry.getId())) {
                    continue;
                }
                TeachingAssignment assignment = assignmentMap.get(entry.getTeachingAssignmentId());
                if (assignment == null) {
                    continue;
                }
                schedule.place(new PlacedLesson(
                        assignment,
                        new ScheduleSlot(entry.getDayOfWeek(), entry.getPeriodNumber()),
                        entry.getId()
                ));
            }
            return schedule;
        }
    }
}
