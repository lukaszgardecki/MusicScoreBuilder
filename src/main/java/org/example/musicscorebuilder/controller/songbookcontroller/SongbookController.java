package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.util.ScoreFactory;
import org.example.musicscorebuilder.data.FileService;
import org.example.musicscorebuilder.data.PreferencesService;
import org.example.musicscorebuilder.data.StorageService;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class SongbookController {
    @FXML private Button openFolderButton;
    @FXML private Label folderPathLabel;
    @FXML private CheckBox compressedOnlyCheckBox;
    @FXML private ListView<SongbookItem> jsonFilesListView;
    @FXML private Button deleteButton;

    @FXML private VBox metadataContainer;
    @FXML private TextField numberField;
    @FXML private TextField oldNumberField;
    @FXML private TextField titleField;
    @FXML private TextField subtitleField;
    @FXML private TextField composerField;

    private final ObservableList<SongbookItem> jsonFilesList = FXCollections.observableArrayList();
    private final StorageService storageService = StorageService.getInstance();
    private final FileService fileService = FileService.getInstance();
    private File currentOpenedFile = null;
    private File copiedFile = null;
    private boolean isUpdatingFields = false;
    private final PauseTransition liveUpdateDebounce = new PauseTransition(Duration.millis(200));

    @FXML
    public void initialize() {
        setupListView();
        setupKeyBindings();
        setupDeleteButtonState();
        setupPlaceholders();
        setupMetadataAutoSave();
        clearAndDisableMetadataFields();
        loadSavedDirectory();
    }

    @FXML
    private void handleCopy() {
        SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.type() == SongbookItem.Type.PARENT_DIR) return;

        File fileToCopy = selected.file();
        if (fileToCopy != null && fileToCopy.exists()) {
            this.copiedFile = fileToCopy;
        }
    }

    @FXML
    private void handlePaste() {
        if (copiedFile == null || !copiedFile.exists()) {
            showErrorAlert("Błąd wklejania", "Brak skopiowanego pliku lub folderu.");
            return;
        }

        Optional<File> currentDirOpt = PreferencesService.getDirectoryFile();
        if (currentDirOpt.isEmpty()) {
            showErrorAlert("Błąd zapisu", "Najpierw wybierz folder śpiewnika!");
            return;
        }

        File currentDir = currentDirOpt.get();
        File targetFile = generateUniqueCopyFile(currentDir, copiedFile);

        try {
            if (copiedFile.isDirectory() && targetFile.getCanonicalPath().startsWith(copiedFile.getCanonicalPath() + File.separator)) {
                showErrorAlert("Błąd kopiowania", "Nie można skopiować folderu do jego własnego podfolderu!");
                return;
            }

            copyRecursively(copiedFile, targetFile);
            loadJsonFiles(currentDir);
            selectItemByFile(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd kopiowania", "Nie udało się wkleić elementu: " + e.getMessage());
        }
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
                if (oldFile.equals(currentOpenedFile)) {
                    currentOpenedFile = targetFile;
                }
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
                        if (fileToDelete.equals(currentOpenedFile)) {
                            clearAndDisableMetadataFields();
                        }
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

    private void setMetadataFieldsDisabled(boolean disabled) {
        numberField.setDisable(disabled);
        oldNumberField.setDisable(disabled);
        titleField.setDisable(disabled);
        subtitleField.setDisable(disabled);
        composerField.setDisable(disabled);
    }

    private void setupPlaceholders() {
        numberField.setPromptText("123");
        oldNumberField.setPromptText("123");
        titleField.setPromptText("Tytuł");
        subtitleField.setPromptText("Podtytuł");
        composerField.setPromptText("Kompozytor");
    }

    private void setupListView() {
        jsonFilesListView.setItems(jsonFilesList);
        setupCellFactory();
        setupMouseNavigation();
        setupListViewContextMenu();
    }

    private void setupListViewContextMenu() {
        ContextMenu emptyAreaContextMenu = new ContextMenu();
        MenuItem pasteMenuItem = new MenuItem("Wklej");
        pasteMenuItem.setOnAction(e -> handlePaste());

        emptyAreaContextMenu.getItems().add(pasteMenuItem);
        emptyAreaContextMenu.setOnShowing(e -> pasteMenuItem.setDisable(copiedFile == null));

        jsonFilesListView.setContextMenu(emptyAreaContextMenu);
    }

    private void setupMetadataAutoSave() {
        liveUpdateDebounce.setOnFinished(e -> {
            if (isUpdatingFields || currentOpenedFile == null) return;

            Score activeScore = storageService.getScore();
            if (activeScore != null) {
                applyFieldsToScore(activeScore);
                ScoreStateManager.getInstance().notifyScoreChanged();
            }
        });

        ChangeListener<String> liveUpdateListener = (obs, oldVal, newVal) -> {
            if (isUpdatingFields || currentOpenedFile == null) return;
            liveUpdateDebounce.playFromStart();
        };

        numberField.textProperty().addListener(liveUpdateListener);
        oldNumberField.textProperty().addListener(liveUpdateListener);
        titleField.textProperty().addListener(liveUpdateListener);
        subtitleField.textProperty().addListener(liveUpdateListener);
        composerField.textProperty().addListener(liveUpdateListener);

        ChangeListener<Boolean> focusChangeListener = (obs, wasFocused, isFocused) -> {
            if (!isFocused && !isUpdatingFields && currentOpenedFile != null) {
                saveMetadataChanges();
            }
        };

        numberField.focusedProperty().addListener(focusChangeListener);
        oldNumberField.focusedProperty().addListener(focusChangeListener);
        titleField.focusedProperty().addListener(focusChangeListener);
        subtitleField.focusedProperty().addListener(focusChangeListener);
        composerField.focusedProperty().addListener(focusChangeListener);

        numberField.setOnAction(e -> saveMetadataChanges());
        oldNumberField.setOnAction(e -> saveMetadataChanges());
        titleField.setOnAction(e -> saveMetadataChanges());
        subtitleField.setOnAction(e -> saveMetadataChanges());
        composerField.setOnAction(e -> saveMetadataChanges());
    }

    private void setupCellFactory() {
        jsonFilesListView.setCellFactory(param -> {
            SongbookListCell cell = new SongbookListCell();

            ContextMenu itemContextMenu = new ContextMenu();
            MenuItem copyMenuItem = new MenuItem("Kopiuj");
            copyMenuItem.setOnAction(e -> handleCopy());

            MenuItem pasteMenuItem = new MenuItem("Wklej");
            pasteMenuItem.setOnAction(e -> handlePaste());

            MenuItem renameMenuItem = new MenuItem("Zmień nazwę");
            renameMenuItem.setOnAction(e -> handleRename());

            MenuItem deleteMenuItem = new MenuItem("Usuń");
            deleteMenuItem.setOnAction(e -> handleDelete());

            itemContextMenu.getItems().addAll(
                    copyMenuItem,
                    pasteMenuItem,
                    new SeparatorMenuItem(),
                    renameMenuItem,
                    deleteMenuItem
            );

            itemContextMenu.setOnShowing(e -> pasteMenuItem.setDisable(copiedFile == null));

            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    cell.setContextMenu(null);
                } else if (newItem.type() == SongbookItem.Type.PARENT_DIR) {
                    ContextMenu parentMenu = new ContextMenu();
                    MenuItem parentPasteItem = new MenuItem("Wklej");
                    parentPasteItem.setDisable(copiedFile == null);
                    parentPasteItem.setOnAction(e -> handlePaste());
                    parentMenu.getItems().add(parentPasteItem);
                    cell.setContextMenu(parentMenu);
                } else {
                    cell.setContextMenu(itemContextMenu);
                }
            });
            return cell;
        });
    }

    private void setupMouseNavigation() {
        jsonFilesListView.setOnMouseClicked(event -> {
            jsonFilesListView.requestFocus();
            if (event.getClickCount() == 2) {
                SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
                openItem(selected);
            }
        });
    }

    private void openItem(SongbookItem item) {
        if (item == null) return;
        switch (item.type()) {
            case PARENT_DIR, DIRECTORY -> navigateToDirectory(item.file());
            case FILE -> loadScoreFileSafely(item.file());
        }
    }

    private void navigateUp() {
        PreferencesService.getDirectoryFile().ifPresent(currentDir -> {
            File parentDir = currentDir.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                navigateToDirectory(parentDir);
            }
        });
    }

    private void loadScoreFileSafely(File file) {
        try {
            storageService.loadScoreFile(file);
            this.currentOpenedFile = file;
            updateMetadataPanel();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
        }
    }

    private void applyFieldsToScore(Score activeScore) {
        activeScore.setNumberNew(numberField.getText());
        activeScore.setNumberOld(oldNumberField.getText());
        activeScore.setTitle(titleField.getText());
        activeScore.setSubtitle(subtitleField.getText());
        activeScore.setComposer(composerField.getText());
    }

    private void saveMetadataChanges() {
        if (currentOpenedFile == null || isUpdatingFields) return;

        liveUpdateDebounce.stop();

        Score activeScore = storageService.getScore();
        if (activeScore == null) return;

        applyFieldsToScore(activeScore);

        File parentDir = currentOpenedFile.getParentFile();
        if (parentDir == null) return;

        File targetFile = fileService.getTargetFile(activeScore, parentDir);

        if (!targetFile.equals(currentOpenedFile) && targetFile.exists()) {
            showErrorAlert("Błąd zapisu", "Plik o nazwie '" + targetFile.getName() + "' już istnieje!");
            return;
        }

        try {
            if (!targetFile.equals(currentOpenedFile)) {
                fileService.deleteRecursively(currentOpenedFile);
            }

            storageService.saveScoreFile(activeScore, parentDir);
            this.currentOpenedFile = targetFile;

            ScoreStateManager.getInstance().notifyScoreChanged();

            loadJsonFiles(parentDir);
            selectItemByFile(targetFile);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd zapisu", "Nie udało się zapisać zmian metadanych: " + e.getMessage());
        }
    }

    private void updateMetadataPanel() {
        Score activeScore = storageService.getScore();
        if (activeScore != null && currentOpenedFile != null) {
            isUpdatingFields = true;
            try {
                numberField.setText(activeScore.getNumberNew() != null ? activeScore.getNumberNew() : "");
                oldNumberField.setText(activeScore.getNumberOld() != null ? activeScore.getNumberOld() : "");
                titleField.setText(activeScore.getTitle() != null ? activeScore.getTitle() : "");
                subtitleField.setText(activeScore.getSubtitle() != null ? activeScore.getSubtitle() : "");
                composerField.setText(activeScore.getComposer() != null ? activeScore.getComposer() : "");

                setMetadataFieldsDisabled(false);
                metadataContainer.setVisible(true);
            } finally {
                isUpdatingFields = false;
            }
        } else {
            clearAndDisableMetadataFields();
        }
    }

    private void clearAndDisableMetadataFields() {
        isUpdatingFields = true;
        try {
            currentOpenedFile = null;
            numberField.clear();
            oldNumberField.clear();
            titleField.clear();
            subtitleField.clear();
            composerField.clear();

            setMetadataFieldsDisabled(true);
            metadataContainer.setVisible(true);
        } finally {
            isUpdatingFields = false;
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
            loadScoreFileSafely(targetFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd zapisu", "Nie udało się utworzyć nowego pliku: " + e.getMessage());
            return false;
        }
    }

    private void setupKeyBindings() {
        jsonFilesListView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (newScene.getFocusOwner() instanceof TextInputControl) return;

                    int currentIndex = jsonFilesListView.getSelectionModel().getSelectedIndex();
                    int totalItems = jsonFilesListView.getItems().size();

                    boolean isShortcut = event.isControlDown() || event.isShortcutDown();

                    if (isShortcut && event.getCode() == KeyCode.C) {
                        handleCopy();
                        event.consume();
                    } else if (isShortcut && event.getCode() == KeyCode.V) {
                        handlePaste();
                        event.consume();
                    } else if (event.getCode() == KeyCode.UP) {
                        if (currentIndex > 0) {
                            int nextIndex = currentIndex - 1;
                            jsonFilesListView.getSelectionModel().select(nextIndex);
                            jsonFilesListView.scrollTo(nextIndex);
                            jsonFilesListView.requestFocus();
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.DOWN) {
                        if (currentIndex < totalItems - 1) {
                            int nextIndex = currentIndex + 1;
                            jsonFilesListView.getSelectionModel().select(nextIndex);
                            jsonFilesListView.scrollTo(nextIndex);
                            jsonFilesListView.requestFocus();
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.ENTER) {
                        SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            openItem(selected);
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.BACK_SPACE) {
                        navigateUp();
                        event.consume();
                    } else if (event.getCode() == KeyCode.DELETE) {
                        SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
                        if (selected != null && selected.type() != SongbookItem.Type.PARENT_DIR) {
                            handleDelete();
                        }
                        event.consume();
                    } else if (event.getCode() == KeyCode.F2) {
                        SongbookItem selected = jsonFilesListView.getSelectionModel().getSelectedItem();
                        if (selected != null && selected.type() != SongbookItem.Type.PARENT_DIR) {
                            handleRename();
                        }
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

    private void loadJsonFiles(File folder) {
        jsonFilesList.clear();
        jsonFilesList.addAll(fileService.getDirectoryContent(folder, compressedOnlyCheckBox.isSelected()));

        if (!jsonFilesList.isEmpty()) {
            jsonFilesListView.getSelectionModel().select(0);
        }

        Platform.runLater(() -> jsonFilesListView.requestFocus());
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

    private File generateUniqueCopyFile(File targetDir, File sourceFile) {
        String originalName = sourceFile.getName();
        File dest = new File(targetDir, originalName);

        if (!dest.exists()) {
            return dest;
        }

        boolean isDirectory = sourceFile.isDirectory();
        SongbookFileHelper.FileInfo info = SongbookFileHelper.extractFileInfo(sourceFile, isDirectory);
        String baseName = info.baseName();
        String ext = isDirectory ? "" : info.extension();

        String copyName = baseName + " - kopia" + ext;
        dest = new File(targetDir, copyName);
        int counter = 2;

        while (dest.exists()) {
            copyName = baseName + " - kopia (" + counter + ")" + ext;
            dest = new File(targetDir, copyName);
            counter++;
        }

        return dest;
    }

    private void copyRecursively(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists() && !target.mkdirs()) {
                throw new IOException("Nie udało się utworzyć folderu: " + target.getAbsolutePath());
            }
            File[] files = source.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyRecursively(file, new File(target, file.getName()));
                }
            }
        } else {
            File parentDir = target.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void showErrorAlert(String title, String message) {
        SongbookDialogHelper.showErrorAlert(title, message);
    }
}