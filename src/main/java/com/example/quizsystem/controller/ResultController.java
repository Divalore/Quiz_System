package com.example.quizsystem.controller;
import javafx.fxml.FXMLLoader;
import  javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import javafx.fxml.FXML;

public class ResultController {
    @FXML
    private Label scoreLabel;

    public void setScore(int score, int total){
        scoreLabel.setText("Your Score: "+score+" / "+ total);
    }
    @FXML
    private void goBack(){
        try{
            FXMLLoader loader= new FXMLLoader(
                    getClass().getResource("/com/example/quizsystem/view/student_dashboard.fxml")
            );
            Stage stage= new Stage();
            stage.setScene(new Scene(loader.load(),800,600));
            stage.setTitle("Student Dashboard");
            stage.show();

            Stage current= (Stage) scoreLabel.getScene().getWindow();
            current.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
