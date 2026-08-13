package org.example.musicscorebuilder.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.musicscorebuilder.data.FileService;
import org.example.musicscorebuilder.data.PreferencesService;
import org.example.musicscorebuilder.data.StorageService;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SongbookController {
    @FXML private Button openFolderButton;
    @FXML private Label folderPathLabel;
    @FXML private CheckBox compressedOnlyCheckBox;
    @FXML private ListView<String> jsonFilesListView;

    private final ObservableList<String> jsonFilesList = FXCollections.observableArrayList();
    private final StorageService storageService = StorageService.getInstance();
    private final FileService fileService = FileService.getInstance();

    @FXML
    public void initialize() {
        jsonFilesListView.setItems(jsonFilesList);

        jsonFilesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Optional.ofNullable(jsonFilesListView.getSelectionModel().getSelectedItem())
                        .flatMap(fileService::getFileFromSongbookDir)
                        .ifPresent(this::loadScoreFileSafely);
            }
        });
        loadSavedDirectory();
    }

    @FXML
    private void handleOpenFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wybierz folder śpiewnika");

        PreferencesService.getDirectoryFile().ifPresent(directoryChooser::setInitialDirectory);

        Stage stage = (Stage) openFolderButton.getScene().getWindow();
        Optional.ofNullable(directoryChooser.showDialog(stage))
                .ifPresent(directory -> {
                    folderPathLabel.setText(directory.getAbsolutePath());
                    loadJsonFiles(directory);
                    PreferencesService.saveDirectoryPath(directory.getAbsolutePath());
                });
    }

    @FXML
    private void handleFilterChange() {
        PreferencesService.getDirectoryFile().ifPresent(this::loadJsonFiles);
    }

    private void loadJsonFiles(File folder) {
        jsonFilesList.clear();
        jsonFilesList.addAll(fileService.getJsonFileNames(folder, compressedOnlyCheckBox.isSelected()));
    }

    private void loadScoreFileSafely(File file) {
        try {
            storageService.loadScoreFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
        }
    }

    private void loadSavedDirectory() {
        PreferencesService.getDirectoryFile()
                .ifPresent(directory -> {
                    folderPathLabel.setText(directory.getAbsolutePath());
                    loadJsonFiles(directory);
                });
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}