package com.example.quizsystem.service;

import  java.util.List;
import com.example.quizsystem.model.Question;

public class ExamService {
    private List<Question> questions;
    private int currentIndex = 0;
    private  int score = 0;

    public ExamService(List<Question>questions){
        this.questions=questions;
    }
    public Question getCurrentQuestion(){
        if(questions==null || currentIndex>= questions.size()){
            return null;
        }
        return questions.get(currentIndex);
    }
    public void nextQuestion(){
        currentIndex++;
    }
    public boolean isFinished(){
        return  currentIndex>=questions.size();
    }
    public void checkAnswer(String selected, String correct){
        if(selected!=null && selected.equals(correct)){
            score++;
        }
    }
    public int getScore(){
        return score;
    }
    public int getTotal(){
        return questions.size();
    }
    public int getCurrentIndex(){
        return currentIndex;
    }
}
