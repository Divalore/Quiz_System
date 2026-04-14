package com.example.quizsystem.utils;

import com.example.quizsystem.model.Question;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.swing.plaf.FileChooserUI;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import java.util.List;

public class FileHandler {
    private static final String FILE_PATH="questions.json";
    public static  void saveQuestion(Question question){
        Gson gson= new Gson();
        List<Question> questions=loadQuestion();
        if(questions==null){
            questions=new ArrayList<>();
        }
        questions.add(question);

        try(Writer writer=new FileWriter(FILE_PATH)){
            gson.toJson(questions,writer);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public static List<Question> loadQuestion(){
        Gson gson= new Gson();
        try(Reader reader= new FileReader(FILE_PATH)){
            Type listType= new TypeToken<List<Question>>(){}.getType();
            return gson.fromJson(reader,listType);
        }catch (Exception e){
            return new ArrayList<>();
        }
    }
}
