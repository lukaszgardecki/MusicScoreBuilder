package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.TimeSigLayout;
import org.example.musicscorebuilder.components.music.SegmentType;
import org.example.musicscorebuilder.components.music.TimeSignature;
import org.example.musicscorebuilder.components.views.TimeSigView;

import java.util.Arrays;
import java.util.List;

public class TimeSignatureSectionController extends AbstractPaletteSectionController<PreDefinedTimeSignature> {
    private final TimeSigView timeSigView = new TimeSigView();

    public TimeSignatureSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() { return 5; }

    @Override
    protected List<PreDefinedTimeSignature> getItems() {
        return Arrays.asList(PreDefinedTimeSignature.values());
    }

    @Override
    protected boolean applyToSelectedElement(PreDefinedTimeSignature sig) {
        Selectable item = stateManager.getSelectedItem();

        if (item instanceof TimeSigLayout) {
            scoreService.getScore().getModes().forEach(mode -> mode.setTimeSignature(sig));
            return true;
        }
        return false;
    }

    @Override
    protected Node createButtonGraphic(PreDefinedTimeSignature sig) {
        SegmentLayout mockParent = new SegmentLayout(SegmentType.TIME_SIG, mockMeasureLayout);
        TimeSignature mockTimeSig = new TimeSignature(sig.getBeat(), sig.getBeatType(), sig.getType(), mockMeasure);
        TimeSigLayout mockLayout = new TimeSigLayout(mockTimeSig, staffLayout, mockParent);

        Canvas canvas = createBaseCanvas(true);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double sMeasureX = calculateMeasureX(mockLayout);
        double sMeasureY = calculateMeasureY(true);

        drawTimeSignature(gc, mockLayout, sMeasureX, sMeasureY, 1.0);
        return canvas;
    }

    private void drawTimeSignature(GraphicsContext gc, TimeSigLayout timeSig, double x, double y, double sp) {
        gc.setGlobalAlpha(1.0);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        timeSigView.draw(gc, timeSig, x, y, sp);
    }
}