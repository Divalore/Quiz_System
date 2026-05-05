package com.example.quizsystem.controller;

import com.example.quizsystem.model.Exam;
import com.example.quizsystem.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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

public class AdminViewExamsController {

    @FXML private TableView<Exam> examsTable;
    @FXML private TableColumn<Exam, Integer> colId;
    @FXML private TableColumn<Exam, String> colClass;
    @FXML private TableColumn<Exam, String> colSubject;
    @FXML private TableColumn<Exam, Integer> colDuration;
    @FXML private TableColumn<Exam, String> colCode;
    @FXML private TableColumn<Exam, Void> colAction;

    private ObservableList<Exam> examList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        examsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("examCode"));

        // Make exam code selectable/copyable
        colCode.setCellFactory(tc -> new TableCell<>() {
            private final TextField tf = new TextField();
            {
                tf.setEditable(false);
                tf.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-padding: 0;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    tf.setText(item);
                    setGraphic(tf);
                }
            }
        });

        colAction.setCellFactory(tc -> new ActionCell());
        loadExams();
    }

    private void loadExams() {
        examList.clear();
        String sql = "SELECT id, className, subjectName, durationMinutes, examCode, isKahoot FROM exams";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                examList.add(new Exam(
                        rs.getInt("id"),
                        rs.getString("className"),
                        rs.getString("subjectName"),
                        rs.getInt("durationMinutes"),
                        rs.getString("examCode"),
                        rs.getInt("isKahoot") == 1
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        examsTable.setItems(examList);
    }

    private void handleDeleteExam(Exam exam) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Exam");
        confirm.setHeaderText("Delete Exam: " + exam.getSubjectName());
        confirm.setContentText("Are you sure? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM exams WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, exam.getId());
                stmt.executeUpdate();
                loadExams();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleEditExam(Exam exam) {
        Dialog<Exam> dialog = new Dialog<>();
        dialog.setTitle("Edit Exam");
        dialog.setHeaderText("Edit Exam Code: " + exam.getExamCode());

        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextField classField = new TextField(exam.getClassName());
        TextField subjectField = new TextField(exam.getSubjectName());
        TextField durationField = new TextField(String.valueOf(exam.getDurationMinutes()));
        CheckBox kahootCheckBox = new CheckBox("Kahoot Mode");
        kahootCheckBox.setSelected(exam.isKahoot());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Class:"), 0, 0);            grid.add(classField, 1, 0);
        grid.add(new Label("Subject:"), 0, 1);          grid.add(subjectField, 1, 1);
        grid.add(new Label("Duration (Mins):"), 0, 2);  grid.add(durationField, 1, 2);
        grid.add(kahootCheckBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    int duration = Integer.parseInt(durationField.getText().trim());
                    return new Exam(exam.getId(), classField.getText(), subjectField.getText(),
                            duration, exam.getExamCode(), kahootCheckBox.isSelected());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Exam> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            String sql = "UPDATE exams SET className = ?, subjectName = ?, durationMinutes = ?, isKahoot = ? WHERE id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, updated.getClassName());
                stmt.setString(2, updated.getSubjectName());
                stmt.setInt(3, updated.getDurationMinutes());
                stmt.setInt(4, updated.isKahoot() ? 1 : 0);
                stmt.setInt(5, updated.getId());
                stmt.executeUpdate();
                loadExams();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ─── Table Cell ────────────────────────────────────────────────────────────

    private class ActionCell extends TableCell<Exam, Void> {
        private final Button btnEdit = new Button("Edit");
        private final Button btnDelete = new Button("Delete");
        private final HBox box = new HBox(10, btnEdit, btnDelete);

        ActionCell() {
            btnEdit.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            btnDelete.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> handleEditExam(getTableView().getItems().get(getIndex())));
            btnDelete.setOnAction(e -> handleDeleteExam(getTableView().getItems().get(getIndex())));
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
        }
    }
}
