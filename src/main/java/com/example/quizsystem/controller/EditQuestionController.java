package com.example.quizsystem.controller;

import com.example.quizsystem.model.Question;
import com.example.quizsystem.utils.FileHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.util.List;

public class EditQuestionController {
    @FXML private TextField questionField;
    @FXML private TextField opt1;
    @FXML private TextField opt2;
    @FXML private TextField opt3;
    @FXML private TextField opt4;
    @FXML private ComboBox<String> correctAnswerBox;

    private List<Question> questions;
    private  int index;

    @FXML
    public void initialize(){
        correctAnswerBox.getItems().addAll(
                "Option 1",
                "Option 2",
                "Option 3",
                "Option 4"
        );
    }

    public void setData(Question q, List<Question> questions, int index){
        this.questions=questions;
        this.index= index;
        questionField.setText(q.question);
        opt1.setText(q.option1);
        opt2.setText(q.option2);
        opt3.setText(q.option3);
        opt4.setText(q.option4);
        correctAnswerBox.setValue(q.correctAnswer);

    }

    @FXML
    private void handleUpdate(ActionEvent event){
        if(questionField.getText().isEmpty()||
        opt1.getText().isEmpty()||
        opt2.getText().isEmpty()||
        opt3.getText().isEmpty()||
        opt4.getText().isEmpty()||
        correctAnswerBox.getValue()==null){
            showAlert("Error", "Fill all Fields!!");
            return;
        }
        Question updated = new Question("",
                questionField.getText(),
                opt1.getText(),
                opt2.getText(),
                opt3.getText(),
                opt4.getText(),
                correctAnswerBox.getValue());
        questions.set(index,updated);
        FileHandler.saveAllQuestions(questions);
        showAlert("Success","Question Updated✅");
        goBack(event);
    }

    @FXML
    private void handleBack(ActionEvent event){
        goBack(event);
    }
    private void goBack(ActionEvent event){
        try{
            FXMLLoader loader= new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/manage_questions.fxml"));
            Scene scene= new Scene(loader.load());
            Stage stage=(Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(scene);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String message){
        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

























}
