package com.example.quizsystem.controller;

import com.example.quizsystem.model.User;
import com.example.quizsystem.utils.DatabaseManager;
import com.example.quizsystem.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        boolean show = showPasswordCheck.isSelected();
        passwordTextField.setVisible(show);
        passwordField.setVisible(!show);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password.");
            return;
        }

        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("parentName"),
                        rs.getString("gender"),
                        rs.getString("dob"),
                        rs.getString("className")
                );
                SessionManager.setCurrentUser(user);
                navigateToDashboard(user.getRole());
            } else {
                errorLabel.setText("Invalid email or password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("A database error occurred. Please try again.");
        }
    }

    private void navigateToDashboard(String role) throws Exception {
        String view = "ADMIN".equals(role)
                ? "/com/example/quizsystem/view/admin_dashboard.fxml"
                : "/com/example/quizsystem/view/student_dashboard.fxml";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(view));
        Stage stage = (Stage) emailField.getScene().getWindow();
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/com/example/quizsystem/style/app.css").toExternalForm());

        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.centerOnScreen();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText(null);
        alert.setContentText("Please contact your Administrator to reset your password.");
        alert.showAndWait();
    }
}
