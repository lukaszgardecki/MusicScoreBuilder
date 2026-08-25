package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.musicscorebuilder.components.dialog.CustomConfirmationDialog;
import org.example.musicscorebuilder.components.dialog.CustomSelectModeDialog;
import org.example.musicscorebuilder.components.music.Key;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.music.util.ScoreFactory;
import org.example.musicscorebuilder.data.FileService;
import org.example.musicscorebuilder.data.PreferencesService;
import org.example.musicscorebuilder.data.StorageService;
import org.example.musicscorebuilder.managers.ClosingManager;
import org.example.musicscorebuilder.managers.ScoreStateManager;
import org.example.musicscorebuilder.managers.TranspositionManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SongbookController {
    @FXML private CheckBox maxDownCheckBox;
    @FXML private HBox maxDownContainer;
    @FXML private Label maxDownLabel;

    @FXML private CheckBox maxUpCheckBox;
    @FXML private HBox maxUpContainer;
    @FXML private Label maxUpLabel;

    @FXML private Button transposeDownButton;
    @FXML private Label currentKeyLabel;
    @FXML private Button transposeUpButton;

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
    @FXML private ListView<ScoreMode> modesListView;
    @FXML private Button addModeButton;
    @FXML private Button deleteModeButton;

    private final ObservableList<SongbookItem> jsonFilesList = FXCollections.observableArrayList();
    private final FileService fileService = FileService.getInstance();
    private final StorageService storageService = StorageService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ClosingManager closingManager = ClosingManager.getInstance();
    private final SongbookExplorerManager songbookExplorerManager = SongbookExplorerManager.getInstance();

    private final SongbookActionManager actionManager = new SongbookActionManager();
    private final SongbookItemComparator itemComparator = new SongbookItemComparator();
    private SongbookMetadataHandler metadataHandler;
    private SongbookDragAndDropHandler dragAndDropHandler;

    private boolean isUpdatingModesList = false;

    @FXML
    public void initialize() {
        metadataHandler = new SongbookMetadataHandler(
                metadataContainer, numberField, oldNumberField, titleField, subtitleField, composerField
        );
        metadataHandler.init(this::refreshDirectoryAndSelect);
        dragAndDropHandler = new SongbookDragAndDropHandler(this::refreshDirectory);

        setupListView();
        setupKeyBindings();
        setupDeleteButtonState();
        setupModesSection();
        stateManager.addScoreChangeListener(this::updateTransposeUI);
        loadSavedDirectory();
        updateTransposeUI();
    }

    private void refreshModesList() {
        isUpdatingModesList = true;
        try {
            Score score = storageService.getScore();
            if (score == null || score.getModes() == null || score.getModes().isEmpty()) {
                modesListView.setItems(FXCollections.observableArrayList());
                deleteModeButton.setDisable(true);
                addModeButton.setDisable(score == null);
                return;
            }

            modesListView.setItems(FXCollections.observableArrayList(score.getModes()));

            int activeIndex = stateManager.getCurrentModeIndex();
            if (activeIndex >= 0 && activeIndex < score.getModes().size()) {
                modesListView.getSelectionModel().select(activeIndex);
            } else {
                modesListView.getSelectionModel().select(0);
            }

            deleteModeButton.setDisable(score.getModes().size() <= 1);
            addModeButton.setDisable(false);
        } finally {
            isUpdatingModesList = false;
        }
    }

    @FXML
    private void handleMaxDownCheckBoxChanged() {
        Score score = storageService.getScore();
        if (score == null) return;

        if (maxDownCheckBox.isSelected()) {
            if (score.getMaxTransposeDown() == null) {
                score.setMaxTransposeDown(0);
                ScoreMode mode = stateManager.getCurrentMode();
                if (mode != null) {
                    while (!score.canTransposeBy(0)) {
                        if (!TranspositionManager.getInstance().transposeUp(mode)) break;
                    }
                    stateManager.notifyScoreChanged();
                }
            }
        } else {
            score.setMaxTransposeDown(null);
        }
        updateTransposeUI();
    }

    @FXML
    private void handleMaxUpCheckBoxChanged() {
        Score score = storageService.getScore();
        if (score == null) return;

        if (maxUpCheckBox.isSelected()) {
            if (score.getMaxTransposeUp() == null) {
                score.setMaxTransposeUp(0);
                ScoreMode mode = stateManager.getCurrentMode();
                if (mode != null) {
                    while (!score.canTransposeBy(0)) {
                        if (!TranspositionManager.getInstance().transposeDown(mode)) break;
                    }
                    stateManager.notifyScoreChanged();
                }
            }
        } else {
            score.setMaxTransposeUp(null);
        }
        updateTransposeUI();
    }

    @FXML
    private void handleIncrementMaxDown() {
        Score score = storageService.getScore();
        if (score == null || score.getMaxTransposeDown() == null) return;

        int current = score.getMaxTransposeDown();
        if (current < 12) {
            score.setMaxTransposeDown(current + 1);
            updateTransposeUI();
        }
    }

    @FXML
    private void handleDecrementMaxDown() {
        Score score = storageService.getScore();
        ScoreMode mode = stateManager.getCurrentMode();
        if (score == null || score.getMaxTransposeDown() == null || mode == null) return;

        int current = score.getMaxTransposeDown();
        if (current > 0) {
            score.setMaxTransposeDown(current - 1);
        } else {
            boolean success = TranspositionManager.getInstance().transposeUp(mode);
            if (success) {
                score.setMaxTransposeDown(0);
                stateManager.notifyScoreChanged();
            }
        }
        updateTransposeUI();
    }

    @FXML
    private void handleIncrementMaxUp() {
        Score score = storageService.getScore();
        if (score == null || score.getMaxTransposeUp() == null) return;

        int current = score.getMaxTransposeUp();
        if (current < 12) {
            score.setMaxTransposeUp(current + 1);
            updateTransposeUI();
        }
    }

    @FXML
    private void handleDecrementMaxUp() {
        Score score = storageService.getScore();
        ScoreMode mode = stateManager.getCurrentMode();
        if (score == null || score.getMaxTransposeUp() == null || mode == null) return;

        int current = score.getMaxTransposeUp();
        if (current > 0) {
            score.setMaxTransposeUp(current - 1);
        } else {
            boolean success = TranspositionManager.getInstance().transposeDown(mode);
            if (success) {
                score.setMaxTransposeUp(0);
                stateManager.notifyScoreChanged();
            }
        }
        updateTransposeUI();
    }

    @FXML
    private void handleTransposeUp() {
        ScoreMode mode = stateManager.getCurrentMode();
        if (mode == null || mode.getScore() == null) return;

        boolean success = TranspositionManager.getInstance().transposeUp(mode);
        if (success) {
            stateManager.notifyScoreChanged();
        }
    }

    @FXML
    private void handleTransposeDown() {
        ScoreMode mode = stateManager.getCurrentMode();
        if (mode == null || mode.getScore() == null) return;

        boolean success = TranspositionManager.getInstance().transposeDown(mode);
        if (success) {
            stateManager.notifyScoreChanged();
        }
    }

    @FXML
    private void handleAddMode() {
        new CustomSelectModeDialog()
                .setTitle("Dodaj tryb")
                .setHeader("Wybierz typ dla nowego trybu partytury:")
                .setContent("Wybrany typ określi konfigurację oraz zasady wyświetlania dla nowej wersji partytury.")
                .setConfirmButton("Dodaj", selectedType -> {
                    ScoreMode mode = ScoreFactory.createMode(selectedType);
                    int newIndex = mode.getScore().getModes().size() - 1;
                    stateManager.setCurrentModeIndex(newIndex);
                    stateManager.notifyScoreChanged();
                    refreshModesList();
                })
                .setCancelButton("Anuluj", null)
                .showAndWait();
    }

    @FXML
    private void handleDeleteMode() {
        Score score = storageService.getScore();
        if (score == null || score.getModes().size() <= 1) {
            SongbookDialogHelper.showErrorAlert("Nie można usunąć", "Partytura musi posiadać co najmniej jeden tryb.");
            return;
        }

        ScoreMode selectedMode = modesListView.getSelectionModel().getSelectedItem();
        int selectedIndex = modesListView.getSelectionModel().getSelectedIndex();
        if (selectedMode == null || selectedIndex < 0) return;

        new CustomConfirmationDialog()
                .setTitle("Usuwanie trybu")
                .setHeader("Czy na pewno chcesz usunąć tryb „" + selectedMode.getType().getName() + "”?")
                .setContent("Operacja ta usunie konfigurację tego trybu z partytury.")
                .setConfirmButton("Usuń", () -> {
                    score.getModes().remove(selectedIndex);

                    int currentIndex = stateManager.getCurrentModeIndex();
                    if (currentIndex >= score.getModes().size()) {
                        stateManager.setCurrentModeIndex(score.getModes().size() - 1);
                    } else if (currentIndex == selectedIndex) {
                        stateManager.setCurrentModeIndex(Math.max(0, selectedIndex - 1));
                    }

                    stateManager.notifyScoreChanged();
                    refreshModesList();
                })
                .setCancelButton("Anuluj", null)
                .showAndWait();
    }

    @FXML private void handleCopy() { actionManager.handleCopy(getSelectedItem()); }
    @FXML private void handlePaste() { actionManager.handlePaste(this::refreshDirectory, this::selectItemByFile); }
    @FXML private void handleDuplicate() { actionManager.handleDuplicate(getSelectedItem(), this::refreshDirectory, this::selectItemByFile); }
    @FXML private void handleRename() { actionManager.handleRename(getSelectedItem(), storageService.getCurrentFile(), this::refreshDirectory, this::selectItemByFile, storageService::setCurrentFile); }
    @FXML private void handleDelete() {
        actionManager.handleDelete(
                getSelectedItem(),
                storageService.getCurrentFile(),
                this::refreshDirectory,
                () -> {
                    storageService.setScore(null);
                    metadataHandler.clearAndDisable();
                }
        );
    }
    @FXML private void handleAddFolder() { actionManager.handleAddFolder(this::refreshDirectory, this::selectItemByFile); }
    @FXML private void handleAddFile() { actionManager.handleAddFile(this::refreshDirectory, this::selectItemByFile, this::loadScoreFileSafely); }
    @FXML private void handleFilterChange() { refreshDirectory(); }

    @FXML
    private void handleOpenFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wybierz folder śpiewnika");
        PreferencesService.getDirectoryFile().ifPresent(directoryChooser::setInitialDirectory);

        Stage stage = (Stage) openFolderButton.getScene().getWindow();
        Optional.ofNullable(directoryChooser.showDialog(stage)).ifPresent(dir -> {
            PreferencesService.saveDirectoryPath(dir.getAbsolutePath());
            navigateToDirectory(dir);
        });
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

            dragAndDropHandler.setupCellDragAndDrop(cell);

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

    private void setupDeleteButtonState() {
        if (deleteButton == null) return;
        deleteButton.setDisable(true);
        jsonFilesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteButton.setDisable(newVal == null || newVal.type() == SongbookItem.Type.PARENT_DIR);
        });
    }

    private void setupModesSection() {
        modesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ScoreMode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getType().getName());
                }
            }
        });

        modesListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (isUpdatingModesList) return;
            if (newIdx != null && newIdx.intValue() >= 0) {
                if (stateManager.getCurrentModeIndex() != newIdx.intValue()) {
                    stateManager.setCurrentModeIndex(newIdx.intValue());
                    stateManager.notifyScoreChanged();
                }
            }
        });

        stateManager.addScoreChangeListener(this::refreshModesList);

        refreshModesList();
    }

    private void loadSavedDirectory() {
        songbookExplorerManager.getCurrentLocation().ifPresent(this::navigateToDirectory);
    }

    private void loadScoreFileSafely(File file) {
        boolean closed = closingManager.closeScore();
        if (closed) {
            try {
                storageService.loadScoreFile(file);
                metadataHandler.updatePanel();
                refreshModesList();
                updateTransposeUI();
            } catch (IOException e) {
                e.printStackTrace();
                SongbookDialogHelper.showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
            }
        }
    }

    public void updateTransposeUI() {
        ScoreMode mode = stateManager.getCurrentMode();
        Score score = storageService.getScore();

        if (score == null || mode == null) {
            if (currentKeyLabel != null) currentKeyLabel.setText("—");

            maxDownCheckBox.setSelected(false);
            maxDownCheckBox.setDisable(true);
            maxDownContainer.setDisable(true);
            maxDownLabel.setText("—");

            maxUpCheckBox.setSelected(false);
            maxUpCheckBox.setDisable(true);
            maxUpContainer.setDisable(true);
            maxUpLabel.setText("—");

            if (transposeDownButton != null) transposeDownButton.setDisable(true);
            if (transposeUpButton != null) transposeUpButton.setDisable(true);
            return;
        }

        if (currentKeyLabel != null) {
            if (!mode.getMeasures().isEmpty() && mode.getMeasures().getFirst().getKeySignature() != null) {
                int fifths = mode.getMeasures().getFirst().getKeySignature().getFifths();
                currentKeyLabel.setText(Key.fromFifths(fifths).getDisplayName());
            } else {
                currentKeyLabel.setText(Key.C_MAJOR.getDisplayName());
            }
        }

        maxDownCheckBox.setDisable(false);
        maxUpCheckBox.setDisable(false);

        Integer maxDown = score.getMaxTransposeDown();
        boolean hasMaxDown = maxDown != null;
        maxDownCheckBox.setSelected(hasMaxDown);
        maxDownContainer.setDisable(!hasMaxDown);
        maxDownLabel.setText(hasMaxDown ? maxDown + " półt." : "—");

        Integer maxUp = score.getMaxTransposeUp();
        boolean hasMaxUp = maxUp != null;
        maxUpCheckBox.setSelected(hasMaxUp);
        maxUpContainer.setDisable(!hasMaxUp);
        maxUpLabel.setText(hasMaxUp ? maxUp + " półt." : "—");

        if (transposeDownButton != null) transposeDownButton.setDisable(!score.canTransposeBy(-1));
        if (transposeUpButton != null) transposeUpButton.setDisable(!score.canTransposeBy(1));
    }

    private void openItem(SongbookItem item) {
        if (item == null) return;
        switch (item.type()) {
            case PARENT_DIR, DIRECTORY -> navigateToDirectory(item.file());
            case FILE -> loadScoreFileSafely(item.file());
        }
    }

    private void navigateUp() {
        songbookExplorerManager.getCurrentLocation().ifPresent(currentDir -> {
            File parentDir = currentDir.getParentFile();
            if (parentDir != null && parentDir.exists()) {
                navigateToDirectory(parentDir);
            }
        });
    }

    private void refreshDirectory() {
        songbookExplorerManager.getCurrentLocation().ifPresent(this::loadJsonFiles);
    }

    private void refreshDirectoryAndSelect(File fileToSelect) {
        refreshDirectory();
        selectItemByFile(fileToSelect);
    }

    private void navigateToDirectory(File dir) {
        folderPathLabel.setText(dir.getAbsolutePath());
        songbookExplorerManager.setCurrentLocation(dir.getAbsoluteFile());
        loadJsonFiles(dir);
    }

    private void loadJsonFiles(File folder) {
        File currentFile = storageService.getCurrentFile();

        jsonFilesList.clear();
        jsonFilesList.addAll(fileService.getDirectoryContent(folder, compressedOnlyCheckBox.isSelected()));
        jsonFilesList.sort(itemComparator);

        if (currentFile != null) {
            selectItemByFile(currentFile);
        }
    }

    private void selectItemByFile(File file) {
        if (file == null) return;
        jsonFilesList.stream()
                .filter(item -> item.file() != null && item.file().equals(file))
                .findFirst()
                .ifPresent(item -> jsonFilesListView.getSelectionModel().select(item));
    }

    private SongbookItem getSelectedItem() {
        return jsonFilesListView.getSelectionModel().getSelectedItem();
    }
}