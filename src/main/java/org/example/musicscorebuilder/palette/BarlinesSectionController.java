package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.BarlineLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.music.Barline;
import org.example.musicscorebuilder.components.music.BarlineStyle;
import org.example.musicscorebuilder.components.music.SegmentType;
import org.example.musicscorebuilder.components.views.BarlineView;

import java.util.Arrays;
import java.util.List;

public class BarlinesSectionController extends AbstractPaletteSectionController<BarlineStyle> {
    private final BarlineView barlineView = new BarlineView();

    public BarlinesSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() { return 5; }

    @Override
    protected List<BarlineStyle> getItems() {
        return Arrays.asList(BarlineStyle.values());
    }

    @Override
    protected boolean applyToSelectedElement(BarlineStyle item) {
        var selectedLayout = stateManager.getSelectedItem();
        if (selectedLayout instanceof BarlineLayout actualBarlineLayout) {
            actualBarlineLayout.setStyle(item);
            return true;
        }
        return false;
    }

    @Override
    protected Node createButtonGraphic(BarlineStyle item) {
        SegmentLayout mockParent = new SegmentLayout(SegmentType.BARLINE, mockMeasureLayout);
        Barline mockBarline = new Barline(item, Barline.Type.START, mockMeasure);

        BarlineLayout mockLayout = new BarlineLayout(mockBarline, staffLayout, mockParent) {
            @Override
            public double getWidth() {
                return switch (item) {
                    case SINGLE, SHORTER, SHORT, TICK_SHORT, TICK_LONG, DOTTED, DASHED -> 2.0;
                    case HEAVY -> 4.0;
                    case DOUBLE_LIGHT -> 2.0 * 2.0 + 3.0;
                    case DOUBLE_HEAVY -> 2.0 * 4.0 + 3.0;
                    case FINAL, FINAL_REVERSED -> 2.0 + 3.0 + 4.0;
                    case REPEAT_LEFT, REPEAT_RIGHT -> (2.0 * 1.0) + 3.0 + 2.0 + 3.0 + 4.0;
                    case REPEAT_BOTH -> 4.0 + 2.0 * (3.0 + 2.0 + 3.0 + (2.0 * 1.0));
                    case NONE -> 0;
                };
            }
        };

        Canvas canvas = createBaseCanvas(false);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double sMeasureX = calculateMeasureX(mockLayout);
        double sMeasureY = calculateMeasureY(false);

        drawBarline(gc, mockLayout, sMeasureX, sMeasureY, 1.0);
        return canvas;
    }

    private void drawBarline(GraphicsContext gc, BarlineLayout barline, double x, double y, double sp) {
        gc.setGlobalAlpha(1.0);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        barlineView.draw(gc, barline, x, y, sp);
    }
}