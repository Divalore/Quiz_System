package com.example.quizsystem.service;

import com.example.quizsystem.model.Question;
import com.example.quizsystem.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    public static boolean addQuestion(Question q) {
        String sql = "INSERT INTO questions " +
                     "(examId, content, optionA, optionB, optionC, optionD, correctAnswer, questionType, points) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, q.getExamId());
            stmt.setString(2, q.getContent());
            stmt.setString(3, q.getOptionA());
            stmt.setString(4, q.getOptionB());
            stmt.setString(5, q.getOptionC());
            stmt.setString(6, q.getOptionD());
            stmt.setString(7, q.getCorrectAnswer());
            stmt.setString(8, q.getQuestionType());
            stmt.setInt(9, q.getPoints());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Question> getQuestionsByExamId(int examId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE examId = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
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

        return questions;
    }

    public static boolean deleteQuestion(int id) {
        String sql = "DELETE FROM questions WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
