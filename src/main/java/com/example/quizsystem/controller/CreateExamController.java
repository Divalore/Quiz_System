package com.example.quizsystem.controller;

import com.example.quizsystem.utils.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class CreateExamController {

    @FXML private TextField classNameField;
    @FXML private TextField subjectNameField;
    @FXML private TextField durationField;
    @FXML private CheckBox kahootModeCheckBox;
    @FXML private Label statusLabel;

    @FXML
    void handleSaveExam(ActionEvent event) {
        String className = classNameField.getText().trim();
        String subjectName = subjectNameField.getText().trim();
        String durationStr = durationField.getText().trim();

        if (className.isEmpty() || subjectName.isEmpty() || durationStr.isEmpty()) {
            showError("Please fill all fields.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            showError("Duration must be a valid number.");
            return;
        }

        String examCode = generateExamCode();
        String sql = "INSERT INTO exams (className, subjectName, durationMinutes, examCode, isKahoot) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, className);
            stmt.setString(2, subjectName);
            stmt.setInt(3, duration);
            stmt.setString(4, examCode);
            stmt.setInt(5, kahootModeCheckBox.isSelected() ? 1 : 0);
            stmt.executeUpdate();

            showSuccess("Exam created! Code: " + examCode);
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error: Could not create exam. The code might already exist.");
        }
    }

    private String generateExamCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    private void clearForm() {
        classNameField.clear();
        subjectNameField.clear();
        durationField.clear();
        kahootModeCheckBox.setSelected(false);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #10b981");
    }
}
