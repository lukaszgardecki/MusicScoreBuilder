package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import org.example.musicscorebuilder.components.layout.BarlineLayout;

public class BarlineView {

    public void draw(GraphicsContext gc, BarlineLayout barline, double sMeasureX, double sMeasureY, double sp) {
        double rawStartY = barline.getY() * sp + sMeasureY;
        double rawEndY = rawStartY + (barline.getHeight() * sp);
        double startY = Math.round(rawStartY);
        double endY = Math.round(rawEndY);
        double baseX = Math.round(sMeasureX + barline.getX() * sp);

        gc.setLineCap(StrokeLineCap.BUTT);

        switch (barline.getStyle()) {
            case SINGLE -> drawSingle(gc, barline, baseX, startY, endY, sp);
            case DOUBLE_LIGHT -> drawDoubleLight(gc, barline, baseX, startY, endY, sp);
            case DOUBLE_HEAVY -> drawDoubleHeavy(gc, barline, baseX, startY, endY, sp);
            case FINAL -> drawFinal(gc, barline, baseX, startY, endY, sp);
            case FINAL_REVERSED -> drawFinalReversed(gc, barline, baseX, startY, endY, sp);
            case REPEAT_LEFT -> drawRepeatLeft(gc, barline, baseX, startY, endY, sp);
            case REPEAT_RIGHT -> drawRepeatRight(gc, barline, baseX, startY, endY, sp);
            case REPEAT_BOTH -> drawRepeatBoth(gc, barline, baseX, startY, endY, sp);
            case DASHED -> drawDashed(gc, barline, baseX, startY, endY, sp);
            case DOTTED -> drawDotted(gc, barline, baseX, startY, endY, sp);
            case HEAVY -> drawHeavy(gc, barline, baseX, startY, endY, sp);
            case TICK_SHORT -> drawTickShort(gc, barline, baseX, startY, endY, sp);
            case TICK_LONG -> drawTickLong(gc, barline, baseX, startY, endY, sp);
            case SHORTER -> drawShorter(gc, barline, baseX, startY, endY, sp);
            case SHORT -> drawShort(gc, barline, baseX, startY, endY, sp);
        }
    }

    private void drawSingle(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        gc.setLineWidth(width);
        double correctedX = x + (width / 2.0);
        gc.strokeLine(correctedX, startY, correctedX, endY);
    }

    private void drawDoubleLight(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double gap = barline.getGap() * sp;
        gc.setLineWidth(thinWidth);

        double leftX = x + (thinWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);

        double rightX = leftX + gap + thinWidth;
        gc.strokeLine(rightX, startY, rightX, endY);
    }

    private void drawDoubleHeavy(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;
        gc.setLineWidth(thickWidth);

        double leftX = x + (thickWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);

        double rightX = leftX + gap + thickWidth;
        gc.strokeLine(rightX, startY, rightX, endY);
    }

    private void drawFinal(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
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

    private void drawFinalReversed(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;

        gc.setLineWidth(thickWidth);
        double leftX = x + (thickWidth / 2.0);
        gc.strokeLine(leftX, startY, leftX, endY);

        gc.setLineWidth(thinWidth);
        double rightX = leftX + (thickWidth / 2.0) + gap + (thinWidth / 2.0);
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
        double dotX = thinX + (thinWidth / 2.0) + dotSpace;
        drawRepeatDots(gc, barline, dotX, startY, endY, sp);
    }

    private void drawRepeatRight(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;
        double dotRadius = barline.getDotRadius() * sp;
        double dotSpace = barline.getDotSpace() * sp;

        drawRepeatDots(gc, barline, x, startY, endY, sp);

        gc.setLineWidth(thinWidth);
        double thinX = x + 2 * dotRadius + dotSpace + (thinWidth / 2.0);
        gc.strokeLine(thinX, startY, thinX, endY);

        gc.setLineWidth(thickWidth);
        double thickX = thinX + (thinWidth / 2.0) + gap + (thickWidth / 2.0);
        gc.strokeLine(thickX, startY, thickX, endY);
    }

    private void drawRepeatBoth(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double thinWidth = barline.getLightLineWidth() * sp;
        double thickWidth = barline.getHeavyLineWidth() * sp;
        double gap = barline.getGap() * sp;
        double dotRadius = barline.getDotRadius() * sp;
        double dotSpace = barline.getDotSpace() * sp;

        double leftDotX = x;
        drawRepeatDots(gc, barline, leftDotX, startY, endY, sp);

        gc.setLineWidth(thinWidth);
        double leftThinX = leftDotX + 2 * dotRadius + dotSpace + (thinWidth / 2.0);
        gc.strokeLine(leftThinX, startY, leftThinX, endY);

        gc.setLineWidth(thickWidth);
        double thickX = leftThinX + (thinWidth / 2.0) + gap + (thickWidth / 2.0);
        gc.strokeLine(thickX, startY, thickX, endY);

        gc.setLineWidth(thinWidth);
        double rightThinX = thickX + (thickWidth / 2.0) + gap + (thinWidth / 2.0);
        gc.strokeLine(rightThinX, startY, rightThinX, endY);

        double rightDotX = rightThinX + (thinWidth / 2.0) + dotSpace;
        drawRepeatDots(gc, barline, rightDotX, startY, endY, sp);
    }

    private void drawDashed(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        double dashLength = barline.getScoreStyle().getStaffLineSpacing() * 0.6 * sp;
        double spaceLength = barline.getScoreStyle().getStaffLineSpacing() * 0.35 * sp;
        gc.setLineWidth(width);
        gc.setLineDashes(dashLength, spaceLength);

        double correctedX = x + (width / 2.0);
        gc.strokeLine(correctedX, startY, correctedX, endY);
        gc.setLineDashes(null);
    }

    private void drawDotted(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        double dotLength = width;
        double spaceLength = barline.getScoreStyle().getStaffLineSpacing() * 0.35 * sp;

        gc.setLineWidth(width);
        gc.setLineDashes(dotLength, spaceLength);

        double correctedX = x + (width / 2.0);
        gc.strokeLine(correctedX, startY, correctedX, endY);
        gc.setLineDashes(null);
    }

    private void drawHeavy(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getHeavyLineWidth() * sp;
        gc.setLineWidth(width);
        double correctedX = x + (width / 2.0);
        gc.strokeLine(correctedX, startY, correctedX, endY);
    }

    private void drawTickShort(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        gc.setLineWidth(width);
        double correctedX = x + (width / 2.0);

        double tickHeight = barline.getScoreStyle().getStaffLineSpacing() * sp;
        double tStartY = startY - (tickHeight / 2.0);
        double tEndY = startY + (tickHeight / 2.0);

        gc.strokeLine(correctedX, tStartY, correctedX, tEndY);
    }

    private void drawTickLong(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        gc.setLineWidth(width);
        double correctedX = x + (width / 2.0);

        double tickHeight = 2 * barline.getScoreStyle().getStaffLineSpacing() * sp;
        double tStartY = startY - (tickHeight / 2.0);
        double tEndY = startY + (tickHeight / 2.0);

        gc.strokeLine(correctedX, tStartY, correctedX, tEndY);
    }

    private void drawShorter(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        gc.setLineWidth(width);
        double correctedX = x + (width / 2.0);
        double shortHeight = barline.getScoreStyle().getStaffLineSpacing() * 2.0 * sp;
        double sStartY = startY + barline.getScoreStyle().getStaffLineSpacing() * sp;
        double sEndY = sStartY + shortHeight;
        gc.strokeLine(correctedX, sStartY, correctedX, sEndY);
    }

    private void drawShort(GraphicsContext gc, BarlineLayout barline, double x, double startY, double endY, double sp) {
        double width = barline.getLightLineWidth() * sp;
        gc.setLineWidth(width);
        double correctedX = x + (width / 2.0);
        double shortHeight = barline.getScoreStyle().getStaffLineSpacing() * 3.0 * sp;
        double sStartY = startY + barline.getScoreStyle().getStaffLineSpacing() * 0.5 * sp;
        double sEndY = sStartY + shortHeight + width * 0.5;
        gc.strokeLine(correctedX, sStartY, correctedX, sEndY);
    }

    private void drawRepeatDots(GraphicsContext gc, BarlineLayout barline, double dotX, double startY, double endY, double sp) {
        double dotRadius = barline.getDotRadius() * sp;
        double lineSpacing = barline.getParent().getScoreStyle().getStaffLineSpacing() * sp;
        double dotDiameter = dotRadius * 2.0;
        double staffLineWidth = barline.getParent().getScoreStyle().getStaffLineWidth() * sp;

        double topDotY = startY + (1.5 * lineSpacing) - dotRadius + (staffLineWidth / 2.0);
        double bottomDotY = startY + (2.5 * lineSpacing) - dotRadius + (staffLineWidth / 2.0);

        gc.fillOval(dotX, topDotY, dotDiameter, dotDiameter);
        gc.fillOval(dotX, bottomDotY, dotDiameter, dotDiameter);
    }
}