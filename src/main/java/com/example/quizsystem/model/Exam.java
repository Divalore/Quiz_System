package com.example.quizsystem.model;

public class Exam {

    private int id;
    private String className;
    private String subjectName;
    private int durationMinutes;
    private String examCode;
    private boolean isKahoot;

    public Exam(int id, String className, String subjectName,
                int durationMinutes, String examCode, boolean isKahoot) {
        this.id = id;
        this.className = className;
        this.subjectName = subjectName;
        this.durationMinutes = durationMinutes;
        this.examCode = examCode;
        this.isKahoot = isKahoot;
    }

    public int getId() { return id; }
    public String getClassName() { return className; }
    public String getSubjectName() { return subjectName; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getExamCode() { return examCode; }
    public boolean isKahoot() { return isKahoot; }
}
