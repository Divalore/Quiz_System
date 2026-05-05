module com.example.quizsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.example.quizsystem.controller to javafx.fxml;
    exports com.example.quizsystem;

    opens com.example.quizsystem.model to com.google.gson, javafx.base;
}
