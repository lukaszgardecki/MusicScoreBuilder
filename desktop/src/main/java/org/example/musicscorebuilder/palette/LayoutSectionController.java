package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Frame;
import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.views.BreakSystemIconView;
import org.example.musicscorebuilder.components.views.InsertVerticalFrameIconView;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LayoutSectionController extends AbstractPaletteSectionController<LayoutAction> {
    private final BreakSystemIconView breakSystemIcon = new BreakSystemIconView();
    private final InsertVerticalFrameIconView insertVerticalFrameIconView = new InsertVerticalFrameIconView();

    public LayoutSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() {
        return 4;
    }

    @Override
    protected List<LayoutAction> getItems() {
        return Arrays.stream(LayoutAction.values()).collect(Collectors.toList());
    }

    @Override
    protected boolean applyToSelectedElement(LayoutAction action) {
        Selectable item = stateManager.getSelectedItem();
        if (item == null) return false;

        Measure measure = null;
        try {
            if (item.getSegment() != null && item.getSegment().getParent() != null) {
                measure = item.getSegment().getParent().getMeasure();
            }
        } catch (Exception ignored) {}

        if (measure != null) {
            switch (action) {
                case SYSTEM_BREAK -> {
                    boolean newState = !measure.hasSystemBreak();
                    measure.setSystemBreak(newState);

                    ScoreStateManager.getInstance().notifyScoreChanged();
                    return true;
                }
                case VERTICAL_FRAME -> {
                    ScoreMode mode = ScoreStateManager.getInstance().getCurrentMode();
                    mode.addFrame(new Frame(measure.getIndex()));

                    ScoreStateManager.getInstance().notifyScoreChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected Node createButtonGraphic(LayoutAction action) {
        Canvas canvas = createBaseCanvas(true, false);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (action == LayoutAction.SYSTEM_BREAK) {
            drawSystemBreakIcon(gc, width, height);
        } else if (action == LayoutAction.VERTICAL_FRAME) {
            drawInsertVerticalFrameIcon(gc, width, height);
        }

        return canvas;
    }

    private void drawSystemBreakIcon(GraphicsContext gc, double w, double h) {
        ScoreStyle style = new ScoreStyle() {
            @Override public String getFrameStrokeColor() { return "#000000"; }
        };

        double boxSize = Math.min(w, h) * 0.55;
        double sp = boxSize / 2.5;
        double targetX = (w - boxSize) / 2.0;
        double targetY = (h - boxSize) / 2.0;
        double measureX = targetX;
        double widthPx = boxSize;
        double measureY = targetY + boxSize + (1.0 * sp);

        breakSystemIcon.draw(gc, measureX, measureY, widthPx, style, sp);
    }

    private void drawInsertVerticalFrameIcon(GraphicsContext gc, double w, double h) {
        double boxSize = Math.min(w, h) * 0.5;
        double rectX = (w - boxSize) / 2.0;
        double rectY = (h - boxSize) / 2.0;
        insertVerticalFrameIconView.draw(gc, rectX, rectY, boxSize);
    }
}