package org.example.musicscorebuilder.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuBar;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.example.musicscorebuilder.components.music.util.ScoreFactory;
import org.example.musicscorebuilder.data.FileService;
import org.example.musicscorebuilder.data.StorageService;
import org.example.musicscorebuilder.managers.ClosingManager;

import java.io.IOException;
import java.util.Optional;

public class MenuBarController {
    @FXML MenuBar menuBar;
    private final StorageService storageService = StorageService.getInstance();
    private final FileService fileService = FileService.getInstance();
    private final ClosingManager closingManager = ClosingManager.getInstance();

    @FXML
    private void handleNew(ActionEvent event) {
        boolean closed = closingManager.closeScore();
        if (closed) StorageService.getInstance().setScore(ScoreFactory.createEmptySoloTemplate());
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otwórz");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));

        fileService.getCurrentProjectFile().ifPresent(fileChooser::setInitialDirectory);

        Window ownerWindow = (menuBar != null && menuBar.getScene() != null)
                ? menuBar.getScene().getWindow()
                : null;

        Optional.ofNullable(fileChooser.showOpenDialog(ownerWindow))
                .ifPresent(file -> {
                    try {
                        storageService.loadScoreFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
                    }
                });
    }

    @FXML
    private void handleClose(ActionEvent event) {
        closingManager.closeScore();
    }

    @FXML
    private void handleSave(ActionEvent event) {
        try {
            storageService.saveCurrentScoreFile();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd zapisu", "Nie udało się zapisać partytury: " + e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            showErrorAlert("Błąd zapisu", "Brak aktywnej partytury do zapisania.");
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
        closingManager.closeApp();
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
}