package com.thorium.application.usecase.export;

import com.thorium.application.port.TimetableExporter;
import com.thorium.application.port.TimetableRepository;

import java.nio.file.Path;

public class ExportTimetableUseCase {

    private final TimetableRepository timetableRepository;
    private final TimetableExporter exporter;

    public ExportTimetableUseCase(TimetableRepository timetableRepository, TimetableExporter exporter) {
        this.timetableRepository = timetableRepository;
        this.exporter = exporter;
    }

    public void exportPdf(Long timetableId, Path outputPath) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));
        exporter.exportPdf(data, outputPath);
    }

    public void exportExcel(Long timetableId, Path outputPath) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));
        exporter.exportExcel(data, outputPath);
    }

    public byte[] previewPdf(Long timetableId) {
        TimetableRepository.TimetableWithEntries data = timetableRepository.findByIdWithEntries(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("Timetable not found: " + timetableId));
        return exporter.renderPdfToBytes(data);
    }
}
