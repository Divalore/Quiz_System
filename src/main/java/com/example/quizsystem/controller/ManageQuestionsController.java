package com.example.quizsystem.controller;

import com.example.quizsystem.model.Question;
import com.example.quizsystem.service.QuestionService;
import com.example.quizsystem.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ManageQuestionsController {

    @FXML private TextField examCodeSearchField;
    @FXML private Label examInfoLabel;
    @FXML private ListView<String> questionListView;

    @FXML private TextField questionContentField;
    @FXML private TextField optionAField;
    @FXML private TextField optionBField;
    @FXML private TextField optionCField;
    @FXML private TextField optionDField;
    @FXML private ComboBox<String> correctAnswerBox;
    @FXML private ComboBox<String> questionTypeBox;
    @FXML private VBox mcqFieldsContainer;
    @FXML private TextField pointsField;
    @FXML private Label addStatusLabel;

    private int currentExamId = -1;
    private ObservableList<String> questionItems = FXCollections.observableArrayList();
    private List<Question> currentQuestions;

    @FXML
    public void initialize() {
        correctAnswerBox.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
        questionTypeBox.setItems(FXCollections.observableArrayList("MCQ", "WRITTEN"));
        questionTypeBox.setValue("MCQ");
        questionListView.setItems(questionItems);
    }

    @FXML
    void handleTypeChange(ActionEvent event) {
        boolean isWritten = "WRITTEN".equals(questionTypeBox.getValue());
        mcqFieldsContainer.setVisible(!isWritten);
        mcqFieldsContainer.setManaged(!isWritten);
    }

    @FXML
    void handleSearchExam(ActionEvent event) {
        String code = examCodeSearchField.getText().trim();
        if (code.isEmpty()) return;

        String sql = "SELECT * FROM exams WHERE examCode = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentExamId = rs.getInt("id");
                examInfoLabel.setText("Exam: " + rs.getString("subjectName") + " (" + rs.getString("className") + ")");
                examInfoLabel.setStyle("-fx-text-fill: #10b981");
                loadQuestions();
            } else {
                examInfoLabel.setText("Exam not found!");
                examInfoLabel.setStyle("-fx-text-fill: #ef4444");
                currentExamId = -1;
                questionItems.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshCurrentExam() {
        loadQuestions();
    }

    private void loadQuestions() {
        if (currentExamId == -1) return;

        currentQuestions = QuestionService.getQuestionsByExamId(currentExamId);
        questionItems.clear();

        for (int i = 0; i < currentQuestions.size(); i++) {
            Question q = currentQuestions.get(i);
            String label = "Q" + (i + 1) + " [" + q.getQuestionType() + "] (" + q.getPoints() + " pts): " + q.getContent();
            if ("MCQ".equals(q.getQuestionType())) {
                label += " (Ans: " + q.getCorrectAnswer() + ")";
            }
            questionItems.add(label);
        }
    }

    @FXML
    void handleAddQuestion(ActionEvent event) {
        if (currentExamId == -1) {
            showStatus("Please load an exam first.", false);
            return;
        }

        String content = questionContentField.getText().trim();
        String type = questionTypeBox.getValue();
        String optA = "", optB = "", optC = "", optD = "", correct = "";

        if ("MCQ".equals(type)) {
            optA = optionAField.getText().trim();
            optB = optionBField.getText().trim();
            optC = optionCField.getText().trim();
            optD = optionDField.getText().trim();
            correct = correctAnswerBox.getValue();

            if (content.isEmpty() || optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty() || correct == null) {
                showStatus("Please fill all fields.", false);
                return;
            }
        } else {
            if (content.isEmpty()) {
                showStatus("Please fill the question content.", false);
                return;
            }
        }

        int points = 1;
        try {
            points = Integer.parseInt(pointsField.getText().trim());
        } catch (NumberFormatException e) {
            // Use default value of 1
        }

        Question q = new Question(0, currentExamId, content, optA, optB, optC, optD, correct, type, points);

        if (QuestionService.addQuestion(q)) {
            showStatus("Question added successfully!", true);
            clearAddForm();
            loadQuestions();
        } else {
            showStatus("Error adding question.", false);
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (currentExamId == -1) {
            showWarning("No Exam Loaded", "Please load an exam using an exam code first.");
            return;
        }

        int selectedIdx = questionListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0 || selectedIdx >= currentQuestions.size()) {
            showWarning("No Selection", "Please select a question from the list to delete.");
            return;
        }

        Question q = currentQuestions.get(selectedIdx);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Question");
        confirm.setHeaderText("Are you sure you want to delete this question?");
        confirm.setContentText("This action cannot be undone.\n\n" + q.getContent());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (QuestionService.deleteQuestion(q.getId())) {
                loadQuestions();
            }
        }
    }

    @FXML
    void handleEditSelected(ActionEvent event) {
        if (currentExamId == -1) {
            showWarning("No Exam Loaded", "Please load an exam using an exam code first.");
            return;
        }

        int selectedIdx = questionListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0 || selectedIdx >= currentQuestions.size()) {
            showWarning("No Selection", "Please select a question from the list to edit.");
            return;
        }

        Question q = currentQuestions.get(selectedIdx);

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/quizsystem/view/edit_question.fxml")
            );
            javafx.scene.Parent root = loader.load();

            EditQuestionController controller = loader.getController();
            controller.setQuestion(q, this);

            Stage stage = new Stage();
            stage.setTitle("Edit Question");
            stage.initModality(Modality.APPLICATION_MODAL);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm());
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAddForm() {
        questionContentField.clear();
        optionAField.clear();
        optionBField.clear();
        optionCField.clear();
        optionDField.clear();
        pointsField.setText("1");
        correctAnswerBox.setValue(null);
        correctAnswerBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "Select Correct Answer" : item);
            }
        });
    }

    private void showStatus(String message, boolean success) {
        addStatusLabel.setText(message);
        addStatusLabel.setStyle(success ? "-fx-text-fill: #10b981" : "-fx-text-fill: #ef4444");
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
