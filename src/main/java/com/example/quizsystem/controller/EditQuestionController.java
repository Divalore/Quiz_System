package com.example.quizsystem.controller;

import com.example.quizsystem.model.Question;
import com.example.quizsystem.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditQuestionController {

    @FXML private TextField questionContentField;
    @FXML private TextField optionAField;
    @FXML private TextField optionBField;
    @FXML private TextField optionCField;
    @FXML private TextField optionDField;
    @FXML private ComboBox<String> correctAnswerBox;
    @FXML private ComboBox<String> questionTypeBox;
    @FXML private TextField pointsField;
    @FXML private VBox mcqFieldsContainer;
    @FXML private Label statusLabel;

    private Question questionToEdit;
    private ManageQuestionsController parentController;

    @FXML
    public void initialize() {
        correctAnswerBox.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
        questionTypeBox.setItems(FXCollections.observableArrayList("MCQ", "WRITTEN"));
    }

    public void setQuestion(Question q, ManageQuestionsController parent) {
        this.questionToEdit = q;
        this.parentController = parent;

        questionContentField.setText(q.getContent());
        questionTypeBox.setValue(q.getQuestionType());
        pointsField.setText(String.valueOf(q.getPoints()));

        boolean isMcq = "MCQ".equals(q.getQuestionType());
        mcqFieldsContainer.setVisible(isMcq);
        mcqFieldsContainer.setManaged(isMcq);

        if (isMcq) {
            optionAField.setText(q.getOptionA());
            optionBField.setText(q.getOptionB());
            optionCField.setText(q.getOptionC());
            optionDField.setText(q.getOptionD());
            correctAnswerBox.setValue(q.getCorrectAnswer());
        }
    }

    @FXML
    void handleTypeChange(ActionEvent event) {
        boolean isWritten = "WRITTEN".equals(questionTypeBox.getValue());
        mcqFieldsContainer.setVisible(!isWritten);
        mcqFieldsContainer.setManaged(!isWritten);
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        String content = questionContentField.getText().trim();
        String type = questionTypeBox.getValue();
        String optA = optionAField.getText().trim();
        String optB = optionBField.getText().trim();
        String optC = optionCField.getText().trim();
        String optD = optionDField.getText().trim();
        String correct = correctAnswerBox.getValue();

        if (content.isEmpty()) {
            showError("Question content cannot be empty.");
            return;
        }

        if ("MCQ".equals(type)) {
            if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty() || correct == null) {
                showError("Please fill all MCQ options and select the correct answer.");
                return;
            }
        }

        int points = 1;
        try {
            points = Integer.parseInt(pointsField.getText().trim());
        } catch (Exception e) {
            
        }

        String sql = "UPDATE questions SET content=?, optionA=?, optionB=?, optionC=?, optionD=?, " +
                     "correctAnswer=?, questionType=?, points=? WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, content);
            stmt.setString(2, optA);
            stmt.setString(3, optB);
            stmt.setString(4, optC);
            stmt.setString(5, optD);
            stmt.setString(6, correct);
            stmt.setString(7, type);
            stmt.setInt(8, points);
            stmt.setInt(9, questionToEdit.getId());

            if (stmt.executeUpdate() > 0) {
                parentController.refreshCurrentExam();
                closeWindow();
            } else {
                showError("Failed to update question.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("A database error occurred.");
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) statusLabel.getScene().getWindow()).close();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444");
    }
}
