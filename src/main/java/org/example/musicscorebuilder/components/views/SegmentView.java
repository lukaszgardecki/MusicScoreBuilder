package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.ClefLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.util.Util;

public class SegmentView extends ComponentView {
    private final ClefView clefView = new ClefView();

    public void draw(GraphicsContext gc, SegmentLayout segment, double sMeasureX, double sMeasureY, double sp) {
        double segmentX = sMeasureX + segment.getX() * sp;
        double segmentY = sMeasureY + segment.getY() * sp;
        double widthPx = segment.getWidth() * sp;
        double heightPx = segment.getHeight() * sp;

        if (segment instanceof ClefLayout clef) clefView.draw(gc, clef, sMeasureX, sMeasureY, sp);
//        else fillBackground(gc, Util.generateRandomColor(), segmentX, segmentY, widthPx, heightPx);
    }
}
