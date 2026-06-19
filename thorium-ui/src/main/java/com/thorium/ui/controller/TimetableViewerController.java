package com.thorium.ui.controller;

import com.thorium.application.dto.TimetableDto;
import com.thorium.application.dto.TimetableEntryDto;
import com.thorium.ui.di.AppContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TimetableViewerController {

    @FXML private ComboBox<TimetableDto> timetableCombo;
    @FXML private TableView<TimetableEntryDto> entryTable;
    @FXML private TableColumn<TimetableEntryDto, String> classColumn;
    @FXML private TableColumn<TimetableEntryDto, String> subjectColumn;
    @FXML private TableColumn<TimetableEntryDto, String> teacherColumn;
    @FXML private TableColumn<TimetableEntryDto, String> dayColumn;
    @FXML private TableColumn<TimetableEntryDto, Number> periodColumn;

    @FXML
    private void initialize() {
        classColumn.setCellValueFactory(new PropertyValueFactory<>("classStreamName"));
        subjectColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        teacherColumn.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("periodNumber"));
        refreshTimetables();
        timetableCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, t) -> {
            if (t != null) entryTable.setItems(FXCollections.observableArrayList(t.entries()));
        });
    }

    @FXML
    private void onRefresh() {
        refreshTimetables();
    }

    private void refreshTimetables() {
        var timetables = AppContext.get().generateTimetableUseCase().findAll();
        timetableCombo.setItems(FXCollections.observableArrayList(timetables));
        if (!timetables.isEmpty()) {
            timetableCombo.getSelectionModel().selectLast();
        }
    }
}
