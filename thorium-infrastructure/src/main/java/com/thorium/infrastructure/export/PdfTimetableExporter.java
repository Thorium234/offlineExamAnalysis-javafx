package com.thorium.infrastructure.export;

import com.thorium.application.port.ClassStreamRepository;
import com.thorium.application.port.SubjectRepository;
import com.thorium.application.port.TeachingAssignmentRepository;
import com.thorium.application.port.TimetableExporter;
import com.thorium.application.port.TimetableRepository;
import com.thorium.domain.model.TeachingAssignment;
import com.thorium.domain.model.TimetableEntry;
import com.thorium.domain.value.DayOfWeek;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PdfTimetableExporter implements TimetableExporter {

    private final TeachingAssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassStreamRepository classStreamRepository;

    public PdfTimetableExporter(TeachingAssignmentRepository assignmentRepository,
                                SubjectRepository subjectRepository,
                                ClassStreamRepository classStreamRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.classStreamRepository = classStreamRepository;
    }

    @Override
    public void exportPdf(TimetableRepository.TimetableWithEntries data, Path outputPath) {
        try (PDDocument document = new PDDocument()) {
            Map<String, List<TimetableEntry>> byClass = groupByClass(data.entries());
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            for (Map.Entry<String, List<TimetableEntry>> classEntry : byClass.entrySet()) {
                PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
                PDPage page = new PDPage(landscape);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    float y = page.getMediaBox().getHeight() - 40;
                    content.beginText();
                    content.setFont(fontBold, 16);
                    content.newLineAtOffset(40, y);
                    content.showText(data.timetable().getName() + " — " + classEntry.getKey());
                    content.endText();

                    y -= 30;
                    float xStart = 40;
                    float cellWidth = 80;
                    float cellHeight = 20;

                    content.setFont(fontBold, 10);
                    drawCell(content, xStart, y, cellWidth, cellHeight, "Period");
                    float x = xStart + cellWidth;
                    for (DayOfWeek day : DayOfWeek.workingDays()) {
                        drawCell(content, x, y, cellWidth, cellHeight, day.displayName());
                        x += cellWidth;
                    }

                    int maxPeriod = classEntry.getValue().stream()
                            .mapToInt(TimetableEntry::getPeriodNumber).max().orElse(8);
                    Map<String, String> cellValues = buildCellMap(classEntry.getValue());

                    for (int period = 1; period <= maxPeriod; period++) {
                        y -= cellHeight;
                        x = xStart;
                        drawCell(content, x, y, cellWidth, cellHeight, "P" + period);
                        x += cellWidth;
                        for (DayOfWeek day : DayOfWeek.workingDays()) {
                            String key = day.name() + "-" + period;
                            drawCell(content, x, y, cellWidth, cellHeight, cellValues.getOrDefault(key, ""));
                            x += cellWidth;
                        }
                    }
                }
            }
            document.save(outputPath.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export PDF", e);
        }
    }

    @Override
    public void exportExcel(TimetableRepository.TimetableWithEntries data, Path outputPath) {
        throw new UnsupportedOperationException("Use ExcelTimetableExporter");
    }

    private Map<String, List<TimetableEntry>> groupByClass(List<TimetableEntry> entries) {
        Map<Long, TeachingAssignment> assignments = assignmentRepository.findAll().stream()
                .collect(Collectors.toMap(TeachingAssignment::getId, a -> a));

        Map<String, List<TimetableEntry>> grouped = new LinkedHashMap<>();
        for (TimetableEntry entry : entries) {
            TeachingAssignment assignment = assignments.get(entry.getTeachingAssignmentId());
            String classKey = "Unknown";
            if (assignment != null) {
                classKey = classStreamRepository.findById(assignment.getClassStreamId())
                        .map(c -> c.getDisplayName())
                        .orElse("Class #" + assignment.getClassStreamId());
            }
            grouped.computeIfAbsent(classKey, k -> new ArrayList<>()).add(entry);
        }
        return grouped;
    }

    private Map<String, String> buildCellMap(List<TimetableEntry> entries) {
        Map<Long, TeachingAssignment> assignments = assignmentRepository.findAll().stream()
                .collect(Collectors.toMap(TeachingAssignment::getId, a -> a));
        Map<String, String> cells = new HashMap<>();
        for (TimetableEntry entry : entries) {
            TeachingAssignment assignment = assignments.get(entry.getTeachingAssignmentId());
            String label = "?";
            if (assignment != null) {
                String subjectName = subjectRepository.findById(assignment.getSubjectId())
                        .map(s -> s.getName()).orElse("?");
                String teacherName = ""; // could be added if desired
                label = subjectName;
            }
            cells.put(entry.getDayOfWeek().name() + "-" + entry.getPeriodNumber(), label);
        }
        return cells;
    }

    private void drawCell(PDPageContentStream content, float x, float y, float w, float h, String text)
            throws IOException {
        content.addRect(x, y - h, w, h);
        content.stroke();
        content.beginText();
        content.newLineAtOffset(x + 4, y - h + 6);
        content.showText(truncate(text, 12));
        content.endText();
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 2) + "..";
    }
}
