package com.thorium.domain.scheduling;

import com.thorium.domain.model.*;
import com.thorium.domain.value.ConstraintType;
import com.thorium.domain.value.DayOfWeek;

import java.util.*;

public final class SchedulingContext {

    private final List<TeachingAssignment> assignments;
    private final Map<Long, Teacher> teachersById;
    private final Map<Long, Subject> subjectsById;
    private final Map<Long, ClassStream> classStreamsById;
    private final Map<Long, Set<ScheduleSlot>> unavailableByTeacher;
    private final List<DayOfWeek> workingDays;
    private final int periodsPerDay;
    private final boolean cbcNoDoubleLessonEnabled;

    private SchedulingContext(Builder builder) {
        this.assignments = List.copyOf(builder.assignments);
        this.teachersById = Map.copyOf(builder.teachersById);
        this.subjectsById = Map.copyOf(builder.subjectsById);
        this.classStreamsById = Map.copyOf(builder.classStreamsById);
        this.unavailableByTeacher = Map.copyOf(builder.unavailableByTeacher);
        this.workingDays = List.copyOf(builder.workingDays);
        this.periodsPerDay = builder.periodsPerDay;
        this.cbcNoDoubleLessonEnabled = builder.cbcNoDoubleLessonEnabled;
    }

    public List<TeachingAssignment> assignments() {
        return assignments;
    }

    public Optional<Teacher> teacher(long id) {
        return Optional.ofNullable(teachersById.get(id));
    }

    public Optional<Subject> subject(long id) {
        return Optional.ofNullable(subjectsById.get(id));
    }

    public Optional<ClassStream> classStream(long id) {
        return Optional.ofNullable(classStreamsById.get(id));
    }

    public boolean isTeacherUnavailable(long teacherId, ScheduleSlot slot) {
        Set<ScheduleSlot> slots = unavailableByTeacher.get(teacherId);
        return slots != null && slots.contains(slot);
    }

    public List<DayOfWeek> workingDays() {
        return workingDays;
    }

    public int periodsPerDay() {
        return periodsPerDay;
    }

    public boolean isCbcNoDoubleLessonEnabled() {
        return cbcNoDoubleLessonEnabled;
    }

    public List<ScheduleSlot> allSlots() {
        List<ScheduleSlot> slots = new ArrayList<>();
        for (DayOfWeek day : workingDays) {
            for (int period = 1; period <= periodsPerDay; period++) {
                slots.add(new ScheduleSlot(day, period));
            }
        }
        return slots;
    }

    public int totalSlots() {
        return workingDays.size() * periodsPerDay;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<TeachingAssignment> assignments = List.of();
        private Map<Long, Teacher> teachersById = Map.of();
        private Map<Long, Subject> subjectsById = Map.of();
        private Map<Long, ClassStream> classStreamsById = Map.of();
        private Map<Long, Set<ScheduleSlot>> unavailableByTeacher = Map.of();
        private List<DayOfWeek> workingDays = DayOfWeek.workingDays();
        private int periodsPerDay = 8;
        private boolean cbcNoDoubleLessonEnabled = true;

        public Builder assignments(List<TeachingAssignment> assignments) {
            this.assignments = assignments;
            return this;
        }

        public Builder teachers(List<Teacher> teachers) {
            Map<Long, Teacher> map = new HashMap<>();
            for (Teacher teacher : teachers) {
                map.put(teacher.getId(), teacher);
            }
            this.teachersById = map;
            return this;
        }

        public Builder subjects(List<Subject> subjects) {
            Map<Long, Subject> map = new HashMap<>();
            for (Subject subject : subjects) {
                map.put(subject.getId(), subject);
            }
            this.subjectsById = map;
            return this;
        }

        public Builder classStreams(List<ClassStream> classStreams) {
            Map<Long, ClassStream> map = new HashMap<>();
            for (ClassStream cs : classStreams) {
                map.put(cs.getId(), cs);
            }
            this.classStreamsById = map;
            return this;
        }

        public Builder teacherAvailability(List<TeacherAvailability> availability) {
            Map<Long, Set<ScheduleSlot>> map = new HashMap<>();
            for (TeacherAvailability entry : availability) {
                if (!entry.isAvailable()) {
                    map.computeIfAbsent(entry.getTeacherId(), k -> new HashSet<>())
                            .add(entry.toSlot());
                }
            }
            this.unavailableByTeacher = map;
            return this;
        }

        public Builder workingDays(List<DayOfWeek> workingDays) {
            this.workingDays = workingDays;
            return this;
        }

        public Builder periodsPerDay(int periodsPerDay) {
            this.periodsPerDay = periodsPerDay;
            return this;
        }

        public Builder cbcNoDoubleLessonEnabled(boolean enabled) {
            this.cbcNoDoubleLessonEnabled = enabled;
            return this;
        }

        public Builder constraints(List<Constraint> constraints) {
            for (Constraint constraint : constraints) {
                if (constraint.getConstraintType() == ConstraintType.CBC_NO_DOUBLE_LESSON) {
                    this.cbcNoDoubleLessonEnabled = constraint.isEnabled();
                }
            }
            return this;
        }

        public SchedulingContext build() {
            if (assignments.isEmpty()) {
                throw new IllegalStateException("At least one teaching assignment is required");
            }
            if (periodsPerDay <= 0) {
                throw new IllegalStateException("periodsPerDay must be positive");
            }
            return new SchedulingContext(this);
        }
    }
}
