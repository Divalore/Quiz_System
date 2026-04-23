package com.example.quizsystem.controller;

import com.example.quizsystem.service.QuestionService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;

public class CreateExamController {
    @FXML private TextField examTitleField;
    @FXML private TextField questionField;
    @FXML private TextField option1;
    @FXML private TextField option2;
    @FXML private TextField option3;
    @FXML private TextField option4;

    @FXML
    private void handleSave(){
        if(
                examTitleField.getText().isEmpty()||
                        questionField.getText().isEmpty()||
                        option1.getText().isEmpty()||
                        option2.getText().isEmpty()||
                        option3.getText().isEmpty()||
                        option4.getText().isEmpty()||
                        correctAnswerBox.getValue() == null
        ){
            showAlert("Error❌","🚨Please fill all fields!!!");
            return;
        }
        String examTitle = examTitleField.getText();
        String question= questionField.getText();

        String opt1=option1.getText();
        String opt2= option2.getText();
        String opt3=option3.getText();
        String opt4=option4.getText();
        String correct= correctAnswerBox.getValue();

        com.example.quizsystem.model.Question q=
                new com.example.quizsystem.model.Question(examTitle,question,
                        opt1,opt2,opt3,opt4,correct
                );

        QuestionService service= new QuestionService();
        service.saveQuestion(q);
        showAlert("Success✅","Question saved successfully✅");

        examTitleField.clear();
        questionField.clear();
        option1.clear();
        option2.clear();
        option3.clear();
        option4.clear();
        correctAnswerBox.setValue(null);

//        System.out.println("Exam: " + examTitle);
//        System.out.println("Q:"+ question);
//        System.out.println(opt1 +", "+ opt2 +", "+ opt3 +", " + opt4);
//        System.out.println("Correct: "+correct);
//        System.out.println("Saved Successfully!");
    }
    @FXML
    private ComboBox<String> correctAnswerBox;

    @FXML
    public void initialize(){
     correctAnswerBox.getItems().addAll(
             "Option 1",
             "Option 2",
             "Option 3",
             "Option 4"
     );
    }
    private void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
