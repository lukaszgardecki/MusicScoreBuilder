package org.example.musicscorebuilder.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.musicscorebuilder.components.dialog.CustomConfirmationDialog;
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
    @FXML private Button deleteButton;

    private final ObservableList<SongbookItem> jsonFilesList = FXCollections.observableArrayList();
    private final StorageService storageService = StorageService.getInstance();
    private final FileService fileService = FileService.getInstance();

    @FXML
    public void initialize() {
        setupListView();
        setupKeyBindings();
        setupDeleteButtonState();
        loadSavedDirectory();
    }

    private void setupListView() {
        jsonFilesListView.setItems(jsonFilesList);
        setupCellFactory();
        setupMouseNavigation();
    }

    private void setupCellFactory() {
        jsonFilesListView.setCellFactory(param -> {
            SongbookListCell cell = new SongbookListCell();

            ContextMenu contextMenu = new ContextMenu();

            MenuItem renameMenuItem = new MenuItem("Zmień nazwę");
            renameMenuItem.setOnAction(e -> handleRename());

            MenuItem deleteMenuItem = new MenuItem("Usuń");
            deleteMenuItem.setOnAction(e -> handleDelete());

            contextMenu.getItems().addAll(renameMenuItem, deleteMenuItem);

            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null || newItem.type() == SongbookItem.Type.PARENT_DIR) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell;
        });
    }

    private void setupMouseNavigation() {
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
    }

    private void setupKeyBindings() {
        jsonFilesListView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (newScene.getFocusOwner() instanceof TextInputControl) return;

                    SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
                    if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;

                    if (event.getCode() == KeyCode.DELETE) {
                        handleDelete();
                        event.consume();
                    } else if (event.getCode() == KeyCode.F2) {
                        handleRename();
                        event.consume();
                    }
                });
            }
        });
    }

    private void setupDeleteButtonState() {
        if (deleteButton == null) return;

        deleteButton.setDisable(true);
        jsonFilesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDeletable = newVal != null && newVal.type() != SongbookItem.Type.PARENT_DIR;
            deleteButton.setDisable(!isDeletable);
        });
    }

    @FXML
    private void handleRename() {
        SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) {
            return;
        }

        File oldFile = selected.file();
        if (oldFile == null || !oldFile.exists()) {
            return;
        }

        boolean isDirectory = selected.type() == SongbookItem.Type.DIRECTORY;
        String itemType = isDirectory ? "folderu" : "pliku";
        String extension = "";
        String baseName = oldFile.getName();

        if (!isDirectory) {
            String lowerName = baseName.toLowerCase();
            if (lowerName.endsWith(".json.gz")) {
                extension = ".json.gz";
                baseName = baseName.substring(0, baseName.length() - ".json.gz".length());
            } else if (lowerName.endsWith(".json")) {
                extension = ".json";
                baseName = baseName.substring(0, baseName.length() - ".json".length());
            } else {
                int lastDotIndex = baseName.lastIndexOf('.');
                if (lastDotIndex > 0) {
                    extension = baseName.substring(lastDotIndex);
                    baseName = baseName.substring(0, lastDotIndex);
                }
            }
        }

        String currentInput = baseName;

        while (true) {
            TextInputDialog dialog = new TextInputDialog(currentInput);
            dialog.setTitle("Zmiana nazwy");
            dialog.setHeaderText("Zmiana nazwy " + itemType);
            dialog.setContentText("Podaj nową nazwę:");

            Optional<String> result = dialog.showAndWait().map(String::trim);
            if (result.isEmpty()) break;

            currentInput = result.get();
            if (currentInput.isEmpty()) {
                showErrorAlert("Błąd", "Nazwa nie może być pusta!");
                continue;
            }

            String finalFileName = isDirectory ? currentInput : (currentInput + extension);
            File targetFile = new File(oldFile.getParentFile(), finalFileName);

            if (targetFile.equals(oldFile)) break;

            if (targetFile.exists()) {
                showErrorAlert("Błąd", "Element o nazwie '" + targetFile.getName() + "' już istnieje!");
                continue;
            }

            if (oldFile.renameTo(targetFile)) {
                PreferencesService.getDirectoryFile().ifPresent(this::loadJsonFiles);
                selectItemByFile(targetFile);
                break;
            } else {
                showErrorAlert("Błąd", "Nie udało się zmienić nazwy na dysku.");
                break;
            }
        }
    }

    @FXML
    private void handleDelete() {
        SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) {
            return;
        }

        File fileToDelete = selected.file();
        if (fileToDelete == null || !fileToDelete.exists()) {
            return;
        }

        boolean isDirectory = selected.type() == SongbookItem.Type.DIRECTORY;
        String itemType = isDirectory ? "folder" : "plik";
        String warningText = isDirectory
                ? "\n\nUWAGA: Folder zostanie usunięty wraz z całą zawartością!"
                : "";
        String trashSvgPath = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";

        new CustomConfirmationDialog()
                .setTitle("Potwierdzenie usunięcia")
                .setHeader("Czy na pewno chcesz usunąć ten " + itemType + "?")
                .setContent(selected.displayName() + warningText)
                .setIconSvg(trashSvgPath, "#DC2626")
                .setConfirmButton("Usuń", () -> {
                    if (fileService.deleteRecursively(fileToDelete)) {
                        PreferencesService.getDirectoryFile().ifPresent(this::loadJsonFiles);
                    } else {
                        showErrorAlert("Błąd usuwania", "Nie udało się usunąć elementu: " + selected.displayName());
                    }
                })
                .setCancelButton("Anuluj", null)
                .showAndWait();
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
        String currentInput = "";

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
        String currentInput = "";

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
        String errorSvgPath = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z";

        new CustomConfirmationDialog()
                .setTitle(title)
                .setHeader(title)
                .setContent(message)
                .setIconSvg(errorSvgPath, "#EF4444")
                .setConfirmButton("OK", null)
                .showAndWait();
    }
}