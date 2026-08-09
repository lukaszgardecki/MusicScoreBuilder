package org.example.musicscorebuilder.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.dialog.CustomConfirmationDialog;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.controller.util.audio.PianoPlayer;
import org.example.musicscorebuilder.data.ScoreStorageService;

import java.io.File;
import java.io.IOException;

public class MenuBarController {
    @FXML MenuBar menuBar;
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStorageService storageService = new ScoreStorageService();

    @FXML
    private void handleNew(ActionEvent event) {
        System.out.println("Tworzenie nowego pliku");
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otwórz");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));

        File projectDir = new File(System.getProperty("user.dir"));
        if (projectDir.exists() && projectDir.isDirectory()) {
            fileChooser.setInitialDirectory(projectDir);
        }

        Window ownerWindow = (menuBar != null && menuBar.getScene() != null)
                ? menuBar.getScene().getWindow()
                : null;
        File selectedFile = fileChooser.showOpenDialog(ownerWindow);

        if (selectedFile != null) {
            try {
                Score score = storageService.loadFromJson(selectedFile);
                scoreService.setScore(score);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        System.out.println("Zamykanie pliku");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        Score score = scoreService.getScore();

        if (score == null) {
            showErrorAlert("Błąd zapisu", "Brak aktywnej partytury do zapisania.");
            return;
        }

//        String fileName = String.format("%s_%s_c.json", score.getNumberNew(), score.getTitle().replaceAll(" ", "-"));
        String fileName = String.format("%s_%s.json", score.getNumberNew(), score.getTitle().replaceAll(" ", "-"));
        File saveFile = new File(fileName);
        try {
//            storageService.saveToCompressedJson(score, saveFile);
            storageService.saveToJson(score, saveFile);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd zapisu", "Nie udało się zapisać partytury: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveAs(ActionEvent event) {
        System.out.println("Zapisz jako");
    }

    @FXML
    private void handleSaveCopy(ActionEvent event) {
        System.out.println("Zapisz kopię");
    }

    @FXML
    private void handleImportPdf(ActionEvent event) {
        System.out.println("Import");
    }

    @FXML
    private void handleExport(ActionEvent event) {
        System.out.println("Eksport");
    }

    @FXML
    private void handleProjectProperties(ActionEvent event) {
        System.out.println("Właściwości");
    }

    @FXML
    private void handlePrint(ActionEvent event) {
        System.out.println("Drukuj");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        String scoreName = scoreService.getScore().getTitle();

        new CustomConfirmationDialog()
                .setTitle("MusicScore Builder")
                .setHeader("Chcesz zapisać zmiany w partyturze „" + scoreName + "” przed zamknięciem?")
                .setContent("Twoje zmiany zostaną utracone, jeśli ich nie zapiszesz.")
                .setConfirmButton("Zapisz", () -> {
                    handleSave(event);
                    closeApp();
                })
                .setDenyButton("Nie zapisuj", this::closeApp)
                .setCancelButton("Anuluj", null)
                .showAndWait();
    }

    @FXML
    private void handleUndo(ActionEvent event) {
        System.out.println("Cofnij");
    }

    @FXML
    private void handleRedo(ActionEvent event) {
        System.out.println("Ponów");
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeApp() {
        PianoPlayer.getInstance().close();
        Platform.exit();
        System.exit(0);
    }
}