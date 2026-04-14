module com.example.quizsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.google.gson;
    requires java.desktop;

    opens com.example.quizsystem.controller to javafx.fxml;
    exports com.example.quizsystem;

    opens com.example.quizsystem.model to com.google.gson;
}
