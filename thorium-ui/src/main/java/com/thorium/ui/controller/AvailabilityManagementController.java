package com.thorium.ui.controller;

import com.thorium.application.dto.PeriodDto;
import com.thorium.application.dto.TeacherAvailabilityDto;
import com.thorium.application.dto.TeacherDto;
import com.thorium.domain.value.DayOfWeek;
import com.thorium.ui.di.AppContext;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Controller for the visual Teacher Availability screen.
 * Replaces list forms with an interactive slot blocking grid (inspired by aSc Timetables)
 * to configure unavailable slots (morning block, evening block, or individual periods).
 */
public class AvailabilityManagementController {

    // FXML Visual Node Injections
    @FXML private ComboBox<TeacherDto> teacherCombo;
    @FXML private HBox quickActionsBox;
    @FXML private Label gridTitleLabel;
    @FXML private Label messageLabel;
    @FXML private GridPane availabilityGrid;

    // Database fields
    private final List<DayOfWeek> days = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    private List<PeriodDto> periods = new ArrayList<>();
    private final Map<String, TeacherAvailabilityDto> currentAvailability = new HashMap<>(); // Key: "DAY|PERIOD_NUMBER"

    // Active Selection Details
    private Long selectedTeacherId = null;
    private String selectedTeacherName = "";

    @FXML
    private void initialize() {
        // Load configured periods
        periods = AppContext.get().periodConfigurationUseCase().findAll();

        // Setup Teacher Combo dropdown
        var teachers = AppContext.get().teacherManagementUseCase().findAll();
        teacherCombo.setItems(FXCollections.observableArrayList(teachers));
        teacherCombo.setCellFactory(lv -> new ListCell<TeacherDto>() {
            @Override
            protected void updateItem(TeacherDto item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name() + " (" + item.code() + ")");
            }
        });

        // Add selection listener
        teacherCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedTeacherId = newVal.id();
                selectedTeacherName = newVal.name();
                quickActionsBox.setDisable(false);
                gridTitleLabel.setText("Availability Grid for " + selectedTeacherName);
                loadAvailabilityData();
            } else {
                clearWorkspace();
            }
        });

        clearWorkspace();
    }

    private void clearWorkspace() {
        selectedTeacherId = null;
        selectedTeacherName = "";
        quickActionsBox.setDisable(true);
        gridTitleLabel.setText("Availability Grid (Please select a teacher)");
        messageLabel.setText("");
        currentAvailability.clear();
        availabilityGrid.getChildren().clear();
    }

    /**
     * Load availability data for the selected teacher and build the lookup map.
     */
    private void loadAvailabilityData() {
        if (selectedTeacherId == null) return;
        try {
            currentAvailability.clear();
            var list = AppContext.get().availabilityManagementUseCase().findByTeacher(selectedTeacherId);
            for (var entry : list) {
                currentAvailability.put(entry.dayOfWeek().name() + "|" + entry.periodNumber(), entry);
            }
            renderGrid();
        } catch (Exception e) {
            showStatus("Failed to load availability: " + e.getMessage(), true);
        }
    }

    /**
     * Render the grid with column headers (days) and row indicators (periods).
     */
    private void renderGrid() {
        availabilityGrid.getChildren().clear();
        availabilityGrid.getColumnConstraints().clear();
        availabilityGrid.getRowConstraints().clear();

        if (selectedTeacherId == null) return;

        // Set column widths
        ColumnConstraints col0 = new ColumnConstraints(120); // Time column
        availabilityGrid.getColumnConstraints().add(col0);
        for (int i = 0; i < 5; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(18); // equally space out day columns
            availabilityGrid.getColumnConstraints().add(col);
        }

        // Draw Column Header: Days of the week
        for (int c = 0; c < days.size(); c++) {
            Label dayLabel = new Label(days.get(c).displayName());
            dayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 13px;");
            StackPane header = new StackPane(dayLabel);
            header.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 8; -fx-alignment: center; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 2 0;");
            availabilityGrid.add(header, c + 1, 0);
        }

        // Draw Rows: Periods
        for (int r = 0; r < periods.size(); r++) {
            PeriodDto period = periods.get(r);
            final int rowNum = r + 1;

            // Row header: Period label
            Label pLabel = new Label(period.label());
            pLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 12px;");
            Label timeLabel = new Label(period.startTime() + " - " + period.endTime());
            timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

            VBox pBox = new VBox(pLabel, timeLabel);
            pBox.setSpacing(2);
            pBox.setPadding(new Insets(6));
            pBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 1 0; -fx-alignment: center;");
            availabilityGrid.add(pBox, 0, rowNum);

            // Period cells
            for (int c = 0; c < days.size(); c++) {
                DayOfWeek day = days.get(c);
                final int periodNum = period.periodNumber();

                StackPane cell = createCellNode(day, periodNum);
                availabilityGrid.add(cell, c + 1, rowNum);
            }
        }
    }

    /**
     * Creates a toggleable availability cell node.
     */
    private StackPane createCellNode(DayOfWeek day, int periodNum) {
        StackPane cell = new StackPane();
        cell.setPrefSize(100, 50);

        String key = day.name() + "|" + periodNum;
        boolean isBlocked = currentAvailability.containsKey(key);

        Label label = new Label();
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        if (isBlocked) {
            // Blocked state (unavailable)
            cell.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #fca5a5; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
            label.setText("✕ Blocked");
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-font-size: 11px;");
        } else {
            // Available state
            cell.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
            label.setText("✓ Available");
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-font-size: 11px;");
        }

        cell.getChildren().add(label);

        // Click to toggle availability
        cell.setOnMouseClicked(event -> {
            try {
                if (isBlocked) {
                    // Unblock (delete availability restriction)
                    TeacherAvailabilityDto entry = currentAvailability.get(key);
                    AppContext.get().availabilityManagementUseCase().delete(entry.id());
                    showStatus("Set " + day.displayName() + " " + periodNum + " to Available", false);
                } else {
                    // Block (save unavailable slot)
                    TeacherAvailabilityDto entry = new TeacherAvailabilityDto(
                            null,
                            selectedTeacherId,
                            selectedTeacherName,
                            day,
                            periodNum,
                            false // available = false
                    );
                    AppContext.get().availabilityManagementUseCase().save(entry);
                    showStatus("Blocked " + day.displayName() + " " + periodNum, false);
                }
                loadAvailabilityData();
            } catch (Exception e) {
                showStatus("Error toggling cell: " + e.getMessage(), true);
            }
        });

        // Subtle hover styling
        cell.setOnMouseEntered(e -> {
            if (isBlocked) {
                cell.setStyle("-fx-background-color: #fecaca; -fx-border-color: #f87171; -fx-border-width: 1.5px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
            } else {
                cell.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #4ade80; -fx-border-width: 1.5px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
            }
        });
        cell.setOnMouseExited(e -> {
            if (isBlocked) {
                cell.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #fca5a5; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
            } else {
                cell.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #bbf7d0; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-cursor: hand;");
            }
        });

        return cell;
    }

    /**
     * Action to block mornings (first two periods) for the selected teacher.
     */
    @FXML
    private void onBlockMornings() {
        if (selectedTeacherId == null) return;
        try {
            boolean changed = false;
            for (DayOfWeek day : days) {
                for (int pNum = 1; pNum <= 2; pNum++) {
                    String key = day.name() + "|" + pNum;
                    if (!currentAvailability.containsKey(key)) {
                        TeacherAvailabilityDto entry = new TeacherAvailabilityDto(
                                null, selectedTeacherId, selectedTeacherName, day, pNum, false
                        );
                        AppContext.get().availabilityManagementUseCase().save(entry);
                        changed = true;
                    }
                }
            }
            if (changed) {
                showStatus("Morning periods blocked for all days", false);
                loadAvailabilityData();
            } else {
                showStatus("Mornings are already blocked", false);
            }
        } catch (Exception e) {
            showStatus("Failed to block mornings: " + e.getMessage(), true);
        }
    }

    /**
     * Action to block evenings (last two periods) for the selected teacher.
     */
    @FXML
    private void onBlockEvenings() {
        if (selectedTeacherId == null || periods.isEmpty()) return;
        try {
            // Find max period number
            int maxPeriod = periods.stream()
                    .mapToInt(PeriodDto::periodNumber)
                    .max()
                    .orElse(8);

            boolean changed = false;
            for (DayOfWeek day : days) {
                for (int pNum = maxPeriod - 1; pNum <= maxPeriod; pNum++) {
                    if (pNum <= 0) continue;
                    String key = day.name() + "|" + pNum;
                    if (!currentAvailability.containsKey(key)) {
                        TeacherAvailabilityDto entry = new TeacherAvailabilityDto(
                                null, selectedTeacherId, selectedTeacherName, day, pNum, false
                        );
                        AppContext.get().availabilityManagementUseCase().save(entry);
                        changed = true;
                    }
                }
            }
            if (changed) {
                showStatus("Evening periods blocked for all days", false);
                loadAvailabilityData();
            } else {
                showStatus("Evenings are already blocked", false);
            }
        } catch (Exception e) {
            showStatus("Failed to block evenings: " + e.getMessage(), true);
        }
    }

    /**
     * Action to clear all availability constraints for the selected teacher.
     */
    @FXML
    private void onClearAllBlocks() {
        if (selectedTeacherId == null) return;
        if (currentAvailability.isEmpty()) {
            showStatus("No availability blocks to clear", false);
            return;
        }
        try {
            for (var entry : currentAvailability.values()) {
                AppContext.get().availabilityManagementUseCase().delete(entry.id());
            }
            showStatus("Cleared all availability blocks", false);
            loadAvailabilityData();
        } catch (Exception e) {
            showStatus("Failed to clear blocks: " + e.getMessage(), true);
        }
    }

    private void showStatus(String msg, boolean error) {
        messageLabel.setText(msg);
        messageLabel.setTextFill(error ? Color.RED : Color.web("#16a34a"));
    }
}
