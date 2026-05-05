package com.example.quizsystem.controller;

import com.example.quizsystem.utils.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminController {

    // Layout
    @FXML private StackPane contentArea;
    @FXML private AnchorPane registerStudentPane;

    // Registration form fields
    @FXML private TextField fNameField;
    @FXML private TextField lNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private TextField parentNameField;
    @FXML private ToggleGroup genderGroup;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private DatePicker dobPicker;
    @FXML private TextField classField;
    @FXML private Label registrationTitleLabel;
    @FXML private RadioButton studentRoleRadio;
    @FXML private RadioButton adminRoleRadio;
    @FXML private Button submitRegisterBtn;
    @FXML private Label statusLabel;

    // Navigation buttons
    @FXML private Button btnRegisterStudent;
    @FXML private Button btnCreateExam;
    @FXML private Button btnManageQuestions;
    @FXML private Button btnManageStudents;
    @FXML private Button btnManageAdmins;
    @FXML private Button btnViewExams;
    @FXML private Button btnViewResults;
    @FXML private Button btnAnalytics;
    @FXML private Button btnGrading;
    @FXML private Button btnProfileSettings;

    @FXML
    public void initialize() {
        registerStudentPane.setVisible(true);

        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }

        if (studentRoleRadio != null && adminRoleRadio != null) {
            studentRoleRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    registrationTitleLabel.setText("Register New Student");
                    submitRegisterBtn.setText("Register Student");
                }
            });
            adminRoleRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    registrationTitleLabel.setText("Register New Admin");
                    submitRegisterBtn.setText("Register Admin");
                }
            });
        }
    }

    // ─── Navigation ────────────────────────────────────────────────────────────

    private void setActiveButton(Button active) {
        Button[] navButtons = {
            btnRegisterStudent, btnCreateExam, btnManageQuestions,
            btnManageStudents, btnManageAdmins, btnViewExams,
            btnViewResults, btnAnalytics, btnGrading, btnProfileSettings
        };
        for (Button btn : navButtons) {
            if (btn != null) btn.getStyleClass().remove("nav-btn-active");
        }
        if (active != null) active.getStyleClass().add("nav-btn-active");
    }

    private void hideAllContent() {
        contentArea.getChildren().forEach(node -> node.setVisible(false));
    }

    private void loadOrShowPane(String paneId, String fxmlPath, boolean alwaysReload) {
        hideAllContent();
        try {
            if (alwaysReload) {
                contentArea.getChildren().removeIf(node -> paneId.equals(node.getId()));
            } else {
                for (Node node : contentArea.getChildren()) {
                    if (paneId.equals(node.getId())) {
                        node.setVisible(true);
                        return;
                    }
                }
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node pane = loader.load();
            pane.setId(paneId);
            contentArea.getChildren().add(pane);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading view: " + fxmlPath);
        }
    }

    @FXML
    void showRegisterStudent(ActionEvent event) {
        setActiveButton(btnRegisterStudent);
        hideAllContent();
        registerStudentPane.setVisible(true);
    }

    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        boolean show = showPasswordCheck.isSelected();
        passwordTextField.setVisible(show);
        passwordField.setVisible(!show);
    }

    @FXML
    void showManageExams(ActionEvent event) {
        setActiveButton(btnCreateExam);
        loadOrShowPane("manageExamsPane", "/com/example/quizsystem/view/create_exam.fxml", false);
    }

    @FXML
    void showManageQuestions(ActionEvent event) {
        setActiveButton(btnManageQuestions);
        loadOrShowPane("questionsPane", "/com/example/quizsystem/view/manage_questions.fxml", false);
    }

    @FXML
    void showManageStudents(ActionEvent event) {
        setActiveButton(btnManageStudents);
        loadOrShowPane("manageStudentsPane", "/com/example/quizsystem/view/admin_manage_students.fxml", true);
    }

    @FXML
    void showManageAdmins(ActionEvent event) {
        setActiveButton(btnManageAdmins);
        loadOrShowPane("manageAdminsPane", "/com/example/quizsystem/view/admin_manage_admins.fxml", true);
    }

    @FXML
    void showViewExams(ActionEvent event) {
        setActiveButton(btnViewExams);
        loadOrShowPane("viewExamsPane", "/com/example/quizsystem/view/admin_view_exams.fxml", true);
    }

    @FXML
    void showResults(ActionEvent event) {
        setActiveButton(btnViewResults);
        loadOrShowPane("resultsPane", "/com/example/quizsystem/view/admin_view_results.fxml", true);
    }

    @FXML
    void showAnalytics(ActionEvent event) {
        setActiveButton(btnAnalytics);
        loadOrShowPane("analyticsPane", "/com/example/quizsystem/view/admin_analytics.fxml", true);
    }

    @FXML
    void showGrading(ActionEvent event) {
        setActiveButton(btnGrading);
        loadOrShowPane("gradingPane", "/com/example/quizsystem/view/admin_grading.fxml", true);
    }

    @FXML
    void showProfileSettings(ActionEvent event) {
        setActiveButton(btnProfileSettings);
        loadOrShowPane("profileSettingsPane", "/com/example/quizsystem/view/profile_settings.fxml", true);
    }

    // ─── Registration ──────────────────────────────────────────────────────────

    @FXML
    void registerStudent(ActionEvent event) {
        String fname = fNameField.getText().trim();
        String lname = lNameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String parent = parentNameField.getText().trim();
        String gender = maleRadio.isSelected() ? "Male" : (femaleRadio.isSelected() ? "Female" : "");
        String dob = (dobPicker.getValue() != null) ? dobPicker.getValue().toString() : "";
        String className = classField.getText().trim();
        String role = adminRoleRadio.isSelected() ? "ADMIN" : "STUDENT";

        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty() || pass.isEmpty() || className.isEmpty()) {
            showError("Please fill all required fields.");
            return;
        }

        if (isEmailTaken(email)) {
            showError("Error: Email is already in use.");
            return;
        }

        String sql = "INSERT INTO users (firstName, lastName, email, password, role, parentName, gender, dob, className) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fname);
            stmt.setString(2, lname);
            stmt.setString(3, email);
            stmt.setString(4, pass);
            stmt.setString(5, role);
            stmt.setString(6, parent);
            stmt.setString(7, gender);
            stmt.setString(8, dob);
            stmt.setString(9, className);
            stmt.executeUpdate();

            showSuccess("ADMIN".equals(role) ? "Admin registered successfully!" : "Student registered successfully!");
            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error: Could not register user.");
        }
    }

    private boolean isEmailTaken(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void clearForm() {
        fNameField.clear();
        lNameField.clear();
        emailField.clear();
        passwordField.clear();
        parentNameField.clear();
        classField.clear();
        dobPicker.setValue(null);
        genderGroup.selectToggle(null);

        if (showPasswordCheck != null) showPasswordCheck.setSelected(false);
        if (passwordTextField != null) passwordTextField.setVisible(false);
        if (passwordField != null) passwordField.setVisible(true);
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ef4444");
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #10b981");
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/quizsystem/view/login.fxml"));
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(700);
            stage.setHeight(550);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
