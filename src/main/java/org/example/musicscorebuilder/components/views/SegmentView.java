package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.util.Util;

public class SegmentView extends ComponentView {
    private final BarlineView barlineView = new BarlineView();
    private final ClefView clefView = new ClefView();
    private final KeySigView keySigView = new KeySigView();

    public void draw(GraphicsContext gc, SegmentLayout segment, double measureX, double measureY, double sp) {
        double segmentX = segment.getX() * sp + measureX;
        double segmentY = segment.getY() * sp + measureY;
        double widthPx = segment.getWidth() * sp;
        double heightPx = segment.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(0.3f), segmentX, segmentY, widthPx, heightPx);



        for (ElementLayout element : segment.getElements()) {
            switch(element) {
                case BarlineLayout barline -> barlineView.draw(gc, barline, segmentX, segmentY, sp);
                case ClefLayout clef -> clefView.draw(gc, clef, segmentX, segmentY, sp);
                case KeySigLayout keySig -> keySigView.draw(gc, keySig, segmentX, segmentY, sp);
                default -> {}
            }
        }
    }
}
