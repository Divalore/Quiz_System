package com.example.quizsystem.controller;

import com.example.quizsystem.model.User;
import com.example.quizsystem.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class AdminManageAdminsController {

    @FXML private TableView<User> adminsTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colFirst;
    @FXML private TableColumn<User, String> colLast;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPassword;
    @FXML private TableColumn<User, Void> colAction;
    @FXML private TextField searchField;

    private ObservableList<User> adminList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        adminsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirst.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));

        colPassword.setCellFactory(tc -> new PasswordToggleCell());
        colAction.setCellFactory(tc -> new EditButtonCell());

        loadAdmins();
        setupSearch();
    }

    private void setupSearch() {
        FilteredList<User> filtered = new FilteredList<>(adminList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filtered.setPredicate(user -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return String.valueOf(user.getId()).contains(filter)
                        || user.getFirstName().toLowerCase().contains(filter)
                        || user.getLastName().toLowerCase().contains(filter)
                        || user.getEmail().toLowerCase().contains(filter);
            });
        });

        SortedList<User> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(adminsTable.comparatorProperty());
        adminsTable.setItems(sorted);
    }

    private void loadAdmins() {
        adminList.clear();
        String sql = "SELECT * FROM users WHERE role = 'ADMIN'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                adminList.add(new User(
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
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleEditAdmin(User admin) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit Admin");
        dialog.setHeaderText("Edit: " + admin.getFirstName() + " " + admin.getLastName());

        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField fNameField = new TextField(admin.getFirstName());
        TextField lNameField = new TextField(admin.getLastName());
        TextField passField = new TextField(admin.getPassword());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("First Name:"), 0, 0); grid.add(fNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);  grid.add(lNameField, 1, 1);
        grid.add(new Label("Password:"), 0, 2);   grid.add(passField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                admin.setFirstName(fNameField.getText());
                admin.setLastName(lNameField.getText());
                admin.setPassword(passField.getText());
                return admin;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            String sql = "UPDATE users SET firstName=?, lastName=?, password=? WHERE id=?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, updated.getFirstName());
                stmt.setString(2, updated.getLastName());
                stmt.setString(3, updated.getPassword());
                stmt.setInt(4, updated.getId());
                stmt.executeUpdate();
                loadAdmins();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ─── Table Cells ───────────────────────────────────────────────────────────

    private class PasswordToggleCell extends TableCell<User, String> {
        private final Label lblPassword = new Label("••••••••");
        private final Button btnToggle = new Button("👁");
        private final HBox hbox = new HBox(5, lblPassword, btnToggle);
        private boolean isVisible = false;

        PasswordToggleCell() {
            btnToggle.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: #374151; -fx-padding: 0;");
            btnToggle.setOnAction(e -> {
                isVisible = !isVisible;
                lblPassword.setText(isVisible ? getItem() : "••••••••");
            });
            hbox.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                lblPassword.setText(isVisible ? item : "••••••••");
                setGraphic(hbox);
            }
        }
    }

    private class EditButtonCell extends TableCell<User, Void> {
        private final Button btnEdit = new Button("Edit");

        EditButtonCell() {
            btnEdit.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> handleEditAdmin(getTableView().getItems().get(getIndex())));
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : btnEdit);
        }
    }
}
