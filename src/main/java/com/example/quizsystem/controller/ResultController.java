package com.example.quizsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ResultController {

    @FXML private Label scoreLabel;
    @FXML private Label percentageLabel;
    @FXML private Label messageLabel;

    public void showResult(int score, int total, boolean hasWritten) {
        scoreLabel.setText(String.format("You scored %d out of %d", score, total));

        double percentage = (total > 0) ? (score * 100.0 / total) : 0;
        percentageLabel.setText(String.format("Percentage: %.1f%%", percentage));

        if (hasWritten) {
            messageLabel.setText("Exam Submitted! Result is Pending Manual Grading.");
            messageLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 18px;");
            return;
        }

        if (percentage >= 80) {
            messageLabel.setText("Excellent Work!");
            messageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 20px;");
        } else if (percentage >= 50) {
            messageLabel.setText("Good Job! You passed.");
            messageLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 20px;");
        } else {
            messageLabel.setText("Better luck next time.");
            messageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 20px;");
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/student_dashboard.fxml"));
            Stage stage = (Stage) scoreLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
