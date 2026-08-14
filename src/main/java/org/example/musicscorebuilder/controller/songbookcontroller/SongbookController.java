package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    @FXML private ListView<SongbookItem> jsonFilesListView;
    @FXML private Button deleteButton;

    @FXML private VBox metadataContainer;
    @FXML private TextField numberField;
    @FXML private TextField oldNumberField;
    @FXML private TextField titleField;
    @FXML private TextField subtitleField;
    @FXML private TextField composerField;

    private final ObservableList<SongbookItem> jsonFilesList = FXCollections.observableArrayList();
    private final FileService fileService = FileService.getInstance();
    private final StorageService storageService = StorageService.getInstance();

    private final SongbookActionManager actionManager = new SongbookActionManager();
    private SongbookMetadataHandler metadataHandler;

    @FXML
    public void initialize() {
        metadataHandler = new SongbookMetadataHandler(
                metadataContainer, numberField, oldNumberField, titleField, subtitleField, composerField
        );

        metadataHandler.init(this::refreshDirectoryAndSelect);
        setupListView();
        setupKeyBindings();
        setupDeleteButtonState();
        loadSavedDirectory();
    }

    @FXML private void handleCopy() { actionManager.handleCopy(getSelectedItem()); }
    @FXML private void handlePaste() { actionManager.handlePaste(this::refreshDirectory, this::selectItemByFile); }
    @FXML private void handleDuplicate() { actionManager.handleDuplicate(getSelectedItem(), this::refreshDirectory, this::selectItemByFile); }
    @FXML private void handleRename() { actionManager.handleRename(getSelectedItem(), metadataHandler.getCurrentOpenedFile(), this::refreshDirectory, this::selectItemByFile, metadataHandler::setCurrentOpenedFile); }
    @FXML private void handleDelete() { actionManager.handleDelete(getSelectedItem(), metadataHandler.getCurrentOpenedFile(), this::refreshDirectory, metadataHandler::clearAndDisable); }
    @FXML private void handleAddFolder() { actionManager.handleAddFolder(this::refreshDirectory, this::selectItemByFile); }
    @FXML private void handleAddFile() { actionManager.handleAddFile(this::refreshDirectory, this::selectItemByFile, this::loadScoreFileSafely); }
    @FXML private void handleFilterChange() { refreshDirectory(); }

    @FXML
    private void handleOpenFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wybierz folder śpiewnika");
        PreferencesService.getDirectoryFile().ifPresent(directoryChooser::setInitialDirectory);

        Stage stage = (Stage) openFolderButton.getScene().getWindow();
        Optional.ofNullable(directoryChooser.showDialog(stage)).ifPresent(this::navigateToDirectory);
    }

    private void setupListView() {
        jsonFilesListView.setItems(jsonFilesList);
        jsonFilesListView.setContextMenu(SongbookContextMenuFactory.createEmptyAreaContextMenu(this::handlePaste, actionManager::isPasteDisabled));

        jsonFilesListView.setCellFactory(param -> {
            SongbookListCell cell = new SongbookListCell();
            ContextMenu itemContextMenu = SongbookContextMenuFactory.createItemContextMenu(
                    this::handleCopy, this::handlePaste, this::handleDuplicate, this::handleRename, this::handleDelete, actionManager::isPasteDisabled
            );

            cell.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    cell.setContextMenu(null);
                } else if (newItem.type() == SongbookItem.Type.PARENT_DIR) {
                    cell.setContextMenu(SongbookContextMenuFactory.createParentDirContextMenu(this::handlePaste, actionManager::isPasteDisabled));
                } else {
                    cell.setContextMenu(itemContextMenu);
                }
            });

            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty() && cell.getItem() != null) {
                    openItem(cell.getItem());
                }
            });

            return cell;
        });

        jsonFilesListView.setOnMouseClicked(event -> jsonFilesListView.requestFocus());
    }

    private void setupKeyBindings() {
        SongbookKeyHandler.attachKeyBindings(jsonFilesListView, new SongbookKeyHandler.KeyActions() {
            @Override public void onCopy() { handleCopy(); }
            @Override public void onPaste() { handlePaste(); }
            @Override public void onDuplicate() { handleDuplicate(); }
            @Override public void onRename() { handleRename(); }
            @Override public void onDelete() { handleDelete(); }
            @Override public void onOpenSelected() { openItem(getSelectedItem()); }
            @Override public void onNavigateUp() { navigateUp(); }
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
            if (parentDir != null && parentDir.exists()) navigateToDirectory(parentDir);
        });
    }

    private void loadScoreFileSafely(File file) {
        try {
            storageService.loadScoreFile(file);
            metadataHandler.updatePanel(file);
        } catch (IOException e) {
            e.printStackTrace();
            SongbookDialogHelper.showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
        }
    }

    private void refreshDirectory() {
        PreferencesService.getDirectoryFile().ifPresent(this::loadJsonFiles);
    }

    private void refreshDirectoryAndSelect(File fileToSelect) {
        refreshDirectory();
        selectItemByFile(fileToSelect);
    }

    private void loadSavedDirectory() {
        PreferencesService.getDirectoryFile().ifPresent(this::navigateToDirectory);
    }

    private void navigateToDirectory(File dir) {
        folderPathLabel.setText(dir.getAbsolutePath());
        PreferencesService.saveDirectoryPath(dir.getAbsolutePath());
        loadJsonFiles(dir);
    }

    private void loadJsonFiles(File folder) {
        jsonFilesList.clear();
        jsonFilesList.addAll(fileService.getDirectoryContent(folder, compressedOnlyCheckBox.isSelected()));
        if (!jsonFilesList.isEmpty()) jsonFilesListView.getSelectionModel().select(0);
        Platform.runLater(() -> jsonFilesListView.requestFocus());
    }

    private void selectItemByFile(File file) {
        jsonFilesList.stream()
                .filter(item -> item.file() != null && item.file().equals(file))
                .findFirst()
                .ifPresent(item -> jsonFilesListView.getSelectionModel().select(item));
    }

    private void setupDeleteButtonState() {
        if (deleteButton == null) return;
        deleteButton.setDisable(true);
        jsonFilesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteButton.setDisable(newVal == null || newVal.type() == SongbookItem.Type.PARENT_DIR);
        });
    }

    private SongbookItem getSelectedItem() {
        return jsonFilesListView.getSelectionModel().getSelectedItem();
    }
}