package com.example.quizsystem.controller;

import com.example.quizsystem.model.AdminResultDTO;
import com.example.quizsystem.model.User;
import com.example.quizsystem.utils.DatabaseManager;
import com.example.quizsystem.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentViewResultsController {

    @FXML private TableView<AdminResultDTO> resultsTable;
    @FXML private TableColumn<AdminResultDTO, String> colExam;
    @FXML private TableColumn<AdminResultDTO, String> colSubject;
    @FXML private TableColumn<AdminResultDTO, Integer> colScore;
    @FXML private TableColumn<AdminResultDTO, Integer> colTotal;
    @FXML private TableColumn<AdminResultDTO, String> colPercentage;
    @FXML private TableColumn<AdminResultDTO, String> colStatus;

    private ObservableList<AdminResultDTO> resultList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colExam.setCellValueFactory(new PropertyValueFactory<>("examCode"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalQuestions"));
        colPercentage.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadData();
    }

    private void loadData() {
        resultList.clear();

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        String sql = "SELECT r.id, e.examCode, e.subjectName, r.score, r.totalQuestions, r.isFullyGraded " +
                     "FROM results r " +
                     "JOIN exams e ON r.examId = e.id " +
                     "WHERE r.studentId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                resultList.add(new AdminResultDTO(
                        rs.getInt("id"),
                        currentUser.getFirstName() + " " + currentUser.getLastName(),
                        currentUser.getClassName(),
                        rs.getString("examCode"),
                        rs.getString("subjectName"),
                        rs.getInt("score"),
                        rs.getInt("totalQuestions"),
                        rs.getInt("isFullyGraded") == 1
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        resultsTable.setItems(resultList);
    }
}
