package org.example.musicscorebuilder.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.data.ScoreStorageService;

import java.io.*;

public class SongbookController {
    @FXML private Button openFolderButton;
    @FXML private Label folderPathLabel;
    @FXML private CheckBox compressedOnlyCheckBox;
    @FXML private ListView<String> jsonFilesListView;

    private final ObservableList<String> jsonFilesList = FXCollections.observableArrayList();
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStorageService storageService = new ScoreStorageService();
    private File currentDirectory;

    @FXML
    public void initialize() {
        jsonFilesListView.setItems(jsonFilesList);

        jsonFilesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedFileName = jsonFilesListView.getSelectionModel().getSelectedItem();
                if (selectedFileName != null && currentDirectory != null) {
                    openJsonFile(selectedFileName);
                }
            }
        });
    }

    @FXML
    private void handleOpenFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wybierz folder śpiewnika");

        Stage stage = (Stage) openFolderButton.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            this.currentDirectory = selectedDirectory;
            folderPathLabel.setText(selectedDirectory.getAbsolutePath());
            loadJsonFiles(selectedDirectory);
        }
    }

    @FXML
    private void handleFilterChange() {
        if (currentDirectory != null) {
            loadJsonFiles(currentDirectory);
        }
    }

    private void loadJsonFiles(File folder) {
        jsonFilesList.clear();

        FilenameFilter jsonFilter = (dir, name) -> name.toLowerCase().endsWith(".json") || name.toLowerCase().endsWith(".json.gz");
        File[] files = folder.listFiles(jsonFilter);

        boolean showCompressed = compressedOnlyCheckBox.isSelected();

        if (files != null) {
            for (File file : files) {
                boolean isGzip = isGzipCompressed(file);
                if (showCompressed == isGzip) {
                    jsonFilesList.add(file.getName());
                }
            }
        }
    }

    private boolean isGzipCompressed(File file) {
        if (!file.isFile() || !file.canRead()) {
            return false;
        }

        try (InputStream fis = new FileInputStream(file)) {
            byte[] magic = new byte[2];
            int read = fis.read(magic);
            return read == 2 && magic[0] == (byte) 0x1F && magic[1] == (byte) 0x8B;
        } catch (IOException e) {
            return false;
        }
    }

    private void openJsonFile(String fileName) {
        if (scoreService == null) {
            showErrorAlert("Błąd konfiguracji", "Serwisy (storageService / scoreService) nie zostały przekazane do SongbookController.");
            return;
        }

        File fileToOpen = new File(currentDirectory, fileName);

        if (fileToOpen.exists() && fileToOpen.isFile()) {
            try {
                Score score = storageService.loadFromJson(fileToOpen);
                scoreService.setScore(score);
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
            }
        }
    }



    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}