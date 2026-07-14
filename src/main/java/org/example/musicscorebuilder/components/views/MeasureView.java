package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.util.Util;

public class MeasureView extends ComponentView {
    private final SegmentView segmentView = new SegmentView();
    private final StaffView staffView = new StaffView();

    public void draw(GraphicsContext gc, MeasureLayout measure, double systemX, double systemY, double sp) {
        double measureX = measure.getX() * sp + systemX;
        double measureY = measure.getY() * sp + systemY;
        double widthPx = measure.getWidth() * sp;
        double heightPx = measure.getHeight() * sp;

        fillBackground(gc, Util.generateRandomColor(), measureX, measureY, widthPx, heightPx);

        for (StaffLayout staff : measure.getStaffs()) {
            staffView.draw(gc, staff, measureX, measureY, sp);
        }

        for(SegmentLayout segment : measure.getSegments()) {
            segmentView.draw(gc, segment, measureX, measureY, sp);
        }
    }
}
