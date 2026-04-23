package com.example.quizsystem.controller;



import com.example.quizsystem.model.Question;
import com.example.quizsystem.service.ExamService;
import com.example.quizsystem.utils.FileHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.Reader;

import java.io.File;
import java.io.FileReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;


public class TakeExamController {
    @FXML private Label questionLabel;
    @FXML private RadioButton opt1;
    @FXML private RadioButton opt2;
    @FXML private RadioButton opt3;
    @FXML private RadioButton opt4;

    private ExamService examService;
    private ToggleGroup group= new ToggleGroup();
    private List<Question> questions;


    @FXML
    public void initialize(){
        System.out.println("INITIALISE RUNNING...");

        opt1.setToggleGroup(group);
        opt2.setToggleGroup(group);
        opt3.setToggleGroup(group);
        opt4.setToggleGroup(group);

        group.selectedToggleProperty().addListener((obs,oldVal,newVal)->{
            nextBtn.setDisable(newVal==null);
            if (newVal!=null){
                RadioButton selected= (RadioButton) newVal;
                selected.setStyle("-fx-background-color: lightblue;");
            }
        });
        questions= FileHandler.loadQuestions();
        if(questions==null){
            questions=new ArrayList<>();
            }
        examService=new ExamService(questions);

        System.out.println("Loaded questions: "+questions.size());

        if(questions.isEmpty()){
            questionLabel.setText("No Questions available.");
            return;}
        System.out.println("Questions loaded: "+ questions);
        loadQuestion();
        startTimer();

    }
    private void startTimer(){
        timeline= new Timeline(new KeyFrame(Duration.seconds(1),event -> {
            timeLeft--;
            timerLabel.setText("Time: "+ timeLeft);
            if(timeLeft<=10){
                timerLabel.setStyle("-fx-text-fill: red; -fx-font-weight:bold;-fx-font-size:30px;");
            }
            if(timeLeft<=0){
                timeline.stop();
                autoSubmit();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    private void autoSubmit(){
        handleSubmit();
    }

    private String getSelectedOption(){
        RadioButton selected=(RadioButton) group.getSelectedToggle();
        return (selected !=null) ? selected.getText() : null;
    }
    private void loadQuestion(){
        opt1.setSelected(false);
        opt2.setSelected(false);
        opt3.setSelected(false);
        opt4.setSelected(false);
        opt1.setStyle("");
        opt2.setStyle("");
        opt3.setStyle("");
        opt4.setStyle("");
        group.selectToggle(null);
        questionLabel.requestFocus();
        Question q= examService.getCurrentQuestion();
        int current= examService.getCurrentIndex()+1;
        int total = examService.getTotal();
        progressLabel.setText("Question "+ current+ " / "+ total);
        if(q==null){
            questionLabel.setText("No questions available!");
            return;
        }
        questionLabel.setText(q.question);
        opt1.setText(q.option1);
        opt2.setText(q.option2);
        opt3.setText(q.option3);
        opt4.setText(q.option4);
    }

    @FXML
    private void handleNext(){
        String selected= getSelectedOption();
        Question q= examService.getCurrentQuestion();
        examService.checkAnswer(selected, q.correctAnswer);
        examService.nextQuestion();
        group.selectToggle(null);
        if(examService.isFinished()){
            handleSubmit();
        }
        else {
            loadQuestion();
        }

    }
    @FXML
    private void handleSubmit(){
        try{
            Alert alert= new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Exam Finished");
            alert.setHeaderText("Your Result");
            alert.setContentText("Score: "+ examService.getScore()+" / "+ examService.getTotal());
            alert.showAndWait();
            if(timeline!=null){
                timeline.stop();
            }
            FXMLLoader loader= new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/result.fxml"));
            Scene scene= new Scene(loader.load() ,800,600);
            ResultController controller= loader.getController();
            controller.setScore(examService.getScore(),
                    examService.getTotal());
            Stage stage= new Stage();
            stage.setTitle("Result");
            stage.setScene(scene);
            stage.show();

            Stage current= (Stage) questionLabel.getScene().getWindow();
            current.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    private Label timerLabel;
    private int timeLeft=60;
    private Timeline timeline;

    @FXML
    private Label progressLabel;

    @FXML
    private Button nextBtn;
}



