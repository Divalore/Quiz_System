package com.example.quizsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class CreateExamController {
    @FXML private TextField examTitleField;
    @FXML private TextField questionField;
    @FXML private TextField option1;
    @FXML private TextField option2;
    @FXML private TextField option3;
    @FXML private TextField option4;
    @FXML private TextField correctAnswer;

    @FXML
    private void handleSave(){
        String examTitle = examTitleField.getText();
        String question= questionField.getText();

        String opt1=option1.getText();
        String opt2= option2.getText();
        String opt3=option3.getText();
        String opt4=option4.getText();
        String correct= correctAnswer.getText();

        com.example.quizsystem.model.Question q=
                new com.example.quizsystem.model.Question(examTitle,question,
                        opt1,opt2,opt3,opt4,correct
                );

        com.example.quizsystem.utils.FileHandler.saveQuestion(q);
        System.out.println("Saved to file successfully!");
//        System.out.println("Exam: " + examTitle);
//        System.out.println("Q:"+ question);
//        System.out.println(opt1 +", "+ opt2 +", "+ opt3 +", " + opt4);
//        System.out.println("Correct: "+correct);
//        System.out.println("Saved Successfully!");
    }
}
