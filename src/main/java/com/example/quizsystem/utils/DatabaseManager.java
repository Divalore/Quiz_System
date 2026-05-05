package com.example.quizsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:quizsystem.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            createTables(stmt);
            seedDefaultAdmin(stmt);
            migrateDatabase(conn);
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "firstName TEXT, " +
            "lastName TEXT, " +
            "email TEXT UNIQUE, " +
            "password TEXT, " +
            "role TEXT, " +
            "parentName TEXT, " +
            "gender TEXT, " +
            "dob TEXT, " +
            "className TEXT)"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS exams (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "className TEXT, " +
            "subjectName TEXT, " +
            "durationMinutes INTEGER, " +
            "examCode TEXT UNIQUE)"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS questions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "examId INTEGER, " +
            "content TEXT, " +
            "optionA TEXT, " +
            "optionB TEXT, " +
            "optionC TEXT, " +
            "optionD TEXT, " +
            "correctAnswer TEXT, " +
            "questionType TEXT DEFAULT 'MCQ', " +
            "FOREIGN KEY(examId) REFERENCES exams(id))"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS results (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "studentId INTEGER, " +
            "examId INTEGER, " +
            "score INTEGER, " +
            "totalQuestions INTEGER, " +
            "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "isFullyGraded INTEGER DEFAULT 1, " +
            "FOREIGN KEY(studentId) REFERENCES users(id), " +
            "FOREIGN KEY(examId) REFERENCES exams(id))"
        );

        stmt.execute(
            "CREATE TABLE IF NOT EXISTS student_answers (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "resultId INTEGER, " +
            "studentId INTEGER, " +
            "examId INTEGER, " +
            "questionId INTEGER, " +
            "answerText TEXT, " +
            "isGraded INTEGER DEFAULT 0, " +
            "pointsEarned INTEGER DEFAULT 0, " +
            "FOREIGN KEY(resultId) REFERENCES results(id), " +
            "FOREIGN KEY(studentId) REFERENCES users(id), " +
            "FOREIGN KEY(examId) REFERENCES exams(id), " +
            "FOREIGN KEY(questionId) REFERENCES questions(id))"
        );
    }

    private static void seedDefaultAdmin(Statement stmt) throws SQLException {
        stmt.execute(
            "INSERT OR IGNORE INTO users (firstName, lastName, email, password, role) " +
            "VALUES ('System', 'Admin', 'admin@quiz.com', 'admin', 'ADMIN')"
        );
    }

    private static void migrateDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            runSilently(stmt, "ALTER TABLE questions ADD COLUMN questionType TEXT DEFAULT 'MCQ'");
            runSilently(stmt, "ALTER TABLE results ADD COLUMN timestamp DATETIME DEFAULT CURRENT_TIMESTAMP");
            runSilently(stmt, "ALTER TABLE results ADD COLUMN isFullyGraded INTEGER DEFAULT 1");
            runSilently(stmt, "ALTER TABLE student_answers ADD COLUMN resultId INTEGER");
            runSilently(stmt, "ALTER TABLE exams ADD COLUMN isKahoot INTEGER DEFAULT 0");
            runSilently(stmt, "ALTER TABLE questions ADD COLUMN points INTEGER DEFAULT 1");
        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
        }
    }

    /**
     * Runs a SQL statement silently, ignoring errors (used for ALTER TABLE migrations
     * where the column might already exist).
     */
    private static void runSilently(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException ignored) {
            // Column likely already exists — safe to ignore
        }
    }
}
