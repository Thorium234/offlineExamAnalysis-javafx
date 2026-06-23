package com.thorium.ui.controller;

import com.thorium.application.dto.SubjectDto;
import com.thorium.ui.di.AppContext;
import com.thorium.ui.util.IconUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class SubjectManagementController {

    @FXML private TableView<SubjectDto> subjectTable;
    @FXML private TextField searchField;
    @FXML private TableColumn<SubjectDto, String> codeColumn;
    @FXML private TableColumn<SubjectDto, String> nameColumn;
    @FXML private TableColumn<SubjectDto, Boolean> examinableColumn;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private CheckBox examinableCheck;
    @FXML private Spinner<Integer> cbcLessonsSpinner;
    @FXML private CheckBox doublePeriodCheck;
    @FXML private CheckBox requiresDoubleCheck;
    @FXML private ColorPicker colorPicker;
    @FXML private Label messageLabel;
    @FXML private Button saveBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;

    private Long editingId;
    private final javafx.collections.ObservableList<SubjectDto> masterData = FXCollections.observableArrayList();
    private FilteredList<SubjectDto> filteredItems;

    @FXML
    private void initialize() {
        IconUtil.addIcon(saveBtn, IconUtil.SAVE, "#16a34a");
        IconUtil.addIcon(deleteBtn, IconUtil.DELETE, "#dc2626");
        IconUtil.addIcon(clearBtn, IconUtil.CLEAR, "#64748b");
        codeColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().code()));
        nameColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().name()));
        examinableColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().examinable()));
        cbcLessonsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
        filteredItems = new FilteredList<>(masterData, p -> true);
        subjectTable.setItems(filteredItems);
        refreshTable();
        subjectTable.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            if (s != null) populateForm(s);
        });
        searchField.textProperty().addListener((obs, old, search) -> {
            filteredItems.setPredicate(dto -> search == null || search.isBlank()
                    || dto.name().toLowerCase().contains(search.toLowerCase())
                    || dto.code().toLowerCase().contains(search.toLowerCase()));
        });
    }

    @FXML private void onSave() {
        try {
            String colorHex = colorPicker.getValue() != null
                    ? "#" + colorPicker.getValue().toString().substring(2, 8)
                    : null;
            SubjectDto dto = new SubjectDto(editingId, codeField.getText().trim(), nameField.getText().trim(),
                    examinableCheck.isSelected(), cbcLessonsSpinner.getValue(), doublePeriodCheck.isSelected(),
                    requiresDoubleCheck.isSelected(), colorHex);
            if (editingId == null) AppContext.get().subjectManagementUseCase().create(dto);
            else AppContext.get().subjectManagementUseCase().update(dto);
            clearForm(); refreshTable(); showMessage("Saved", false);
        } catch (IllegalArgumentException | IllegalStateException e) { showMessage(e.getMessage(), true); }
        catch (Exception e) { showMessage("An unexpected error occurred", true); }
    }

    @FXML private void onDelete() {
        SubjectDto selected = subjectTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Select a subject", true); return; }
        try {
            AppContext.get().subjectManagementUseCase().delete(selected.id());
            clearForm(); refreshTable(); showMessage("Deleted", false);
        } catch (IllegalArgumentException | IllegalStateException e) { showMessage(e.getMessage(), true); }
        catch (Exception e) { showMessage("An unexpected error occurred", true); }
    }

    @FXML private void onClear() { clearForm(); }

    private void refreshTable() {
        masterData.setAll(AppContext.get().subjectManagementUseCase().findAll());
    }

    private void populateForm(SubjectDto dto) {
        editingId = dto.id();
        codeField.setText(dto.code()); nameField.setText(dto.name());
        examinableCheck.setSelected(dto.examinable());
        cbcLessonsSpinner.getValueFactory().setValue(dto.cbcDefaultLessons());
        doublePeriodCheck.setSelected(dto.allowsDoublePeriod());
        requiresDoubleCheck.setSelected(dto.requiresDoublePeriod());
        if (dto.color() != null && !dto.color().isBlank()) {
            colorPicker.setValue(Color.web(dto.color()));
        }
    }

    private void clearForm() {
        editingId = null; codeField.clear(); nameField.clear();
        examinableCheck.setSelected(false); cbcLessonsSpinner.getValueFactory().setValue(5);
        doublePeriodCheck.setSelected(false); requiresDoubleCheck.setSelected(false);
        colorPicker.setValue(null);
        subjectTable.getSelectionModel().clearSelection();
    }

    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("error", "success");
        messageLabel.getStyleClass().add(error ? "error" : "success");
    }
}
