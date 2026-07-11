package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.PMeasureLayout;
import org.example.musicscorebuilder.components.layout.SMeasureLayout;
import org.example.musicscorebuilder.util.Util;

public class PMeasureView extends ComponentView {
    private final SMeasureView sMeasureView = new SMeasureView();

    public void draw(GraphicsContext gc, PMeasureLayout partMeasureLayout, double partX, double partY, double sp) {
        double measureX = partMeasureLayout.getX() * sp + partX;
        double measureY = partMeasureLayout.getY() * sp + partY;
        double widthPx = partMeasureLayout.getWidth() * sp;
        double heightPx = partMeasureLayout.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(), measureX, measureY, widthPx, heightPx);

        for (SMeasureLayout sMeasure : partMeasureLayout.getSMeasures()) {
            sMeasureView.draw(gc, sMeasure, measureX, measureY, sp);
        }
    }
}
