package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.BackgroundView;

import java.util.ArrayList;
import java.util.List;

public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private BackgroundView container;
    @FXML private ToggleButton viewModeToggle;
    private LayoutEngine layoutEngine;
    private ScoreLayout currentScoreLayout;
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final InsertModeManager insertModeManager = InsertModeManager.getInstance();
    private final ShortcutHandler shortcutHandler = new ShortcutHandler();

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
        container.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                shortcutHandler.unregister(oldScene);
            }
            if (newScene != null) {
                shortcutHandler.register(newScene);
            }
        });

        insertModeManager.addModeChangeListener(isInsert -> {
            if (isInsert ) {
                SegmentLayout first = getFirstNoteSegment();
                if (first != null) insertModeManager.setEditedSegment(first);
            }
            redraw();
        });

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
        if (insertModeManager.isInsertMode()) insertModeManager.toggleInsertMode();
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
        if (insertModeManager.isInsertMode()) return;
        if (currentScoreLayout == null) return;
        if (!container.wasLastMousePressJustClick()) return;
        Selectable clickedElement = findClickedElement(event);
        stateManager.clearSelection();
        toggleSelection(clickedElement);
        redraw();
    }

    private SegmentLayout getFirstNoteSegment() {
        if (currentScoreLayout == null) return null;
        if (currentScoreLayout.getPages().isEmpty()) return null;
        var page = currentScoreLayout.getPages().getFirst();
        if (page.getSystems().isEmpty()) return null;
        var system = page.getSystems().getFirst();
        if (system.getMeasures().isEmpty()) return null;
        var measure = system.getMeasures().getFirst();
        for (SegmentLayout segment : measure.getSegments()) {
            if (segment.getType() == SegmentType.CHORDREST) {
                return segment;
            }
        }
        return null;
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

                    if (measure.getBeamGroups() != null && !measure.getBeamGroups().isEmpty()) {
                        for (BeamGroupLayout beamGroup : measure.getBeamGroups()) {
                            if (beamGroup.contains(measureX, measureY)) {
                                return beamGroup;
                            }
                        }
                    }

                    for (SegmentLayout segment : measure.getSegments()) {
                        double segmentMusicX = measureX - segment.getX();
                        double segmentMusicY = measureY - segment.getY();

                        for (ElementLayout element : segment.getElements()) {
                            if (element instanceof NoteLayout noteLayout) {
                                if (noteLayout.getBeamSingle() != null && noteLayout.getBeamSingle().contains(segmentMusicX, segmentMusicY)) {
                                    return noteLayout.getBeamSingle();
                                }

                                if (noteLayout.getStem() != null && noteLayout.getStem().contains(segmentMusicX, segmentMusicY)) {
                                    return noteLayout.getStem();
                                }
                            }

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

        List<Selectable> itemsToSelect = new ArrayList<>();

        if (clickedElement instanceof ElementLayout element) {
            if (clickedElement instanceof TimeSigLayout || clickedElement instanceof KeySigLayout) {
                itemsToSelect.addAll(element.getParent().getElements());
            } else {
                itemsToSelect.add(element);
            }
        } else if (clickedElement instanceof StemLayout stem) {
            itemsToSelect.add(stem);
        } else if (clickedElement instanceof BeamGroupLayout beam) {
            itemsToSelect.add(beam);
        } else if (clickedElement instanceof MeasureStaffSelection selection) {
            itemsToSelect.add(selection);

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
                            itemsToSelect.add(element);
                            if (element instanceof NoteLayout noteLayout) {
                                if (noteLayout.getStem() != null) {
                                    itemsToSelect.add(noteLayout.getStem());
                                }
                                if (noteLayout.getBeamSingle() != null) {
                                    itemsToSelect.add(noteLayout.getBeamSingle());
                                }
                            }
                        }
                    }
                }

                if (measure.getBeamGroups() != null) {
                    for (BeamGroupLayout beamGroup : measure.getBeamGroups()) {
                        if (!beamGroup.isEmpty()) {
                            StaffLayout groupStaff = beamGroup.getFirstNote().getStaffLayout();
                            if (groupStaff == targetStaff) {
                                itemsToSelect.add(beamGroup);
                            }
                        }
                    }
                }
            }
        } else {
            itemsToSelect.add(clickedElement);
        }

        stateManager.setSelectedItem(itemsToSelect);
    }
}
