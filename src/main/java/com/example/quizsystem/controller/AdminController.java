package com.example.quizsystem.controller;

import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.Node;

import javax.swing.tree.ExpandVetoException;
import javafx.event.ActionEvent;

public class AdminController {
    @FXML
    private  void openCreateExam(ActionEvent event){
        try{
            FXMLLoader loader= new FXMLLoader(
                    getClass().getResource("/com/example/quizsystem/view/create_exam.fxml")
            );
            Scene scene= new Scene(loader.load());
            Stage stage= (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Create Exam");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @FXML
    private void openManageQuestions(ActionEvent event){
        System.out.println("Clicked!");
        try{
            FXMLLoader  loader= new FXMLLoader(getClass().
                    getResource("/com/example/quizsystem/view/manage_questions.fxml"));
            Scene scene= new Scene(loader.load());
            Stage stage= (Stage) ((Node) event.getSource()).getScene().getWindow();;
            stage.setScene(scene);
            stage.setTitle("Manage Questions");

        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Error Loading FXML!");
        }
//        Stage current= (Stage) ((Node) event.getSource()).getScene().getWindow();
//        current.close();
    }
    @FXML
    private void openResults(ActionEvent event){
        try{
            FXMLLoader loader= new FXMLLoader(
                    getClass().getResource("/com/example/quizsystem/view/result.fxml"));
                    Scene scene= new Scene(loader.load());
                    Stage stage= (Stage)((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("Results");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @FXML
    private void logout(ActionEvent event){
        try{
            FXMLLoader loader= new FXMLLoader(getClass().
                    getResource("/com/example/quizsystem/view/logout.fxml"));
            Scene scene= new Scene(loader.load());
            Stage stage= (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
