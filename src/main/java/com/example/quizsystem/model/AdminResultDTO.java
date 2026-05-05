package com.example.quizsystem.model;

public class AdminResultDTO {

    private int resultId;
    private String studentName;
    private String className;
    private String examCode;
    private String subject;
    private int score;
    private int totalQuestions;
    private boolean isFullyGraded;

    public AdminResultDTO(int resultId, String studentName, String className,
                          String examCode, String subject, int score,
                          int totalQuestions, boolean isFullyGraded) {
        this.resultId = resultId;
        this.studentName = studentName;
        this.className = className;
        this.examCode = examCode;
        this.subject = subject;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.isFullyGraded = isFullyGraded;
    }

    public int getResultId() { return resultId; }
    public String getStudentName() { return studentName; }
    public String getClassName() { return className; }
    public String getExamCode() { return examCode; }
    public String getSubject() { return subject; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public boolean isFullyGraded() { return isFullyGraded; }

    public String getStatus() {
        return isFullyGraded ? "Graded" : "Pending";
    }

    public String getPercentage() {
        if (totalQuestions == 0) return "0.0%";
        return String.format("%.1f%%", (score * 100.0) / totalQuestions);
    }
}
