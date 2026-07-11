package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.SMeasureLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.util.Util;

public class SMeasureView extends ComponentView {
    public final StaffView staffView = new StaffView();
    public final BarlineView barlineView = new BarlineView();
    public final SegmentView segmentView = new SegmentView();

    public void draw(GraphicsContext gc, SMeasureLayout sMeasureLayout, double partMeasureX, double partMeasureY, double sp) {
        double sMeasureX = sMeasureLayout.getX() * sp + partMeasureX;
        double sMeasureY = sMeasureLayout.getY() * sp + partMeasureY;
        double widthPx = sMeasureLayout.getWidth() * sp;
        double heightPx = sMeasureLayout.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(), sMeasureX, sMeasureY, widthPx, heightPx);

        for (SegmentLayout segment : sMeasureLayout.getActiveSegments()) {
            segmentView.draw(gc, segment, sMeasureX, sMeasureY, sp);
        }

        staffView.draw(gc, sMeasureLayout.getStaffLayout(), sMeasureX, sMeasureY, sp);

        if (sMeasureLayout.getStartBarline() != null) barlineView.draw(gc, sMeasureLayout.getStartBarline(), sMeasureX, sMeasureY, sp);
        barlineView.draw(gc, sMeasureLayout.getEndBarline(), sMeasureX, sMeasureY, sp);


    }
}


