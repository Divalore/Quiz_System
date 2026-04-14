package com.example.quizsystem.controller;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(){
        String user = usernameField.getText();
        String pass= passwordField.getText();
        try{
            FXMLLoader loader;
            Scene scene;
            Stage stage = (Stage) usernameField.getScene().getWindow();
            if(user.equals("admin")&& pass.equals("1234")){
                loader=new FXMLLoader(getClass().getResource(
                        "/com/example/quizsystem/view/admin_dashboard.fxml"
                ));
            }
            else if(user.equals("student")&& pass.equals("1234")){
                loader= new FXMLLoader(getClass().getResource("" +
                        "/com/example/quizsystem/view/student_dashboard.fxml"));
            }
            else{
                System.out.println("Invalid Login");
                return;
            }
            scene= new Scene(loader.load());
            stage.setScene(scene);
        } catch (Exception e) {
             e.printStackTrace();
        }

    }
}
