package com.example.quizsystem.controller;

import com.example.quizsystem.model.Exam;
import com.example.quizsystem.model.Question;
import com.example.quizsystem.model.User;
import com.example.quizsystem.utils.DatabaseManager;
import com.example.quizsystem.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeExamController {

    @FXML private Label examTitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label progressLabel;
    @FXML private Label questionLabel;
    @FXML private RadioButton opt1;
    @FXML private RadioButton opt2;
    @FXML private RadioButton opt3;
    @FXML private RadioButton opt4;
    @FXML private ToggleGroup optionsGroup;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button submitBtn;
    @FXML private VBox mcqOptionsContainer;
    @FXML private TextArea writtenAnswerArea;
    @FXML private VBox feedbackContainer;
    @FXML private Label feedbackLabel;
    @FXML private Label feedbackDetailLabel;
    @FXML private Button continueBtn;

    private Exam currentExam;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private Map<Integer, String> studentAnswers = new HashMap<>(); // questionId -> answer letter or text
    private Timeline timer;
    private int timeRemainingSeconds;

    // ─── Initialization ────────────────────────────────────────────────────────

    public void initExam(Exam exam) {
        this.currentExam = exam;
        examTitleLabel.setText(exam.getSubjectName() + " Exam");
        timeRemainingSeconds = exam.getDurationMinutes() * 60;

        loadQuestions();

        if (!questions.isEmpty()) {
            displayQuestion();
            startTimer();
        } else {
            questionLabel.setText("No questions available for this exam.");
            disableNavigation();
            submitBtn.setVisible(true);
            submitBtn.setDisable(false);
        }
    }

    private void loadQuestions() {
        String sql = "SELECT * FROM questions WHERE examId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentExam.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"),
                        rs.getInt("examId"),
                        rs.getString("content"),
                        rs.getString("optionA"),
                        rs.getString("optionB"),
                        rs.getString("optionC"),
                        rs.getString("optionD"),
                        rs.getString("correctAnswer"),
                        rs.getString("questionType"),
                        rs.getInt("points")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Display ───────────────────────────────────────────────────────────────

    private void displayQuestion() {
        if (questions.isEmpty()) return;

        Question q = questions.get(currentQuestionIndex);
        progressLabel.setText(String.format("Question %d of %d", currentQuestionIndex + 1, questions.size()));
        questionLabel.setText(q.getContent());

        if ("WRITTEN".equals(q.getQuestionType())) {
            setMcqVisible(false);
            writtenAnswerArea.setText(studentAnswers.getOrDefault(q.getId(), ""));
        } else {
            setMcqVisible(true);
            opt1.setText(q.getOptionA());
            opt2.setText(q.getOptionB());
            opt3.setText(q.getOptionC());
            opt4.setText(q.getOptionD());
            restoreSavedAnswer(q);
        }

        configurePrevButton();
        hideFeedback();
        setInputsEnabled(true);
        configureNextAndSubmitButtons(q);
    }

    private void setMcqVisible(boolean visible) {
        mcqOptionsContainer.setVisible(visible);
        mcqOptionsContainer.setManaged(visible);
        writtenAnswerArea.setVisible(!visible);
        writtenAnswerArea.setManaged(!visible);
    }

    private void restoreSavedAnswer(Question q) {
        optionsGroup.selectToggle(null);
        String saved = studentAnswers.get(q.getId());
        if (saved != null) {
            switch (saved) {
                case "A" -> opt1.setSelected(true);
                case "B" -> opt2.setSelected(true);
                case "C" -> opt3.setSelected(true);
                case "D" -> opt4.setSelected(true);
            }
        }
    }

    private void configurePrevButton() {
        if (currentExam.isKahoot()) {
            prevBtn.setVisible(false);
            prevBtn.setManaged(false);
        } else {
            boolean hasPrev = currentQuestionIndex > 0;
            prevBtn.setVisible(hasPrev);
            prevBtn.setManaged(hasPrev);
            prevBtn.setDisable(false);
        }
    }

    private void configureNextAndSubmitButtons(Question q) {
        boolean isLastQuestion = currentQuestionIndex == questions.size() - 1;

        if (currentExam.isKahoot()) {
            nextBtn.setText("Submit Answer");
            nextBtn.setVisible(true);
            submitBtn.setVisible(false);
        } else {
            nextBtn.setText("Next");
            if (isLastQuestion) {
                nextBtn.setVisible(false);
                submitBtn.setVisible(true);
            } else {
                nextBtn.setVisible(true);
                submitBtn.setVisible(false);
            }
        }
    }

    // ─── Answer Handling ───────────────────────────────────────────────────────

    private void saveCurrentAnswer() {
        if (questions.isEmpty()) return;

        Question q = questions.get(currentQuestionIndex);

        if ("WRITTEN".equals(q.getQuestionType())) {
            studentAnswers.put(q.getId(), writtenAnswerArea.getText());
        } else {
            RadioButton selected = (RadioButton) optionsGroup.getSelectedToggle();
            if (selected == null) return;

            String letter = "";
            if (selected == opt1) letter = "A";
            else if (selected == opt2) letter = "B";
            else if (selected == opt3) letter = "C";
            else if (selected == opt4) letter = "D";

            studentAnswers.put(q.getId(), letter);
        }
    }

    // ─── Navigation Events ─────────────────────────────────────────────────────

    @FXML
    void handleNext(ActionEvent event) {
        saveCurrentAnswer();
        Question q = questions.get(currentQuestionIndex);

        if (currentExam.isKahoot() && "MCQ".equals(q.getQuestionType())) {
            showFeedback(q);
        } else {
            if (currentQuestionIndex < questions.size() - 1) {
                currentQuestionIndex++;
                displayQuestion();
            } else if (currentExam.isKahoot()) {
                // Kahoot mode on a written final question
                nextBtn.setVisible(false);
                submitBtn.setVisible(true);
            }
        }
    }

    @FXML
    void handlePrev(ActionEvent event) {
        saveCurrentAnswer();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            displayQuestion();
        }
    }

    @FXML
    void handleContinue(ActionEvent event) {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            displayQuestion();
        }
    }

    // ─── Kahoot Feedback ───────────────────────────────────────────────────────

    private void showFeedback(Question q) {
        String selected = studentAnswers.get(q.getId());
        boolean isCorrect = selected != null && selected.equals(q.getCorrectAnswer());

        setInputsEnabled(false);
        nextBtn.setVisible(false);
        nextBtn.setManaged(false);
        feedbackContainer.setVisible(true);
        feedbackContainer.setManaged(true);

        if (isCorrect) {
            feedbackLabel.setText("CORRECT!");
            feedbackLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 24px; -fx-font-weight: bold;");
            feedbackContainer.setStyle("-fx-background-color: #d1fae5; -fx-padding: 15; -fx-background-radius: 10;");
            feedbackDetailLabel.setText("Great job!");
        } else {
            feedbackLabel.setText("INCORRECT");
            feedbackLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 24px; -fx-font-weight: bold;");
            feedbackContainer.setStyle("-fx-background-color: #fee2e2; -fx-padding: 15; -fx-background-radius: 10;");
            feedbackDetailLabel.setText("The correct answer was: " + q.getCorrectAnswer());
        }

        boolean isLastQuestion = currentQuestionIndex == questions.size() - 1;
        continueBtn.setVisible(!isLastQuestion);
        continueBtn.setManaged(!isLastQuestion);
        if (!isLastQuestion) continueBtn.setText("Next Question");
        submitBtn.setVisible(isLastQuestion);
    }

    private void hideFeedback() {
        feedbackContainer.setVisible(false);
        feedbackContainer.setManaged(false);
        continueBtn.setVisible(false);
        continueBtn.setManaged(false);
        nextBtn.setVisible(true);
        nextBtn.setManaged(true);
    }

    private void setInputsEnabled(boolean enabled) {
        opt1.setDisable(!enabled);
        opt2.setDisable(!enabled);
        opt3.setDisable(!enabled);
        opt4.setDisable(!enabled);
        writtenAnswerArea.setDisable(!enabled);
    }

    // ─── Timer ─────────────────────────────────────────────────────────────────

    private void startTimer() {
        updateTimerLabel();
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemainingSeconds--;
            updateTimerLabel();
            if (timeRemainingSeconds <= 0) {
                timer.stop();
                handleSubmit(null);
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerLabel() {
        int hours = timeRemainingSeconds / 3600;
        int minutes = (timeRemainingSeconds % 3600) / 60;
        int seconds = timeRemainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void disableNavigation() {
        opt1.setDisable(true);
        opt2.setDisable(true);
        opt3.setDisable(true);
        opt4.setDisable(true);
        nextBtn.setDisable(true);
        prevBtn.setDisable(true);
    }

    // ─── Submit ────────────────────────────────────────────────────────────────

    @FXML
    void handleSubmit(ActionEvent event) {
        if (timer != null) timer.stop();
        saveCurrentAnswer();
        disableNavigation();

        int score = calculateMcqScore();
        int totalPossible = questions.stream().mapToInt(Question::getPoints).sum();
        boolean hasWritten = questions.stream().anyMatch(q -> "WRITTEN".equals(q.getQuestionType()));

        User user = SessionManager.getCurrentUser();
        if (user != null && totalPossible > 0) {
            persistResult(user.getId(), score, totalPossible, hasWritten);
        }

        navigateToResultScreen(score, totalPossible, hasWritten);
    }

    private void persistResult(int studentId, int score, int totalPossible, boolean hasWritten) {
        try (Connection conn = DatabaseManager.getConnection()) {
            int resultId = insertResult(conn, studentId, score, totalPossible, hasWritten);
            if (resultId > 0 && hasWritten) {
                insertWrittenAnswers(conn, resultId, studentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int insertResult(Connection conn, int studentId, int score, int total, boolean hasWritten) throws Exception {
        String sql = "INSERT INTO results (studentId, examId, score, totalQuestions, isFullyGraded) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, currentExam.getId());
            stmt.setInt(3, score);
            stmt.setInt(4, total);
            stmt.setInt(5, hasWritten ? 0 : 1);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    private void insertWrittenAnswers(Connection conn, int resultId, int studentId) throws Exception {
        String sql = "INSERT INTO student_answers (resultId, studentId, examId, questionId, answerText) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Question q : questions) {
                if ("WRITTEN".equals(q.getQuestionType())) {
                    stmt.setInt(1, resultId);
                    stmt.setInt(2, studentId);
                    stmt.setInt(3, currentExam.getId());
                    stmt.setInt(4, q.getId());
                    stmt.setString(5, studentAnswers.getOrDefault(q.getId(), ""));
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    private int calculateMcqScore() {
        int score = 0;
        for (Question q : questions) {
            if ("MCQ".equals(q.getQuestionType())) {
                String answer = studentAnswers.get(q.getId());
                if (answer != null && answer.equals(q.getCorrectAnswer())) {
                    score += q.getPoints();
                }
            }
        }
        return score;
    }

    private void navigateToResultScreen(int score, int total, boolean hasWritten) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/result.fxml"));
            Stage stage = (Stage) examTitleLabel.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm());

            ResultController controller = loader.getController();
            controller.showResult(score, total, hasWritten);

            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
