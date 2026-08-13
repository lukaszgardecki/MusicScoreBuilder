package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;

        File oldFile = selected.file();
        if (oldFile == null || !oldFile.exists()) return;

        boolean isDirectory = selected.type() == SongbookItem.Type.DIRECTORY;
        SongbookFileHelper.FileInfo fileInfo = SongbookFileHelper.extractFileInfo(oldFile, isDirectory);

        String currentInput = fileInfo.baseName();
        String iconPath = isDirectory ? SongbookDialogHelper.SVG_FOLDER : SongbookDialogHelper.SVG_RENAME;
        String iconColor = isDirectory ? SongbookDialogHelper.COLOR_AMBER : SongbookDialogHelper.COLOR_BLUE;
        String itemType = isDirectory ? "folderu" : "pliku";

        while (true) {
            Optional<String> result = SongbookDialogHelper.showInputDialog(
                    "Zmiana nazwy",
                    "Zmiana nazwy " + itemType,
                    "Wprowadź nową nazwę dla wybranego elementu:",
                    currentInput,
                    iconPath,
                    iconColor,
                    "Zmień"
            );

            if (result.isEmpty()) break;

            currentInput = result.get();
            if (currentInput.isEmpty()) {
                showErrorAlert("Błąd", "Nazwa nie może być pusta!");
                continue;
            }

            String finalFileName = isDirectory ? currentInput : (currentInput + fileInfo.extension());
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
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;

        File fileToDelete = selected.file();
        if (fileToDelete == null || !fileToDelete.exists()) return;

        boolean isDirectory = selected.type() == SongbookItem.Type.DIRECTORY;
        String itemType = isDirectory ? "folder" : "plik";

        SongbookDialogHelper.showDeleteConfirmation(
                itemType,
                selected.displayName(),
                isDirectory,
                () -> {
                    if (fileService.deleteRecursively(fileToDelete)) {
                        PreferencesService.getDirectoryFile().ifPresent(this::loadJsonFiles);
                    } else {
                        showErrorAlert("Błąd usuwania", "Nie udało się usunąć elementu: " + selected.displayName());
                    }
                }
        );
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
            Optional<String> result = SongbookDialogHelper.showInputDialog(
                    "Nowy folder",
                    "Tworzenie nowego podfolderu w śpiewniku",
                    "Podaj nazwę nowego folderu:",
                    currentInput,
                    SongbookDialogHelper.SVG_ADD_FOLDER,
                    SongbookDialogHelper.COLOR_AMBER,
                    "Utwórz"
            );

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
            Optional<String> result = SongbookDialogHelper.showInputDialog(
                    "Nowy plik",
                    "Tworzenie nowego utworu",
                    "Podaj nazwę nowego pliku:",
                    currentInput,
                    SongbookDialogHelper.SVG_ADD_FILE,
                    SongbookDialogHelper.COLOR_BLUE,
                    "Utwórz"
            );

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
        PreferencesService.getDirectoryFile().ifPresent(this::navigateToDirectory);
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
        SongbookDialogHelper.showErrorAlert(title, message);
    }
}