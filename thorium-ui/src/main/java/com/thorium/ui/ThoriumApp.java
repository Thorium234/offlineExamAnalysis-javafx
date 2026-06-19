package com.thorium.ui;

import com.thorium.ui.di.AppContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ThoriumApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Path dbPath = Paths.get(System.getProperty("user.home"), ".thorium", "timetable.db");
        dbPath.getParent().toFile().mkdirs();
        AppContext.initialize(dbPath);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        stage.setTitle("Thorium Timetable Generator");
        stage.setScene(scene);
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
