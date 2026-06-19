package com.thorium.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SettingsController {

    @FXML private Label dbPathLabel;

    @FXML
    private void initialize() {
        dbPathLabel.setText(System.getProperty("user.home") + "/.thorium/timetable.db");
    }
}
