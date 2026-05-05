package com.example.quizsystem.model;

public class Question {

    private int id;
    private int examId;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String questionType;
    private int points;

    public Question(int id, int examId, String content,
                    String optionA, String optionB, String optionC, String optionD,
                    String correctAnswer, String questionType, int points) {
        this.id = id;
        this.examId = examId;
        this.content = content;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
        this.questionType = questionType;
        this.points = points;
    }

    public int getId() { return id; }
    public int getExamId() { return examId; }
    public String getContent() { return content; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectAnswer() { return correctAnswer; }
    public String getQuestionType() { return questionType; }
    public int getPoints() { return points; }
}
