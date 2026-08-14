package org.example.musicscorebuilder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.musicscorebuilder.NoteDragHandler;
import org.example.musicscorebuilder.ShortcutHandler;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;
import org.example.musicscorebuilder.components.layout.engine.LayoutEngine;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.views.BackgroundView;
import org.example.musicscorebuilder.controller.util.audio.MidiInputService;
import org.example.musicscorebuilder.controller.util.audio.PianoPlayer;
import org.example.musicscorebuilder.data.StorageService;
import org.example.musicscorebuilder.managers.*;

import java.util.List;
import java.util.Optional;

public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private BackgroundView container;
    @FXML private ToggleGroup viewModeGroup;
    @FXML private ToggleButton soloViewButton;
    @FXML private ToggleButton fullScoreViewButton;

    private ContextMenuController contextMenuController;

    private LayoutEngine layoutEngine;
    private ScoreLayout currentScoreLayout;
    private final StorageService storageService = StorageService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();
    private final ShortcutHandler shortcutHandler = new ShortcutHandler();

    @FXML
    public void initialize() {
        initContainerBinding();
        initDragHandling();
        initClickHandling();
        initListeners();
        initViewModeToggles();
        this.contextMenuController = ContextMenuController.create();
        this.layoutEngine = new LayoutEngine();
        MidiInputService.getInstance().startListening();

        refreshView();
    }

    private void initViewModeToggles() {
        soloViewButton.setSelected(true);
        viewModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
            }
        });
    }

    @FXML
    private void handleViewModeChange() {
        Score score = storageService.getScore();
        int targetModeIndex = soloViewButton.isSelected() ? 0 : 1;

        if (score == null || score.getModes().size() <= targetModeIndex) {
            return;
        }

        if (stateManager.getCurrentModeIndex() == targetModeIndex) {
            return;
        }

        stateManager.setCurrentModeIndex(targetModeIndex);

        if (modeManager.isInsertMode()) modeManager.toggleInsertMode();
        scoreNavigator.clearCursor();
        stateManager.clearSelection();
        refreshView();
    }

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
        container.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (contextMenuController != null && contextMenuController.isShowing()) {
                contextMenuController.hide();
            }

            if (modeManager.isInsertMode() || currentScoreLayout == null) return;

            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                double modelX = container.toModelX(event.getX());
                double modelY = container.toModelY(event.getY());
                var pages = currentScoreLayout.getPages();

                LayoutHitTester.LyricHit lyricHit = LayoutHitTester.findClickedLyric(pages, modelX, modelY);
                if (lyricHit != null) {
                    LyricEditorManager.getInstance().startEditing(
                            lyricHit.noteLayout(),
                            lyricHit.verse(),
                            currentScoreLayout,
                            event.getX()
                    );
                    event.consume();
                    return;
                }
            }

            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                double modelX = container.toModelX(event.getX());
                double modelY = container.toModelY(event.getY());

                Selectable clickedElement = LayoutHitTester.findClickedElement(currentScoreLayout.getPages(), modelX, modelY);

                boolean isAdditive = event.isShortcutDown() || event.isControlDown() || event.isMetaDown();
                stateManager.setSelected(clickedElement, isAdditive);

                if (clickedElement instanceof NoteLayout note && note.getNote() != null) {
                    PianoPlayer.getInstance().playNote(note.getNote().getPitch());
                }
                redraw();
            }
        });

        container.setOnMouseClicked(event -> {
            if (modeManager.isInsertMode() || currentScoreLayout == null) return;

            if (event.getButton() == MouseButton.SECONDARY) {
                double modelX = container.toModelX(event.getX());
                double modelY = container.toModelY(event.getY());
                Selectable clickedElement = LayoutHitTester.findClickedElement(currentScoreLayout.getPages(), modelX, modelY);
                handleRightClick(event, clickedElement);
            }
        });
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

    private void redraw() {
        if (currentScoreLayout != null) {
            container.updateContent(currentScoreLayout);
        }
    }

    private void refreshView() {
        ScoreMode activeScoreMode = stateManager.getCurrentMode();
        if (activeScoreMode == null) return;
        this.currentScoreLayout = layoutEngine.compute(activeScoreMode);
        stateManager.applyPostRefreshAction(this.currentScoreLayout);
        redraw();
    }

    private void handleRightClick(MouseEvent event, Selectable clickedElement) {
        if (contextMenuController == null) return;

        if (clickedElement != null) {
            stateManager.setSelected(clickedElement, false);
            redraw();
        }
        contextMenuController.setContext(clickedElement);
        contextMenuController.show(container, event.getScreenX(), event.getScreenY());
    }
}