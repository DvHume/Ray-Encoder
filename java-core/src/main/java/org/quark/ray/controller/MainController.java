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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.quark.ray.I18n;

public class MainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button cryptButt;

    @FXML
    private PasswordField inputMasterPassword;

    @FXML
    private TextField inputSecret;

    @FXML
    private ComboBox<Locale> langBox;

    @FXML
    private Label status;

    @FXML
    void initialize() {
        langBox.getItems().addAll(I18n.SUPP_LOCALES);
        langBox.setCellFactory(lv -> new LocaleCell());
        langBox.setButtonCell(new LocaleCell());
        langBox.setValue(I18n.getCurrentLocale());

        langBox.setOnAction(event -> {
            Locale selectedLocale = langBox.getValue();
            if (selectedLocale != null && !selectedLocale.equals(I18n.getCurrentLocale())) {
                I18n.setLocale(selectedLocale);
                //reloadScene;
            }
        });
    }

    private static class LocaleCell extends ListCell<Locale> {
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

