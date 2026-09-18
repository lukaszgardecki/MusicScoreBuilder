package org.example.musicscorebuilder.palette;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.NoteRestLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.TempoLayout;
import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.music.Segment;
import org.example.musicscorebuilder.components.music.Tempo;
import org.example.musicscorebuilder.components.views.TempoView;
import org.example.musicscorebuilder.managers.FontManager;

import java.util.Arrays;
import java.util.List;

public class TempoSectionController extends AbstractPaletteSectionController<TempoText> {
    private final TempoView tempoView = new TempoView();

    public TempoSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() { return 3; }

    @Override
    protected List<TempoText> getItems() {
        return Arrays.asList(TempoText.values());
    }

    @Override
    protected boolean applyToSelectedElement(TempoText item) {
        Selectable selected = stateManager.getSelectedItem();

        NoteRestLayout noteRest = null;
        Tempo existingTempo = null;

        if (selected instanceof NoteRestLayout nr) {
            noteRest = nr;
        } else if (selected instanceof TempoLayout tl) {
            noteRest = tl.getNoteRest();
            existingTempo = tl.getTempo();
        } else {
            return false;
        }

        Measure targetMeasure = noteRest.getMeasureLayout().getMeasure();
        ScoreMode mode = stateManager.getCurrentMode();
        if (targetMeasure == null || mode == null) return false;

        Segment segment = noteRest.getSegment().getSegment();
        int segmentIndex = targetMeasure.getSegments().indexOf(segment);

        if (existingTempo == null) {
            existingTempo = mode.getTempos().stream()
                    .filter(t -> targetMeasure.equals(t.getMeasure()) && t.getSegmentIndex() == segmentIndex)
                    .findFirst()
                    .orElse(null);
        }

        Boolean customBold = !item.isBold() ? false : null;
        Boolean customItalic = item.isItalic() ? true : null;

        if (existingTempo != null) {
            if (item.getText().trim().equalsIgnoreCase(existingTempo.getText().trim())) {
                mode.removeTempo(existingTempo);
            } else {
                existingTempo.setText(item.getText());
                existingTempo.setBold(customBold);
                existingTempo.setItalic(customItalic);
            }
        } else {
            Tempo tempo = new Tempo(targetMeasure, segmentIndex, item.getText(), customBold, customItalic);
            mode.addTempo(tempo);
        }

        stateManager.notifyScoreChanged();
        return true;
    }

    @Override
    protected Node createButtonGraphic(TempoText item) {
        Canvas canvas = createBaseCanvas(false, false);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        drawTempoIcon(gc, item, w, h);
        return canvas;
    }

    private void drawTempoIcon(GraphicsContext gc, TempoText item, double w, double h) {
        if (item == null || item.getText() == null || item.getText().isEmpty()) return;

        gc.save();

        Font baseFont = FontManager.getFreeSerifFont(13.0);
        FontWeight weight = item.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = item.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;

        gc.setFont(Font.font(baseFont.getName(), weight, posture, 13.0));
        gc.setFill(Color.BLACK);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        gc.fillText(item.getText(), w / 2.0, h / 2.0);
        gc.restore();
    }
}