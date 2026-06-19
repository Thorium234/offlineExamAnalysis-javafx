package com.thorium.application.port;

import com.thorium.domain.model.Timetable;
import com.thorium.domain.model.TimetableEntry;

import java.util.List;
import java.util.Optional;

public interface TimetableRepository {

    Timetable save(Timetable timetable);

    Timetable saveWithEntries(Timetable timetable, List<TimetableEntry> entries);

    Optional<Timetable> findById(Long id);

    Optional<TimetableWithEntries> findByIdWithEntries(Long id);

    List<Timetable> findAll();

    void deleteById(Long id);

    record TimetableWithEntries(Timetable timetable, List<TimetableEntry> entries) {
    }
}
