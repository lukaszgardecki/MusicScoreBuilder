package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import org.example.musicscorebuilder.components.layout.BarlineLayout;
import org.example.musicscorebuilder.util.Util;

import java.util.Random;

public class BarlineView {

    public void draw(GraphicsContext gc, BarlineLayout barline, double sMeasureX, double sMeasureY, double sp) {
        double startY = barline.getY() * sp + sMeasureY;
        double endY = startY + (barline.getHeight() * sp);
        double baseX = barline.getX() * sp + sMeasureX;

        gc.setStroke(Color.BLACK);
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
        double correctedX = x - (width / 2.0);
        gc.strokeLine(correctedX, startY, correctedX, endY);
    }

    private void drawDouble(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double gap = barline.getGap() * sp;
        gc.setLineWidth(thinWidth);

        double rightX = x - (thinWidth / 2.0);
        gc.strokeLine(rightX, startY, rightX, endY);

        double leftX = rightX - gap - thinWidth;
        gc.strokeLine(leftX, startY, leftX, endY);
    }

    private void drawEnd(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;

        gc.setLineWidth(thickWidth);
        double rightX = x - (thickWidth / 2.0);
        gc.strokeLine(rightX, startY, rightX, endY);

        gc.setLineWidth(thinWidth);
        double leftX = rightX - (thickWidth / 2.0) - gap - (thinWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);
    }

    private void drawRepeatLeft(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;

        gc.setLineWidth(thickWidth);
        double rightX = x - (thickWidth / 2.0);
        gc.strokeLine(rightX, startY, rightX, endY);

        gc.setLineWidth(thinWidth);
        double leftX = rightX - (thickWidth / 2.0) - gap - (thinWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);

        drawRepeatDots(gc, barline, leftX - (thinWidth / 2.0) - (0.3 * sp), startY, endY, sp);
    }

    private void drawRepeatRight(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;

        gc.setLineWidth(thickWidth);
        double leftX = x + (thickWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);

        gc.setLineWidth(thinWidth);
        double rightX = leftX + (thickWidth / 2.0) + gap + (thinWidth / 2.0);
        gc.strokeLine(rightX, startY, rightX, endY);

        drawRepeatDots(gc, barline, rightX + (thinWidth / 2.0) + (0.3 * sp), startY, endY, sp);
    }

    //TODO: źle pozycjonuje kropki, tzn. są na złej wysokości (X jest dobry)
    private void drawRepeatDots(GraphicsContext gc, BarlineLayout barline, double dotX, double startY, double endY, double sp) {
        double staffHeight = endY - startY;
        double lineSpacing = staffHeight / 4.0;

        double dotRadius = 0.12 * sp;

        gc.setFill(Color.BLACK);
        // Kropka górna: w środku drugiego pola od góry
        gc.fillOval(dotX - dotRadius, startY + (1.5 * lineSpacing) - dotRadius, dotRadius * 2, dotRadius * 2);
        // Kropka dolna: w środku trzeciego pola od góry
        gc.fillOval(dotX - dotRadius, startY + (2.5 * lineSpacing) - dotRadius, dotRadius * 2, dotRadius * 2);
    }
}