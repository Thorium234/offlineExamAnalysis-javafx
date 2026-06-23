package com.thorium.ui.controller;

import com.thorium.application.dto.DashboardSummaryDto;
import com.thorium.ui.di.AppContext;
import com.thorium.ui.util.IconUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class DashboardController {

    @FXML private Label teachersCount;
    @FXML private Label subjectsCount;
    @FXML private Label classesCount;
    @FXML private Label assignmentsCount;
    @FXML private Label totalLessonsCount;
    @FXML private Label roomsCount;
    @FXML private Label timetablesCount;
    @FXML private Label latestTimetable;
    @FXML private Label workloadCount;
    @FXML private StackPane teachersIcon;
    @FXML private StackPane subjectsIcon;
    @FXML private StackPane classesIcon;
    @FXML private StackPane assignmentsIcon;
    @FXML private StackPane totalLessonsIcon;
    @FXML private StackPane roomsIcon;
    @FXML private StackPane timetablesIcon;
    @FXML private StackPane latestIcon;
    @FXML private StackPane workloadIcon;
    @FXML private Button refreshBtn;

    private static final String ICON_TEACHERS = "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2 M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z";
    private static final String ICON_SUBJECTS = "M4 19.5v-15a2.5 2.5 0 0 1 2.5-2.5H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5z";
    private static final String ICON_CLASSES = "M22 10v6M2 10l10-5 10 5-10 5z M6 12v5c0 2 2 3 6 3s6-1 6-3v-5";
    private static final String ICON_ASSIGNMENTS = "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z M14 2v6h6 M16 13H8 M16 17H8 M10 9H8";
    private static final String ICON_TIMETABLES = "M3 3h18v18H3z M21 9H3 M3 15h18 M9 3v18 M15 3v18";
    private static final String ICON_LATEST = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z M12 6v6l4 2";

    @FXML
    private void initialize() {
        IconUtil.addIcon(refreshBtn, IconUtil.REFRESH, "#3b82f6");
        setIcon(teachersIcon, ICON_TEACHERS, "#10b981");
        setIcon(subjectsIcon, ICON_SUBJECTS, "#f59e0b");
        setIcon(classesIcon, ICON_CLASSES, "#8b5cf6");
        setIcon(assignmentsIcon, ICON_ASSIGNMENTS, "#06b6d4");
        setIcon(totalLessonsIcon, ICON_TIMETABLES, "#6366f1");
        setIcon(roomsIcon, ICON_CLASSES, "#ef4444");
        setIcon(timetablesIcon, ICON_TIMETABLES, "#3b82f6");
        setIcon(latestIcon, ICON_LATEST, "#f97316");
        setIcon(workloadIcon, ICON_TEACHERS, "#e11d48");
        refresh();
    }

    @FXML
    private void onRefresh() {
        refresh();
    }

    private void refresh() {
        DashboardSummaryDto summary = AppContext.get().dashboardUseCase().getSummary();
        teachersCount.setText(String.valueOf(summary.teacherCount()));
        subjectsCount.setText(String.valueOf(summary.subjectCount()));
        classesCount.setText(String.valueOf(summary.classStreamCount()));
        assignmentsCount.setText(String.valueOf(summary.assignmentCount()));
        totalLessonsCount.setText(String.valueOf(summary.totalLessonsPerWeek()));
        roomsCount.setText(String.valueOf(summary.roomCount()));
        timetablesCount.setText(String.valueOf(summary.timetableCount()));
        latestTimetable.setText(summary.latestTimetableName());
        workloadCount.setText(summary.teachersOverloaded() + " / " + summary.teachersNearCapacity() + " near");
    }

    private void setIcon(StackPane container, String svgPath, String color) {
        SVGPath path = new SVGPath();
        path.setContent(svgPath);
        path.setFill(Color.TRANSPARENT);
        path.setStroke(Color.web(color));
        path.setStrokeWidth(1.8);
        container.getChildren().add(path);
        container.setPadding(new Insets(0, 0, 4, 0));
    }
}
