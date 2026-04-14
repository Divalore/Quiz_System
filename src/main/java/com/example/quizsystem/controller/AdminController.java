package com.example.quizsystem.controller;

import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class AdminController {
    @FXML
    private  void openCreateExam(){
        try{
            FXMLLoader loader= new FXMLLoader(
                    getClass().getResource("/com/example/quizsystem/view/create_exam.fxml")
            );
            Scene scene= new Scene(loader.load());
            Stage stage= new Stage();
            stage.setTitle("Create Exam");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
