package com.thorium.application.port;

import com.thorium.application.port.TimetableRepository.TimetableWithEntries;

import java.nio.file.Path;

public interface TimetableExporter {

    void exportPdf(TimetableWithEntries data, Path outputPath);

    void exportExcel(TimetableWithEntries data, Path outputPath);
}
