package com.thorium.domain.model;

import java.util.Objects;

public class TeachingAssignment {

    private Long id;
    private Long teacherId;
    private Long subjectId;
    private Long classStreamId;
    private int lessonsPerWeek;
    private LessonDuration duration;

    public TeachingAssignment() {
        this.duration = LessonDuration.SINGLE;
    }

    public TeachingAssignment(Long id, Long teacherId, Long subjectId, Long classStreamId, int lessonsPerWeek) {
        this(id, teacherId, subjectId, classStreamId, lessonsPerWeek, LessonDuration.SINGLE);
    }

    public TeachingAssignment(Long id, Long teacherId, Long subjectId, Long classStreamId,
                               int lessonsPerWeek, LessonDuration duration) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.classStreamId = classStreamId;
        this.lessonsPerWeek = lessonsPerWeek;
        this.duration = duration != null ? duration : LessonDuration.SINGLE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getClassStreamId() {
        return classStreamId;
    }

    public void setClassStreamId(Long classStreamId) {
        this.classStreamId = classStreamId;
    }

    public int getLessonsPerWeek() {
        return lessonsPerWeek;
    }

    public void setLessonsPerWeek(int lessonsPerWeek) {
        this.lessonsPerWeek = lessonsPerWeek;
    }

    public LessonDuration getDuration() {
        return duration;
    }

    public void setDuration(LessonDuration duration) {
        this.duration = duration != null ? duration : LessonDuration.SINGLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TeachingAssignment that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
