package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.KeySigLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.music.KeySignature;
import org.example.musicscorebuilder.components.music.SegmentType;
import org.example.musicscorebuilder.components.views.KeySigView;

import java.util.List;

public class KeySignatureSectionController extends AbstractPaletteSectionController<Integer> {
    private final KeySigView keySigView = new KeySigView();

    public KeySignatureSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() {
        return 5;
    }

    @Override
    protected List<Integer> getItems() { return List.of(1, 2, 3, 4, 5, 6, 7, -7, -6, -5, -4, -3, -2, -1, 0); }

    @Override
    protected boolean applyToSelectedElement(Integer key) {
        Selectable item = stateManager.getSelectedItem();
        if (item instanceof KeySigLayout) {
            scoreService.getScore().getModes().forEach(mode -> mode.setKeySignature(key));
            return true;
        }
        return false;
    }

    @Override
    protected Node createButtonGraphic(Integer key) {
        SegmentLayout mockParent = new SegmentLayout(SegmentType.KEY_SIG, mockMeasureLayout);
        KeySignature mockSignature = new KeySignature(key, mockMeasure);
        KeySigLayout mockLayout = new KeySigLayout(mockSignature, staffLayout, mockParent);

        Canvas canvas = createBaseCanvas(true);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double sMeasureX = calculateMeasureX(mockLayout);
        double sMeasureY = calculateMeasureY(true);

        drawKeySignature(gc, mockLayout, sMeasureX, sMeasureY, 1.0);
        return canvas;
    }

    private void drawKeySignature(GraphicsContext gc, KeySigLayout keySig, double x, double y, double sp) {
        gc.setGlobalAlpha(1.0);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        keySigView.draw(gc, keySig, x, y, sp);
    }
}