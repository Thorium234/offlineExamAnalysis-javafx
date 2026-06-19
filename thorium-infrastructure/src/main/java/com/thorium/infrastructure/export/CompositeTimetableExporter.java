package com.thorium.infrastructure.export;

import com.thorium.application.port.TimetableExporter;
import com.thorium.application.port.TimetableRepository;
import com.thorium.infrastructure.export.ExcelTimetableExporter;
import com.thorium.infrastructure.export.PdfTimetableExporter;

import java.nio.file.Path;

public class CompositeTimetableExporter implements TimetableExporter {

    private final PdfTimetableExporter pdfExporter;
    private final ExcelTimetableExporter excelExporter;

    public CompositeTimetableExporter(PdfTimetableExporter pdfExporter, ExcelTimetableExporter excelExporter) {
        this.pdfExporter = pdfExporter;
        this.excelExporter = excelExporter;
    }

    @Override
    public void exportPdf(TimetableRepository.TimetableWithEntries data, Path outputPath) {
        pdfExporter.exportPdf(data, outputPath);
    }

    @Override
    public void exportExcel(TimetableRepository.TimetableWithEntries data, Path outputPath) {
        excelExporter.exportExcel(data, outputPath);
    }
}
