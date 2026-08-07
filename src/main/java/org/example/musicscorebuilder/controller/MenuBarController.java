package org.example.musicscorebuilder.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.dialog.CustomConfirmationDialog;

public class MenuBarController {

    @FXML
    private void handleNew(ActionEvent event) {
        System.out.println("Tworzenie nowego pliku");
    }

    @FXML
    private void handleOpen(ActionEvent event) {
        System.out.println("Otwieranie pliku");
    }

    @FXML
    private void handleClose(ActionEvent event) {
        System.out.println("Zamykanie pliku");
    }

    @FXML
    private void handleSave(ActionEvent event) {
        System.out.println("Zapisz");
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
        String scoreName = ScoreService.getInstance().getScore().getTitle();

        new CustomConfirmationDialog()
                .setTitle("MusicScore Builder")
                .setHeader("Chcesz zapisać zmiany w partyturze „" + scoreName + "” przed zamknięciem?")
                .setContent("Twoje zmiany zostaną utracone, jeśli ich nie zapiszesz.")
                .setConfirmButton("Zapisz", () -> {
                    handleSave(event);
                    Platform.exit();
                    System.exit(0);
                })
                .setDenyButton("Nie zapisuj", () -> {
                    Platform.exit();
                    System.exit(0);
                })
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
}