package com.thorium.ui.controller;

import com.thorium.application.dto.ClassStreamDto;
import com.thorium.ui.di.AppContext;
import com.thorium.ui.util.IconUtil;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClassManagementController {

    @FXML private TableView<ClassStreamDto> classTable;
    @FXML private TextField searchField;
    @FXML private TableColumn<ClassStreamDto, String> codeColumn;
    @FXML private TableColumn<ClassStreamDto, Number> formColumn;
    @FXML private TableColumn<ClassStreamDto, String> streamColumn;
    @FXML private TableColumn<ClassStreamDto, String> displayColumn;
    @FXML private TextField codeField;
    @FXML private Spinner<Integer> formSpinner;
    @FXML private TextField streamField;
    @FXML private TextField displayField;
    @FXML private Label messageLabel;
    @FXML private Button saveBtn;
    @FXML private Button deleteBtn;
    @FXML private Button clearBtn;

    private Long editingId;
    private final javafx.collections.ObservableList<ClassStreamDto> masterData = FXCollections.observableArrayList();
    private FilteredList<ClassStreamDto> filteredItems;

    @FXML
    private void initialize() {
        IconUtil.addIcon(saveBtn, IconUtil.SAVE, "#16a34a");
        IconUtil.addIcon(deleteBtn, IconUtil.DELETE, "#dc2626");
        IconUtil.addIcon(clearBtn, IconUtil.CLEAR, "#64748b");
        codeColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().code()));
        formColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().form()));
        streamColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().stream()));
        displayColumn.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().displayName()));
        formSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 4, 1));
        filteredItems = new FilteredList<>(masterData, p -> true);
        classTable.setItems(filteredItems);
        refreshTable();
        classTable.getSelectionModel().selectedItemProperty().addListener((obs, o, s) -> {
            if (s != null) populateForm(s);
        });
        searchField.textProperty().addListener((obs, old, search) -> {
            filteredItems.setPredicate(dto -> {
                if (search == null || search.isBlank()) return true;
                String q = search.toLowerCase();
                return (dto.displayName() != null && dto.displayName().toLowerCase().contains(q))
                    || (dto.code() != null && dto.code().toLowerCase().contains(q))
                    || (dto.stream() != null && dto.stream().toLowerCase().contains(q));
            });
        });
    }

    @FXML private void onSave() {
        try {
            ClassStreamDto dto = new ClassStreamDto(editingId, codeField.getText().trim(),
                    formSpinner.getValue(), streamField.getText().trim(), displayField.getText().trim());
            if (editingId == null) AppContext.get().classStreamManagementUseCase().create(dto);
            else AppContext.get().classStreamManagementUseCase().update(dto);
            clearForm(); refreshTable(); showMessage("Saved", false);
        } catch (IllegalArgumentException | IllegalStateException e) { showMessage(e.getMessage(), true); }
        catch (Exception e) { showMessage("An unexpected error occurred", true); }
    }

    @FXML private void onDelete() {
        ClassStreamDto selected = classTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Select a class", true); return; }
        try {
            AppContext.get().classStreamManagementUseCase().delete(selected.id());
            clearForm(); refreshTable(); showMessage("Deleted", false);
        } catch (IllegalArgumentException | IllegalStateException e) { showMessage(e.getMessage(), true); }
        catch (Exception e) { showMessage("An unexpected error occurred", true); }
    }

    @FXML private void onClear() { clearForm(); }

    private void refreshTable() {
        masterData.setAll(AppContext.get().classStreamManagementUseCase().findAll());
    }

    private void populateForm(ClassStreamDto dto) {
        editingId = dto.id();
        codeField.setText(dto.code());
        formSpinner.getValueFactory().setValue(dto.form());
        streamField.setText(dto.stream());
        displayField.setText(dto.displayName());
    }

    private void clearForm() {
        editingId = null; codeField.clear(); formSpinner.getValueFactory().setValue(1);
        streamField.clear(); displayField.clear(); classTable.getSelectionModel().clearSelection();
    }

    private void showMessage(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.getStyleClass().removeAll("error", "success");
        messageLabel.getStyleClass().add(error ? "error" : "success");
    }
}
