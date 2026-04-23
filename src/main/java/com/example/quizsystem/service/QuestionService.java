package com.example.quizsystem.service;
import com.example.quizsystem.model.Question;
import com.example.quizsystem.utils.FileHandler;
import java.util.List;

public class QuestionService {
    public void saveQuestion(Question q){
        FileHandler.saveQuestion(q);
    }
    public List<Question> getAllQuestions(){
        return FileHandler.loadQuestions();
    }
}
