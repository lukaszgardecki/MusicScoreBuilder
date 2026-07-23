package org.example.musicscorebuilder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import org.example.musicscorebuilder.NoteDragHandler;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.ShortcutHandler;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.engine.LayoutEngine;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.PageFormat;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.views.BackgroundView;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.List;

public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private BackgroundView container;
    @FXML private ToggleButton viewModeToggle;
    private LayoutEngine layoutEngine;
    private ScoreLayout currentScoreLayout;
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ShortcutHandler shortcutHandler = new ShortcutHandler();

    @FXML
    public void initialize() {
        initContainerBinding();
        initDragHandling();
        initClickHandling();
        initListeners();
        initViewModeToggle();
        initLayoutEngine();

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
        container.setOnMouseClicked(this::handleCanvasClick);
    }

    private void initListeners() {
        stateManager.addScoreChangeListener(this::refreshView);

        container.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) shortcutHandler.unregister(oldScene);
            if (newScene != null) shortcutHandler.register(newScene);
        });

        modeManager.addModeChangeListener(isInsert -> {
            if (isInsert) {
                handleInsertModeActivation();
            }
            redraw();
        });
    }

    private void handleInsertModeActivation() {
        var targetSegment = stateManager.getSelectedSegment();

        if (targetSegment == null) {
            targetSegment =  modeManager.getEditedSegment();
        }

        stateManager.clearSelection();

        if (targetSegment == null && currentScoreLayout != null) {
            targetSegment = currentScoreLayout.findFirstNoteSegment();
        }

        if (targetSegment != null) {
            modeManager.setEditedSegment(targetSegment);
        }
    }

    private void initViewModeToggle() {
        viewModeToggle.setSelected(true);
        viewModeToggle.setText("Widok: Głos Solowy");
    }

    private void initLayoutEngine() {
        this.layoutEngine = new LayoutEngine(
                new Page(PageFormat.A4_V, 10, 10, 10, 10),
                new ScoreStyle()
        );
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
        stateManager.setSelected(clickedElement);
        redraw();
    }
}