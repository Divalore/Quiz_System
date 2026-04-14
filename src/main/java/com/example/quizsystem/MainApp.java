package com.example.quizsystem;

import javafx.scene.Scene;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws  Exception{
        FXMLLoader loader= new FXMLLoader(
                MainApp.class.getResource(
                        "/com/example/quizsystem/view/login.fxml"));

        Scene scene= new Scene(loader.load(),600,400);
        stage.setTitle("Exam System");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[]args){
        launch();
    }



}
