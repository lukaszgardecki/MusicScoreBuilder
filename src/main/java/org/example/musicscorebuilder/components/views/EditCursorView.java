package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.SegmentLayout;

public class EditCursorView extends ComponentView {

    public void draw(GraphicsContext gc, SegmentLayout segment, double measureX, double measureY, double sp) {
        double segmentX = segment.getX() * sp + measureX;
        double segmentY = segment.getY() * sp + measureY;
        double staffHeight = segment.getElementsByStaff().keySet().stream().findFirst().get().getHeight() * sp;
        var style = segment.getScoreStyle();
        double yD = style.getEditCursorAboveBelowLength() * sp;
        var startY = segmentY - yD;
        var bgWidth = segment.getWidth() * sp;
        var bgHeight = staffHeight + 2 * yD;
        gc.save();

        Color color = Color.rgb(0, 120, 255, 0.3);
        fillBackground(gc, color, segmentX, startY, bgWidth, bgHeight);

        double cursorWidth = style.getEditCursorWidth() * sp;
        var cursorX = segmentX - 0.5 * cursorWidth;
        var cursorEndY = startY + bgHeight;
        var cursorColor = Color.rgb(0, 120, 255, 0.8);
        gc.setStroke(cursorColor);
        gc.setLineWidth(cursorWidth);
        gc.strokeLine(cursorX, startY, cursorX, cursorEndY);

        gc.restore();
    }
}