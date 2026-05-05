package com.example.quizsystem.controller;

import com.example.quizsystem.model.Exam;
import com.example.quizsystem.model.User;
import com.example.quizsystem.utils.DatabaseManager;
import com.example.quizsystem.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentController {

    @FXML private Label welcomeLabel;
    @FXML private TextField examCodeField;
    @FXML private Label statusLabel;
    @FXML private StackPane contentArea;
    @FXML private AnchorPane dashboardPane;
    @FXML private Button btnTakeExam;
    @FXML private Button btnViewResults;
    @FXML private Button btnProfileSettings;

    @FXML
    public void initialize() {
        dashboardPane.setVisible(true);
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome " + currentUser.getFirstName() + "!");
        }
    }

    // ─── Navigation ────────────────────────────────────────────────────────────

    private void setActiveButton(Button active) {
        if (btnTakeExam != null) btnTakeExam.getStyleClass().remove("nav-btn-active");
        if (btnViewResults != null) btnViewResults.getStyleClass().remove("nav-btn-active");
        if (btnProfileSettings != null) btnProfileSettings.getStyleClass().remove("nav-btn-active");
        if (active != null) active.getStyleClass().add("nav-btn-active");
    }

    private void hideAllContent() {
        contentArea.getChildren().forEach(node -> node.setVisible(false));
    }

    private void loadFreshPane(String paneId, String fxmlPath) {
        hideAllContent();
        try {
            contentArea.getChildren().removeIf(node -> paneId.equals(node.getId()));
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node pane = loader.load();
            pane.setId(paneId);
            contentArea.getChildren().add(pane);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading view.");
        }
    }

    @FXML
    void showDashboard(ActionEvent event) {
        setActiveButton(btnTakeExam);
        hideAllContent();
        dashboardPane.setVisible(true);
    }

    @FXML
    void showResults(ActionEvent event) {
        setActiveButton(btnViewResults);
        loadFreshPane("studentResultsPane", "/com/example/quizsystem/view/student_view_results.fxml");
    }

    @FXML
    void showProfileSettings(ActionEvent event) {
        setActiveButton(btnProfileSettings);
        loadFreshPane("profileSettingsPane", "/com/example/quizsystem/view/profile_settings.fxml");
    }

    // ─── Exam Entry ────────────────────────────────────────────────────────────

    @FXML
    void openExam(ActionEvent event) {
        String code = examCodeField.getText().trim();
        if (code.isEmpty()) {
            statusLabel.setText("Please enter an Exam Code.");
            return;
        }

        String sql = "SELECT * FROM exams WHERE examCode = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                statusLabel.setText("Invalid Exam Code. Please try again.");
                return;
            }

            int examId = rs.getInt("id");

            if (hasAlreadyTakenExam(conn, examId)) {
                statusLabel.setText("You have already completed this exam.");
                return;
            }

            Exam exam = new Exam(
                    examId,
                    rs.getString("className"),
                    rs.getString("subjectName"),
                    rs.getInt("durationMinutes"),
                    rs.getString("examCode"),
                    rs.getInt("isKahoot") == 1
            );

            openExamScreen(exam);

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("An error occurred. Please try again.");
        }
    }

    private boolean hasAlreadyTakenExam(Connection conn, int examId) throws Exception {
        String sql = "SELECT id FROM results WHERE studentId = ? AND examId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, SessionManager.getCurrentUser().getId());
            stmt.setInt(2, examId);
            return stmt.executeQuery().next();
        }
    }

    private void openExamScreen(Exam exam) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/take_exam.fxml"));
        Stage stage = (Stage) examCodeField.getScene().getWindow();
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm());

        TakeExamController controller = loader.getController();
        controller.initExam(exam);

        stage.setScene(scene);
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    @FXML
    void handleLogout(ActionEvent event) {
        SessionManager.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/login.fxml"));
            Stage stage = (Stage) examCodeField.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(700);
            stage.setHeight(550);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
