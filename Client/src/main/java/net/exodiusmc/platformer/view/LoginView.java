package net.exodiusmc.platformer.view;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Macjuul
 * @version 1.0.0
 * @since 08/02/2017
 */
public class LoginView implements View {

    @Override
    public void apply(Stage window) {
        window.setResizable(false);

        window.setScene(new Scene(View.templateFromFile("login", this)));

    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        System.out.println("LoginView - Loading content");
    }
}
