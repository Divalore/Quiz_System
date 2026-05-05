package com.example.quizsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.example.quizsystem.utils.DatabaseManager;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseManager.initializeDatabase();

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/com/example/quizsystem/view/login.fxml")
        );

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm()
        );

        stage.setTitle("Exam System");
        stage.setScene(scene);
        stage.setWidth(700);
        stage.setHeight(550);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
