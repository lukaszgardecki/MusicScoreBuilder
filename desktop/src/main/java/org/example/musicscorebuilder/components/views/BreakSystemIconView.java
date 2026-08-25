package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class BreakSystemIconView extends ComponentView {

    public void draw(GraphicsContext gc, double measureX, double measureY, double widthPx, ScoreStyle style, double sp) {
        gc.save();

        double boxSize = 2.5 * sp;
        double x = measureX + widthPx - boxSize;
        double y = measureY - boxSize - (1.0 * sp);
        Color color = Color.web(style.getFrameStrokeColor());

        drawDashedFrame(gc, x, y, boxSize, color, style, sp);

        double cornerY = y + boxSize * 0.58;
        double shaftEndX = x + boxSize * 0.42;

        drawArrowShaft(gc, x, y, boxSize, cornerY, shaftEndX, color);
        drawArrowHead(gc, x, boxSize, cornerY, shaftEndX, color);

        gc.restore();
    }

    private void drawDashedFrame(GraphicsContext gc, double x, double y, double boxSize, Color color, ScoreStyle style, double sp) {
        gc.setStroke(color);
        gc.setLineWidth(style.getFrameStrokeThickness() * sp);
        gc.setLineDashes(style.getFrameStrokeDashLength() * sp, style.getFrameStrokeSpaceLength() * sp);
        gc.strokeRect(x, y, boxSize, boxSize);
        gc.setLineDashes(null);
    }

    private void drawArrowShaft(GraphicsContext gc, double x, double y, double boxSize, double cornerY, double shaftEndX, Color color) {
        double arrowLineWidth = boxSize * 0.12;
        double startX = x + boxSize * 0.72;
        double startY = y + boxSize * 0.28;

        gc.setStroke(color);
        gc.setLineWidth(arrowLineWidth);
        gc.setLineCap(StrokeLineCap.SQUARE);
        gc.setLineJoin(StrokeLineJoin.MITER);

        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.lineTo(startX, cornerY);
        gc.lineTo(shaftEndX, cornerY);
        gc.stroke();
    }

    private void drawArrowHead(GraphicsContext gc, double x, double boxSize, double cornerY, double shaftEndX, Color color) {
        double tipX = x + boxSize * 0.12;
        double barbHeight = boxSize * 0.22;
        double barbTopY = cornerY - barbHeight;
        double barbBottomY = cornerY + barbHeight;

        gc.setFill(color);

        gc.beginPath();
        gc.moveTo(tipX, cornerY);
        gc.lineTo(shaftEndX, barbTopY);
        gc.lineTo(shaftEndX, barbBottomY);
        gc.closePath();
        gc.fill();
    }
}