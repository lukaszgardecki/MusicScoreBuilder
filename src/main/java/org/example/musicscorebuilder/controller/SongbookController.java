package org.example.musicscorebuilder.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.util.ScoreFactory;
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
    @FXML private ListView<SongbookItem> jsonFilesListView;

    private final ObservableList<SongbookItem> jsonFilesList = FXCollections.observableArrayList();
    private final StorageService storageService = StorageService.getInstance();
    private final FileService fileService = FileService.getInstance();

    @FXML
    public void initialize() {
        jsonFilesListView.setItems(jsonFilesList);
        jsonFilesListView.setCellFactory(param -> new SongbookListCell());

        jsonFilesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
                if (selected == null) return;

                switch (selected.type()) {
                    case PARENT_DIR, DIRECTORY -> navigateToDirectory(selected.file());
                    case FILE -> loadScoreFileSafely(selected.file());
                }
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
                .ifPresent(this::navigateToDirectory);
    }

    @FXML
    private void handleFilterChange() {
        PreferencesService.getDirectoryFile().ifPresent(this::loadJsonFiles);
    }

    @FXML
    private void handleAddFolder() {
        Optional<File> parentDirOpt = PreferencesService.getDirectoryFile();
        if (parentDirOpt.isEmpty()) {
            showErrorAlert("Brak folderu", "Najpierw wybierz folder śpiewnika!");
            return;
        }

        File parentDir = parentDirOpt.get();
        String currentInput = "NowyFolder";

        while (true) {
            TextInputDialog dialog = new TextInputDialog(currentInput);
            dialog.setTitle("Nowy folder");
            dialog.setHeaderText("Tworzenie nowego podfolderu w śpiewniku");
            dialog.setContentText("Podaj nazwę folderu:");

            Optional<String> result = dialog.showAndWait().map(String::trim);
            if (result.isEmpty()) break;

            currentInput = result.get();
            if (currentInput.isEmpty()) {
                showErrorAlert("Błąd", "Nazwa folderu nie może być pusta!");
                continue;
            }

            File newDir = new File(parentDir, currentInput);
            if (newDir.exists()) {
                showErrorAlert("Błąd", "Folder o nazwie '" + currentInput + "' już istnieje!");
            } else if (newDir.mkdirs()) {
                loadJsonFiles(parentDir);
                selectItemByFile(newDir);
                break;
            } else {
                showErrorAlert("Błąd", "Nie udało się utworzyć folderu na dysku.");
            }
        }
    }

    @FXML
    private void handleAddFile() {
        Optional<File> parentDirOpt = PreferencesService.getDirectoryFile();
        if (parentDirOpt.isEmpty()) {
            showErrorAlert("Brak folderu", "Najpierw wybierz folder śpiewnika!");
            return;
        }

        File parentDir = parentDirOpt.get();
        String currentInput = "NowyUtwor";

        while (true) {
            TextInputDialog dialog = new TextInputDialog(currentInput);
            dialog.setTitle("Nowy plik");
            dialog.setHeaderText("Tworzenie nowego utworu (.json)");
            dialog.setContentText("Podaj nazwę pliku:");

            Optional<String> result = dialog.showAndWait().map(String::trim);

            if (result.isEmpty()) break;

            currentInput = result.get();
            if (currentInput.isEmpty()) {
                showErrorAlert("Błąd", "Nazwa pliku nie może być pusta!");
                continue;
            }

            if (createAndSaveScore(parentDir, currentInput)) {
                break;
            }
        }
    }

    private boolean createAndSaveScore(File parentDir, String inputName) {
        String cleanTitle = inputName.replaceAll("(?i)\\.json(\\.gz)?$", "");
        Score defaultScore = ScoreFactory.createScoreTemplate();
        defaultScore.setTitle(cleanTitle);

        File targetFile = fileService.getTargetFile(defaultScore, parentDir);
        if (targetFile.exists()) {
            showErrorAlert("Błąd", "Plik o nazwie '" + targetFile.getName() + "' już istnieje!");
            return false;
        }

        try {
            storageService.saveScoreFile(defaultScore, parentDir);
            loadJsonFiles(parentDir);
            selectItemByFile(targetFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd zapisu", "Nie udało się utworzyć nowego pliku: " + e.getMessage());
            return false;
        }
    }

    private void loadJsonFiles(File folder) {
        jsonFilesList.clear();
        jsonFilesList.addAll(fileService.getDirectoryContent(folder, compressedOnlyCheckBox.isSelected()));
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
                .ifPresent(this::navigateToDirectory);
    }

    private void navigateToDirectory(File dir) {
        folderPathLabel.setText(dir.getAbsolutePath());
        PreferencesService.saveDirectoryPath(dir.getAbsolutePath());
        loadJsonFiles(dir);
    }

    private void selectItemByFile(File file) {
        jsonFilesList.stream()
                .filter(item -> item.file() != null && item.file().equals(file))
                .findFirst()
                .ifPresent(item -> jsonFilesListView.getSelectionModel().select(item));
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}