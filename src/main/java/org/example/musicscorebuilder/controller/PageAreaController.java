package org.example.musicscorebuilder.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.example.musicscorebuilder.NoteDragHandler;
import org.example.musicscorebuilder.ShortcutHandler;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.PageLayout;
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
    @FXML private HBox modeBarContainer;
    @FXML private ToggleGroup viewModeGroup;

    private ContextMenuController contextMenuController;

    private LayoutEngine layoutEngine;
    private ScoreLayout currentScoreLayout;
    private final StorageService storageService = StorageService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();
    private final ShortcutHandler shortcutHandler = new ShortcutHandler();

    private boolean refreshPending = false;
    private boolean redrawPending = false;

    @FXML
    public void initialize() {
        initContainerBinding();
        initDragHandling();
        initClickHandling();
        initListeners();
        initViewModeGroup();
        this.contextMenuController = ContextMenuController.create();
        this.layoutEngine = new LayoutEngine();
        MidiInputService.getInstance().startListening();

        refreshView();
    }

    private void initViewModeGroup() {
        viewModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                oldToggle.setSelected(true);
            }
        });
    }

    private void updateModeSelector() {
        Score score = storageService.getScore();
        if (score == null || score.getModes() == null || score.getModes().size() <= 1) {
            modeBarContainer.setVisible(false);
            modeBarContainer.setManaged(false);
            return;
        }

        modeBarContainer.setVisible(true);
        modeBarContainer.setManaged(true);
        modeBarContainer.getChildren().clear();

        List<ScoreMode> modes = score.getModes();
        int activeIndex = stateManager.getCurrentModeIndex();
        int modeCount = modes.size();

        for (int i = 0; i < modeCount; i++) {
            ScoreMode mode = modes.get(i);
            int modeIndex = i;

            ToggleButton button = new ToggleButton(mode.getType().getName());
            button.setToggleGroup(viewModeGroup);
            button.getStyleClass().add("custom-button");

            if (i == 0) {
                button.getStyleClass().add("first-segment");
            } else if (i == modeCount - 1) {
                button.getStyleClass().add("last-segment");
            } else {
                button.getStyleClass().add("middle-segment");
            }

            if (i == activeIndex) {
                button.setSelected(true);
            }

            button.setOnAction(e -> {
                if (stateManager.getCurrentModeIndex() != modeIndex) {
                    stateManager.setCurrentModeIndex(modeIndex);
                    if (modeManager.isInsertMode()) modeManager.toggleInsertMode();
                    scoreNavigator.clearCursor();
                    stateManager.clearSelection();
                    refreshView();
                }
            });

            modeBarContainer.getChildren().add(button);
        }
    }

    private void initContainerBinding() {
        container.prefWidthProperty().bind(scrollPane.widthProperty());
        container.prefHeightProperty().bind(scrollPane.heightProperty());
    }

    private void initDragHandling() {
        NoteDragHandler dragHandler = new NoteDragHandler(
                container,
                event -> {
                    if (currentScoreLayout == null) return null;
                    List<PageLayout> pages = currentScoreLayout.getPages();
                    if (pages == null || pages.isEmpty()) return null;

                    return LayoutHitTester.findClickedElement(
                            pages,
                            container.toModelX(event.getX()),
                            container.toModelY(event.getY())
                    );
                },
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

            List<PageLayout> pages = currentScoreLayout.getPages();
            if (pages == null || pages.isEmpty()) return;

            double modelX = container.toModelX(event.getX());
            double modelY = container.toModelY(event.getY());

            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
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
                Selectable clickedElement = LayoutHitTester.findClickedElement(pages, modelX, modelY);

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
                List<PageLayout> pages = currentScoreLayout.getPages();
                if (pages == null || pages.isEmpty()) return;

                double modelX = container.toModelX(event.getX());
                double modelY = container.toModelY(event.getY());
                Selectable clickedElement = LayoutHitTester.findClickedElement(pages, modelX, modelY);
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
        if (redrawPending) return;
        redrawPending = true;

        Platform.runLater(() -> {
            redrawPending = false;
            if (currentScoreLayout != null) {
                container.updateContent(currentScoreLayout);
            }
        });
    }

    private void refreshView() {
        if (refreshPending) return;
        refreshPending = true;

        Platform.runLater(() -> {
            refreshPending = false;
            updateModeSelector();
            ScoreMode activeScoreMode = stateManager.getCurrentMode();
            if (activeScoreMode == null) return;

            this.currentScoreLayout = layoutEngine.compute(activeScoreMode);
            stateManager.applyPostRefreshAction(this.currentScoreLayout);

            if (currentScoreLayout != null) {
                container.updateContent(currentScoreLayout);
            }
        });
    }

    private void handleRightClick(MouseEvent event, Selectable clickedElement) {
        if (contextMenuController == null) return;

        if (clickedElement != null) {
            stateManager.setSelected(clickedElement, false);
            redraw();
        }
        contextMenuController.setContext(clickedElement, () -> currentScoreLayout);
        contextMenuController.show(container, event.getScreenX(), event.getScreenY());
    }
}