package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.BarlineView;
import org.example.musicscorebuilder.components.views.StaffView;

import java.util.Arrays;
import java.util.List;

public class BarlinesSectionController extends AbstractPaletteSectionController<BarlineStyle> {
    private final BarlineView barlineView = new BarlineView();
    private final StaffView staffView = new StaffView();

    public BarlinesSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected List<BarlineStyle> getItems() {
        return Arrays.asList(BarlineStyle.values());
    }

    @Override
    protected boolean applyToSelectedElement(BarlineStyle item) {
        var selectedLayout = stateManager.getSelectedElement();
        if (selectedLayout instanceof BarlineLayout actualBarlineLayout) {
            actualBarlineLayout.setStyle(item);
            return true;
        }
        return false;
    }

    @Override
    protected Node createButtonGraphic(BarlineStyle item) {
        double canvasWidth = 40;
        double canvasHeight = 40;
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setImageSmoothing(false);

        ScoreStyle mockStyle = new ScoreStyle() {
            @Override public double getStaffLineSpacing() { return 6.0; }
            @Override public double getStaffLineWidth() { return 1.0; }
            @Override public double getStaffSpacing() { return 0.0; }
            @Override public double getBarlineLightWidth() { return 2.0; }
            @Override public double getBarlineHeavyWidth() { return 4.0; }
            @Override public double getBarlineGap() { return 3.0; }
            @Override public double getBarlineDotSpace() { return 3.0; }
            @Override public double getBarlineDotRadius() { return 1.0; }
        };

        Staff mockStaff = new Staff(0, ClefType.G);
        Measure mockMeasure = new Measure(item, List.of(mockStaff));

        MeasureLayout mockMeasureLayout = new MeasureLayout(mockMeasure, 0, mockStyle) {
            @Override public double getWidth() { return 32.0; }
        };

        StaffLayout staffLayout = new StaffLayout(mockStaff, mockMeasureLayout, mockStyle);
        SegmentLayout mockParent = new SegmentLayout(SegmentType.BARLINE, mockMeasureLayout);
        Barline mockBarline = new Barline(item, Barline.Type.START);

        BarlineLayout mockLayout = new BarlineLayout(mockBarline, mockParent, staffLayout) {
            @Override
            public double getWidth() {
                return switch (item) {
                    case SINGLE -> 2.0;
                    case DOUBLE -> 4.0;
                    case END -> 5.0;
                    case REPEAT_LEFT, REPEAT_RIGHT -> 7.0;
                };
            }
        };

        double sMeasureX = Math.round((canvasWidth / 2.0) - (mockLayout.getWidth() / 2.0));
        double sMeasureY = 8.5;
        double sp = 1.0;

        drawStaff(gc, staffLayout, 4.0, sMeasureY, sp);
        drawBarline(gc, mockLayout, sMeasureX, sMeasureY, sp);
        return canvas;
    }

    private void drawStaff(GraphicsContext gc, StaffLayout staff, double x, double y, double sp) {
        gc.setGlobalAlpha(0.25);
        staffView.draw(gc, staff, x, y, sp);
    }

    private void drawBarline(GraphicsContext gc, BarlineLayout barline, double x, double y, double sp) {
        gc.setGlobalAlpha(1.0);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        barlineView.draw(gc, barline, x, y, sp);
    }
}