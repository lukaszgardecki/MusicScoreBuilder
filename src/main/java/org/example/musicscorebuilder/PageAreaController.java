package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
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
    private LayoutEngine layoutEngine;
    private ScoreLayout currentScoreLayout;

    @FXML
    public void initialize() {
        container.prefWidthProperty().bind(scrollPane.widthProperty());
        container.prefHeightProperty().bind(scrollPane.heightProperty());

        container.setOnMouseClicked(this::handleCanvasClick);
        ScoreService.getInstance().addListener(this::refreshView);

        this.layoutEngine = new LayoutEngine(
                new Page(PageFormat.A4_V, 10, 10, 10, 10),
                new ScoreStyle()
        );
        refreshView();
    }

    private void refreshView() {
        Score score = ScoreService.getInstance().getScore();
        Mode mode = score.getModes().getFirst();
        this.currentScoreLayout = layoutEngine.compute(mode);
        container.updateContent(currentScoreLayout);
    }

    private void handleCanvasClick(MouseEvent event) {
        if (currentScoreLayout == null) return;
        if (!container.wasLastMousePressJustClick()) return;

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
                            if (element instanceof EmptyElement) continue;

                            if (element instanceof VoiceLayout voice) {
                                double voiceX = voice.getX();

                                for (ChordLayout chord : voice.getChords()) {
                                    double chordX = chord.getX();

                                    for (NoteLayout note : chord.getNotes()) {
                                        double absoluteNoteBoxX = voiceX + chordX + note.getBoxX();
                                        double absoluteNoteBoxY = note.getBoxY();

                                        if (segmentMusicX >= absoluteNoteBoxX && segmentMusicX <= (absoluteNoteBoxX + note.getBoxWidth()) &&
                                                segmentMusicY >= absoluteNoteBoxY && segmentMusicY <= (absoluteNoteBoxY + note.getHeight())) {

                                            clickedElement = note;
                                            break;
                                        }
                                    }
                                    if (clickedElement != null) break;
                                }
                            } else {
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
            if (clickedElement != null) break;
        }

        currentScoreLayout.clearAllSelections();
        if (clickedElement != null) {
            clickedElement.setSelected(true);
        }
        container.updateContent(currentScoreLayout);
    }
}
