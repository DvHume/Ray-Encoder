package org.quark.ray;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */
public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/scene.fmx")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("Ray Encoder");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
