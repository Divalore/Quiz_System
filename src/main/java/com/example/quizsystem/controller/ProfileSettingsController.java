package com.example.quizsystem.controller;

import com.example.quizsystem.model.User;
import com.example.quizsystem.utils.DatabaseManager;
import com.example.quizsystem.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProfileSettingsController {

    @FXML private TextField fNameField;
    @FXML private TextField lNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField newPassField;
    @FXML private TextField newPassTextField;
    @FXML private PasswordField confirmPassField;
    @FXML private TextField confirmPassTextField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Label statusLabel;

    private User currentUser;

    @FXML
    public void initialize() {
        if (newPassTextField != null && newPassField != null) {
            newPassTextField.textProperty().bindBidirectional(newPassField.textProperty());
        }
        if (confirmPassTextField != null && confirmPassField != null) {
            confirmPassTextField.textProperty().bindBidirectional(confirmPassField.textProperty());
        }

        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            fNameField.setText(currentUser.getFirstName());
            lNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
        }
    }

    @FXML
    void togglePasswordVisibility(ActionEvent event) {
        boolean show = showPasswordCheck.isSelected();
        newPassTextField.setVisible(show);
        newPassField.setVisible(!show);
        confirmPassTextField.setVisible(show);
        confirmPassField.setVisible(!show);
    }

    @FXML
    void handleSave(ActionEvent event) {
        String fname = fNameField.getText().trim();
        String lname = lNameField.getText().trim();
        String email = emailField.getText().trim();
        String newPass = newPassField.getText();
        String confirmPass = confirmPassField.getText();

        if (fname.isEmpty() || lname.isEmpty() || email.isEmpty()) {
            showStatus("First Name, Last Name, and Email cannot be empty.", false);
            return;
        }

        if (!newPass.isEmpty() && !newPass.equals(confirmPass)) {
            showStatus("New passwords do not match.", false);
            return;
        }

        if (isEmailTakenByOther(email)) {
            showStatus("Error: Email is already in use by another account.", false);
            return;
        }

        updateProfile(fname, lname, email, newPass.isEmpty() ? null : newPass);
    }

    private boolean isEmailTakenByOther(String email) {
        String sql = "SELECT id FROM users WHERE email = ? AND id != ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, currentUser.getId());
            return stmt.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateProfile(String fname, String lname, String email, String newPass) {
        try (Connection conn = DatabaseManager.getConnection()) {
            PreparedStatement stmt;

            if (newPass != null) {
                stmt = conn.prepareStatement(
                        "UPDATE users SET firstName = ?, lastName = ?, email = ?, password = ? WHERE id = ?");
                stmt.setString(1, fname);
                stmt.setString(2, lname);
                stmt.setString(3, email);
                stmt.setString(4, newPass);
                stmt.setInt(5, currentUser.getId());
            } else {
                stmt = conn.prepareStatement(
                        "UPDATE users SET firstName = ?, lastName = ?, email = ? WHERE id = ?");
                stmt.setString(1, fname);
                stmt.setString(2, lname);
                stmt.setString(3, email);
                stmt.setInt(4, currentUser.getId());
            }

            stmt.executeUpdate();

            currentUser.setFirstName(fname);
            currentUser.setLastName(lname);
            currentUser.setEmail(email);

            showStatus("Profile updated successfully!", true);
            newPassField.clear();
            confirmPassField.clear();

        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Error updating profile.", false);
        }
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success
                ? "-fx-text-fill: #10b981; -fx-font-weight: bold;"
                : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
    }
}
