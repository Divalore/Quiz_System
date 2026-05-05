package com.example.quizsystem.controller;

import com.example.quizsystem.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AdminGradingController {

    @FXML private ListView<String> pendingListView;
    @FXML private VBox gradingDetailPane;
    @FXML private VBox noSelectionPane;
    @FXML private Label questionLabel;
    @FXML private Label answerLabel;
    @FXML private TextField pointsField;
    @FXML private Label statusLabel;

    private List<PendingAnswer> pendingAnswers = new ArrayList<>();
    private PendingAnswer selectedAnswer;

    @FXML
    public void initialize() {
        refreshList();
        pendingListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            if (idx >= 0 && idx < pendingAnswers.size()) {
                selectedAnswer = pendingAnswers.get(idx);
                showDetail();
            }
        });
    }

    @FXML
    void refreshList() {
        pendingAnswers.clear();
        ObservableList<String> items = FXCollections.observableArrayList();

        String sql = "SELECT sa.id, sa.resultId, sa.studentId, sa.examId, " +
                     "u.firstName, u.lastName, e.subjectName, q.content, sa.answerText, q.points AS maxPoints " +
                     "FROM student_answers sa " +
                     "JOIN users u ON sa.studentId = u.id " +
                     "JOIN exams e ON sa.examId = e.id " +
                     "JOIN questions q ON sa.questionId = q.id " +
                     "WHERE sa.isGraded = 0";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("firstName") + " " + rs.getString("lastName");
                String examName = rs.getString("subjectName");

                PendingAnswer pa = new PendingAnswer(
                        rs.getInt("id"),
                        rs.getInt("resultId"),
                        rs.getInt("studentId"),
                        rs.getInt("examId"),
                        name, examName,
                        rs.getString("content"),
                        rs.getString("answerText"),
                        rs.getInt("maxPoints")
                );

                pendingAnswers.add(pa);
                items.add(name + " - " + examName + " (Max: " + pa.maxPoints + " pts)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        pendingListView.setItems(items);
        gradingDetailPane.setVisible(false);
        noSelectionPane.setVisible(true);
    }

    private void showDetail() {
        gradingDetailPane.setVisible(true);
        noSelectionPane.setVisible(false);
        questionLabel.setText(selectedAnswer.questionContent + " (Maximum Points: " + selectedAnswer.maxPoints + ")");
        answerLabel.setText(selectedAnswer.answerText);
        pointsField.setPromptText("Enter 0–" + selectedAnswer.maxPoints);
        pointsField.clear();
        statusLabel.setText("");
    }

    @FXML
    void submitGrade(ActionEvent event) {
        if (selectedAnswer == null) return;

        String pointsStr = pointsField.getText().trim();
        if (pointsStr.isEmpty()) {
            showStatus("Please enter points.", false);
            return;
        }

        int points;
        try {
            points = Integer.parseInt(pointsStr);
        } catch (NumberFormatException e) {
            showStatus("Invalid points format.", false);
            return;
        }

        if (points < 0 || points > selectedAnswer.maxPoints) {
            showStatus("Points must be between 0 and " + selectedAnswer.maxPoints + ".", false);
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            markAnswerGraded(conn, selectedAnswer.id, points);
            updateResultScore(conn, points);

            if (areAllAnswersGraded(conn)) {
                markResultFullyGraded(conn);
            }

            conn.commit();
            showStatus("Grade submitted successfully!", true);
            refreshList();

        } catch (Exception e) {
            e.printStackTrace();
            showStatus("A database error occurred.", false);
        }
    }

    private void markAnswerGraded(Connection conn, int answerId, int points) throws Exception {
        String sql = "UPDATE student_answers SET isGraded = 1, pointsEarned = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, points);
            stmt.setInt(2, answerId);
            stmt.executeUpdate();
        }
    }

    private void updateResultScore(Connection conn, int points) throws Exception {
        String sql;
        if (selectedAnswer.resultId > 0) {
            sql = "UPDATE results SET score = score + ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, points);
                stmt.setInt(2, selectedAnswer.resultId);
                stmt.executeUpdate();
            }
        } else {
            // Fallback for legacy data: find the most recent result for this student/exam
            sql = "UPDATE results SET score = score + ? WHERE id = " +
                  "(SELECT id FROM results WHERE studentId = ? AND examId = ? ORDER BY timestamp DESC LIMIT 1)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, points);
                stmt.setInt(2, selectedAnswer.studentId);
                stmt.setInt(3, selectedAnswer.examId);
                stmt.executeUpdate();
            }
        }
    }

    private boolean areAllAnswersGraded(Connection conn) throws Exception {
        String sql;
        if (selectedAnswer.resultId > 0) {
            sql = "SELECT COUNT(*) FROM student_answers WHERE resultId = ? AND isGraded = 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedAnswer.resultId);
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) == 0;
            }
        } else {
            sql = "SELECT COUNT(*) FROM student_answers WHERE studentId = ? AND examId = ? AND isGraded = 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedAnswer.studentId);
                stmt.setInt(2, selectedAnswer.examId);
                ResultSet rs = stmt.executeQuery();
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }

    private void markResultFullyGraded(Connection conn) throws Exception {
        String sql;
        if (selectedAnswer.resultId > 0) {
            sql = "UPDATE results SET isFullyGraded = 1 WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedAnswer.resultId);
                stmt.executeUpdate();
            }
        } else {
            sql = "UPDATE results SET isFullyGraded = 1 WHERE id = " +
                  "(SELECT id FROM results WHERE studentId = ? AND examId = ? ORDER BY timestamp DESC LIMIT 1)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selectedAnswer.studentId);
                stmt.setInt(2, selectedAnswer.examId);
                stmt.executeUpdate();
            }
        }
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success ? "-fx-text-fill: #10b981" : "-fx-text-fill: #ef4444");
    }

    // ─── Inner Data Class ──────────────────────────────────────────────────────

    private static class PendingAnswer {
        int id, resultId, studentId, examId;
        String studentName, examName, questionContent, answerText;
        int maxPoints;

        PendingAnswer(int id, int resultId, int studentId, int examId,
                      String studentName, String examName,
                      String questionContent, String answerText, int maxPoints) {
            this.id = id;
            this.resultId = resultId;
            this.studentId = studentId;
            this.examId = examId;
            this.studentName = studentName;
            this.examName = examName;
            this.questionContent = questionContent;
            this.answerText = answerText;
            this.maxPoints = maxPoints;
        }
    }
}
