package org.quark.ray;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("lang.messages");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scene.fxml"), bundle);

        Scene scene = new Scene(loader.load());

        stage.setTitle(I18n.get("app.title"));
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setResizable(false);
        stage.setFullScreen(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/app_icon")));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
