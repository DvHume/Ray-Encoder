package org.quark.ray.controller;

/*
 * Copyright (c) 2026 DvHume
 *
 * Licensed under the MIT License.
 * See the LICENSE file in the project root for license information
 */

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.quark.ray.I18n;
import org.quark.ray.core.CryptCore;
import org.quark.ray.core.Result;

public class MainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cryptButt;

    @FXML
    private Button decryptButt;

    @FXML
    private PasswordField inputMasterPassword;

    @FXML
    private TextField inputSecret;

    @FXML
    private ComboBox<Locale> langBox;

    @FXML
    private Label status;

    @FXML
    private TextField outputField;

    @FXML
    void initialize() {
        langBox.getItems().addAll(I18n.SUPP_LOCALES);
        langBox.setCellFactory(lv -> new LocaleCell(true));
        langBox.setButtonCell(new LocaleCell(false));

        langBox.setOnAction(event -> {
            Locale selectedLocale = langBox.getValue();
            if (selectedLocale != null && !selectedLocale.equals(I18n.getCurrentLocale())) {
                I18n.setLocale(selectedLocale);
                reloadScene();
            }
        });

        cryptButt.setOnAction(event -> handleEncrypt());
        decryptButt.setOnAction(event -> handleDecrypt());
    }

    /**
     * ENCRYPTION
     */
    private void handleEncrypt() {
        String secret = inputSecret.getText();
        String password = inputMasterPassword.getText();
        outputField.clear();
        Task<Result> task = new Task<>() {
            @Override
            protected Result call() throws Exception {
                return CryptCore.encrypt(secret, password);
            }
        };
        task.setOnSucceeded(event -> processResult(task.getValue()));
        new Thread(task).start();
    }

    /**
     * DECRYPTION
     */
    private void handleDecrypt() {
        String b64Data = inputSecret.getText();
        String password = inputMasterPassword.getText();
        outputField.clear();
        Task<Result> task = new Task<>() {
            @Override
            protected Result call() throws Exception {
                return CryptCore.decrypt(b64Data, password);
            }
        };
        task.setOnSucceeded(event -> processResult(task.getValue()));
        new Thread(task).start();
    }

    private void processResult(Result result) {
        if (result.success()) {
            outputField.setText(result.data());
            showStatus(result.msg(), true);
        } else {
            outputField.clear();
            showStatus(result.msg(), false);
        }
    }

    private void showStatus(String message, boolean isSuccess) {
        status.setText(message);
        if (isSuccess) {
            status.setTextFill(Color.web("#2e7d32"));
        } else {
            status.setTextFill(Color.web("#d32f2f"));
        }
    }

    private void reloadScene() {
        try {

            Stage stage = (Stage) langBox.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scene.fxml"), I18n.getBundle());
            stage.getScene().setRoot(loader.load());
            stage.setTitle(I18n.get("app.title"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class LocaleCell extends ListCell<Locale> {
        private final boolean showGlobe;

        public LocaleCell(boolean showGlobe) {
            this.showGlobe = showGlobe;
        }
        @Override
        protected void updateItem(Locale item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                String langName = item.getDisplayLanguage(item);
                String formatted = langName.substring(0, 1).toUpperCase() + langName.substring(1);
                setText("🌐 " + formatted);
            }
        }
    }
}

