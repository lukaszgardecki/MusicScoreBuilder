package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import org.example.musicscorebuilder.components.layout.BarlineLayout;

public class BarlineView {

    public void draw(GraphicsContext gc, BarlineLayout barline, double sMeasureX, double sMeasureY, double sp) {
        double startY = barline.getY() * sp + sMeasureY;
        double endY = startY + (barline.getHeight() * sp);
        double baseX = sMeasureX;

        gc.setLineCap(StrokeLineCap.BUTT);

        switch (barline.getStyle()) {
            case SINGLE -> drawSingle(gc, barline, baseX, startY, endY, sp);
            case DOUBLE -> drawDouble(gc, barline, baseX, startY, endY, sp);
            case END -> drawEnd(gc, barline, baseX, startY, endY, sp);
            case REPEAT_LEFT -> drawRepeatLeft(gc, barline, baseX, startY, endY, sp);
            case REPEAT_RIGHT -> drawRepeatRight(gc, barline, baseX, startY, endY, sp);
        }
    }

    private void drawSingle(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        gc.setLineWidth(width);
        double correctedX = x + (width / 2.0);
        gc.strokeLine(correctedX, startY, correctedX, endY);
    }

    private void drawDouble(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double gap = barline.getGap() * sp;
        gc.setLineWidth(thinWidth);

        double leftX = x + (thinWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);

        double rightX = leftX + gap + thinWidth;
        gc.strokeLine(rightX, startY, rightX, endY);
    }

    private void drawEnd(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;

        gc.setLineWidth(thinWidth);
        double leftX = x + (thinWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);

        gc.setLineWidth(thickWidth);
        double rightX = leftX + (thinWidth / 2.0) + gap + (thickWidth / 2.0);
        gc.strokeLine(rightX, startY, rightX, endY);
    }

    private void drawRepeatLeft(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;

        gc.setLineWidth(thickWidth);
        double thickX = x + (thickWidth / 2.0);
        gc.strokeLine(thickX, startY, thickX, endY);

        gc.setLineWidth(thinWidth);
        double thinX = thickX + (thickWidth / 2.0) + gap + (thinWidth / 2.0);
        gc.strokeLine(thinX, startY, thinX, endY);

        double dotSpace = barline.getDotSpace() * sp;
        double dotRadius = barline.getDotRadius() * sp;
        double dotX = thinX + (thinWidth / 2.0) + dotSpace + dotRadius;
        drawRepeatDots(gc, barline, dotX, startY, endY, sp);
    }

    private void drawRepeatRight(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;
        double dotRadius = barline.getDotRadius() * sp;
        double dotSpace = barline.getDotSpace() * sp;

        double dotX = x + dotRadius;
        drawRepeatDots(gc, barline, dotX, startY, endY, sp);

        gc.setLineWidth(thinWidth);
        double thinX = dotX + dotRadius + dotSpace + (thinWidth / 2.0);
        gc.strokeLine(thinX, startY, thinX, endY);

        gc.setLineWidth(thickWidth);
        double thickX = thinX + (thinWidth / 2.0) + gap + (thickWidth / 2.0);
        gc.strokeLine(thickX, startY, thickX, endY);
    }

    //TODO: źle pozycjonuje kropki, tzn. są na złej wysokości (X jest dobry)
    private void drawRepeatDots(GraphicsContext gc, BarlineLayout barline, double dotX, double startY, double endY, double sp) {
        double staffHeight = endY - startY;
        double lineSpacing = staffHeight / 4.0;
        double dotRadius = barline.getDotRadius() * sp;

        gc.fillOval(dotX - dotRadius, startY + (1.5 * lineSpacing) - dotRadius, dotRadius * 2, dotRadius * 2);
        // Kropka dolna: w środku trzeciego pola od góry
        gc.fillOval(dotX - dotRadius, startY + (2.5 * lineSpacing) - dotRadius, dotRadius * 2, dotRadius * 2);
    }
}