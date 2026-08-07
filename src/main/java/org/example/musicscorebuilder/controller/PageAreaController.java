package org.example.musicscorebuilder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.example.musicscorebuilder.NoteDragHandler;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.ShortcutHandler;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;
import org.example.musicscorebuilder.components.layout.engine.LayoutEngine;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.views.BackgroundView;
import org.example.musicscorebuilder.data.ScoreStorageService;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private BackgroundView container;
    @FXML private ToggleButton viewModeToggle;
    private LayoutEngine layoutEngine;
    private ScoreLayout currentScoreLayout;
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();
    private final ShortcutHandler shortcutHandler = new ShortcutHandler();
    private final ScoreStorageService storageService = new ScoreStorageService();

    @FXML
    public void initialize() {
        initContainerBinding();
        initDragHandling();
        initClickHandling();
        initListeners();
        initViewModeToggle();
        this.layoutEngine = new LayoutEngine();
        MidiInputService.getInstance().startListening();

        refreshView();
    }

    @FXML
    private void toggleViewMode() {
        Score score = scoreService.getScore();
        if (score.getModes().size() <= 1) return;

        if (viewModeToggle.isSelected()) {
            viewModeToggle.setText("Widok: Głos Solowy");
            stateManager.setCurrentModeIndex(0);
        } else {
            viewModeToggle.setText("Widok: Pełna Partytura");
            stateManager.setCurrentModeIndex(1);
        }
        if (modeManager.isInsertMode()) modeManager.toggleInsertMode();
        scoreNavigator.clearCursor();
        stateManager.clearSelection();
        refreshView();
    }

    @FXML
    private void saveScore() {
        Score score = scoreService.getScore();

        if (score == null) {
            showErrorAlert("Błąd zapisu", "Brak aktywnej partytury do zapisania.");
            return;
        }

//        String fileName = String.format("%s_%s_c.json", score.getNumberNew(), score.getTitle().replaceAll(" ", "-"));
        String fileName = String.format("%s_%s.json", score.getNumberNew(), score.getTitle().replaceAll(" ", "-"));
        File saveFile = new File(fileName);
        try {
//            storageService.saveToCompressedJson(score, saveFile);
            storageService.saveToJson(score, saveFile);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd zapisu", "Nie udało się zapisać partytury: " + e.getMessage());
        }
    }

    @FXML
    private void loadScore() {
//        File loadFile = new File("251_O-milcząca-Hostio-biała_c.json");
        File loadFile = new File("251_O-milcząca-Hostio-biała.json");

        if (!loadFile.exists()) {
            showErrorAlert("Błąd wczytywania", "Plik " + loadFile.getName() + " nie istnieje!");
            return;
        }

        try {
//            Score score = storageService.loadFromCompressedJson(loadFile);
            Score score = storageService.loadFromJson(loadFile);
            scoreService.setScore(score);
            refreshView();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Błąd wczytywania", "Nie udało się wczytać partytury: " + e.getMessage());
        }
    }

//    @FXML
//    private void loadScore() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Otwórz plik partytury");
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki JSON", "*.json"));
//
//        // Pobranie aktualnego Okna (Stage)
//        File selectedFile = fileChooser.showOpenDialog(scrollPane.getScene().getWindow());
//
//        if (selectedFile != null) {
//            try {
//                Score score = storageService.loadFromJson(selectedFile);
//                scoreService.setScore(score);
//                refreshView();
//                showInfoAlert("Wczytywanie zakończone", "Wczytano: " + selectedFile.getName());
//            } catch (IOException e) {
//                e.printStackTrace();
//                showErrorAlert("Błąd wczytywania", "Nie udało się wczytać pliku: " + e.getMessage());
//            }
//        }
//    }

    private void initContainerBinding() {
        container.prefWidthProperty().bind(scrollPane.widthProperty());
        container.prefHeightProperty().bind(scrollPane.heightProperty());
    }

    private void initDragHandling() {
        NoteDragHandler dragHandler = new NoteDragHandler(
                container,
                event -> LayoutHitTester.findClickedElement(
                        currentScoreLayout != null ? currentScoreLayout.getPages() : List.of(),
                        container.toModelX(event.getX()),
                        container.toModelY(event.getY())
                ),
                () -> this.currentScoreLayout
        );
        container.addEventFilter(MouseEvent.MOUSE_PRESSED, dragHandler::handlePressed);
        container.addEventFilter(MouseEvent.MOUSE_DRAGGED, dragHandler::handleDragged);
        container.addEventFilter(MouseEvent.MOUSE_RELEASED, dragHandler::handleReleased);
    }

    private void initClickHandling() {
        container.setOnMouseClicked(this::handleCanvasClick);
    }

    private void initListeners() {
        stateManager.addScoreChangeListener(this::refreshView);
        stateManager.addSelectionChangeListener(selectedItem -> redraw());
        container.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) shortcutHandler.unregister(oldScene);
            if (newScene != null) shortcutHandler.register(newScene);
        });

        modeManager.addModeChangeListener(isInsert -> {
            if (isInsert) handleInsertModeActivation();
            else handleInsertModeDeactivation();
            redraw();
        });
    }

    private void handleInsertModeActivation() {
        stateManager.getFirstSelectedNoteRest()
                .map(CursorLayout::new)
                .or(() -> Optional.ofNullable(scoreNavigator.getLastCursor())
                        .map(el -> new CursorLayout(el.getElement())))
                .or(() -> Optional.ofNullable(currentScoreLayout)
                        .map(ScoreLayout::findFirstNoteElement)
                        .map(CursorLayout::new))
                .ifPresent(scoreNavigator::setCursorLayout);

        stateManager.clearSelection();
    }

    private void handleInsertModeDeactivation() {
        CursorLayout currentCursor = scoreNavigator.getLastCursor();
        if (currentCursor != null && currentCursor.getSegment() != null) {
            currentCursor.getSegment().setCursor(null);
        }
    }

    private void initViewModeToggle() {
        viewModeToggle.setSelected(true);
        viewModeToggle.setText("Widok: Głos Solowy");
    }

    private void redraw() {
        if (currentScoreLayout != null) {
            container.updateContent(currentScoreLayout);
        }
    }

    private void refreshView() {
        Score score = scoreService.getScore();
        ScoreMode activeScoreMode = stateManager.getCurrentMode(score);
        if (activeScoreMode == null) return;
        this.currentScoreLayout = layoutEngine.compute(activeScoreMode);
        stateManager.applyPostRefreshAction(this.currentScoreLayout);
        redraw();
    }

    private void handleCanvasClick(MouseEvent event) {
        if (modeManager.isInsertMode()) return;
        if (currentScoreLayout == null) return;
        if (!container.wasLastMousePressJustClick()) return;

        Selectable clickedElement = LayoutHitTester.findClickedElement(
                currentScoreLayout.getPages(),
                container.toModelX(event.getX()),
                container.toModelY(event.getY())
        );

        boolean isAdditive = event.isShortcutDown() || event.isControlDown() || event.isMetaDown();
        stateManager.setSelected(clickedElement, isAdditive);
        redraw();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}