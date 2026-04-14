package com.example.quizsystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StudentController {

    @FXML
    private void openExam(){
        try{
            FXMLLoader loader= new FXMLLoader(
                    getClass().getResource("/com/example/quizsystem/view/take_exam.fxml")
            );
            Scene scene= new Scene(loader.load(),800,600);
            Stage stage= new Stage();
            stage.setTitle("Take Exam");
            stage.setScene(scene);
            stage.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
