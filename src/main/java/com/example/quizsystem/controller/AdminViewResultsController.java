package com.example.quizsystem.controller;

import com.example.quizsystem.model.AdminResultDTO;
import com.example.quizsystem.utils.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.TreeSet;

public class AdminViewResultsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> classFilter;
    @FXML private ComboBox<String> subjectFilter;
    @FXML private TableView<AdminResultDTO> resultsTable;
    @FXML private TableColumn<AdminResultDTO, Integer> colId;
    @FXML private TableColumn<AdminResultDTO, String> colStudent;
    @FXML private TableColumn<AdminResultDTO, String> colClass;
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
        colId.setCellValueFactory(new PropertyValueFactory<>("resultId"));
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));
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

        Set<String> classes = new TreeSet<>();
        Set<String> subjects = new TreeSet<>();
        classes.add("All Classes");
        subjects.add("All Subjects");

        String sql = "SELECT r.id, u.firstName, u.lastName, u.className, e.examCode, e.subjectName, " +
                     "r.score, r.totalQuestions, r.isFullyGraded " +
                     "FROM results r " +
                     "JOIN users u ON r.studentId = u.id " +
                     "JOIN exams e ON r.examId = e.id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fullName = rs.getString("firstName") + " " + rs.getString("lastName");
                AdminResultDTO dto = new AdminResultDTO(
                        rs.getInt("id"),
                        fullName,
                        rs.getString("className"),
                        rs.getString("examCode"),
                        rs.getString("subjectName"),
                        rs.getInt("score"),
                        rs.getInt("totalQuestions"),
                        rs.getInt("isFullyGraded") == 1
                );
                resultList.add(dto);
                if (dto.getClassName() != null) classes.add(dto.getClassName());
                if (dto.getSubject() != null) subjects.add(dto.getSubject());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        classFilter.setItems(FXCollections.observableArrayList(classes));
        classFilter.setValue("All Classes");
        subjectFilter.setItems(FXCollections.observableArrayList(subjects));
        subjectFilter.setValue("All Subjects");

        setupFiltering();
    }

    private void setupFiltering() {
        FilteredList<AdminResultDTO> filtered = new FilteredList<>(resultList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(filtered));
        classFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters(filtered));
        subjectFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters(filtered));

        SortedList<AdminResultDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(resultsTable.comparatorProperty());
        resultsTable.setItems(sorted);
    }

    private void applyFilters(FilteredList<AdminResultDTO> filtered) {
        filtered.setPredicate(result -> {
            String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
            String selectedClass = classFilter.getValue();
            String selectedSubject = subjectFilter.getValue();

            boolean matchesSearch = search.isEmpty()
                    || String.valueOf(result.getResultId()).contains(search)
                    || result.getStudentName().toLowerCase().contains(search)
                    || result.getExamCode().toLowerCase().contains(search)
                    || result.getSubject().toLowerCase().contains(search);

            boolean matchesClass = selectedClass == null || "All Classes".equals(selectedClass)
                    || selectedClass.equals(result.getClassName());

            boolean matchesSubject = selectedSubject == null || "All Subjects".equals(selectedSubject)
                    || selectedSubject.equals(result.getSubject());

            return matchesSearch && matchesClass && matchesSubject;
        });
    }

    @FXML
    void exportToCSV(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Results to CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("exam_results.csv");

        File file = chooser.showSaveDialog(resultsTable.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Result ID,Student Name,Class,Exam Code,Subject,Score,Total Questions,Percentage");

            for (AdminResultDTO res : resultsTable.getItems()) {
                writer.printf("%d,%s,%s,%s,%s,%d,%d,%s%n",
                        res.getResultId(),
                        escapeCsv(res.getStudentName()),
                        escapeCsv(res.getClassName()),
                        escapeCsv(res.getExamCode()),
                        escapeCsv(res.getSubject()),
                        res.getScore(),
                        res.getTotalQuestions(),
                        res.getPercentage()
                );
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setHeaderText(null);
            alert.setContentText("Results exported to: " + file.getName());
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText("Could not export data");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}
