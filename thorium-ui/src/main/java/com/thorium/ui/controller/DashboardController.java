package com.thorium.ui.controller;

import com.thorium.application.dto.DashboardSummaryDto;
import com.thorium.ui.di.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label teachersCount;
    @FXML
    private Label subjectsCount;
    @FXML
    private Label classesCount;
    @FXML
    private Label assignmentsCount;
    @FXML
    private Label timetablesCount;
    @FXML
    private Label latestTimetable;

    @FXML
    private void initialize() {
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
        timetablesCount.setText(String.valueOf(summary.timetableCount()));
        latestTimetable.setText(summary.latestTimetableName());
    }
}
