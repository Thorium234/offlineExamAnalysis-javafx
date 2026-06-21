package com.thorium.ui.controller;

import com.thorium.application.dto.TimetableDto;
import com.thorium.application.dto.TimetableEntryDto;
import com.thorium.ui.di.AppContext;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
        classColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().classStreamName()));
        subjectColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().subjectName()));
        teacherColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().teacherName()));
        dayColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().dayOfWeek().displayName()));
        periodColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().periodNumber()));
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
        timetableCombo.setCellFactory(lv -> new ListCell<TimetableDto>() {
            @Override protected void updateItem(TimetableDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (" + item.status() + ")");
            }
        });
        if (!timetables.isEmpty()) {
            timetableCombo.getSelectionModel().selectLast();
        }
    }
}
