package com.example.quizsystem.controller;


import com.almasb.fxgl.quest.Quest;
import com.example.quizsystem.model.Question;
import com.example.quizsystem.utils.FileHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.Reader;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TakeExamController {
    @FXML private Label questionLabel;
    @FXML private RadioButton opt1;
    @FXML private RadioButton opt2;
    @FXML private RadioButton opt3;
    @FXML private RadioButton opt4;

    private ToggleGroup group= new ToggleGroup();
    private List<Question> questions;
    private int currentIndex=0;
    private int score=0;

    @FXML
    public void initialize(){
        System.out.println("INITIALISE RUNNING...");

        opt1.setToggleGroup(group);
        opt2.setToggleGroup(group);
        opt3.setToggleGroup(group);
        opt4.setToggleGroup(group);

        questions= FileHandler.loadQuestion();
        if(questions==null){
            questions=new ArrayList<>();
            }

        System.out.println("Loaded questions: "+questions.size());

        if(questions.isEmpty()){
            questionLabel.setText("No Questions available.");
            return;}
        System.out.println("Questions loaded: "+ questions);
        loadQuestion();

    }
//    public static List<Question>loadQuestion(){
//        try {
//            File file = new File("questions.json");
//            System.out.println("Path: "+file.getAbsolutePath());
//            if (!file.exists()) {
//                System.out.println("FILE NOT FOUND!");
//                return new ArrayList<>();
//            }
//            Gson gson= new Gson();
//            Reader reader= new FileReader(file);
//            List<Question>list=gson.fromJson(reader,
//                    new TypeToken<List<Question>>(){}.getType());
//            reader.close();
//
//            if(list==null){
//                return  new ArrayList<>();
//            }
//            return list;
//        }catch (Exception e){
//        e.printStackTrace();
//        return new ArrayList<>();}
//    }
    private void loadQuestion(){
        if(questions==null || questions.isEmpty()){
            questionLabel.setText("No Questions available.");
            return;
        }
        if(currentIndex>= questions.size()){
          questionLabel.setText("End of Questions");
          return;
        }

        Question q= questions.get(currentIndex);
        questionLabel.setText(q.question);
        opt1.setText(q.option1);
        opt2.setText(q.option2);
        opt3.setText(q.option3);
        opt4.setText(q.option4);
    }

    @FXML
    private void handleNext(){
        if(currentIndex>=questions.size()) return;
        RadioButton selected = (RadioButton) group.getSelectedToggle();

        if(selected !=null){
            String answer= selected.getText();
            if(answer.equals(questions.get(currentIndex).correctAnswer)){
                score++;
            }
        }
        currentIndex++;
        group.selectToggle(null);

        if(currentIndex<questions.size()){
            loadQuestion();
        } else{
            questionLabel.setText("End of Questions");
        }
    }
    @FXML
    private void handleSubmit(){
        questionLabel.setText("Your Score: "+ score+ "/"+ questions.size());
    }
}
