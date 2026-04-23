package com.example.quizsystem.controller;

import com.example.quizsystem.model.Question;
import com.example.quizsystem.utils.FileHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.util.List;

public class ManageQuestionsController {

    @FXML
    private ListView<String> questionList;
    private List<Question> questions;
    @FXML
    public void initialize(){
        questions= FileHandler.loadQuestions();

        if(questions !=null){
            for( Question q: questions){
                questionList.getItems().add(q.question);
            }
        }
    }
    @FXML
    private void handleDelete(){
        int index= questionList.getSelectionModel().getSelectedIndex();
        if( index==-1){
            showAlert("Error", "Select a question first!");
            return;
        }
        questions.remove(index);
        FileHandler.saveAllQuestions(questions);
        questionList.getItems().remove(index);
        showAlert("Success!","Question Deleted!");
    }

    @FXML
    private void handleEdit(ActionEvent event){
        int index= questionList.getSelectionModel().getSelectedIndex();

        if(index==-1){
            showAlert("Error", "Select a question first!");
            return;
        }
        try{
            FXMLLoader loader= new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/edit_questions.fxml"));
            Scene scene= new Scene(loader.load());
            EditQuestionController controller= loader.getController();
            controller.setData(questions.get(index), questions, index);
            Stage stage=(Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
    @FXML
    private void handleBack(){
        Stage stage=(Stage) questionList.getScene().getWindow();
        stage.close();
    }
    private void showAlert(String title, String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
