package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.Mode;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.PageFormat;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.views.BackgroundView;

public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private BackgroundView container;
    @FXML private ToggleButton viewModeToggle;
    private LayoutEngine layoutEngine;
    private ScoreLayout currentScoreLayout;
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();

    @FXML
    public void initialize() {
        container.prefWidthProperty().bind(scrollPane.widthProperty());
        container.prefHeightProperty().bind(scrollPane.heightProperty());

        NoteDragHandler dragHandler = new NoteDragHandler(container, this::findClickedElement, () -> this.currentScoreLayout);
        container.addEventFilter(MouseEvent.MOUSE_PRESSED, dragHandler::handlePressed);
        container.addEventFilter(MouseEvent.MOUSE_DRAGGED, dragHandler::handleDragged);
        container.addEventFilter(MouseEvent.MOUSE_RELEASED, dragHandler::handleReleased);

        container.setOnMouseClicked(this::handleCanvasClick);
        stateManager.addScoreChangeListener(this::refreshView);

        viewModeToggle.setSelected(true);
        viewModeToggle.setText("Widok: Głos Solowy");

        this.layoutEngine = new LayoutEngine(
                new Page(PageFormat.A4_V, 10, 10, 10, 10),
                new ScoreStyle()
        );
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
    }

    private void refreshView() {
        Score score = scoreService.getScore();
        Mode activeMode = stateManager.getCurrentMode(score);
        if (activeMode == null) return;
        this.currentScoreLayout = layoutEngine.compute(activeMode);
        container.updateContent(currentScoreLayout);
    }

    private void handleCanvasClick(MouseEvent event) {
        if (currentScoreLayout == null) return;
        if (!container.wasLastMousePressJustClick()) return;
        ElementLayout clickedElement = findClickedElement(event);

        currentScoreLayout.clearAllSelections();
        toggleSelection(clickedElement);
        container.updateContent(currentScoreLayout);
    }

    private ElementLayout findClickedElement(MouseEvent event) {
        double globalMusicX = container.toModelX(event.getX());
        double globalMusicY = container.toModelY(event.getY());

        ElementLayout clickedElement = null;

        for (PageLayout page : currentScoreLayout.getPages()) {
            double pageX = globalMusicX - page.getX();
            double pageY = globalMusicY;

            for (SystemLayout system : page.getSystems()) {
                double systemX = pageX - system.getX();
                double systemY = pageY - system.getY();

                for (MeasureLayout measure : system.getMeasures()) {
                    double measureX = systemX - measure.getX();
                    double measureY = systemY - measure.getY();

                    for (SegmentLayout segment : measure.getSegments()) {
                        double segmentMusicX = measureX - segment.getX();
                        double segmentMusicY = measureY - segment.getY();

                        for (ElementLayout element : segment.getElements()) {
                            if (element.contains(segmentMusicX, segmentMusicY)) {
                                clickedElement = element;
                                break;
                            }
                        }
                        if (clickedElement != null) break;
                    }
                    if (clickedElement != null) break;
                }
                if (clickedElement != null) break;
            }
            if (clickedElement != null) break;
        }
        return clickedElement;
    }

    private void toggleSelection(ElementLayout element) {
        if (element != null) {
            if (element instanceof TimeSigLayout || element instanceof KeySigLayout) {
                element.getParent().getElements().forEach(e -> e.setSelected(true));
            } else {
                element.setSelected(true);
            }
            stateManager.setSelectedElement(element);
        } else {
            stateManager.setSelectedElement(null);
        }
    }
}
