package com.example.quizsystem.controller;

import com.example.quizsystem.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class AdminAnalyticsController {

    @FXML private Label totalExamsLabel;
    @FXML private Label averageScoreLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private ComboBox<String> classFilter;
    @FXML private ComboBox<String> subjectFilter;
    @FXML private ComboBox<String> studentFilter;
    @FXML private PieChart performancePieChart;
    @FXML private BarChart<String, Number> subjectBarChart;

    private Map<String, Integer> studentIdMap = new HashMap<>();

    @FXML
    public void initialize() {
        setupFilters();
        refreshAnalytics();
    }

    private void setupFilters() {
        TreeSet<String> classes = new TreeSet<>();
        TreeSet<String> subjects = new TreeSet<>();
        classes.add("All Classes");
        subjects.add("All Subjects");

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT className FROM users WHERE role = 'STUDENT' AND className IS NOT NULL AND className != ''");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) classes.add(rs.getString(1));
            }
            try (PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT subjectName FROM exams");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) subjects.add(rs.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        classFilter.setItems(FXCollections.observableArrayList(classes));
        classFilter.setValue("All Classes");
        subjectFilter.setItems(FXCollections.observableArrayList(subjects));
        subjectFilter.setValue("All Subjects");

        classFilter.setOnAction(e -> {
            updateStudentList(classFilter.getValue());
            refreshAnalytics();
        });
        subjectFilter.setOnAction(e -> refreshAnalytics());
        studentFilter.setOnAction(e -> refreshAnalytics());

        updateStudentList("All Classes");
    }

    private void updateStudentList(String classVal) {
        studentIdMap.clear();
        List<String> names = new ArrayList<>();
        names.add("All Students");

        String sql = "SELECT id, firstName, lastName FROM users WHERE role = 'STUDENT'";
        if (classVal != null && !"All Classes".equals(classVal)) {
            sql += " AND className = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (classVal != null && !"All Classes".equals(classVal)) {
                stmt.setString(1, classVal);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("firstName") + " " + rs.getString("lastName");
                    studentIdMap.put(name, rs.getInt("id"));
                    names.add(name);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        studentFilter.setItems(FXCollections.observableArrayList(names));
        studentFilter.setValue("All Students");
    }

    private void refreshAnalytics() {
        String classVal = classFilter.getValue();
        String subjectVal = subjectFilter.getValue();
        String studentName = studentFilter.getValue();
        Integer studentId = (studentName == null || "All Students".equals(studentName))
                ? null : studentIdMap.get(studentName);

        loadSummaryStats(classVal, subjectVal, studentId);
        loadPerformancePieChart(classVal, subjectVal, studentId);
        loadSubjectBarChart(classVal, subjectVal, studentId);
    }

    private void loadSummaryStats(String classVal, String subjectVal, Integer studentId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            
            String examSql = buildQuery(
                    "SELECT COUNT(*) FROM results r JOIN users u ON r.studentId = u.id JOIN exams e ON r.examId = e.id WHERE 1=1",
                    classVal, subjectVal, studentId
            );
            try (PreparedStatement stmt = buildStatement(conn, examSql, classVal, subjectVal, studentId);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) totalExamsLabel.setText(String.valueOf(rs.getInt(1)));
            }

            String studentSql = "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'";
            if (!"All Classes".equals(classVal)) studentSql += " AND className = ?";
            if (studentId != null) studentSql += " AND id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(studentSql)) {
                int idx = 1;
                if (!"All Classes".equals(classVal)) stmt.setString(idx++, classVal);
                if (studentId != null) stmt.setInt(idx, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) totalStudentsLabel.setText(String.valueOf(rs.getInt(1)));
                }
            }

            String avgSql = buildQuery(
                    "SELECT AVG(CAST(r.score AS DOUBLE) / r.totalQuestions * 100) " +
                    "FROM results r JOIN users u ON r.studentId = u.id JOIN exams e ON r.examId = e.id " +
                    "WHERE r.totalQuestions > 0",
                    classVal, subjectVal, studentId
            );
            try (PreparedStatement stmt = buildStatement(conn, avgSql, classVal, subjectVal, studentId);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) averageScoreLabel.setText(String.format("%.1f%%", rs.getDouble(1)));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPerformancePieChart(String classVal, String subjectVal, Integer studentId) {
        int fail = 0, pass = 0, excellent = 0;

        String sql = buildQuery(
                "SELECT r.score, r.totalQuestions FROM results r " +
                "JOIN users u ON r.studentId = u.id JOIN exams e ON r.examId = e.id WHERE r.totalQuestions > 0",
                classVal, subjectVal, studentId
        );

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = buildStatement(conn, sql, classVal, subjectVal, studentId);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                double pct = (rs.getInt("score") * 100.0) / rs.getInt("totalQuestions");
                if (pct < 40) fail++;
                else if (pct <= 70) pass++;
                else excellent++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        performancePieChart.getData().clear();
        performancePieChart.getData().add(new PieChart.Data("Fail (<40%)", fail));
        performancePieChart.getData().add(new PieChart.Data("Pass (40–70%)", pass));
        performancePieChart.getData().add(new PieChart.Data("Excellent (>70%)", excellent));
    }

    private void loadSubjectBarChart(String classVal, String subjectVal, Integer studentId) {
        Map<String, Double> sumBySubject = new HashMap<>();
        Map<String, Integer> countBySubject = new HashMap<>();

        String sql = buildQuery(
                "SELECT e.subjectName, r.score, r.totalQuestions FROM results r " +
                "JOIN exams e ON r.examId = e.id JOIN users u ON r.studentId = u.id WHERE r.totalQuestions > 0",
                classVal, subjectVal, studentId
        );

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = buildStatement(conn, sql, classVal, subjectVal, studentId);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String subject = rs.getString("subjectName");
                double pct = (rs.getInt("score") * 100.0) / rs.getInt("totalQuestions");
                sumBySubject.merge(subject, pct, Double::sum);
                countBySubject.merge(subject, 1, Integer::sum);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Average Score");
        for (String subject : sumBySubject.keySet()) {
            double avg = sumBySubject.get(subject) / countBySubject.get(subject);
            series.getData().add(new XYChart.Data<>(subject, avg));
        }

        subjectBarChart.getData().clear();
        subjectBarChart.getData().add(series);
    }

    private String buildQuery(String base, String classVal, String subjectVal, Integer studentId) {
        StringBuilder sb = new StringBuilder(base);
        if (!"All Classes".equals(classVal)) sb.append(" AND u.className = ?");
        if (!"All Subjects".equals(subjectVal)) sb.append(" AND e.subjectName = ?");
        if (studentId != null) sb.append(" AND r.studentId = ?");
        return sb.toString();
    }

    private PreparedStatement buildStatement(Connection conn, String sql, String classVal, String subjectVal, Integer studentId) throws Exception {
        PreparedStatement stmt = conn.prepareStatement(sql);
        int idx = 1;
        if (!"All Classes".equals(classVal)) stmt.setString(idx++, classVal);
        if (!"All Subjects".equals(subjectVal)) stmt.setString(idx++, subjectVal);
        if (studentId != null) stmt.setInt(idx, studentId);
        return stmt;
    }
}
