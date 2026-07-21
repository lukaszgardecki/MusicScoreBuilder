package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.BackgroundView;

import java.util.List;

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
        refreshView();
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
        if (currentScoreLayout == null) return;
        if (!container.wasLastMousePressJustClick()) return;
        Selectable clickedElement = findClickedElement(event);
        currentScoreLayout.clearAllSelections();
        toggleSelection(clickedElement);
        redraw();
    }

    private Selectable findClickedElement(MouseEvent event) {
        double globalMusicX = container.toModelX(event.getX());
        double globalMusicY = container.toModelY(event.getY());

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
                                return element;
                            }
                        }
                    }

                    MeasureStaffSelection region = measure.getElementsRegionAt(measureX, measureY);
                    if (region != null && region.contains(measureX, measureY)) {
                        return region;
                    }
                }
            }
        }

        return null;
    }

    private void toggleSelection(Selectable clickedElement) {
        if (clickedElement == null) {
            stateManager.clearSelection();
            return;
        }

        if (clickedElement instanceof ElementLayout element) {
            if (clickedElement instanceof TimeSigLayout || clickedElement instanceof KeySigLayout) {
                element.getParent().getElements().forEach(e -> e.setSelected(true));
            } else {
                clickedElement.setSelected(true);
            }
        } else if (clickedElement instanceof MeasureStaffSelection selection) {
            selection.setSelected(true);

            MeasureLayout measure = selection.getMeasure();
            StaffLayout targetStaff = selection.getStaff();

            if (measure != null && measure.getSegments() != null && targetStaff != null) {
                List<SegmentLayout> segments = measure.getSegments();

                int firstChordRestIdx = -1;
                int lastChordRestIdx = -1;

                for (int i = 0; i < segments.size(); i++) {
                    if (segments.get(i).getType() == SegmentType.CHORDREST) {
                        if (firstChordRestIdx == -1) {
                            firstChordRestIdx = i;
                        }
                        lastChordRestIdx = i;
                    }
                }

                if (firstChordRestIdx != -1) {
                    for (int i = firstChordRestIdx; i <= lastChordRestIdx; i++) {
                        SegmentLayout segment = segments.get(i);
                        List<ElementLayout> staffElements = segment.getElementsForStaff(targetStaff);

                        for (ElementLayout element : staffElements) {
                            element.setSelected(true);
                        }
                    }
                }
            }
        }

        stateManager.setSelectedItem(clickedElement);
    }
}
